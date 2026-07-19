package com.payflow.service;

import com.payflow.dto.upi.CreateUpiRequest;
import com.payflow.dto.upi.UpiIdResponse;
import com.payflow.entity.BankAccount;
import com.payflow.entity.UpiId;
import com.payflow.entity.User;
import com.payflow.exception.BadRequestException;
import com.payflow.exception.ResourceNotFoundException;
import com.payflow.repository.BankAccountRepository;
import com.payflow.repository.UpiIdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UpiService {

    private static final String DOMAIN = "@payflow";
    private static final Pattern SLUG_PATTERN = Pattern.compile("[^a-z0-9.]");

    private final UpiIdRepository upiIdRepository;
    private final BankAccountRepository bankAccountRepository;
    private final QrCodeService qrCodeService;
    private final AuditLogService auditLogService;

	@Transactional
    public UpiIdResponse createUpiId(User user, CreateUpiRequest request) {
        @SuppressWarnings("null")
		BankAccount account = bankAccountRepository.findById(request.getBankAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found"));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("This bank account does not belong to you");
        }

        String base = request.getPreferredHandle() != null && !request.getPreferredHandle().isBlank()
                ? slugify(request.getPreferredHandle())
                : slugify(user.getFullName());

        String vpa = generateUniqueVpa(base);

        UpiId upiId = new UpiId();
        upiId.setUser(user);
        upiId.setBankAccount(account);
        upiId.setVpa(vpa);
        upiId.setMerchant(request.isMerchant());
        upiId.setQrCodeBase64(qrCodeService.generateQrCodeBase64(vpa, user.getFullName()));

        UpiId saved = upiIdRepository.save(upiId);
        auditLogService.log(user.getId(), "UPI_ID_CREATED", "UpiId", saved.getId(), vpa);

        return toResponse(saved);
    }

    public List<UpiIdResponse> listUpiIds(User user) {
        return upiIdRepository.findByUser(user).stream().map(this::toResponse).toList();
    }

    public UpiIdResponse getDetails(User user, String vpa) {
        UpiId upiId = upiIdRepository.findByVpa(vpa)
                .orElseThrow(() -> new ResourceNotFoundException("UPI ID not found"));
        return toResponse(upiId);
    }

    /** Dynamic QR: encodes a specific amount/note on the fly, without persisting anything. */
    
    public String generateDynamicQr(User user, String vpa, java.math.BigDecimal amount, String note) {
        UpiId upiId = upiIdRepository.findByVpa(vpa)
                .orElseThrow(() -> new ResourceNotFoundException("UPI ID not found"));
        if (!upiId.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("This UPI ID does not belong to you");
        }
        return qrCodeService.generateDynamicQrCodeBase64(vpa, upiId.getUser().getFullName(), amount, note);
    }
    
    public String generateUniqueVpa(String base) {
        String candidate = base + DOMAIN;
        int suffix = 1;
        while (upiIdRepository.existsByVpa(candidate)) {
            candidate = base + suffix + DOMAIN;
            suffix++;
        }
        return candidate;
    }

    public String slugify(String input) {
        String lower = input.toLowerCase().trim().replace(" ", ".");
        String cleaned = SLUG_PATTERN.matcher(lower).replaceAll("");
        return cleaned.isBlank() ? "user" : cleaned;
    }

   
    private UpiIdResponse toResponse(UpiId upiId) {
        return UpiIdResponse.builder()
                .id(upiId.getId())
                .vpa(upiId.getVpa())
                .active(upiId.isActive())
                .merchant(upiId.isMerchant())
                .linkedBankAccountId(upiId.getBankAccount().getId())
                .linkedBankName(upiId.getBankAccount().getBankName())
                .qrCodeBase64(upiId.getQrCodeBase64())
                .build();
    }
}
