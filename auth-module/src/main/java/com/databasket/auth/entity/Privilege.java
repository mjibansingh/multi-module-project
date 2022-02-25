package com.databasket.auth.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.databasket.auth.config.Auditable;
import com.databasket.auth.dto.PrivilegeInfo;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Privilege extends Auditable {
    
	public Privilege(PrivilegeInfo info) {
		this.description = info.getDescription();
		this.displayName = info.getDisplayName() == null ? info.getPrivilege() : info.getDisplayName();
		this.id = info.getId();
		this.name = info.getPrivilege();
		this.system = info.getSystem();
		this.category = info.getCategory();
	}
	
	public Privilege() {
	
	}

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(nullable = false, unique = true)
    private String name;
    
    private String displayName;
    private String description;
    private String category;
 
    /**
     * System Code which can be FO (Front Office), PMS, FB (Food and Beverages) etc.
     */
    private String system;
    
}