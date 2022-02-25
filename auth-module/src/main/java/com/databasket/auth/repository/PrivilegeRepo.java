package com.databasket.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.databasket.auth.entity.Privilege;


public interface PrivilegeRepo extends JpaRepository<Privilege, Long> {

}
