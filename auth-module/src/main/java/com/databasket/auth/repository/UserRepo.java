package com.databasket.auth.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.databasket.auth.entity.User;


public interface UserRepo extends JpaRepository<User, Long> {
	
	public User findByUsername(String username);
	
    public User findByEmail(String email);
    
    public User findByTelephone(String telephone);
    
    public User findByTelephoneAndEmail(String telephone, String email);

    @Override
    void delete(User user);	  
    
	@Query(value = "SELECT u from User u")
	public List<User> findAllCustomer();

	public List<User> findAllByIdIn(List ids);

	public List<User> findAllByUsernameIn(String[] names);    
}
