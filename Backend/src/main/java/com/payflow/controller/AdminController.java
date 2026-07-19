package com.payflow.controller;

import com.payflow.dto.admin.AdminDashboardResponse;
import com.payflow.dto.admin.UpdateKycRequest;
import com.payflow.dto.complaint.ComplaintResponse;
import com.payflow.dto.complaint.ResolveComplaintRequest;
import com.payflow.entity.Transaction;
import com.payflow.entity.User;
import com.payflow.service.AdminService;
import com.payflow.service.ComplaintService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin-only endpoints - requires ROLE_ADMIN")
public class AdminController {

	private final AdminService adminService;
	private final ComplaintService complaintService;

	@GetMapping("/dashboard")
	@Operation(summary = "Get platform-wide stats for the admin dashboard")
	public ResponseEntity<AdminDashboardResponse> dashboard() {
		return ResponseEntity.ok(adminService.getDashboard());
	}

	@GetMapping("/users")
	@Operation(summary = "List all users")
	public ResponseEntity<List<User>> users() {
		return ResponseEntity.ok(adminService.listUsers());
	}

	@SuppressWarnings("null")
	@PatchMapping("/users/{id}/enabled")
	@Operation(summary = "Enable or disable a user account")
	public ResponseEntity<User> setEnabled(@PathVariable String id, @RequestParam boolean enabled) {
		return ResponseEntity.ok(adminService.setUserEnabled(id, enabled));
	}

	@GetMapping("/transactions")
	@Operation(summary = "List all transactions across the platform (monitoring)")
	public ResponseEntity<List<Transaction>> transactions() {
		return ResponseEntity.ok(adminService.listAllTransactions());
	}

	@PatchMapping("/users/{id}/kyc")
	@Operation(summary = "Update a user's KYC verification status")
	public ResponseEntity<User> updateKyc(@PathVariable String id,
			@org.springframework.web.bind.annotation.RequestBody UpdateKycRequest request) {
		return ResponseEntity.ok(adminService.updateKycStatus(id, request.getStatus()));
	}

	@GetMapping("/complaints")
	@Operation(summary = "List all complaints across the platform")
	public ResponseEntity<List<ComplaintResponse>> complaints() {
		return ResponseEntity.ok(complaintService.listAll());
	}

	@PostMapping("/complaints/{id}/resolve")
	@Operation(summary = "Resolve, reject, or move a complaint into review")
	public ResponseEntity<ComplaintResponse> resolveComplaint(@PathVariable String id,
			@RequestBody ResolveComplaintRequest request) {
		return ResponseEntity.ok(complaintService.resolve(id, request));
	}

	@GetMapping("/cashback-overview")
	@Operation(summary = "Platform-wide cashback issuance summary")
	public ResponseEntity<Map<String, Object>> cashbackOverview() {
		return ResponseEntity.ok(adminService.getCashbackOverview());
	}
	
	@GetMapping("/fraud-alerts")
    @Operation(summary = "Platform-wide transactions flagged by fraud prediction (medium/high risk)")
    public ResponseEntity<List<com.payflow.dto.ai.FraudAlertResponse>> fraudAlerts() {
        return ResponseEntity.ok(adminService.listFraudAlerts());
    }
}
