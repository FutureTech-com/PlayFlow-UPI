package com.payflow.dto.device;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import com.payflow.entity.TrustedDevice;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrustedDeviceResponse {
    private String id;
    private String deviceName;
    private String deviceLabel;
    private String userAgent;
    private String lastIp;
    private Instant lastSeenAt;
    private boolean trusted;
    private Instant trustedAt;
    
    
    public static TrustedDeviceResponse from(TrustedDevice d) {
        return TrustedDeviceResponse.builder()
                .id(d.getId())
                .id(d.getDeviceId())
                .deviceName(d.getDeviceName())
                .userAgent(d.getUserAgent())
                .lastIp(d.getLastIp())
                .trustedAt(d.getTrustedAt())
                .lastSeenAt(d.getLastSeenAt())
                .build();
    }
}
