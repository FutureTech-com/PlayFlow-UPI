package com.payflow.dto.splitbill;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateSplitBillRequest {

    @NotBlank
    private String title;

    private String description;

    @NotNull
    @DecimalMin(value = "1.00", message = "Total amount must be at least 1.00")
    private BigDecimal totalAmount;

    private String sourceTransactionId;

    @NotEmpty(message = "At least one participant is required")
    @Valid
    private List<ParticipantShare> participants;

    @Data
    public static class ParticipantShare {

        @NotBlank
        private String vpa;

        @NotNull
        @DecimalMin(value = "0.01")
        private BigDecimal amount;
    }
}