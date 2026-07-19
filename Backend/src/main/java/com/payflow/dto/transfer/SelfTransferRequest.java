package com.payflow.dto.transfer;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class SelfTransferRequest {

	@NotBlank
	@NotNull(message = "From account ID is required")
	private String fromBankAccountId;
	
	@NotBlank
	@NotNull(message = "To account ID is required")
	private String toBankAccountId;

	@NotNull(message = "Amount is required")
	@DecimalMin(value = "1.0", message = "Amount must be greater than zero")
	private BigDecimal amount;

	private String remarks;
	private String transactionPin;
}