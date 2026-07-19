package com.payflow.service;

import com.payflow.dto.scheduledpayment.CreateScheduledPaymentRequest;
import com.payflow.dto.scheduledpayment.ScheduledPaymentResponse;
import com.payflow.entity.ScheduledPayment;
import com.payflow.entity.User;
import com.payflow.exception.BadRequestException;
import com.payflow.exception.ResourceNotFoundException;
import com.payflow.repository.ScheduledPaymentRepository;
import com.payflow.repository.UpiIdRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledPaymentService {

    private final ScheduledPaymentRepository scheduledPaymentRepository;
    private final UpiIdRepository upiIdRepository;
    private final PaymentService paymentService;

    @Transactional
    public ScheduledPaymentResponse create(User owner, CreateScheduledPaymentRequest request) {
        ScheduledPayment.Recurrence recurrence;
        try {
            recurrence = ScheduledPayment.Recurrence.valueOf(request.getRecurrence().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Recurrence must be ONCE, DAILY, WEEKLY, or MONTHLY");
        }

        // Fail fast on a bad VPA rather than waiting for the first scheduled run to discover it
        upiIdRepository.findByVpa(request.getReceiverVpa())
                .orElseThrow(() -> new BadRequestException("Recipient UPI ID not found: " + request.getReceiverVpa()));

        ScheduledPayment sp = new ScheduledPayment();
        sp.setOwner(owner);
        sp.setReceiverVpa(request.getReceiverVpa());
        sp.setAmount(request.getAmount());
        sp.setNote(request.getNote());
        sp.setRecurrence(recurrence);
        sp.setNextRunAt(request.getStartAt());
        sp.setActive(true);

        return toResponse(scheduledPaymentRepository.save(sp));
    }

    public List<ScheduledPaymentResponse> list(User owner) {
        return scheduledPaymentRepository.findByOwnerOrderByNextRunAtAsc(owner).stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    public void cancel(User owner, @NonNull String id) {
        ScheduledPayment sp = scheduledPaymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled payment not found"));
        if (!sp.getOwner().getId().equals(owner.getId())) {
            throw new BadRequestException("This scheduled payment does not belong to you");
        }
        if (!sp.isActive()) {
            throw new BadRequestException("This scheduled payment is already cancelled");
        }
        sp.setActive(false);
        scheduledPaymentRepository.save(sp);
    }

    /** Runs every minute; executes any due scheduled/recurring payments. */
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void runDuePayments() {
        List<ScheduledPayment> due = scheduledPaymentRepository.findByActiveTrueAndNextRunAtLessThanEqual(LocalDateTime.now());
        for (ScheduledPayment sp : due) {
            executeOne(sp);
        }
    }

    private void executeOne(ScheduledPayment sp) {
        boolean succeeded = false;
        try {
            if (!sp.getOwner().isPinSet()) {
                throw new BadRequestException("Transaction PIN not set - AutoPay paused");
            }
            paymentService.executeAutoPay(sp.getOwner(), sp.getReceiverVpa(), sp.getAmount(),
                    sp.getNote() != null ? sp.getNote() : "AutoPay");
            sp.setLastFailureReason(null);
            succeeded = true;
        } catch (Exception e) {
            sp.setLastFailureReason(e.getMessage());
            log.warn("Scheduled payment {} failed: {}", sp.getId(), e.getMessage());
        } finally {
            sp.setLastRunAt(LocalDateTime.now());
            // Only advance ONCE schedules on success - a failed one-time payment should
            // remain active and retry on the next poll, not silently disappear.
            // Recurring schedules still advance to the next cycle regardless of outcome,
            // matching how real UPI AutoPay mandates behave (a missed cycle doesn't retry
            // mid-cycle; it just tries again next cycle).
            if (sp.getRecurrence() == ScheduledPayment.Recurrence.ONCE) {
                if (succeeded) {
                    sp.setActive(false);
                }
            } else {
                advance(sp);
            }
            scheduledPaymentRepository.save(sp);
        }
    }

    private void advance(ScheduledPayment sp) {
        switch (sp.getRecurrence()) {
            case DAILY -> sp.setNextRunAt(sp.getNextRunAt().plusDays(1));
            case WEEKLY -> sp.setNextRunAt(sp.getNextRunAt().plusWeeks(1));
            case MONTHLY -> sp.setNextRunAt(sp.getNextRunAt().plusMonths(1));
            case ONCE -> { /* handled in executeOne */ }
        }
    }

    private ScheduledPaymentResponse toResponse(ScheduledPayment sp) {
        return ScheduledPaymentResponse.builder()
                .id(sp.getId())
                .receiverVpa(sp.getReceiverVpa())
                .amount(sp.getAmount())
                .note(sp.getNote())
                .recurrence(sp.getRecurrence().name())
                .nextRunAt(sp.getNextRunAt())
                .lastRunAt(sp.getLastRunAt())
                .active(sp.isActive())
                .lastFailureReason(sp.getLastFailureReason())
                .build();
    }
}