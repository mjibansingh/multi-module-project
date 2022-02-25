package com.databasket.auth.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.databasket.auth.entity.Role;


public interface RoleRepo extends JpaRepository<Role, Long> {	
    Role findByName(String name);
    
    @Override
    void delete(Role role);	
    
    @Query("SELECT r FROM Role r WHERE r.name NOT IN :name")
    List<Role> findRolesHierarchyNotContain(@Param("name") String[] name);    
    
    List<Role> findAllByNameIn(String[] roles);
    
	List<Role> findAllByIdIn(List<Long> ids);      
}
