package com.payflow.controller;

import com.payflow.dto.device.TrustedDeviceResponse;
import com.payflow.security.UserPrincipal;
import com.payflow.service.TrustedDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@Tag(name = "Trusted Devices", description = "Devices that have logged into your account")
public class TrustedDeviceController {

    private final TrustedDeviceService trustedDeviceService;

    @GetMapping
    @Operation(summary = "List devices that have logged into your account")
    public ResponseEntity<List<TrustedDeviceResponse>> list(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(trustedDeviceService.list(principal.getUser()));
    }

    @PatchMapping("/{id}/trust")
    @Operation(summary = "Mark a device as trusted")
    public ResponseEntity<TrustedDeviceResponse> trust(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String id,  @RequestParam(required = false) String deviceName) {
        return ResponseEntity.ok(trustedDeviceService.markTrusted(principal.getUser(), id, deviceName));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove/sign out a device you don't recognise")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String id) {
        trustedDeviceService.remove(principal.getUser(), id);
        return ResponseEntity.noContent().build();
    }
}
