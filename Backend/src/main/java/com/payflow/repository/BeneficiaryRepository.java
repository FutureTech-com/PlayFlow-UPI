package com.payflow.repository;

import com.payflow.entity.Beneficiary;
import com.payflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, String> {
    List<Beneficiary> findByOwner(User owner);
}
