package com.payflow.repository;

import com.payflow.entity.Biller;
import com.payflow.entity.BillerCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BillerRepository extends JpaRepository<Biller, String> {

 List<Biller> findByCategory(BillerCategory category);

 List<Biller> findByCategoryAndActiveTrue(com.payflow.entity.Biller.BillerCategory category);

 List<Biller> findByActiveTrue();

 Optional<Biller> findByCode(String code);

 boolean existsByCode(String code);
}