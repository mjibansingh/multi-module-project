package com.databasket.auth.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.databasket.auth.entity.User;
import com.databasket.auth.entity.VerificationToken;


public interface VerificationTokenRepo extends JpaRepository<VerificationToken, Long> {

	List<VerificationToken> findAllByUserAndStatus(User user, String status);

	VerificationToken findByToken(String tokenString);
	
	 VerificationToken findByUser(User user);
	
	
}
