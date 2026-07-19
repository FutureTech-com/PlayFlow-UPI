package com.payflow.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateKycRequest {
	@NotBlank(message = "Status must be PENDING, VERIFIED, or REJECTED")
    private String status;      // PENDING, VERIFIED, REJECTED
}