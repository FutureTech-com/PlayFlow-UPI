package com.payflow.dto.complaint;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

import com.payflow.entity.Complaint;

@Data
@Builder
public class ComplaintResponse {
    private String id;
    private String userId;
    private String subject;
    private String description;
    private String status;      // OPEN, IN_REVIEW, RESOLVED, REJECTED
    private Instant createdAt;
    private Instant resolvedAt;
    private String resolutionNote;
    private String relatedTransactionId;
    private String transactionId;
	
    public static ComplaintResponse from(Complaint complaint) {
        if (complaint == null) {
            return null;
        }
        return ComplaintResponse.builder()
                .id(complaint.getId())
                .userId(complaint.getUser() != null ? complaint.getUser().getId() : null)
                .subject(complaint.getSubject())
                .description(complaint.getDescription())
                .status(complaint.getStatus() != null ? complaint.getStatus().name() : null)
                .createdAt(complaint.getCreatedAt())
                .resolvedAt(complaint.getResolvedAt())
                .resolutionNote(complaint.getResolutionNote())
                .transactionId(complaint.getTransactionId())
                .build();
    }
}