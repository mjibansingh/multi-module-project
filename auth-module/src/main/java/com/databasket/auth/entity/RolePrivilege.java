package com.databasket.auth.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.databasket.auth.config.Auditable;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class RolePrivilege extends Auditable {
    
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
	@ManyToOne
	@JoinColumn(name="role")
	@JsonIgnore 
	private Role role;
	
	@ManyToOne
	@JoinColumn(name="privilege")
	@JsonIgnore 
	private Privilege privilege;
	
	private String displayName;
	
	private String propertyId;
	
	private boolean hasPermission;
	
}