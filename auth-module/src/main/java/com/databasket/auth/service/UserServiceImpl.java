package com.databasket.auth.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.databasket.auth.events.OnPasswordResetCompleteEvent;
import com.databasket.auth.events.UserEvent;
import com.databasket.auth.repository.RoleRepo;
import com.databasket.auth.repository.UserRepo;
import com.databasket.auth.repository.VerificationTokenRepo;
import com.databasket.auth.utility.SecConstants;
import com.databasket.auth.utility.SecConstants.DefaultRole;
import com.databasket.auth.utility.SecConstants.SystemRole;
import com.databasket.auth.utility.SecConstants.TokenStatus;
import com.databasket.auth.utility.SecConstants.UserEventSubtype;
import com.databasket.auth.utility.SecurityUtil;
import com.databasket.auth.config.SecurityContext;
import com.databasket.auth.dto.AuthUserInfo;
import com.databasket.auth.dto.ChangePasswordInfo;
import com.databasket.auth.dto.UserDto;
import com.databasket.auth.dto.UserInfo;
import com.databasket.auth.entity.Role;
import com.databasket.auth.entity.User;
import com.databasket.auth.entity.VerificationToken;

@Service
public class UserServiceImpl implements UserService {
	
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	@Autowired
	UserRepo userRepository;

	@Autowired
	private RoleRepo roleRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Override
	public User get(String username) {
		return userRepository.findByUsername(username);
	}
	
	public UserInfo getUserInfoByEmail(String email) {
		User user = userRepository.findByEmail(email);
		if (user == null)
			return null;
		return new UserInfo(user);
	}

	public UserInfo getUserInfoByTelephone(String telephone) {
		User user = userRepository.findByTelephone(telephone);
		if (user == null)
			return null;
		return new UserInfo(user);
	}
	
	@Override
	public UserInfo register(final UserInfo userInfo, boolean generatePassword, boolean activate, boolean mergeAccount) {

		if (userInfo.getUsername() == null || userInfo.getPassword() == null)
			throw new IllegalArgumentException("Username and password are required.");
		if (userInfo.getEmail() == null && userInfo.getTelephone() == null)
			throw new IllegalArgumentException("Either an email or telephone is required.");
		
		User user = get(userInfo);
		
		if (user != null && mergeAccount) 
			isEmailOrTelephoneAvailable(userInfo.getEmail(), userInfo.getTelephone(), user);
		else {
			isEmailOrTelephoneAvailable(userInfo.getEmail(), userInfo.getTelephone(), null);
			user = new User();
			user.setUsername(userInfo.getUsername());
		}
		
		List<Role> roles = new ArrayList<>();
		for (String roleName : userInfo.getRoles()) {
			Role role = roleRepository.findByName(roleName);
			roles.add(role);
		}
		
		if (roles.isEmpty()) {
			Role role = roleRepository.findByName(SecConstants.SystemRole.DEFAULT.toString());
			roles.add(role);
		}

		user.setFirstName(userInfo.getFirstName());
		user.setEmail(userInfo.getEmail());
		user.setTelephone(userInfo.getTelephone());
		
		user.setRoles(roles);

		if (generatePassword) {
			userInfo.setPassword(SecurityUtil.generatePassword());
			user.setPassword(bCryptPasswordEncoder.encode(userInfo.getPassword()));
		}else user.setPassword(bCryptPasswordEncoder.encode(userInfo.getPassword()));

		if (activate) user.setEnabled(true);

		user.setDateCreated(new Date());
		user.setCreatedBy(user.getUsername());
		userRepository.save(user);
		BeanUtils.copyProperties(user, userInfo, "password");
		userInfo.setId(user.getId());
		
		if (userInfo.getSystem() == null) broadcastUserRegistration(user, activate);

		return userInfo;
	}
	
	
	
	@Override
	public String sendActivationLink(String email) {
		Assert.notNull(email, "Email is required.");
		
		User userByEmail = userRepository.findByEmail(email);
		Assert.notNull(userByEmail, "User is not found.");
		broadcastUserRegistration(userByEmail, userByEmail.isEnabled());
		return "Success";
	}

	@Override
	public AuthUserInfo register(final UserInfo userInfo) {
		try {
			LOGGER.info("register : {}", new ObjectMapper().writeValueAsString(userInfo));
		} catch (IOException e) {e.printStackTrace();
		}
		if (userInfo.getEmail() == null && userInfo.getTelephone() == null)
			throw new IllegalArgumentException("Either an email or telephone is required.");
		
		User user = null;
		
		isEmailOrTelephoneAvailable(userInfo.getEmail(), userInfo.getTelephone(), null);
		user = new User();
		user.setUsername(userInfo.getUsername());
	
		List<Role> roles = new ArrayList<>();
		for (String roleName : userInfo.getRoles()) {
			Role role = roleRepository.findByName(roleName);
			roles.add(role);
		}
		
		if (roles.isEmpty()) {
			Role role = roleRepository.findByName(SecConstants.SystemRole.DEFAULT.toString());
			roles.add(role);
		}

		user.setFirstName(userInfo.getFirstName());
		user.setEmail(userInfo.getEmail());
		user.setTelephone(userInfo.getTelephone());
		
		user.setRoles(roles);

		user.setPassword(bCryptPasswordEncoder.encode(userInfo.getPassword()));

		user.setEnabled(true);

		user.setDateCreated(new Date());
		user.setCreatedBy(user.getUsername());
		user = userRepository.save(user);
		BeanUtils.copyProperties(user, userInfo, "password");
		userInfo.setId(user.getId());
		
		return new AuthUserInfo(user);
	}
	
	@Autowired
	ApplicationEventPublisher eventPublisher;
	
	private void broadcastUserRegistration(User user, boolean activate) {
		if (!activate) {
			eventPublisher.publishEvent(new UserEvent(
												user, 
												LocaleContextHolder.getLocale(), 
												UserEventSubtype.USER_REGISTERED.toString(), null, null));
		} 
	}
	
	private void isEmailOrTelephoneAvailable(String email, String telephone, User user) {  
		if (user != null) {
			User userByEmail = userRepository.findByEmail(email);
			if (userByEmail != null && !userByEmail.getId().equals(user.getId()))
				throw new IllegalArgumentException("Email is not available");
			if (telephone != null) {
				User userByTelephone = userRepository.findByTelephone(telephone);
				if (userByTelephone != null && !userByTelephone.getId().equals(user.getId()))
					throw new IllegalArgumentException("Telephone is not available");
			}
			
		} else {
			if (userRepository.findByEmail(email) != null)
				throw new IllegalArgumentException("Email is not available");
			if (telephone != null && userRepository.findByTelephone(telephone) != null)
				throw new IllegalArgumentException("Telephone is not available");
		}
	}
	
	private User get(UserInfo userInfo) {
		if (userInfo.getUsername() != null && !userInfo.getUsername().isEmpty()) {
			return get(userInfo.getUsername());
		} else {
			if (userInfo.getEmail() != null && !userInfo.getEmail().isEmpty() && userInfo.getTelephone() != null
					&& !userInfo.getTelephone().isEmpty()) {
				UserInfo existingUser = get(userInfo.getEmail(), userInfo.getTelephone());
				if (existingUser != null)
					return get(userInfo.getUsername());
			} else if (userInfo.getEmail() != null && !userInfo.getEmail().isEmpty()) {
				UserInfo existingUser = getUserInfoByEmail(userInfo.getEmail());
				if (existingUser.getEmail().equals(existingUser.getUsername()))
					return get(userInfo.getUsername());
			} else {
				UserInfo existingUser = getUserInfoByTelephone(userInfo.getTelephone());
				if (existingUser.getTelephone().equals(existingUser.getUsername()))
					return get(userInfo.getUsername());
			}
		}

		return null;
	}
	
	public UserInfo get(String email, String telephone) {
		User user = userRepository.findByTelephoneAndEmail(telephone, email);
		if (user == null)
			return null;
		return new UserInfo(user);
	}
	
	@Autowired
	VerificationTokenRepo verificationTokenRepo;
	
	

	/*
	 * @Override public VerificationToken saveVerificationToken(User user) {
	 * VerificationToken token = verificationTokenRepo.findByUser(user);
	 * 
	 * if(token == null) { token = new VerificationToken(); token.setUser(user); }
	 * 
	 * //token.setExpiryDate(generateTokenExpiryDate(5));//set expiry time with 5
	 * minutes token.setExpiryDate(generateTokenExpiryDate(60*24*7)); //set expiry
	 * time 7 days token.setToken(UUID.randomUUID().toString()); token.setOtp(null);
	 * token.setStatus(TokenStatus.GENERATED.toString());
	 * 
	 * return verificationTokenRepo.save(token); }
	 */

	private Date generateTokenExpiryDate(int expiryTimeInMinutes) {
        Calendar cal = Calendar.getInstance();
        return new Date(cal.getTimeInMillis() + (1000 * 60 * expiryTimeInMinutes));
    }
	
	@Override
	public String activate(String tokenString) {

		VerificationToken existVerificationToken = verificationTokenRepo.findByToken(tokenString);
		
		if(existVerificationToken == null) {
			return "Invalid";
		}
		
		if(TokenStatus.USED.toString().equals(existVerificationToken.getStatus())) {
			return TokenStatus.USED.toString();
		}

		final User user = existVerificationToken.getUser();
		final Calendar cal = Calendar.getInstance();		
		if ((existVerificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
			return TokenStatus.EXPIRED.toString() + "&email=" + user.getUsername();
		}		
		
		user.setEnabled(true);
		userRepository.save(user);
		
		existVerificationToken.setStatus(TokenStatus.USED.toString());
		verificationTokenRepo.save(existVerificationToken);
		
		return "Success";	
	}

	@Override
	public User changePassword(ChangePasswordInfo credential) {
		String currentUserName = null;
		
		if (credential.getUserName() != null) currentUserName = credential.getUserName();
		else currentUserName = SecurityContextHolder.getContext().getAuthentication().getName();
		
		User loaded = userRepository.findByUsername(currentUserName);
		if (loaded == null) throw new RuntimeException("Unauthorized.");

		if (!bCryptPasswordEncoder.matches(credential.getCurrentPassword(), loaded.getPassword())) 
			throw new IllegalArgumentException("Old password is incorrect.");
		
		if (bCryptPasswordEncoder.matches(credential.getNewPassword(), loaded.getPassword())) 
			throw new IllegalArgumentException("Last password and new password are same.");
		
		loaded.setPassword(bCryptPasswordEncoder.encode(credential.getNewPassword()));
		loaded = userRepository.save(loaded);
		return loaded;
	}
	
	@Override
	public User updateProfile(UserInfo user) {
		LOGGER.info("> updateProfile : {}", user);		
		Assert.notNull(user.getUsername(), "Username is required");

		User existUser = userRepository.findByUsername(user.getUsername());
		Assert.notNull(existUser, "User is not found.");
		
		if(user.getFirstName() !=null){
			existUser.setFirstName(user.getFirstName());
		}
		if(user.getLastName() !=null){
			existUser.setLastName(user.getLastName());
		}	
		if(user.getTelephone() !=null && !user.getTelephone().equals(existUser.getUsername()) ){
			existUser.setTelephone(user.getTelephone());
		}	
		if(user.getEmail() !=null && !user.getEmail().equals(existUser.getUsername()) ){
			existUser.setEmail(user.getEmail());
		}		

		userRepository.save(existUser);

		return existUser;
	}

	@Override
	public String getAuditorRole(){
		for(Role role : SecurityContext.getCurrentUser().getRoles()){
			if(role.getName().contains("Root") || role.getName().contains("Super Admin")){
				return "Super Admin";
			}
		}
		
		for(Role role : SecurityContext.getCurrentUser().getRoles()){
			if(role.getName().contains("Admin")){
				return "Admin";
			}
		}
	
		return SecurityContext.getCurrentUser().getRoles().iterator().next().getName();
	}
	
	@Override
	public String getUserRole(Collection<Role> roles){		
		Iterator<Role> itrRole = roles.iterator();		
		while(itrRole.hasNext()){
			Role role = itrRole.next();
			if(role.getName().contains("Root") || role.getName().contains("Super Admin")){
				return "Super Admin";
			}			
		}
		
		Iterator<Role> itrRole1 = roles.iterator();
		while(itrRole1.hasNext()){
			Role role = itrRole1.next();
			if(role.getName().contains("Admin")){
				return "Admin";
			}			
		}
		
		Iterator<Role>  itrRole2 = roles.iterator();
		return itrRole2.next().getName();			
	}

	@Override
	public List<String> getRolesHierachy(String auditorRole) {
		List<String> roleList = new ArrayList<>();
		if(auditorRole.contains("Super Admin")){	
			String[] r = {"Root", "Default", "Super Admin"};
			List<Role> roles = roleRepository.findRolesHierarchyNotContain(r);
			for(Role role : roles){
				roleList.add(role.getName());
			}
		}
		else if(auditorRole.contains("Admin")){
			String[] r = {"Root", "Default", "Super Admin", "Admin"};
			List<Role> roles = roleRepository.findRolesHierarchyNotContain(r);
			for(Role role : roles){
				roleList.add(role.getName());
			}				
		}
		return roleList;
	}
	
	@Override
	public User resetPassword(UserDto userInfo, HttpServletRequest request) {
		LOGGER.info("resetPassword : {}", userInfo);
		Assert.notNull(userInfo.getId(), "Id is required");
		User existUser = userRepository.findById(userInfo.getId()).get();
		Assert.notNull(existUser, "User's record is not found.");

		String auditorRole = SecurityContext.getCurrentUser().getRoles().iterator().next().getName().toString();//first record
		List<String> existRoleList = getRolesHierachy(auditorRole);
		Iterator<String> itrRole = existRoleList.iterator();
		Boolean flag = false;
		while(itrRole.hasNext()){
			String existRole = itrRole.next();
			if(existRole.equals(existUser.getRoles().iterator().next().getName().toString())){
				flag = true;
				break;
			}
		}
		if(!flag){
			throw new RuntimeException("Unauthorized.");
		}
		

		String p = SecurityUtil.generatePassword();
		userInfo.setPassword(p);
		existUser.setPassword(bCryptPasswordEncoder.encode(p));
		userRepository.save(existUser);
		LOGGER.info("Password reset successful.");
		existUser.setPassword(null);
		BeanUtils.copyProperties(existUser, userInfo, "password");
		userInfo.setId(existUser.getId());		
		
		//send mail
		eventPublisher.publishEvent(new OnPasswordResetCompleteEvent(userInfo, new Locale("en"),
				userInfo.getAccountActivationEndpoint() != null ? userInfo.getAccountActivationEndpoint()
						: getAppUrl(request)));
		return existUser;
	}
	
	@Override
	public User findUserByEmail(final String email) {
		return userRepository.findByEmail(email);
	}
	
	private String getAppUrl(HttpServletRequest request) {
		return "http://" + request.getLocalAddr() + ":" + request.getServerPort() + request.getContextPath();
	}
	
	@Override
	public User disableAccount(UserDto user) {
		LOGGER.info("disableAccount : {}", user);
		Assert.notNull(user.getId(), "Id is required");
		
		User existUser = userRepository.findById(user.getId()).get();
		Assert.notNull(existUser, "User's record is not found.");

		//Validate Role 
		if(getAuditorRole().equals(getUserRole(existUser.getRoles()))){
			throw new RuntimeException("Unauthorized role.");
		}

		if (existUser.isEnabled() == false){
			throw new RuntimeException("User is already disabled.");
		}


		existUser.setEnabled(false);
		userRepository.save(existUser);
		existUser.setPassword(null);
		LOGGER.info("Account disable successful.");
		
		return existUser;
	}

	@Override
	public User enableAccount(UserDto user) {
		LOGGER.info("enableAccount : {}", user);
		Assert.notNull(user.getId(), "Id is required");
		
		User existUser = userRepository.findById(user.getId()).get();
		Assert.notNull(existUser, "User's record is not found.");
		
		//Validate Role 
		if(getAuditorRole().equals(getUserRole(existUser.getRoles()))){
			throw new RuntimeException("Unauthorized role.");
		}

		if (existUser.isEnabled() == true){
			throw new RuntimeException("User is already enabled.");
		}

		existUser.setEnabled(true);
		userRepository.save(existUser);
		existUser.setPassword(null);
		LOGGER.info("Account enable successful.");
		return existUser;
	}
	
	@Override
	@Transactional
	public User updateRole(UserDto user) {
		LOGGER.info("> updateRole : {}", user);
		Assert.notNull(user.getId(), "ID is required");
		Assert.notNull(user.getRoles(), "Role is required");
		
		User existUser = userRepository.findById(user.getId()).get();
		Assert.notNull(existUser, "User is not found.");		

		//Identify auditor's role		
		String auditorRole = getAuditorRole();
		
		if(auditorRole.equals(getUserRole(existUser.getRoles()))){
			throw new RuntimeException("Unauthorized role.");
		}

		
		//Identify allowed roles for auditor
		List<String> roles = getRolesHierachy(auditorRole);
		
		//Validate update roles with allowed roles
		for(String updateRole : user.getRoles()){
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


		//create roles collection
		List<Role> collection = new ArrayList<>();		
		for(int i=0; i< user.getRoles().size(); i++){
			Role existRole = roleRepository.findByName(user.getRoles().get(i));
			Assert.notNull(existRole, "[" + user.getRoles().get(i) + "] is not found.");
			collection.add(existRole);		
		}	
		
		existUser.setRoles(collection);
		userRepository.save(existUser);

		LOGGER.info("Account's Role update successful.");
		return existUser;
	}
	
	@Override
	public UserDto updateProfile(UserDto user) {
		LOGGER.info("> updateProfile : {}", user);		
		Assert.notNull(user.getId(), "ID is required");

		User existUser = userRepository.findById(user.getId()).get();
		Assert.notNull(existUser, "User is not found.");

		//Validate Role 
		if(!getAuditorRole().equals(getUserRole(existUser.getRoles()))){			
			List<String> roles = getRolesHierachy(getAuditorRole());
			Boolean flag = false;
			for(String itr : roles) {
				if(itr.equals(getUserRole(existUser.getRoles()))) {
					flag = true;
				}
			}
			if(!flag) {
				throw new RuntimeException("Unauthorized role.");				
			}
		}
		
		if(user.getFirstName() !=null){
			existUser.setFirstName(user.getFirstName());
		}
		if(user.getLastName() !=null){
			existUser.setLastName(user.getLastName());
		}	
		if(user.getTelephone() !=null && !user.getTelephone().equals(existUser.getUsername()) ){
			existUser.setTelephone(user.getTelephone());
		}	
		if(user.getEmail() !=null && !user.getEmail().equals(existUser.getUsername()) ){
			existUser.setEmail(user.getEmail());
		}		

		userRepository.save(existUser);
		
		UserDto userDTO = new UserDto();
		BeanUtils.copyProperties(existUser, userDTO);
		
		ArrayList<String> roles = new ArrayList<String>();
		Iterator<Role> itr = existUser.getRoles().iterator();

		while (itr.hasNext()) {
			Role role = (Role) itr.next();
			roles.add(role.getName());
		}				
		userDTO.setRoles(roles);		
		userDTO.setPassword(null);
		
		LOGGER.info("Account's Profile update successful.");
		return userDTO;
	}	
	

}