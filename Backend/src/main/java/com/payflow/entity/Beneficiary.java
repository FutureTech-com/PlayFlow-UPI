package com.payflow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "beneficiaries")
public class Beneficiary extends BaseEntity{

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner_id")
	private User owner;

	@Column(nullable = false)
	private String nickName;

	@Column(nullable = false)
	private String vpa;

	private boolean favourite = false;
	
}
