package com.payflow.service;

import com.payflow.dto.beneficiary.AddBeneficiaryRequest;
import com.payflow.dto.beneficiary.BeneficiaryResponse;
import com.payflow.entity.Beneficiary;
import com.payflow.entity.User;
import com.payflow.exception.BadRequestException;
import com.payflow.exception.ResourceNotFoundException;
import com.payflow.repository.BeneficiaryRepository;
import com.payflow.repository.UpiIdRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final UpiIdRepository upiIdRepository;

    @Transactional
    public BeneficiaryResponse add(User owner, AddBeneficiaryRequest request) {
        if (!upiIdRepository.existsByVpa(request.getVpa())) {
            throw new BadRequestException("No PayFlow user found with UPI ID " + request.getVpa());
        }
        Beneficiary b = new Beneficiary();
        b.setOwner(owner);
        b.setNickName(request.getNickname());
        b.setVpa(request.getVpa());
        return toResponse(beneficiaryRepository.save(b));
    }

    public List<BeneficiaryResponse> list(User owner) {
        return beneficiaryRepository.findByOwner(owner).stream().map(this::toResponse).toList();
    }

    @Transactional
    public BeneficiaryResponse toggleFavourite(User owner, @NonNull String id) {
        Beneficiary b = beneficiaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary not found"));
        if (!b.getOwner().getId().equals(owner.getId())) {
            throw new BadRequestException("This beneficiary does not belong to you");
        }
        b.setFavourite(!b.isFavourite());
        return toResponse(beneficiaryRepository.save(b));
    }

    @Transactional
    public void remove(User owner, @NonNull String id) {
        Beneficiary b = beneficiaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary not found"));
        if (!b.getOwner().getId().equals(owner.getId())) {
            throw new BadRequestException("This beneficiary does not belong to you");
        }
        beneficiaryRepository.delete(b);
    }

    private BeneficiaryResponse toResponse(Beneficiary b) {
        return BeneficiaryResponse.builder()
                .id(b.getId())
                .nickname(b.getNickName())
                .vpa(b.getVpa())
                .favourite(b.isFavourite())
                .build();
    }
}
