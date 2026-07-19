package com.payflow.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {
    private String id;
    private String fullName;
    private String email;
    private String phone;
    private boolean phoneVerified;
    private boolean emailVerified;
    private boolean pinSet;
    private String kycStatus;
    private LocalDateTime memberSince;    
    
}
