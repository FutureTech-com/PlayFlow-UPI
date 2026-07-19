package com.payflow.service;

import com.payflow.dto.wallet.AddMoneyRequest;
import com.payflow.dto.wallet.WalletResponse;
import com.payflow.dto.wallet.WalletToBankRequest;
import com.payflow.entity.*;
import com.payflow.exception.BadRequestException;
import com.payflow.repository.BankAccountRepository;
import com.payflow.repository.TransactionRepository;
import com.payflow.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A simple in-app wallet, separate from linked bank accounts. Money can be added to it
 * (simulated - no real payment gateway) and moved back out to a bank account.
 */
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    private final SecureRandom random = new SecureRandom();
    private static final DateTimeFormatter REF_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public WalletResponse getWallet(User user) {
        Wallet wallet = getOrCreateWallet(user);
        return toResponse(wallet);
    }

    @Transactional
    public WalletResponse addMoney(User user, AddMoneyRequest request) {
        Wallet wallet = getOrCreateWallet(user);
        wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        walletRepository.save(wallet);

        Transaction txn = new Transaction();
        txn.setReferenceId(generateReferenceId());
        txn.setSender(user);
        txn.setReceiver(user);
        txn.setSenderVpa("Linked card/bank (simulated)");
        txn.setReceiverVpa("PayFlow Wallet");
        txn.setAmount(request.getAmount());
        txn.setNote("Added to wallet");
        txn.setTransactionType(Transaction.TransactionType.WALLET_ADD);
        txn.setCategory(ExpenseCategorizationService.TRANSFER);
        txn.setStatus(Transaction.TransactionStatus.SUCCESS);
        transactionRepository.save(txn);

        notificationService.createNotification(user, "Money added to wallet",
                "\u20B9" + request.getAmount() + " added to your PayFlow Wallet.", Notification.NotificationType.PAYMENT_SUCCESS);
        auditLogService.log(user.getId(), "WALLET_ADD_MONEY", "Wallet", wallet.getId(), "amount=" + request.getAmount());

        return toResponse(wallet);
    }

    @Transactional
    public WalletResponse transferToBank(User user, WalletToBankRequest request) {
        Wallet wallet = getOrCreateWallet(user);
        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BadRequestException("Insufficient wallet balance");
        }
        BankAccount primary = bankAccountRepository.findByUserAndPrimaryAccountTrue(user)
                .orElseThrow(() -> new BadRequestException("No primary bank account set"));

        wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
        primary.setBalance(primary.getBalance().add(request.getAmount()));
        walletRepository.save(wallet);
        bankAccountRepository.save(primary);

        Transaction txn = new Transaction();
        txn.setReferenceId(generateReferenceId());
        txn.setSender(user);
        txn.setReceiver(user);
        txn.setSenderVpa("PayFlow Wallet");
        txn.setReceiverVpa(primary.getBankName());
        txn.setAmount(request.getAmount());
        txn.setNote("Wallet to bank transfer");
        txn.setTransactionType(Transaction.TransactionType.WALLET_TO_BANK);
        txn.setCategory(ExpenseCategorizationService.TRANSFER);
        txn.setStatus(Transaction.TransactionStatus.SUCCESS);
        transactionRepository.save(txn);

        notificationService.createNotification(user, "Wallet transfer complete",
                "\u20B9" + request.getAmount() + " moved from your wallet to " + primary.getBankName() + ".",
                Notification.NotificationType.PAYMENT_SUCCESS);
        auditLogService.log(user.getId(), "WALLET_TO_BANK", "Wallet", wallet.getId(), "amount=" + request.getAmount());

        return toResponse(wallet);
    }

    private Wallet getOrCreateWallet(User user) {
        return walletRepository.findByUser(user).orElseGet(() -> {
            Wallet wallet = new Wallet();
            wallet.setUser(user);
            wallet.setBalance(BigDecimal.ZERO);
            return walletRepository.save(wallet);
        });
    }

    private String generateReferenceId() {
        String timestamp = LocalDateTime.now().format(REF_FORMAT);
        int randomSuffix = 1000 + random.nextInt(9000);
        return "PFLW" + timestamp + randomSuffix;
    }

    private WalletResponse toResponse(Wallet wallet) {
        return WalletResponse.builder().id(wallet.getId()).balance(wallet.getBalance()).build();
    }
}