package com.payflow.repository;

import com.payflow.entity.UpiId;
import com.payflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UpiIdRepository extends JpaRepository<UpiId, String> {
    Optional<UpiId> findByVpa(String vpa);
    boolean existsByVpa(String vpa);
    List<UpiId> findByUser(User user);
}
