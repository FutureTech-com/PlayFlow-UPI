package com.payflow.service;

import com.payflow.dto.bank.BankAccountResponse;
import com.payflow.dto.bank.FetchedAccountPreview;
import com.payflow.dto.bank.LinkBankAccountRequest;
import com.payflow.dto.bank.LinkByMobileRequest;
import com.payflow.entity.BankAccount;
import com.payflow.entity.Notification;
import com.payflow.entity.User;
import com.payflow.exception.BadRequestException;
import com.payflow.exception.ResourceNotFoundException;
import com.payflow.repository.BankAccountRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Data
public class BankAccountService {

	private final BankAccountRepository bankAccountRepository;
	private final NotificationService notificationService;
	private final AuditLogService auditLogService;

	@Transactional
	public BankAccountResponse linkAccount(User user, LinkBankAccountRequest request) {
		boolean isFirstAccount = bankAccountRepository.findByUser(user).isEmpty();

		BankAccount account = new BankAccount();
		account.setUser(user);
		account.setBankName(request.getBankName());
		account.setAccountHolderName(request.getAccountHolderName());
		account.setAccountNumber(request.getAccountNumber());
		account.setIfscCode(request.getIfscCode().toUpperCase());
		try {
			account.setAccountType(BankAccount.AccountType.valueOf(request.getAccountType().toUpperCase()));
		} catch (IllegalArgumentException e) {
			throw new BadRequestException("Invalid account type. Use SAVINGS or CURRENT.");
		}
		account.setPrimaryAccount(isFirstAccount); // first linked account becomes primary automatically
		account.setVerified(true); // simulated instant verification for demo purposes

		BankAccount saved = bankAccountRepository.save(account);

		notificationService.createNotification(
				user, "Bank account linked", "Your " + saved.getBankName() + " account ending in "
						+ last4(saved.getAccountNumber()) + " has been linked successfully.",
				Notification.NotificationType.ACCOUNT_LINKED);
		auditLogService.log(user.getId(), "BANK_ACCOUNT_LINKED", "BankAccount", saved.getId(), saved.getBankName());

		return toResponse(saved);
	}

	// Simulated "banks NPCI knows about" for the mobile-number lookup demo. Real
	// UPI apps
	// query NPCI/the bank's server here; we deterministically derive
	// fake-but-consistent
	// account data from the mobile number instead, so the same number always
	// resolves to
	// the same account(s) without needing any server-side session/cache.
	private static final String[] DEMO_BANKS = { "HDFC Bank", "State Bank of India", "ICICI Bank", "Axis Bank",
			"Kotak Mahindra Bank" };
	private static final String[] DEMO_IFSC_PREFIX = { "HDFC", "SBIN", "ICIC", "UTIB", "KKBK" };


	/**
	 * Simulates discovering the bank account(s) linked to a mobile number - the way
	 * a real UPI app would via an NPCI lookup after you grant SMS/carrier
	 * permission. Deterministic: the same mobile number always returns the same 1-2
	 * accounts.
	 */
	public List<FetchedAccountPreview> fetchAccountsForMobile(User user, String mobileNumber) {
		Random rnd = new Random(mobileNumber.hashCode());
		int count = 1 + rnd.nextInt(2); // 1 or 2 simulated accounts, deterministic per number

		List<FetchedAccountPreview> results = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			int bankIdx = rnd.nextInt(DEMO_BANKS.length);
			String accountNumber = generateAccountNumber(mobileNumber, i);
			String ifsc = generateIfsc(DEMO_IFSC_PREFIX[bankIdx], mobileNumber, i);

			results.add(FetchedAccountPreview.builder().accountIndex(i).bankName(DEMO_BANKS[bankIdx])
					.accountHolderName(user.getFullName()).maskedAccountNumber("XXXX XXXX " + last4(accountNumber))
					.ifscCode(ifsc).accountType("SAVINGS").build());
		}
		return results;
	}

	/**
	 * Confirms and links one of the accounts previously returned by
	 * {@link #fetchAccountsForMobile}.
	 */
	@Transactional
	public BankAccountResponse linkAccountByMobile(User user, LinkByMobileRequest request) {
		List<FetchedAccountPreview> available = fetchAccountsForMobile(user, request.getMobileNumber());
		FetchedAccountPreview chosen = available.stream().filter(a -> a.getAccountIndex() == request.getAccountIndex())
				.findFirst().orElseThrow(
						() -> new BadRequestException("Selected account is no longer available. Please fetch again."));

		// Regenerate the same bank/IFSC deterministically to build the full (unmasked)
		// account.
		Random rnd = new Random(request.getMobileNumber().hashCode());
		int count = 1 + rnd.nextInt(2);
		String accountNumber = null;
		for (int i = 0; i < count && i <= request.getAccountIndex(); i++) {
			accountNumber = generateAccountNumber(request.getMobileNumber(), i);
		}
		if (accountNumber == null) {
			throw new BadRequestException("Selected account is no longer available. Please fetch again.");
		}

		LinkBankAccountRequest linkRequest = new LinkBankAccountRequest();
		linkRequest.setBankName(chosen.getBankName());
		linkRequest.setAccountHolderName(user.getFullName());
		linkRequest.setAccountNumber(accountNumber);
		linkRequest.setIfscCode(chosen.getIfscCode());
		linkRequest.setAccountType(chosen.getAccountType());

		return linkAccount(user, linkRequest);
	}

	private String generateAccountNumber(String mobileNumber, int index) {
		long base = Math.abs((long) (mobileNumber + ":acct:" + index).hashCode());
		// Pad/derive to a realistic 12-digit account number.
		long acct = (base % 900_000_000_000L) + 100_000_000_000L;
		return String.valueOf(acct);
	}

	private String generateIfsc(String bankPrefix, String mobileNumber, int index) {
		int branchCode = Math.abs((mobileNumber + ":ifsc:" + bankPrefix + ":" + index).hashCode()) % 1_000_000;
		return bankPrefix + "0" + String.format("%06d", branchCode);
	}

	public List<BankAccountResponse> listAccounts(User user) {
		return bankAccountRepository.findByUserOrderByPrimaryAccountDescCreatedAtDesc(user).stream().map(this::toResponse)
				.toList();
	}

	@Transactional
	public BankAccountResponse setPrimary(User user, String accountId) {
		List<BankAccount> accounts = bankAccountRepository.findByUser(user);
		BankAccount target = accounts.stream().filter(a -> a.getId().equals(accountId)).findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("Bank account not found"));

		accounts.forEach(a -> a.setPrimaryAccount(a.getId().equals(accountId)));
		bankAccountRepository.saveAll(accounts);

		return toResponse(target);
	}

	public BankAccount getPrimaryAccount(User user) {
		return bankAccountRepository.findByUserAndPrimaryAccountTrue(user).orElseThrow(
				() -> new BadRequestException("No primary bank account set. Please link a bank account first."));
	}

	private String last4(String accountNumber) {
		if (accountNumber.length() <= 4)
			return accountNumber;
		return accountNumber.substring(accountNumber.length() - 4);
	}

	private BankAccountResponse toResponse(BankAccount a) {
		String masked = "XXXX XXXX " + last4(a.getAccountNumber());
		return BankAccountResponse.builder().id(a.getId()).bankName(a.getBankName())
				.accountHolderName(a.getAccountHolderName()).maskedAccountNumber(masked).ifscCode(a.getIfscCode())
				.balance(a.getBalance()).primary(a.isPrimaryAccount()).verified(a.isVerified())
				.accountType(a.getAccountType().name()).build();
	}
}
