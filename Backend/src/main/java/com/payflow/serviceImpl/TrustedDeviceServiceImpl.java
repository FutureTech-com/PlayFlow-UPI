package com.payflow.serviceImpl;

import com.payflow.dto.device.TrustedDeviceResponse;
import com.payflow.entity.LoginHistory;
import com.payflow.entity.TrustedDevice;
import com.payflow.entity.User;
import com.payflow.repository.LoginHistoryRepository;
import com.payflow.repository.TrustedDeviceRepository;
import com.payflow.service.TrustedDeviceService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrustedDeviceServiceImpl implements TrustedDeviceService {

    private final TrustedDeviceRepository trustedDeviceRepository;
    private final LoginHistoryRepository loginHistoryRepository;

    @SuppressWarnings("null")
	@Override
    @Transactional
    public void recordLogin(User user, String userAgent, String ipAddress) {
        LoginHistory entry = LoginHistory.builder()
                .user(user)
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .build();
        loginHistoryRepository.save(entry);
    }

    @Override
    @Transactional
    public void trustDevice(User user, String deviceId, String deviceName) {
        TrustedDevice device = trustedDeviceRepository.findByUserAndDeviceId(user, deviceId)
                .orElseGet(() -> TrustedDevice.builder().user(user).deviceId(deviceId).build());
        device.setDeviceName(deviceName);
        device.setLastSeenAt(Instant.now());
        trustedDeviceRepository.save(device);
    }

    @Override
    public boolean isTrusted(User user, String deviceId) {
        return trustedDeviceRepository.findByUserAndDeviceId(user, deviceId).isPresent();
    }

    @Override
    @Transactional
    public void revokeTrust(User user, String deviceId) {
        trustedDeviceRepository.deleteByUserAndDeviceId(user, deviceId);
    }

    @Override
    @Transactional
    public void revokeAllTrustedDevices(User user) {
        trustedDeviceRepository.deleteByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrustedDeviceResponse> list(User user) {
        return trustedDeviceRepository.findByUserOrderByTrustedAtDesc(user).stream()
                .map(TrustedDeviceResponse::from)
                .toList();
    }

	@Override
    @Transactional
    public TrustedDeviceResponse markTrusted(User user, String id, String deviceName) {
        TrustedDevice device = trustedDeviceRepository.findByUserAndDeviceId(user, id)
                .orElseGet(() -> TrustedDevice.builder().user(user).deviceId(id).build());
        device.setDeviceName(deviceName);
        device.setLastSeenAt(Instant.now());
        TrustedDevice saved = trustedDeviceRepository.save(device);
        return TrustedDeviceResponse.from(saved);
    }

	@Override
	public void remove(User user, String id) {
		// TODO Auto-generated method stub
		
	}
}