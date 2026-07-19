package com.payflow.dto.bank;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LinkByMobileRequest {
    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Enter a valid 10-digit mobile number")
    private String mobileNumber;

    /** Which of the accounts returned by /bank/fetch-by-mobile to link (its accountIndex). */
    @NotNull(message = "Select an account to link")
    private Integer accountIndex;
}