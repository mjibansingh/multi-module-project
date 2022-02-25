/**
 * 
 */
package com.databasket.auth.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.databasket.auth.entity.AuthenticationLog;

public interface AuthenticationLogRepo extends JpaRepository<AuthenticationLog, Long> {
	
	public List<AuthenticationLog> findByUsername(String username);
	
	@Query("SELECT auth FROM AuthenticationLog auth WHERE date(auth.timestamp) BETWEEN :startDate AND :endDate")
	public List<AuthenticationLog> getAllAuthenticationLogBetween(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
	

	@Query("SELECT auth FROM AuthenticationLog auth WHERE date(auth.timestamp) BETWEEN :startDate AND :endDate AND auth.username = :username")
	public List<AuthenticationLog> getAllAuthenticationLogBetweenForUsername(@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("username") String username);
	
	@Query("SELECT auth FROM AuthenticationLog auth WHERE date(auth.timestamp) BETWEEN :startDate and :endDate AND auth.event = :event")
	public List<AuthenticationLog> getAllAuthenticationLogBetweenForEvent(@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("event") String event);
	
	@Query("SELECT auth FROM AuthenticationLog auth WHERE date(auth.timestamp) BETWEEN :startDate AND :endDate AND auth.event = :event AND auth.username = :username")
	public List<AuthenticationLog> getAllAuthenticationLogBetweenForUsernameAndEvent(@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("username") String username, @Param("event") String event);	
}
