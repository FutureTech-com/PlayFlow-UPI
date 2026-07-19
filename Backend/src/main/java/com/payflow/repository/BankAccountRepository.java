package com.payflow.repository;

import com.payflow.entity.BankAccount;
import com.payflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, String> {
	List<BankAccount> findByUser(User user);

	Optional<BankAccount> findByUserAndPrimaryAccountTrue(User user);

	List<BankAccount> findByUserOrderByPrimaryAccountDescCreatedAtDesc(User user);

	Optional<BankAccount> findByAccountNumberAndIfscCode(String accountNumber, String ifscCode);
}
