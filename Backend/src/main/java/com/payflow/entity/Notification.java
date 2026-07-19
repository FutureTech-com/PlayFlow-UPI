package com.payflow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "notifications")
public class Notification extends BaseEntity {
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private String message;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private NotificationType type;
	
	@Column(name = "is_read")
	private boolean read=false;
	
	public enum NotificationType {
		 PAYMENT_SUCCESS, PAYMENT_FAILED, REQUEST_RECEIVED, REQUEST_ACCEPTED,
	        REQUEST_DECLINED, ACCOUNT_LINKED, SECURITY_ALERT, GENERAL
	}

}
