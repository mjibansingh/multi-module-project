package com.databasket.auth.dto;

import java.io.Serializable;

import com.databasket.auth.entity.RolePrivilege;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class PrivilegeInfo implements Serializable{
    
    private static final long serialVersionUID = 1L;
	
	public PrivilegeInfo(RolePrivilege rolePrivilege) {
		this.id = rolePrivilege.getId();
		this.category = rolePrivilege.getPrivilege().getCategory();
		this.description = rolePrivilege.getPrivilege().getDescription();
		this.displayName = rolePrivilege.getDisplayName();
		this.privilege = rolePrivilege.getPrivilege().getName();
		this.privilegeId = rolePrivilege.getPrivilege().getId();
		this.propertyId = rolePrivilege.getPropertyId();
		this.role = rolePrivilege.getRole().getName();
		this.roleId = rolePrivilege.getRole().getId();
		this.system = rolePrivilege.getPrivilege().getSystem();
		this.hasPermission = rolePrivilege.isHasPermission();
	}
    
    public PrivilegeInfo() {
	}
    
	private Long id;
    private Long roleId;
    private String role;
    private Long privilegeId;
	private String privilege;
	private String displayName;
	private String propertyId;
	private String system;
	private String category;
	private String description;
	private boolean hasPermission;
	
}