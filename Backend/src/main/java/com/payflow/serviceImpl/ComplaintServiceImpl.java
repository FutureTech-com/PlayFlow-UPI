package com.payflow.serviceImpl;

import com.payflow.dto.complaint.ComplaintResponse;
import com.payflow.dto.complaint.CreateComplaintRequest;
import com.payflow.dto.complaint.ResolveComplaintRequest;
import com.payflow.entity.Complaint;
import com.payflow.entity.Complaint.ComplaintStatus;
import com.payflow.entity.User;
import com.payflow.exception.BadRequestException;
import com.payflow.exception.ResourceNotFoundException;
import com.payflow.repository.ComplaintRepository;
import com.payflow.service.ComplaintService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComplaintServiceImpl implements ComplaintService {

	private final ComplaintRepository complaintRepository;

	@SuppressWarnings("null")
	@Override
	public ComplaintResponse create(User user, CreateComplaintRequest request) {
		Complaint complaint = Complaint.builder().user(user).transactionId(request.getRelatedTransactionId())
				.description(request.getDescription()).status(ComplaintStatus.OPEN).build();

		return ComplaintResponse.from(complaintRepository.save(complaint));
	}
	
	@Override
    @Transactional(readOnly = true)
    public List<ComplaintResponse> listMine(User user) {
        return complaintRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(ComplaintResponse::from)
                .toList();
    }


	@Override
	public List<ComplaintResponse> listAll() {
		return complaintRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
	}

	@Override
	@Transactional
	public ComplaintResponse resolve(String id, ResolveComplaintRequest request) {
		@SuppressWarnings("null")
		Complaint complaint = complaintRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));

		Complaint.ComplaintStatus newStatus;
		try {
			newStatus = switch (request.getAction().toUpperCase()) {
			case "RESOLVE" -> Complaint.ComplaintStatus.RESOLVED;
			case "REJECT" -> Complaint.ComplaintStatus.REJECTED;
			case "REVIEW" -> Complaint.ComplaintStatus.IN_REVIEW;
			default -> throw new BadRequestException("Action must be RESOLVE, REJECT, or REVIEW");
			};
		} catch (NullPointerException e) {
			throw new BadRequestException("Action is required");
		}

		complaint.setStatus(newStatus);
		complaint.setResolutionNote(request.getNote());
		if (newStatus == Complaint.ComplaintStatus.RESOLVED || newStatus == Complaint.ComplaintStatus.REJECTED) {
			complaint.setResolvedAt(Instant.now());
		}

		return toResponse(complaintRepository.save(complaint));
	}

	private ComplaintResponse toResponse(Complaint c) {
		return ComplaintResponse.builder().id(c.getId()).userId(c.getUser().getId()).subject(c.getSubject())
				.description(c.getDescription()).status(c.getStatus().name()).createdAt(c.getCreatedAt())
				.resolvedAt(c.getResolvedAt()).resolutionNote(c.getResolutionNote()).build();
	}
}