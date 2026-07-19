package com.payflow.service;

import com.payflow.dto.admin.AdminDashboardResponse;
import com.payflow.dto.ai.FraudAlertResponse;
import com.payflow.entity.RewardTransaction;
import com.payflow.entity.Transaction;
import com.payflow.entity.User;
import com.payflow.exception.BadRequestException;
import com.payflow.exception.ResourceNotFoundException;
import com.payflow.repository.BankAccountRepository;
import com.payflow.repository.RewardTransactionRepository;
import com.payflow.repository.TransactionRepository;
import com.payflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

	private final UserRepository userRepository;
	private final BankAccountRepository bankAccountRepository;
	private final TransactionRepository transactionRepository;
	private final RewardTransactionRepository rewardTransactionRepository;

	public AdminDashboardResponse getDashboard() {
		List<Transaction> allTxns = transactionRepository.findAll();

		long successCount = allTxns.stream().filter(t -> t.getStatus() == Transaction.TransactionStatus.SUCCESS)
				.count();
		long failedCount = allTxns.stream().filter(t -> t.getStatus() == Transaction.TransactionStatus.FAILED).count();
		BigDecimal totalVolume = allTxns.stream().filter(t -> t.getStatus() == Transaction.TransactionStatus.SUCCESS)
				.map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

		return AdminDashboardResponse.builder().totalUsers(userRepository.count())
				.totalBankAccounts(bankAccountRepository.count()).totalTransactions(allTxns.size())
				.successfulTransactions(successCount).failedTransactions(failedCount).totalVolume(totalVolume).build();
	}

	public List<User> listUsers() {
		return userRepository.findAll();
	}

	@Transactional
	public User setUserEnabled(@NonNull String userId, boolean enabled) {
		User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
		user.setEnabled(enabled);
		return userRepository.save(user);
	}

	public List<Transaction> listAllTransactions() {
		return transactionRepository.findAll();
	}

	@SuppressWarnings("null")
	@Transactional
	public User updateKycStatus(String userId, String status) {
		User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
		try {
			user.setKycStatus(User.KycStatus.valueOf(status.toUpperCase()));
		} catch (IllegalArgumentException e) {
			throw new BadRequestException("Status must be PENDING, VERIFIED, or REJECTED");
		}
		return userRepository.save(user);
	}

	/**
	 * Total cashback issued platform-wide - simple aggregate for the admin
	 * "cashback management" view.
	 */
	public Map<String, Object> getCashbackOverview() {
		var all = rewardTransactionRepository.findAll();
		BigDecimal totalIssued = all.stream().map(RewardTransaction::getCashbackAmount).reduce(BigDecimal.ZERO,
				BigDecimal::add);
		return Map.of("totalCashbackIssued", totalIssued, "totalRewardEntries", all.size());
	}

	/**
	 * Platform-wide transactions flagged by fraud prediction, highest risk first -
	 * admin "Fraud Monitoring" view.
	 */
	public List<FraudAlertResponse> listFraudAlerts() {

		List<Transaction> transactions = transactionRepository.findByRiskLevelInOrderByRiskScoreDesc(
				List.of(Transaction.RiskLevel.MEDIUM, Transaction.RiskLevel.HIGH));

		return transactions.stream().map(transaction -> FraudDetectionService.toAlert(transaction, true)).toList();
	}
}
