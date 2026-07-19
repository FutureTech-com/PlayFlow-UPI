package com.payflow.service;

import com.payflow.dto.bill.BillPaymentResponse;
import com.payflow.dto.bill.BillerResponse;
import com.payflow.dto.bill.PayBillRequest;
import com.payflow.entity.*;
import com.payflow.exception.BadRequestException;
import com.payflow.exception.InsufficientBalanceException;
import com.payflow.exception.ResourceNotFoundException;
import com.payflow.repository.BankAccountRepository;
import com.payflow.repository.BillPaymentRepository;
import com.payflow.repository.BillerRepository;
import com.payflow.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Recharge & bill payments (mobile, DTH, electricity, water, gas, broadband, FASTag, credit card).
 * Every "biller" here is simulated - payment always succeeds against your linked bank account
 * balance (subject to sufficient funds), same as any other debit. No real biller/BBPS network
 * is contacted.
 */
@Service
@RequiredArgsConstructor
public class BillPaymentService {

    private final BillerRepository billerRepository;
    private final BillPaymentRepository billPaymentRepository;
    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    private final SecureRandom random = new SecureRandom();
    private static final DateTimeFormatter REF_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public List<BillerResponse> listBillers(Biller.BillerCategory category) {
        List<Biller> billers = category != null
                ? billerRepository.findByCategoryAndActiveTrue(category)
                : billerRepository.findByActiveTrue();
        return billers.stream()
                .map(b -> BillerResponse.builder().id(b.getId()).name(b.getName()).category(b.getCategory().name()).build())
                .toList();
    }

    @Transactional
    public BillPaymentResponse pay(User user, PayBillRequest request) {
        userService.verifyTransactionPin(user, request.getTransactionPin());

        @SuppressWarnings("null")
		Biller biller = billerRepository.findById(request.getBillerId())
                .orElseThrow(() -> new ResourceNotFoundException("Biller not found"));

        BankAccount account = bankAccountRepository.findByUserAndPrimaryAccountTrue(user)
                .orElseThrow(() -> new BadRequestException("No primary bank account set"));

        BillPayment payment = new BillPayment();
        payment.setUser(user);
        payment.setBiller(biller);
        payment.setConsumerIdentifier(request.getConsumerIdentifier());
        payment.setAmount(request.getAmount());

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            payment.setStatus(Transaction.TransactionStatus.FAILED);
            billPaymentRepository.save(payment);
            notificationService.createNotification(user, "Bill payment failed",
                    biller.getName() + " payment of \u20B9" + request.getAmount() + " failed - insufficient balance.",
                    Notification.NotificationType.PAYMENT_FAILED);
            throw new InsufficientBalanceException("Insufficient balance for this bill payment");
        }

        account.setBalance(account.getBalance().subtract(request.getAmount()));
        bankAccountRepository.save(account);

        Transaction txn = new Transaction();
        txn.setReferenceId(generateReferenceId());
        txn.setSender(user);
        txn.setReceiver(null);
        txn.setSenderVpa(account.getBankName());
        txn.setReceiverVpa(biller.getName());
        txn.setAmount(request.getAmount());
        txn.setNote(biller.getCategory().name() + " - " + request.getConsumerIdentifier());
        txn.setTransactionType(Transaction.TransactionType.BILL_PAYMENT);
        txn.setStatus(Transaction.TransactionStatus.SUCCESS);
        txn.setCategory(biller.getCategory().name());
        Transaction savedTxn = transactionRepository.save(txn);

        payment.setStatus(Transaction.TransactionStatus.SUCCESS);
        payment.setTransactionRef(savedTxn.getReferenceId());
        BillPayment saved = billPaymentRepository.save(payment);

        notificationService.createNotification(user, "Bill payment successful",
                "\u20B9" + request.getAmount() + " paid to " + biller.getName() + ".", Notification.NotificationType.PAYMENT_SUCCESS);
        auditLogService.log(user.getId(), "BILL_PAYMENT", "BillPayment", saved.getId(), biller.getName());

        return toResponse(saved);
    }

    public List<BillPaymentResponse> history(User user) {
        return billPaymentRepository.findByUserOrderByCreatedAtDesc(user).stream().map(this::toResponse).toList();
    }

    private String generateReferenceId() {
        String timestamp = LocalDateTime.now().format(REF_FORMAT);
        int randomSuffix = 1000 + random.nextInt(9000);
        return "PFB" + timestamp + randomSuffix;
    }

    private BillPaymentResponse toResponse(BillPayment p) {
        return BillPaymentResponse.builder()
                .id(p.getId())
                .billerName(p.getBiller().getName())
                .category(p.getBiller().getCategory().name())
                .consumerIdentifier(p.getConsumerIdentifier())
                .amount(p.getAmount())
                .status(p.getStatus().name())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
