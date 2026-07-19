package com.payflow.dto.beneficiary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BeneficiaryResponse {
    private String id;
    private String nickname;
    private String vpa;
    private boolean favourite;
}
