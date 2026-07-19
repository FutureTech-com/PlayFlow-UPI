package com.payflow.controller;

import com.payflow.dto.beneficiary.AddBeneficiaryRequest;
import com.payflow.dto.beneficiary.BeneficiaryResponse;
import com.payflow.security.UserPrincipal;
import com.payflow.service.BeneficiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beneficiaries")
@RequiredArgsConstructor
@Tag(name = "Favourites", description = "Saved beneficiaries / favourite contacts")
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    @PostMapping
    @Operation(summary = "Save a new beneficiary")
    public ResponseEntity<BeneficiaryResponse> add(@AuthenticationPrincipal UserPrincipal principal,
                                                     @Valid @RequestBody AddBeneficiaryRequest request) {
        return ResponseEntity.ok(beneficiaryService.add(principal.getUser(), request));
    }

    @GetMapping
    @Operation(summary = "List saved beneficiaries")
    public ResponseEntity<List<BeneficiaryResponse>> list(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(beneficiaryService.list(principal.getUser()));
    }

    @SuppressWarnings("null")
	@PatchMapping("/{id}/favourite")
    @Operation(summary = "Toggle favourite status for a beneficiary")
    public ResponseEntity<BeneficiaryResponse> toggleFavourite(@AuthenticationPrincipal UserPrincipal principal,
                                                                  @PathVariable String id) {
        return ResponseEntity.ok(beneficiaryService.toggleFavourite(principal.getUser(), id));
    }

    @SuppressWarnings("null")
	@DeleteMapping("/{id}")
    @Operation(summary = "Remove a saved beneficiary")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String id) {
        beneficiaryService.remove(principal.getUser(), id);
        return ResponseEntity.noContent().build();
    }
}
