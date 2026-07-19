package com.payflow.service;

import com.payflow.dto.ai.ChatResponse;
import com.payflow.dto.ai.SuggestedAction;
import com.payflow.entity.BankAccount;
import com.payflow.entity.Transaction;
import com.payflow.entity.UpiId;
import com.payflow.entity.User;
import com.payflow.repository.BankAccountRepository;
import com.payflow.repository.TransactionRepository;
import com.payflow.repository.UpiIdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Rule-based support chatbot: intent matching over keywords, backed by real
 * account data where it can answer directly (balance, UPI ID, last transaction)
 * rather than only ever pointing at a help page. This is deliberately not wired
 * to an external LLM - see the project README for why (no API key management
 * inside a demo app, and this keeps every response's data source auditable).
 * The intent-matching structure here is exactly what you'd wrap an LLM call
 * around later for open-ended queries; the "in-app data lookup" tools
 * (getBalance, getUpiId, getLastTransaction) are the same tools an LLM-backed
 * version would call.
 */
@Service
@RequiredArgsConstructor
public class ChatbotService {

	private final BankAccountRepository bankAccountRepository;
	private final UpiIdRepository upiIdRepository;
	private final TransactionRepository transactionRepository;

	public ChatResponse respond(User user, String rawMessage) {
		String msg = rawMessage.toLowerCase(Locale.ROOT).trim();

		if (containsAny(msg, "balance", "how much money", "how much do i have")) {
			return balanceReply(user);
		}
		if (containsAny(msg, "upi id", "my vpa", "my upi", "payment address")) {
			return upiIdReply(user);
		}
		if (containsAny(msg, "last transaction", "recent payment", "last payment", "transaction history",
				"my transactions")) {
			return lastTransactionReply(user);
		}
		if (containsAny(msg, "send money", "how to send", "pay someone", "transfer money")) {
			return new ChatResponse(
					"You can send money by UPI ID, mobile number, or bank account + IFSC. Tap Send, enter the recipient, amount, and your transaction PIN.",
					List.of(new SuggestedAction("Send money", "/send")));
		}
		if (containsAny(msg, "receive money", "qr code", "get paid", "generate qr")) {
			return new ChatResponse(
					"Share your UPI ID or QR code with the sender - money lands in your linked bank account instantly.",
					List.of(new SuggestedAction("Show my QR code", "/receive")));
		}
		if (containsAny(msg, "scan", "scan qr")) {
			return new ChatResponse("Open Scan & Pay and point your camera at any PayFlow QR code to pay instantly.",
					List.of(new SuggestedAction("Scan & pay", "/scan")));
		}
		if (containsAny(msg, "link account", "link bank", "add bank", "add account")) {
			return new ChatResponse(
					"You can link a bank account by fetching accounts linked to your mobile number, or by entering the details manually.",
					List.of(new SuggestedAction("Bank accounts", "/bank-accounts")));
		}
		if (containsAny(msg, "request money", "collect request", "ask for money")) {
			return new ChatResponse(
					"You can request money from any UPI ID - they'll get a notification to accept or decline.",
					List.of(new SuggestedAction("Request money", "/request")));
		}
		if (containsAny(msg, "forgot pin", "reset pin", "change pin", "forgot my pin")) {
			return new ChatResponse(
					"Go to Settings and tap \"Forgot PIN?\" - we'll send an OTP to your registered mobile number to verify it's you before you set a new PIN.",
					List.of(new SuggestedAction("Settings", "/settings")));
		}
		if (containsAny(msg, "fraud", "suspicious", "hacked", "unauthorized", "scam", "unauthorised")) {
			return new ChatResponse(
					"If you notice a payment you didn't make, report it right away from Security & devices, and consider changing your password and PIN immediately.",
					List.of(new SuggestedAction("Security & devices", "/security"),
							new SuggestedAction("Report an issue", "/complaints")));
		}
		if (containsAny(msg, "budget", "spending", "insight", "analytics")) {
			return new ChatResponse(
					"You can see AI-generated spending insights and budget suggestions based on your last few months of activity.",
					List.of(new SuggestedAction("Spending insights", "/analytics")));
		}
		if (containsAny(msg, "complaint", "help", "support", "issue", "problem", "not working")) {
			return new ChatResponse(
					"Sorry you're running into trouble. You can raise a complaint and track it from the Help & complaints page, and our team will follow up.",
					List.of(new SuggestedAction("Help & complaints", "/complaints")));
		}
		if (containsAny(msg, "hi", "hello", "hey", "namaste")) {
			return new ChatResponse("Hi " + user.getFullName().split(" ")[0]
					+ "! I'm the PayFlow assistant. Ask me about your balance, UPI ID, recent payments, or how to send/request money.",
					List.of());
		}
		if (containsAny(msg, "thank", "thanks", "thank you")) {
			return new ChatResponse("You're welcome! Anything else I can help with?", List.of());
		}

		return new ChatResponse(
				"I'm not sure about that one yet. I can help with your balance, UPI ID, sending or requesting money, linking a bank account, or resetting your PIN. "
						+ "For anything else, our support team can help.",
				List.of(new SuggestedAction("Help & complaints", "/complaints")));
	}

	private ChatResponse balanceReply(User user) {
		Optional<BankAccount> primary = bankAccountRepository.findByUserAndPrimaryAccountTrue(user);
		if (primary.isEmpty()) {
			return new ChatResponse(
					"You don't have a primary bank account set up yet. Link one to see your balance here.",
					List.of(new SuggestedAction("Bank accounts", "/bank-accounts")));
		}
		BankAccount acc = primary.get();
		return new ChatResponse(String.format("Your %s account balance is ₹%s.", acc.getBankName(), acc.getBalance()),
				List.of(new SuggestedAction("View accounts", "/bank-accounts")));
	}

	private ChatResponse upiIdReply(User user) {
		List<UpiId> ids = upiIdRepository.findByUser(user);
		if (ids.isEmpty()) {
			return new ChatResponse(
					"You haven't created a UPI ID yet. Link a bank account first, then create one from there.",
					List.of(new SuggestedAction("Bank accounts", "/bank-accounts")));
		}
		String vpaList = ids.stream().map(UpiId::getVpa).reduce((a, b) -> a + ", " + b).orElse("");
		return new ChatResponse("Your UPI ID is " + vpaList + ".",
				List.of(new SuggestedAction("Show QR code", "/receive")));
	}

	private ChatResponse lastTransactionReply(User user) {
		Optional<Transaction> last = transactionRepository.findAllForUser(user).stream()
				.max(Comparator.comparing(Transaction::getCreatedAt));
		if (last.isEmpty()) {
			return new ChatResponse("You haven't made any transactions yet.",
					List.of(new SuggestedAction("Send money", "/send")));
		}
		Transaction t = last.get();
		boolean isSender = t.getSender() != null && t.getSender().getId().equals(user.getId());
		String direction = isSender ? "paid" : "received";
		String counterparty = isSender ? t.getReceiverVpa() : t.getSenderVpa();
		return new ChatResponse(
				String.format("Your last transaction: you %s ₹%s %s %s, status %s.", direction, t.getAmount(),
						isSender ? "to" : "from", counterparty, t.getStatus().name().toLowerCase()),
				List.of(new SuggestedAction("View all transactions", "/transactions")));
	}

	private boolean containsAny(String haystack, String... needles) {
		for (String n : needles) {
			if (haystack.contains(n))
				return true;
		}
		return false;
	}
}