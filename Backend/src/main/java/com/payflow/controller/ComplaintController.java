package com.payflow.controller;

import com.payflow.dto.complaint.ComplaintResponse;
import com.payflow.dto.complaint.CreateComplaintRequest;
import com.payflow.security.UserPrincipal;
import com.payflow.service.ComplaintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
@Tag(name = "Complaints", description = "Raise and track support complaints (admin resolves these)")
public class ComplaintController {

    private final ComplaintService complaintService;

    @PostMapping
    @Operation(summary = "Raise a complaint, optionally linked to a transaction")
    public ResponseEntity<ComplaintResponse> create(@AuthenticationPrincipal UserPrincipal principal,
                                                      @Valid @RequestBody CreateComplaintRequest request) {
        return ResponseEntity.ok(complaintService.create(principal.getUser(), request));
    }

    @GetMapping
    @Operation(summary = "List your complaints")
    public ResponseEntity<List<ComplaintResponse>> mine(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(complaintService.listMine(principal.getUser()));
    }
}
