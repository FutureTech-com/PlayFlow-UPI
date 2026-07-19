package com.payflow.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class PaymentRequest extends BaseEntity{
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="requester_id", nullable=false)
	private User requester;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="payer_id")
	private User payer;
	
	@Column(nullable=false)
	private BigDecimal amount;
	
	private String note;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable=false)
	private RequestStatus status=RequestStatus.PENDING;
	
	private String fulfilledTransactionId;
	
	public enum RequestStatus{
		PENDING, ACCEPTED, DECLINED, EXPIRED
	}

}
