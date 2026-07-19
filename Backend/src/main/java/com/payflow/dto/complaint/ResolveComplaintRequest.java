package com.payflow.dto.complaint;

import com.payflow.entity.Complaint.ComplaintStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResolveComplaintRequest {

	@NotBlank(message = "Status must be IN_REVIEW, RESOLVED, or REJECTED")
	private String action; // RESOLVE, REJECT, REVIEW
	private String note;
	@NotNull(message = "Status is required")
	ComplaintStatus status;
	
	@Size(max = 2000, message = "Resolution note must not exceed 2000 characters")
    String resolutionNote;
}