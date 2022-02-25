package com.databasket.auth.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.databasket.auth.entity.Role;
import com.databasket.auth.entity.RolePrivilege;


public interface RolePrivilegeRepo extends JpaRepository<RolePrivilege, Long> {

	List<RolePrivilege> findAllByRoleAndPropertyId(Role role, String propertyId);

}
