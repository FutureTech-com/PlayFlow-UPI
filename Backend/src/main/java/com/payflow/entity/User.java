package com.payflow.entity;

import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity

@Table(name = "users", uniqueConstraints = {
		@UniqueConstraint(columnNames = "email"),
		@UniqueConstraint(columnNames = "phone")
})
public class User extends BaseEntity {
	
	@NotBlank
	@Column(nullable = false)
	private String fullName;
	
	@Email
	@Column(nullable = false, unique = true)
	private String email;
	
	@NotBlank
	@Column(nullable = false , unique = true)
	private String phone;
	
	@JsonIgnore
	@Column(nullable = false)
	private String passwordHash;

	/** Hashed 4-6 digit UPI transaction PIN, separate from login password. */
    
	@JsonIgnore
	private String transactionPinHash;
	
	private boolean pinSet=false;
	private boolean enabled=true;
	private boolean phoneVerified=false;
	private boolean emailVerified=false;
	
	@ElementCollection(fetch = FetchType.EAGER)
	@Enumerated(EnumType.STRING)
	@CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
	@Column(name = "role")
	private Set<Role> roles=new HashSet<>();

	@Enumerated(EnumType.STRING)
    private KycStatus kycStatus = KycStatus.PENDING;
 
    public enum KycStatus {
        PENDING, VERIFIED, REJECTED
    }
}
