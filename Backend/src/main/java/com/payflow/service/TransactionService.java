package com.payflow.service;

import com.payflow.dto.transaction.TransactionFilterRequest;
import com.payflow.dto.transaction.TransactionResponse;
import com.payflow.entity.Transaction;
import com.payflow.entity.User;
import com.payflow.exception.ResourceNotFoundException;
import com.payflow.repository.TransactionRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Builder
public class TransactionService {

	private final TransactionRepository transactionRepository;
	private final PdfReceiptService pdfReceiptService;

	public List<TransactionResponse> getHistory(User user, TransactionFilterRequest filter) {
		List<Transaction> transactions = transactionRepository.findAllForUser(user);

		return transactions.stream().filter(t -> matchesFilter(t, filter)).map(t -> toResponse(t, user)).toList();
	}

	public TransactionResponse getDetails(User user, String transactionId) {
		Transaction txn = findOwnedTransaction(user, transactionId);
		return toResponse(txn, user);
	}

	public byte[] downloadReceipt(User user, String transactionId) throws IOException {
		Transaction txn = findOwnedTransaction(user, transactionId);
		return pdfReceiptService.generateReceipt(txn);
	}

	@SuppressWarnings("null")
	private Transaction findOwnedTransaction(User user, String transactionId) {
		Transaction txn = transactionRepository.findById(transactionId)
				.orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

		boolean owns = (txn.getSender() != null && txn.getSender().getId().equals(user.getId()))
				|| (txn.getReceiver() != null && txn.getReceiver().getId().equals(user.getId()));
		if (!owns) {
			throw new ResourceNotFoundException("Transaction not found");
		}
		return txn;
	}

	private boolean matchesFilter(Transaction t, TransactionFilterRequest filter) {
		if (filter == null)
			return true;

		if (filter.getFromDate() != null && t.getCreatedAt().toLocalDate().isBefore(filter.getFromDate())) {
			return false;
		}
		if (filter.getToDate() != null && t.getCreatedAt().isAfter(filter.getToDate().atTime(LocalTime.MAX))) {
			return false;
		}
		if (filter.getStatus() != null && !filter.getStatus().isBlank()
				&& !t.getStatus().name().equalsIgnoreCase(filter.getStatus())) {
			return false;
		}
		if (filter.getType() != null && !filter.getType().isBlank()
				&& !t.getTransactionType().name().equalsIgnoreCase(filter.getType())) {
			return false;
		}
		if (filter.getMinAmount() != null && t.getAmount().compareTo(filter.getMinAmount()) < 0) {
			return false;
		}
		if (filter.getMaxAmount() != null && t.getAmount().compareTo(filter.getMaxAmount()) > 0) {
			return false;
		}
		if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
			String q = filter.getSearch().toLowerCase();
			boolean matches = t.getReferenceId().toLowerCase().contains(q)
					|| (t.getSenderVpa() != null && t.getSenderVpa().toLowerCase().contains(q))
					|| (t.getReceiverVpa() != null && t.getReceiverVpa().toLowerCase().contains(q))
					|| (t.getSender() != null && t.getSender().getFullName().toLowerCase().contains(q))
					|| (t.getReceiver() != null && t.getReceiver().getFullName().toLowerCase().contains(q));
			if (!matches)
				return false;
		}
		return true;
	}

	private TransactionResponse toResponse(Transaction t, User viewpoint) {
		boolean isSender = t.getSender() != null && t.getSender().getId().equals(viewpoint.getId());
		User counterparty = isSender ? t.getReceiver() : t.getSender();
		return TransactionResponse.builder().id(t.getId()).referenceId(t.getReferenceId())
				.type(isSender ? "DEBIT" : "CREDIT")
				.counterpartyName(counterparty != null ? counterparty.getFullName() : "PayFlow Wallet")
				.counterpartyVpa(isSender ? t.getReceiverVpa() : t.getSenderVpa()).amount(t.getAmount())
				.note(t.getNote()).status(t.getStatus().name()).timestamp(t.getCreatedAt()).category(t.getCategory())
				.riskLevel(t.getRiskLevel() != null ? t.getRiskLevel().name() : null).build();
	}
}
