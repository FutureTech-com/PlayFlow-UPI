package com.payflow.dto.payment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RespondToRequestRequest {
    @NotNull(message = "accept must be true or false")
    private Boolean accept;

    /** Required only when accept = true */
    private String transactionPin;

}
