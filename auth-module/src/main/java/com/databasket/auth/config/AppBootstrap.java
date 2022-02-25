package com.databasket.auth.config;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.databasket.auth.entity.Role;
import com.databasket.auth.entity.User;
import com.databasket.auth.repository.PrivilegeRepo;
import com.databasket.auth.repository.RoleRepo;
import com.databasket.auth.repository.UserRepo;
import com.databasket.auth.utility.SecConstants.DefaultRole;
import com.databasket.auth.utility.SecConstants.SystemRole;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AppBootstrap {

	@Autowired
	RoleRepo roleRepo;
	
	@Autowired
	UserRepo userRepo;
	
	@Autowired
	PrivilegeRepo privilegeRepo;
	
	
	@PostConstruct
	public void init(){
		log.info ("Initialise applications...");
		Date now = new Date();
		String user = "App Bootstrap";
		List<Role> roles = roleRepo.findAll();
		if(roles == null || roles.isEmpty()){
			
			for(SystemRole roleName : SystemRole.values()){
				Role role = new Role();
				role.setName(roleName.toString());
				role.setInternal(true);
				//role.setSystem(SystemCode.PMS.toString());
				roleRepo.save(role);
			}
			
			for(DefaultRole roleName : DefaultRole.values()){
				Role role = new Role();
				role.setName(roleName.toString());
				roleRepo.save(role);
			} 
			
			User root = new User(); 
			root.setEnabled(true);
			root.setUsername("root");
			root.setPassword(new BCryptPasswordEncoder().encode("root@123")); 
			root.setRoles(Arrays.asList(roleRepo.findByName(SystemRole.ROOT.toString()))); 
			root.setCreatedBy(user);
			root.setDateCreated(now);
	        userRepo.save(root);		        
			
			
			User admin = new User(); 
			admin.setEnabled(true);
			admin.setUsername("admin");
			admin.setPassword(new BCryptPasswordEncoder().encode("admin@123")); 
			admin.setRoles(Arrays.asList(roleRepo.findByName(DefaultRole.SUPER_ADMIN.toString()))); 
			admin.setCreatedBy(user);
			admin.setDateCreated(now);
	        userRepo.save(admin);	
	        

			User test = new User(); 
			test.setEnabled(true);
			test.setUsername("test");
			test.setPassword(new BCryptPasswordEncoder().encode("test@123")); 
			test.setRoles(Arrays.asList(roleRepo.findByName(DefaultRole.SUPER_ADMIN.toString()))); 
			test.setCreatedBy(user);
			test.setDateCreated(now);
	        userRepo.save(test);	

		}
		
	}
		

}