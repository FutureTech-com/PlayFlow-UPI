package com.payflow.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.payflow.dto.device.TrustedDeviceResponse;
import com.payflow.entity.User;

@Service
public interface TrustedDeviceService {

	void recordLogin(User user, String userAgent, String ipAddress);

	void trustDevice(User user, String deviceId, String deviceName);

	boolean isTrusted(User user, String deviceId);

	void revokeTrust(User user, String deviceId);

	void revokeAllTrustedDevices(User user);

	List<TrustedDeviceResponse> list(User user);

	TrustedDeviceResponse markTrusted(User user, String id, String deviceName);

	void remove(User user, String id);

	/*
	 * @Service
	 * 
	 * @RequiredArgsConstructor public class TrustedDeviceService {
	 * 
	 * private final TrustedDeviceRepository trustedDeviceRepository; private final
	 * NotificationService notificationService;
	 * 
	 * @Transactional public void recordLogin(User user, String userAgent, String
	 * ip) { String fingerprint = fingerprint(userAgent, ip); var existing =
	 * trustedDeviceRepository.findByUserAndDeviceFingerprint(user, fingerprint);
	 * 
	 * if (existing.isPresent()) { TrustedDevice device = existing.get();
	 * device.setLastSeenAt(LocalDateTime.now()); device.setLastIp(ip);
	 * trustedDeviceRepository.save(device); return; }
	 * 
	 * TrustedDevice device = new TrustedDevice(); device.setUser(user);
	 * device.setDeviceFingerprint(fingerprint);
	 * device.setDeviceLabel(labelFor(userAgent)); device.setLastIp(ip);
	 * device.setLastSeenAt(LocalDateTime.now()); device.setTrusted(false);
	 * trustedDeviceRepository.save(device);
	 * 
	 * boolean isFirstDeviceEver =
	 * trustedDeviceRepository.findByUserOrderByLastSeenAtDesc(user).size() == 1; if
	 * (!isFirstDeviceEver) { notificationService.createNotification(user,
	 * "New login detected", "Your account was just accessed from a new device (" +
	 * device.getDeviceLabel() + "). " +
	 * "If this wasn't you, change your password immediately.",
	 * Notification.NotificationType.SECURITY_ALERT); } }
	 * 
	 * public List<TrustedDeviceResponse> list(User user) { return
	 * trustedDeviceRepository.findByUserOrderByLastSeenAtDesc(user).stream().map(
	 * this::toResponse).toList(); }
	 * 
	 * @Transactional public TrustedDeviceResponse markTrusted(User user, String id)
	 * { TrustedDevice device = trustedDeviceRepository.findById(id) .orElseThrow(()
	 * -> new ResourceNotFoundException("Device not found")); if
	 * (!device.getUser().getId().equals(user.getId())) { throw new
	 * BadRequestException("This device does not belong to you"); }
	 * device.setTrusted(true); return
	 * toResponse(trustedDeviceRepository.save(device)); }
	 * 
	 * @Transactional public void remove(User user, String id) { TrustedDevice
	 * device = trustedDeviceRepository.findById(id) .orElseThrow(() -> new
	 * ResourceNotFoundException("Device not found")); if
	 * (!device.getUser().getId().equals(user.getId())) { throw new
	 * BadRequestException("This device does not belong to you"); }
	 * trustedDeviceRepository.delete(device); }
	 * 
	 * private String fingerprint(String userAgent, String ip) { try { MessageDigest
	 * digest = MessageDigest.getInstance("SHA-256"); byte[] hash =
	 * digest.digest((userAgent + "|" + ip).getBytes()); return
	 * HexFormat.of().formatHex(hash); } catch (Exception e) { return
	 * Integer.toHexString((userAgent + ip).hashCode()); } }
	 * 
	 * private String labelFor(String userAgent) { if (userAgent == null) return
	 * "Unknown device"; String ua = userAgent.toLowerCase(); if
	 * (ua.contains("android")) return "Android device"; if (ua.contains("iphone")
	 * || ua.contains("ios")) return "iPhone"; if (ua.contains("mac")) return "Mac";
	 * if (ua.contains("windows")) return "Windows PC"; return "Web browser"; }
	 * 
	 * private TrustedDeviceResponse toResponse(TrustedDevice d) { return
	 * TrustedDeviceResponse.builder().id(d.getId()).deviceLabel(d.getDeviceLabel())
	 * .lastIp(d.getLastIp())
	 * .lastSeenAt(d.getLastSeenAt()).trusted(d.isTrusted()).build(); }
	 */

}