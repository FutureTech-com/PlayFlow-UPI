package com.payflow.repository;

import com.payflow.entity.LoginHistory;
import com.payflow.entity.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, String> {
	List<LoginHistory> findByUserOrderByLoggedInAtDesc(User user);
}
