package com.databasket.auth.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.databasket.auth.config.SecurityContext;
import com.databasket.auth.dto.UserDto;
import com.databasket.auth.entity.Role;
import com.databasket.auth.entity.User;
import com.databasket.auth.repository.RoleRepo;
import com.databasket.auth.repository.UserRepo;
//import com.databasket.opd.service.CommonService;


@Service
public class UserMangementService implements UserManagement {
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private UserRepo userRepository;
	
	@Autowired
	private RoleRepo roleRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	UserService userService;
	
	@Override
	public List<UserDto> getAllUser() {
		List<User> users = userRepository.findAll();
		List<UserDto> userDto = new ArrayList<>();
		for (User model : users) {
			UserDto dto = new UserDto();
			BeanUtils.copyProperties(model, dto, "password");
			dto.setPassword(null);
			
			ArrayList<String> roles = new ArrayList<String>();
			Iterator<Role> itr = model.getRoles().iterator();

			while (itr.hasNext()) {
				Role role = (Role) itr.next();
				roles.add(role.getName());
			}				
			dto.setRoles(roles);			
			userDto.add(dto);			
		}
		return userDto;
	}

	@Override
	public UserDto resetUserPasswordByAdmin(UserDto userInfo) {
		LOGGER.info("resetUserPasswordByAdmin : {}", userInfo);
		Assert.notNull(userInfo.getUsername(), "Username is required.");
		
		
		
		User userExist = userRepository.findByUsername(userInfo.getUsername());
		Assert.notNull(userExist, "User is not found.");
		
		String password = userExist.getUsername().concat("@123");
		
		//Validate Role 
		if(userService.getAuditorRole().equals(userService.getUserRole(userExist.getRoles()))){
			throw new RuntimeException("Unauthorized role.");
		}
	
		userExist.setPassword(bCryptPasswordEncoder.encode(password));
		userRepository.save(userExist);
		userExist.setPassword(password);
		LOGGER.info("Password reset successful.");
		BeanUtils.copyProperties(userExist, userInfo);
		userInfo.setPassword(password);
		return userInfo;		
	}
	
	@Override
	public UserDto resetUserPassword(UserDto userInfo) {
		LOGGER.info("resetUserPassword : {}", userInfo);
		Assert.notNull(userInfo.getUsername(), "Username is required.");
		Assert.notNull(userInfo.getEmail(), "Email is required.");
		Assert.notNull(userInfo.getTelephone(), "Telephone is required.");
		
		String password ="password";
		
		User userExist = userRepository.findByUsername(userInfo.getUsername());
		Assert.notNull(userExist, "User is not found.");
		
		if(!userExist.getTelephone().equals(userInfo.getTelephone())){
			throw new RuntimeException("Telephone doesn't match.");
		}
		
		if(!userExist.getEmail().equals(userInfo.getEmail())){
			throw new RuntimeException("Email doesn't match.");
		}		
	
		userExist.setPassword(bCryptPasswordEncoder.encode(password));
		userRepository.save(userExist);
		userExist.setPassword(password);
		LOGGER.info("Password reset successful.");
		BeanUtils.copyProperties(userExist, userInfo);
		return userInfo;
	}
	
//	@Autowired
//	CommonService commonService;
	
	@Override
	@Transactional
	public UserDto registerUserByAdmin(UserDto userInfo) {
		LOGGER.info("registerUser : {}", userInfo);
		Assert.notNull(userInfo.getUsername(), "Username is required.");
		Assert.notNull(userInfo.getPassword(), "Password is required.");
		//Assert.notNull(userInfo.getEmail(), "Email is required.");
		//Assert.notNull(userInfo.getTelephone(), "Telephone is required.");
		Assert.notNull(userInfo.getRoles(), "User's Role is required.");

		if (userRepository.findByUsername(userInfo.getUsername()) != null)
			throw new RuntimeException("Username is not available.");
//		if (userRepository.findByEmail(userInfo.getEmail()) != null)
//			throw new RuntimeException("Email is used.");
//		if (userRepository.findByTelephone(userInfo.getTelephone()) != null)
//			throw new RuntimeException("Telephone is used.");
		
		User user = new User();
		user.setUsername(userInfo.getUsername());
		user.setPassword(bCryptPasswordEncoder.encode(userInfo.getPassword()));
		if(userInfo.getTelephone() != null) {
			user.setTelephone(userInfo.getTelephone());
		}
		if(userInfo.getEmail() != null) {
			user.setEmail(userInfo.getEmail());
		}	
		if(userInfo.getFirstName() != null){
			user.setFirstName(userInfo.getFirstName());	
		}
		if(userInfo.getLastName() != null){
			user.setLastName(userInfo.getLastName());
		}
		//user.setCreateTimestamp(new Date());
		
		//Update Role
		List<Role> collection = new ArrayList<>();
		Iterator<String> itr = userInfo.getRoles().iterator();

		while (itr.hasNext()) {
			String roleName = (String) itr.next();
			Role existRole = roleRepository.findByName(roleName);
			Assert.notNull(existRole, roleName + " is not found.");
			collection.add(existRole);
		}

		user.setRoles(collection);
		
		//Enable User
		user.setEnabled(true);			
				
		userRepository.save(user);
		
		//commonService.addInstanceUser(user.getUsername(), SecurityContext.getCurrentInstance().getName());
		
		LOGGER.info("User registered successfully.");
		BeanUtils.copyProperties(user, userInfo);
		userInfo.setPassword(null);
		return userInfo;		
	}
	
	@Override
	@Transactional
	public UserDto registerUser(UserDto userInfo) {
		LOGGER.info("registerUser : {}", userInfo);
		Assert.notNull(userInfo.getUsername(), "Username is required.");
		Assert.notNull(userInfo.getPassword(), "Password is required.");
		Assert.notNull(userInfo.getEmail(), "Email is required.");
		Assert.notNull(userInfo.getTelephone(), "Telephone is required.");

		if (userRepository.findByUsername(userInfo.getUsername()) != null)
			throw new RuntimeException("Username is not available.");
		if (userRepository.findByEmail(userInfo.getEmail()) != null)
			throw new RuntimeException("Email is used.");
		if (userRepository.findByTelephone(userInfo.getTelephone()) != null)
			throw new RuntimeException("Telephone is used.");
		
		User user = new User();
		user.setUsername(userInfo.getUsername());
		user.setPassword(bCryptPasswordEncoder.encode(userInfo.getPassword()));
		user.setTelephone(userInfo.getTelephone());
		user.setEmail(userInfo.getEmail());
		if(userInfo.getFirstName() != null){
			user.setFirstName(userInfo.getFirstName());	
		}
		if(userInfo.getLastName() != null){
			user.setLastName(userInfo.getLastName());
		}
		//user.setCreateTimestamp(new Date());
		userRepository.save(user);
		LOGGER.info("User registered successfully.");
		BeanUtils.copyProperties(user, userInfo);
		userInfo.setPassword(null);
		return userInfo;
	}

	@Override
	public UserDto activeteRegisterUser(UserDto userInfo) {
		LOGGER.info("activeteUser : {}", userInfo);
		Assert.notNull(userInfo.getId(), "User's ID is required.");
		Assert.notNull(userInfo.getRoles(), "User's Role is required.");
		
		User userExist = userRepository.findById(userInfo.getId()).get();
		Assert.notNull(userExist, "User is not found.");
		
		//Identify auditor's role		
		String auditorRole = userService.getAuditorRole();	
		
		//Identify allowed roles for auditor
		List<String> roles = userService.getRolesHierachy(auditorRole);
		
		//Validate update roles with allowed roles
		for(String updateRole : userInfo.getRoles()){
			boolean flag = false;
			for(String allowUpdateRole : roles){
				if(updateRole.equals(allowUpdateRole)){
					flag = true;
					break;
				}
			}
			
			if(!flag){
				throw new RuntimeException(auditorRole + " is unauthorized to activate user with  " + updateRole);
			}
		}

		
		//Update Role
		List<Role> collection = new ArrayList<>();
		Iterator<String> itr = userInfo.getRoles().iterator();

		while (itr.hasNext()) {
			String roleName = (String) itr.next();
			Role existRole = roleRepository.findByName(roleName);
			Assert.notNull(existRole, roleName + " is not found.");
			collection.add(existRole);
		}

		userExist.setRoles(collection);
		
		//Enable User
		userExist.setEnabled(true);
		userRepository.save(userExist);
		LOGGER.info("User activated successfully.");
		BeanUtils.copyProperties(userExist, userInfo);
		userInfo.setPassword(null);
		return userInfo;
	}

	@Override
	public List<String> getAllRole() {
		//Identify auditor's role		
		String auditorRole = userService.getAuditorRole();
		return userService.getRolesHierachy(auditorRole);
	}

	@Override
	public List<User> registerCustomers(List<UserDto> users) {
		
		List<Role> collection = new ArrayList<>();
		String roleName = "Customer";
		Role existRole = roleRepository.findByName(roleName);
		if(existRole == null) {
			existRole = new Role();
			existRole.setName(roleName);
			roleRepository.save(existRole);
		}
		collection.add(existRole);
		
		List<User> userList = new ArrayList<User>();
		for(UserDto userItr : users) {
			if(userRepository.findByTelephone(userItr.getTelephone()) == null && 
					userRepository.findByUsername(userItr.getTelephone()) == null) {
				User newUser = new User();
				newUser.setFirstName(userItr.getFirstName());
				newUser.setUsername(userItr.getUsername());
				newUser.setPassword(bCryptPasswordEncoder.encode("Adm1n"));
				newUser.setTelephone(userItr.getTelephone());
				newUser.setEmail(userItr.getEmail());
				newUser.setRoles(collection);
				newUser.setEnabled(true);
				userList.add(newUser);
			}
		}
		if(userList.size() != 0) {
			userRepository.saveAll(userList);
		}
		
		return userList;
	}
	
	


}
