package com.databasket.auth.service;

import com.databasket.auth.dto.PrivilegeInfo;
import com.databasket.auth.entity.Privilege;
import com.databasket.auth.entity.Role;
import com.databasket.auth.entity.RolePrivilege;
import com.databasket.auth.repository.PrivilegeRepo;
import com.databasket.auth.repository.RolePrivilegeRepo;
import com.databasket.auth.repository.RoleRepo;
//import com.databasket.smarthotel.commons.message.MessagePublisher;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class DefaultRoleManager implements RoleManager {
	
	@Autowired
	RoleRepo roleRepo;
	
	@Autowired
	RolePrivilegeRepo rolePrivilegeRepo;
	
	@Autowired
	PrivilegeRepo privilegeRepo;

	@Override
	public List<PrivilegeInfo> getRolePrivileges(Long id, String propertyId) {
		Role loaded = roleRepo.findById(id).get();
		Assert.notNull(loaded, "Invalid role ID.");		
		return getPrivilegeInfos(propertyId, loaded);
	}
	
	@Override
	public List<PrivilegeInfo> getRolePrivileges(String name, String propertyId) {
		Role loaded = roleRepo.findByName(name);
		Assert.notNull(loaded, "Invalid role ID.");		
		return getPrivilegeInfos(propertyId, loaded);
	}

	private List<PrivilegeInfo> getPrivilegeInfos(String propertyId, Role loaded) {
		List<RolePrivilege> rolePrivileges = rolePrivilegeRepo.findAllByRoleAndPropertyId(loaded, propertyId);
		if (rolePrivileges == null || rolePrivileges.isEmpty()) {
			List<Privilege> privileges = privilegeRepo.findAll();
			rolePrivileges = new ArrayList<>();
			for (Privilege privilege : privileges) {
				RolePrivilege rolePrivilege = new RolePrivilege();
				rolePrivilege.setDisplayName(privilege.getDisplayName());
				rolePrivilege.setPrivilege(privilege);
				rolePrivilege.setPropertyId(propertyId);
				rolePrivilege.setRole(loaded);
				rolePrivileges.add(rolePrivilege);
			}
			
			rolePrivilegeRepo.saveAll(rolePrivileges);
		}
		
		List<PrivilegeInfo> response = new ArrayList<>();
		
		for (RolePrivilege rolePrivilege : rolePrivileges) {
			PrivilegeInfo privilege = new PrivilegeInfo(rolePrivilege);
			response.add(privilege);
		}
		return response;
	}

//	@Autowired
//	@Qualifier("changeOfPrivilegeBuilder")
//	com.databasket.smarthotel.commons.message.Builder builder;
	
//	@Autowired
//	MessagePublisher publisher;
	
	@Override
	public PrivilegeInfo togglePermission(Long id, boolean toggle) {
		RolePrivilege privilege = rolePrivilegeRepo.findById(id).get();
		Assert.notNull(privilege, "Invalid permission.");
		
		if (privilege.isHasPermission() != toggle) {
			privilege.setHasPermission(toggle);
			rolePrivilegeRepo.save(privilege);
			//publisher.publishMessage(builder, new PrivilegeInfo(privilege));
		}
		
		return new PrivilegeInfo(privilege);
	}
}