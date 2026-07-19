package com.payflow.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
	private String accessToken;
	private String refreshToken;
	
	@Builder.Default
	private String tokenType = "Bearer";
	private UserSummary user;

	

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class UserSummary {
		private String id;
		private String fullName;
		private String email;
		private String phone;
	}
}
