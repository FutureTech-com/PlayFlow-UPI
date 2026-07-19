package com.payflow.repository;

import com.payflow.entity.TrustedDevice;
import com.payflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrustedDeviceRepository extends JpaRepository<TrustedDevice, String> {

    Optional<TrustedDevice> findByUserAndDeviceId(User user, String deviceId);

    List<TrustedDevice> findByUser(User user);

    Optional<TrustedDevice>findByUserOrderByTrustedAtDesc(User user);
    void deleteByUserAndDeviceId(User user, String deviceId);

    void deleteByUser(User user);
}