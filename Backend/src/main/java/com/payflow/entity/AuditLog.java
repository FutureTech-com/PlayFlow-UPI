package com.payflow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class AuditLog extends BaseEntity{
	private String userId;;
	
	@Column(nullable = false)
	private String action;
	private String entityType;
	private String entityId;
	private String ipAddress;
	
	@Column(columnDefinition = "TEXT")
	private String details;

}
