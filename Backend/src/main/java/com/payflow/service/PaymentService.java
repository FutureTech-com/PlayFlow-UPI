package com.payflow.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.payflow.dto.payment.PaymentRequestResponse;
import com.payflow.dto.payment.RequestMoneyRequest;
import com.payflow.dto.payment.RespondToRequestRequest;
import com.payflow.dto.payment.SendMoneyRequest;
import com.payflow.dto.transaction.TransactionResponse;
import com.payflow.dto.transfer.SelfTransferRequest;
import com.payflow.entity.BankAccount;
import com.payflow.entity.Notification;
import com.payflow.entity.PaymentRequest;
import com.payflow.entity.RewardTransaction;
import com.payflow.entity.Transaction;
import com.payflow.entity.UpiId;
import com.payflow.entity.User;
import com.payflow.exception.BadRequestException;
import com.payflow.exception.InsufficientBalanceException;
import com.payflow.exception.ResourceNotFoundException;
import com.payflow.repository.BankAccountRepository;
import com.payflow.repository.PaymentRequestRepository;
import com.payflow.repository.RewardTransactionRepository;
import com.payflow.repository.TransactionRepository;
import com.payflow.repository.UpiIdRepository;
import com.payflow.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

	private final UpiIdRepository upiIdRepository;
	private final BankAccountRepository bankAccountRepository;
	private final TransactionRepository transactionRepository;
	private final PaymentRequestRepository paymentRequestRepository;
	private final UserService userService;
	private final NotificationService notificationService;
	private final AuditLogService auditLogService;
	private final RewardTransactionRepository rewardTransactionRepository;
	private final UserRepository userRepository;
	private final ExpenseCategorizationService expenseCategorizationService;
	private final FraudDetectionService fraudDetectionService;
	

	private final SecureRandom random = new SecureRandom();
	private static final DateTimeFormatter REF_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

	@Value("${payflow.fraud.velocity-limit:10}")
	private int velocityLimit;

	@Value("${payflow.fraud.velocity-window-minutes:5}")
	private int velocityWindowMinutes;

	@Value("${payflow.cashback.rate-percent:1}")
	private BigDecimal cashbackRatePercent;

	@Value("${payflow.cashback.min-amount-for-cashback:100}")
	private BigDecimal cashbackMinAmount;

	@Transactional
	public TransactionResponse sendMoney(User sender, SendMoneyRequest request) {
		userService.verifyTransactionPin(sender, request.getTransactionPin());
		checkVelocity(sender);

		UpiId receiverUpi = resolveReceiver(request);
		User receiver = receiverUpi.getUser();
		if (receiver.getId().equals(sender.getId())) {
			throw new BadRequestException("Use self-transfer to move money between your own accounts");
		}

		BankAccount senderAccount = bankAccountRepository.findByUserAndPrimaryAccountTrue(sender)
				.orElseThrow(() -> new BadRequestException("No primary bank account set"));
		BankAccount receiverAccount = receiverUpi.getBankAccount();

		Transaction txn = new Transaction();
		txn.setReferenceId(generateReferenceId());
		txn.setSender(sender);
		txn.setReceiver(receiver);
		txn.setSenderVpa(findAnyVpa(sender));
		txn.setReceiverVpa(receiverUpi.getVpa());
		txn.setAmount(request.getAmount());
		txn.setNote(request.getNote());
		txn.setTransactionType(receiverUpi.isMerchant() ? Transaction.TransactionType.MERCHANT_PAYMENT
				: Transaction.TransactionType.SEND);
		txn.setCategory(
				expenseCategorizationService.categorize(request.getNote(), receiverUpi.getVpa(), txn.getTransactionType().name()));
		
		FraudDetectionService.FraudAssessment fraud = fraudDetectionService.assess(sender, request.getAmount(),
				receiverUpi.getVpa());

		txn.setRiskScore(fraud.score());
		txn.setRiskLevel(fraud.level());
		txn.setRiskReasons(String.join("|", fraud.reasons()));
		txn.setFlaggedForReview(fraud.level() == Transaction.RiskLevel.HIGH);

		if (senderAccount.getBalance().compareTo(request.getAmount()) < 0) {
			txn.setStatus(Transaction.TransactionStatus.FAILED);
			txn.setFailureReason("Insufficient balance");
			transactionRepository.save(txn);

			notificationService.createNotification(
					sender, "Payment failed", "Your payment of \u20B9" + request.getAmount() + " to "
							+ receiverUpi.getVpa() + " failed due to insufficient balance.",
					Notification.NotificationType.PAYMENT_FAILED);
			auditLogService.log(sender.getId(), "PAYMENT_FAILED", "Transaction", txn.getId(), "Insufficient balance");

			throw new InsufficientBalanceException("Insufficient balance in your primary account");
		}

		// Move funds (simulated ledger update within the same DB transaction)
		senderAccount.setBalance(senderAccount.getBalance().subtract(request.getAmount()));
		receiverAccount.setBalance(receiverAccount.getBalance().add(request.getAmount()));
		bankAccountRepository.save(senderAccount);
		bankAccountRepository.save(receiverAccount);

		txn.setStatus(Transaction.TransactionStatus.SUCCESS);
		Transaction saved = transactionRepository.save(txn);

		notificationService.createNotification(sender, "Payment successful",
				"You paid \u20B9" + request.getAmount() + " to " + receiverUpi.getVpa() + ".",
				Notification.NotificationType.PAYMENT_SUCCESS);
		notificationService.createNotification(receiver, "Money received",
				"You received \u20B9" + request.getAmount() + " from " + findAnyVpa(sender) + ".",
				Notification.NotificationType.PAYMENT_SUCCESS);

		auditLogService.log(sender.getId(), "MONEY_SENT", "Transaction", saved.getId(),
				"amount=" + request.getAmount() + " to=" + receiverUpi.getVpa());

		if (fraud.level() == Transaction.RiskLevel.HIGH) {
			notificationService.createNotification(sender, "Unusual payment flagged",
					"Your payment of \u20B9" + request.getAmount() + " to " + receiverUpi.getVpa()
							+ " looked unusual and has been flagged for review. Contact support if this wasn't you.",
					Notification.NotificationType.SECURITY_ALERT);
			auditLogService.log(sender.getId(), "FRAUD_FLAGGED", "Transaction", saved.getId(),
					"score=" + fraud.score() + " reasons=" + String.join("; ", fraud.reasons()));
		}

		awardCashback(sender, request.getAmount(), saved.getId());

		return toTransactionResponse(saved, sender);
	}

	@Transactional
	public TransactionResponse selfTransfer(User user, SelfTransferRequest request) {
		userService.verifyTransactionPin(user, request.getTransactionPin());

		if (request.getFromBankAccountId().equals(request.getToBankAccountId())) {
			throw new BadRequestException("Source and destination accounts must be different");
		}

		@SuppressWarnings("null")
		BankAccount from = bankAccountRepository.findById(request.getFromBankAccountId())
				.orElseThrow(() -> new ResourceNotFoundException("Source account not found"));
		@SuppressWarnings("null")
		BankAccount to = bankAccountRepository.findById(request.getToBankAccountId())
				.orElseThrow(() -> new ResourceNotFoundException("Destination account not found"));

		if (!from.getUser().getId().equals(user.getId()) || !to.getUser().getId().equals(user.getId())) {
			throw new BadRequestException("Both accounts must belong to you");
		}
		if (from.getBalance().compareTo(request.getAmount()) < 0) {
			throw new InsufficientBalanceException("Insufficient balance in source account");
		}

		from.setBalance(from.getBalance().subtract(request.getAmount()));
		to.setBalance(to.getBalance().add(request.getAmount()));
		bankAccountRepository.save(from);
		bankAccountRepository.save(to);

		Transaction txn = new Transaction();
		txn.setReferenceId(generateReferenceId());
		txn.setSender(user);
		txn.setReceiver(user);
		txn.setSenderVpa(from.getBankName() + " •" + last4(from.getAccountNumber()));
		txn.setReceiverVpa(to.getBankName() + " •" + last4(to.getAccountNumber()));
		txn.setAmount(request.getAmount());
		txn.setNote("Self-transfer");
		txn.setTransactionType(Transaction.TransactionType.SELF_TRANSFER);
		txn.setCategory(ExpenseCategorizationService.TRANSFER);
		txn.setStatus(Transaction.TransactionStatus.SUCCESS);
		Transaction saved = transactionRepository.save(txn);

		notificationService.createNotification(user, "Self-transfer complete",
				"\u20B9" + request.getAmount() + " moved from " + from.getBankName() + " to " + to.getBankName() + ".",
				Notification.NotificationType.PAYMENT_SUCCESS);
		auditLogService.log(user.getId(), "SELF_TRANSFER", "Transaction", saved.getId(), null);

		return toTransactionResponse(saved, user);
	}

	@Transactional
	public TransactionResponse executeAutoPay(User sender, String receiverVpa, BigDecimal amount, String note) {
		checkVelocity(sender);

		UpiId receiverUpi = upiIdRepository.findByVpa(receiverVpa)
				.orElseThrow(() -> new BadRequestException("Recipient UPI ID not found: " + receiverVpa));
		User receiver = receiverUpi.getUser();

		BankAccount senderAccount = bankAccountRepository.findByUserAndPrimaryAccountTrue(sender)
				.orElseThrow(() -> new BadRequestException("No primary bank account set"));
		BankAccount receiverAccount = receiverUpi.getBankAccount();

		Transaction txn = new Transaction();
		txn.setReferenceId(generateReferenceId());
		txn.setSender(sender);
		txn.setReceiver(receiver);
		txn.setSenderVpa(findAnyVpa(sender));
		txn.setReceiverVpa(receiverUpi.getVpa());
		txn.setAmount(amount);
		txn.setNote(note);
		txn.setTransactionType(Transaction.TransactionType.SCHEDULED);
		txn.setCategory(expenseCategorizationService.categorize(note, receiverUpi.getVpa(), txn.getTransactionType().name()));

		if (senderAccount.getBalance().compareTo(amount) < 0) {
			txn.setStatus(Transaction.TransactionStatus.FAILED);
			txn.setFailureReason("Insufficient balance");
			transactionRepository.save(txn);
			notificationService
					.createNotification(sender, "AutoPay failed",
							"Your scheduled payment of \u20B9" + amount + " to " + receiverVpa
									+ " failed due to insufficient balance.",
							Notification.NotificationType.PAYMENT_FAILED);
			throw new InsufficientBalanceException("Insufficient balance for scheduled payment");
		}

		senderAccount.setBalance(senderAccount.getBalance().subtract(amount));
		receiverAccount.setBalance(receiverAccount.getBalance().add(amount));
		bankAccountRepository.save(senderAccount);
		bankAccountRepository.save(receiverAccount);

		txn.setStatus(Transaction.TransactionStatus.SUCCESS);
		Transaction saved = transactionRepository.save(txn);

		notificationService.createNotification(sender, "AutoPay successful",
				"\u20B9" + amount + " was automatically paid to " + receiverVpa + ".",
				Notification.NotificationType.PAYMENT_SUCCESS);
		notificationService.createNotification(receiver, "Money received",
				"You received \u20B9" + amount + " from " + findAnyVpa(sender) + " (AutoPay).",
				Notification.NotificationType.PAYMENT_SUCCESS);
		auditLogService.log(sender.getId(), "AUTOPAY_EXECUTED", "Transaction", saved.getId(), "amount=" + amount);

		return toTransactionResponse(saved, sender);
	}

	@Transactional
	public PaymentRequestResponse requestMoney(User requester, RequestMoneyRequest request) {
		UpiId payerUpi = upiIdRepository.findByVpa(request.getPayerVpa())
				.orElseThrow(() -> new BadRequestException("Payer UPI ID not found: " + request.getPayerVpa()));

		User payer = payerUpi.getUser();
		if (payer.getId().equals(requester.getId())) {
			throw new BadRequestException("You cannot request money from yourself");
		}

		PaymentRequest paymentRequest = new PaymentRequest();
		paymentRequest.setRequester(requester);
		paymentRequest.setPayer(payer);
		paymentRequest.setAmount(request.getAmount());
		paymentRequest.setNote(request.getNote());
		paymentRequest.setStatus(PaymentRequest.RequestStatus.PENDING);

		PaymentRequest saved = paymentRequestRepository.save(paymentRequest);

		notificationService.createNotification(payer, "Payment request received",
				findAnyVpa(requester) + " requested \u20B9" + request.getAmount() + " from you.",
				Notification.NotificationType.REQUEST_RECEIVED);

		auditLogService.log(requester.getId(), "PAYMENT_REQUEST_CREATED", "PaymentRequest", saved.getId(), null);

		return toRequestResponse(saved);
	}

	@Transactional
	public PaymentRequestResponse respondToRequest(User currentUser, String requestId,
			RespondToRequestRequest response) {
		@SuppressWarnings("null")
		PaymentRequest paymentRequest = paymentRequestRepository.findById(requestId)
				.orElseThrow(() -> new ResourceNotFoundException("Payment request not found"));

		if (!paymentRequest.getPayer().getId().equals(currentUser.getId())) {
			throw new BadRequestException("This request is not addressed to you");
		}
		if (paymentRequest.getStatus() != PaymentRequest.RequestStatus.PENDING) {
			throw new BadRequestException(
					"This request has already been " + paymentRequest.getStatus().name().toLowerCase());
		}

		if (Boolean.FALSE.equals(response.getAccept())) {
			paymentRequest.setStatus(PaymentRequest.RequestStatus.DECLINED);
			paymentRequestRepository.save(paymentRequest);
			notificationService.createNotification(paymentRequest.getRequester(), "Request declined",
					currentUser.getFullName() + " declined your request for \u20B9" + paymentRequest.getAmount() + ".",
					Notification.NotificationType.REQUEST_DECLINED);
			return toRequestResponse(paymentRequest);
		}

		if (response.getTransactionPin() == null || response.getTransactionPin().isBlank()) {
			throw new BadRequestException("Transaction PIN is required to accept a payment request");
		}

		SendMoneyRequest sendRequest = new SendMoneyRequest();
		sendRequest.setReceiverVpa(findAnyVpa(paymentRequest.getRequester()));
		sendRequest.setAmount(paymentRequest.getAmount());
		sendRequest.setNote(paymentRequest.getNote());
		sendRequest.setTransactionPin(response.getTransactionPin());

		TransactionResponse txnResponse = sendMoney(currentUser, sendRequest);

		paymentRequest.setStatus(PaymentRequest.RequestStatus.ACCEPTED);
		paymentRequest.setFulfilledTransactionId(txnResponse.getId());
		paymentRequestRepository.save(paymentRequest);

		notificationService.createNotification(paymentRequest.getRequester(), "Request accepted",
				currentUser.getFullName() + " paid your request of \u20B9" + paymentRequest.getAmount() + ".",
				Notification.NotificationType.REQUEST_ACCEPTED);

		return toRequestResponse(paymentRequest);
	}

	public List<PaymentRequestResponse> listIncomingRequests(User user) {
		return paymentRequestRepository.findByPayerOrderByCreatedAtDesc(user).stream().map(this::toRequestResponse)
				.toList();
	}

	public List<PaymentRequestResponse> listOutgoingRequests(User user) {
		return paymentRequestRepository.findByRequesterOrderByCreatedAtDesc(user).stream().map(this::toRequestResponse)
				.toList();
	}

	/**
	 * Resolves the receiver's UpiId from whichever identifier was supplied: VPA,
	 * mobile, or bank account + IFSC.
	 */
	private UpiId resolveReceiver(SendMoneyRequest request) {
		if (request.getReceiverVpa() != null && !request.getReceiverVpa().isBlank()) {
			return upiIdRepository.findByVpa(request.getReceiverVpa()).orElseThrow(
					() -> new BadRequestException("Recipient UPI ID not found: " + request.getReceiverVpa()));
		}
		if (request.getReceiverMobile() != null && !request.getReceiverMobile().isBlank()) {
			User receiver = userRepository.findByPhone(request.getReceiverMobile())
					.orElseThrow(() -> new BadRequestException(
							"No PayFlow user found with mobile number " + request.getReceiverMobile()));
			return upiIdRepository.findByUser(receiver).stream().findFirst().orElseThrow(
					() -> new BadRequestException(receiver.getFullName() + " has not created a UPI ID yet"));
		}
		if (request.getReceiverAccountNumber() != null && !request.getReceiverAccountNumber().isBlank()) {
			BankAccount account = bankAccountRepository
					.findByAccountNumberAndIfscCode(request.getReceiverAccountNumber(), request.getReceiverIfsc())
					.orElseThrow(() -> new BadRequestException(
							"No bank account found matching that account number and IFSC"));
			return upiIdRepository.findByUser(account.getUser()).stream()
					.filter(u -> u.getBankAccount().getId().equals(account.getId())).findFirst()
					.orElseThrow(() -> new BadRequestException("That bank account has no linked UPI ID"));
		}
		throw new BadRequestException("Provide a receiver VPA, mobile number, or bank account + IFSC");
	}

	/**
	 * Simple velocity-based fraud rule: too many transactions in a short window
	 * gets blocked.
	 */
	private void checkVelocity(User sender) {
		LocalDateTime since = LocalDateTime.now().minusMinutes(velocityWindowMinutes);
		long recentCount = transactionRepository.countBySenderSince(sender, since);
		if (recentCount >= velocityLimit) {
			notificationService.createNotification(sender, "Suspicious activity detected",
					"We've temporarily blocked payments from your account due to unusually high transaction activity. "
							+ "Contact support if this wasn't you.",
					Notification.NotificationType.SECURITY_ALERT);
			auditLogService.log(sender.getId(), "PAYMENT_BLOCKED_VELOCITY", "User", sender.getId(),
					"count=" + recentCount + " windowMinutes=" + velocityWindowMinutes);
			throw new BadRequestException(
					"Too many payments in a short time. Please wait a few minutes and try again.");
		}
	}

	/**
	 * Credits a small cashback reward on qualifying payments - a lightweight
	 * loyalty mechanic.
	 */
	private void awardCashback(User sender, BigDecimal amount, String relatedTxnId) {
		if (amount.compareTo(cashbackMinAmount) < 0)
			return;

		BigDecimal cashback = amount.multiply(cashbackRatePercent).divide(BigDecimal.valueOf(100), 2,
				RoundingMode.HALF_UP);
		if (cashback.compareTo(BigDecimal.ZERO) <= 0)
			return;

		RewardTransaction reward = new RewardTransaction();
		reward.setUser(sender);
		reward.setCashbackAmount(cashback);
		reward.setPoints(amount.intValue() / 10);
		reward.setReason("Cashback on payment of \u20B9" + amount);
		reward.setRelatedTransactionId(relatedTxnId);
		rewardTransactionRepository.save(reward);

		notificationService.createNotification(sender, "Cashback earned",
				"You earned \u20B9" + cashback + " cashback on your last payment.",
				Notification.NotificationType.GENERAL);
	}

	private String last4(String accountNumber) {
		if (accountNumber == null || accountNumber.length() < 4)
			return accountNumber;
		return accountNumber.substring(accountNumber.length() - 4);
	}

	private String findAnyVpa(User user) {
		return upiIdRepository.findByUser(user).stream().findFirst().map(UpiId::getVpa)
				.orElseThrow(() -> new BadRequestException(user.getFullName() + " has not created a UPI ID yet"));
	}

	private String generateReferenceId() {
		String timestamp = LocalDateTime.now().format(REF_FORMAT);
		int randomSuffix = 1000 + random.nextInt(9000);
		return "PFL" + timestamp + randomSuffix;
	}

	private TransactionResponse toTransactionResponse(Transaction t, User viewpoint) {
		boolean isSender = t.getSender() != null && t.getSender().getId().equals(viewpoint.getId());
		User counterparty = isSender ? t.getReceiver() : t.getSender();
		return TransactionResponse.builder().id(t.getId()).referenceId(t.getReferenceId())
				.type(isSender ? "DEBIT" : "CREDIT")
				.counterpartyName(counterparty != null ? counterparty.getFullName() : "PayFlow Wallet")
				.counterpartyVpa(isSender ? t.getReceiverVpa() : t.getSenderVpa()).amount(t.getAmount())
				.note(t.getNote()).status(t.getStatus().name()).timestamp(t.getCreatedAt()).category(t.getCategory())
				.riskLevel(t.getRiskLevel() != null ? t.getRiskLevel().name() : null).build();
	}

	private PaymentRequestResponse toRequestResponse(PaymentRequest r) {
		return PaymentRequestResponse.builder().id(r.getId()).requesterName(r.getRequester().getFullName())
				.requesterVpa(findAnyVpaSafe(r.getRequester())).payerName(r.getPayer().getFullName())
				.payerVpa(findAnyVpaSafe(r.getPayer())).amount(r.getAmount()).note(r.getNote())
				.status(r.getStatus().name()).createdAt(r.getCreatedAt()).build();
	}

	private String findAnyVpaSafe(User user) {
		return upiIdRepository.findByUser(user).stream().findFirst().map(UpiId::getVpa).orElse(null);
	}
}
