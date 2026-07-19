package com.payflow.service;

import com.payflow.dto.payment.SendMoneyRequest;
import com.payflow.dto.splitbill.CreateSplitBillRequest;
import com.payflow.dto.splitbill.SplitBillResponse;
import com.payflow.entity.*;
import com.payflow.exception.BadRequestException;
import com.payflow.exception.ResourceNotFoundException;
import com.payflow.repository.SplitBillRepository;
import com.payflow.repository.SplitBillShareRepository;
import com.payflow.repository.UpiIdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SplitBillService {

    private final SplitBillRepository splitBillRepository;
    private final SplitBillShareRepository splitBillShareRepository;
    private final UpiIdRepository upiIdRepository;
    private final PaymentService paymentService;
    private final NotificationService notificationService;

    @SuppressWarnings("null")
	@Transactional
    public SplitBillResponse create(User organizer, CreateSplitBillRequest request) {
        SplitBill bill = new SplitBill();
        bill.setOrganizer(organizer);
        bill.setTitle(request.getTitle());
        bill.setTotalAmount(request.getTotalAmount());
        SplitBill savedBill = splitBillRepository.save(bill);

        for (CreateSplitBillRequest.ParticipantShare p : request.getParticipants()) {
            UpiId participantUpi = upiIdRepository.findByVpa(p.getVpa())
                    .orElseThrow(() -> new BadRequestException("No PayFlow user found with UPI ID " + p.getVpa()));

            SplitBillShare share = new SplitBillShare();
            share.setSplitBill(savedBill);
            share.setParticipant(participantUpi.getUser());
            share.setAmountOwed(p.getAmount());
            splitBillShareRepository.save(share);

            if (!participantUpi.getUser().getId().equals(organizer.getId())) {
                notificationService.createNotification(participantUpi.getUser(), "You've been added to a split bill",
                        organizer.getFullName() + " added you to \"" + request.getTitle() + "\" - you owe \u20B9" + p.getAmount() + ".",
                        Notification.NotificationType.GENERAL);
            }
        }

        return toResponse(splitBillRepository.findById(savedBill.getId()).orElseThrow());
    }

    /** The participant pays their share; requires their own transaction PIN, same as any payment. */
    @Transactional
    public SplitBillResponse settleShare(User participant, String shareId, String transactionPin) {
        @SuppressWarnings("null")
		SplitBillShare share = splitBillShareRepository.findById(shareId)
                .orElseThrow(() -> new ResourceNotFoundException("Split bill share not found"));

        if (!share.getParticipant().getId().equals(participant.getId())) {
            throw new BadRequestException("This share does not belong to you");
        }
        if (share.isSettled()) {
            throw new BadRequestException("This share has already been settled");
        }

        String organizerVpa = upiIdRepository.findByUser(share.getSplitBill().getOrganizer()).stream()
                .findFirst().map(UpiId::getVpa)
                .orElseThrow(() -> new BadRequestException("Organizer has no UPI ID"));

        SendMoneyRequest sendRequest = new SendMoneyRequest();
        sendRequest.setReceiverVpa(organizerVpa);
        sendRequest.setAmount(share.getAmountOwed());
        sendRequest.setNote("Split bill: " + share.getSplitBill().getTitle());
        sendRequest.setTransactionPin(transactionPin);

        var txn = paymentService.sendMoney(participant, sendRequest);

        share.setSettled(true);
        share.setSettledTransactionId(txn.getId());
        splitBillShareRepository.save(share);

        return toResponse(share.getSplitBill());
    }

    public List<SplitBillResponse> listOrganized(User organizer) {
        return splitBillRepository.findByOrganizerOrderByCreatedAtDesc(organizer).stream().map(this::toResponse).toList();
    }

    public List<com.payflow.dto.splitbill.MyShareResponse> listMyShares(User participant) {
        return splitBillShareRepository.findByParticipantOrderByCreatedAtDesc(participant).stream()
                .map(s -> com.payflow.dto.splitbill.MyShareResponse.builder()
                        .shareId(s.getId())
                        .billTitle(s.getSplitBill().getTitle())
                        .organizerName(s.getSplitBill().getOrganizer().getFullName())
                        .amountOwed(s.getAmountOwed())
                        .settled(s.isSettled())
                        .build())
                .toList();
    }

    private SplitBillResponse toResponse(SplitBill bill) {
        return SplitBillResponse.builder()
                .id(bill.getId())
                .title(bill.getTitle())
                .totalAmount(bill.getTotalAmount())
                .organizerName(bill.getOrganizer().getFullName())
                .createdAt(bill.getCreatedAt())
                .shares(bill.getShares().stream().map(s -> SplitBillResponse.ShareView.builder()
                        .id(s.getId())
                        .participantName(s.getParticipant().getFullName())
                        .participantVpa(vpaOf(s.getParticipant()))
                        .amountOwed(s.getAmountOwed())
                        .settled(s.isSettled())
                        .build()).toList())
                .build();
    }

    private String vpaOf(User user) {
        return upiIdRepository.findByUser(user).stream().findFirst().map(UpiId::getVpa).orElse(null);
    }
}