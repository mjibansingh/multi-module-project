package com.databasket.auth.service;

import java.util.List;

import com.databasket.auth.dto.PrivilegeInfo;

public interface RoleManager {

	List<PrivilegeInfo> getRolePrivileges(Long id, String propertyId);

	PrivilegeInfo togglePermission(Long id, boolean toggle);

	List<PrivilegeInfo> getRolePrivileges(String name, String propertyId);
	
}
