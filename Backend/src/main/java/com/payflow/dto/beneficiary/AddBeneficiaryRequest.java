package com.payflow.dto.beneficiary;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddBeneficiaryRequest {
    @NotBlank
    private String nickname;

    @NotBlank
    private String vpa;
}
