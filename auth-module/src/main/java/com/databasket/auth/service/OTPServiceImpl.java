package com.databasket.auth.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.aerogear.security.otp.Totp;
import org.jboss.aerogear.security.otp.api.Base32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.databasket.auth.dto.ChangePasswordInfo;
import com.databasket.auth.dto.OTPRequestInfo;
import com.databasket.auth.dto.SMSRequestInfo;
import com.databasket.auth.dto.UserDto;
import com.databasket.auth.entity.Role;
import com.databasket.auth.entity.User;
import com.databasket.auth.entity.VerificationToken;
import com.databasket.auth.events.UserEvent;
import com.databasket.auth.repository.RoleRepo;
import com.databasket.auth.repository.UserRepo;
import com.databasket.auth.repository.VerificationTokenRepo;
import com.databasket.auth.utility.SecConstants.TokenStatus;
import com.databasket.auth.utility.SecConstants.UserEventSubtype;

@Service
public class OTPServiceImpl implements OTPService {
	
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	@Autowired
	UserRepo userRepository;
	
	@Autowired
    private MessageSource messages;
	
	@Autowired
	private RoleRepo roleRepository;
	
	@Autowired
	private VerificationTokenRepo verificationTokenRepository;
	
	@Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private SMSService smsService;
	
	private Locale locale;
	
	@Override
	@Transactional
	public String validateUserWithOTP(OTPRequestInfo otpRequest) {
		Assert.notNull(otpRequest.getOtp(), "OTP is required.");
    	Assert.hasLength(otpRequest.getToken(), "Token is required.");
    	Assert.notNull(otpRequest.getTelephone(), "Mobile number is required.");
    	
    	if (!otpRequest.getTelephone().matches("\\d{10}")) throw new RuntimeException("Mobile number is invalid");   	
    	
    	User user = userRepository.findByUsername(otpRequest.getTelephone());    	
    	Assert.notNull(user, "User is not registered.");
    	  	
    	VerificationToken verificationToken = verificationTokenRepository.findByUser(user);
    	Assert.notNull(verificationToken, "VerificationToken is not found.");
    	
    	if(TokenStatus.USED.toString().equals(verificationToken.getStatus())) {
    		throw new RuntimeException("OTP has been used.");
    	}
    	
		Date now = new Date();		
		Date expiryVerification = new Date(verificationToken.getExpiryDate().getTime());		   			
		if(now.after(expiryVerification)) {
			throw new RuntimeException("OTP has been expired.");
		}    	
    	
    	if(!otpRequest.getOtp().equals(verificationToken.getOtp())) {
    		throw new RuntimeException("OTP is invalid.");
    	}
    	
    	if(!otpRequest.getToken().equals(verificationToken.getToken())) {
    		throw new RuntimeException("Token is invalid.");
    	}
    	
		if(otpRequest.getPassword() != null && !otpRequest.getPassword().trim().equals("")) {
			 user.setPassword(bCryptPasswordEncoder.encode(otpRequest.getPassword()));
		} 
//		else {
//			user.setPassword(bCryptPasswordEncoder.encode(otpRequest.getOtp()));	
//		}
		
		user.setEnabled(true);
		userRepository.save(user);
		
		verificationToken.setStatus(TokenStatus.USED.toString());
        verificationTokenRepository.save(verificationToken);		
		
    	return "Account activated.";
	}
	
	@Override
	public Map<String, String> saveVerificationTokenForUser(User user, String token, String otp) {
		VerificationToken existVerificationToken = verificationTokenRepository.findByUser(user);
		
		if (existVerificationToken == null ) {
			existVerificationToken = new VerificationToken();
			existVerificationToken.setUser(user);
			existVerificationToken.setToken(token);
			existVerificationToken.setOtp(otp);			
		} 
			
		if(existVerificationToken != null && TokenStatus.USED.toString().equals(existVerificationToken.getStatus())) {
			existVerificationToken.setToken(token);
			existVerificationToken.setOtp(otp);			
		}
		
		existVerificationToken.setExpiryDate(generateTokenExpiryDate(5));//set expiry time with 5 minutes
		existVerificationToken.setStatus(TokenStatus.GENERATED.toString());
        verificationTokenRepository.save(existVerificationToken);
        
        Map<String, String> response = new HashMap<String, String>();
        response.put("otp", existVerificationToken.getOtp());
        response.put("token", existVerificationToken.getToken());
        return response;
	}
	
	@Override
	public String changePasswordWithCode(ChangePasswordInfo credential) {
		try {
			LOGGER.info("changePasswordWithCode : {}", new ObjectMapper().writeValueAsString(credential));
		} catch (IOException e) {e.printStackTrace();
		}
		
		Assert.notNull(credential.getOtp(), "Code is required.");
		//Assert.notNull(credential.getToken(), "Token is required.");
		Assert.notNull(credential.getNewPassword(), "Password is required.");
		Assert.notNull(credential.getUserName(), "Username is required.");
		
		User existUser = userRepository.findByUsername(credential.getUserName());
		if (existUser == null) throw new RuntimeException("User is not found..");
		
		VerificationToken existVerificationToken = verificationTokenRepository.findByUser(existUser);
		if (existVerificationToken == null) throw new RuntimeException("Token is not found..");
		
		if (!TokenStatus.GENERATED.toString().equals(existVerificationToken.getStatus())) {
			throw new RuntimeException("Invalid token.");
		}
		
		if(!credential.getOtp().equals(existVerificationToken.getOtp())) {
			throw new RuntimeException("Code is incorrect.");
		}
		
//		if(!credential.getToken().equals(existVerificationToken.getToken())) {
//			throw new RuntimeException("Token is incorrect.");
//		}	
		
		Date now = new Date();		
		Date expiryVerification = new Date(existVerificationToken.getExpiryDate().getTime());
		   			
		if(now.after(expiryVerification)) {
			throw new RuntimeException("Code has been expired.");
		}

		existUser.setPassword(bCryptPasswordEncoder.encode(credential.getNewPassword()));
		userRepository.save(existUser);
		
		existVerificationToken.setStatus(TokenStatus.USED.toString());
		verificationTokenRepository.save(existVerificationToken);
		return "Success";
	}	
	
	@Override
	public String sendPasswordResetCode(String email) {
		Assert.notNull(email, "Email is required.");
		
		User userByEmail = userRepository.findByEmail(email);
		Assert.notNull(userByEmail, "User is not found.");
		
		if(userByEmail.isEnabled()) {	
			String secret = Base32.random();
		    Totp totp = new Totp(secret);
		    String otp = totp.now();  
		    
		    //remove leading zero from otp and replace by 9
		    otp = otp.replaceFirst("^0", "9");
		    
		    LOGGER.info("New OTP generated for {}: {}", userByEmail.getUsername(), otp);
	           	     
	        final String token = UUID.randomUUID().toString();
	        saveVerificationTokenForUser(userByEmail, token, otp);		
	        
	        broadcastUserRegistration(userByEmail, false);
	        
	        return "{\"token\": \"" + token + "\"}";
		}
		else {
			throw new RuntimeException("Active your account from the link sent to your email account.");
		}
	}
	
	@Autowired
	ApplicationEventPublisher eventPublisher;
	
	private void broadcastUserRegistration(User user, boolean activate) {
		if (!activate) {
			eventPublisher.publishEvent(new UserEvent(
												user, 
												LocaleContextHolder.getLocale(), 
												UserEventSubtype.PASSWORD_RESET_CODE.toString(), null, null));
		} 
	}

	@Transactional
	@Override
	public String sendOTP(OTPRequestInfo otpRequest) {
    	Assert.notNull(otpRequest.getTelephone(), "Mobile number is required.");
    	
    	if (!otpRequest.getTelephone().matches("\\d{10}")) {
    		throw new RuntimeException("Mobile number is invalid");
    	}   	
    	
    	User user = userRepository.findByUsername(otpRequest.getTelephone());
    	Assert.notNull(user, "Mobile number is not registered.");

    	String token = generateOTP(user);
    	
    	return "{\"token\": \"" + token + "\"}";
	}
	
	@Override
	public String resendOTP(OTPRequestInfo otpRequest) {
		Assert.notNull(otpRequest.getTelephone(), "Mobile number is required");
		
    	if (!otpRequest.getTelephone().matches("\\d{10}")) {
    		throw new RuntimeException("Mobile number is invalid");
    	} 		
		Assert.notNull(otpRequest.getToken(), "Token is required");
		
		User user = user = userRepository.findByUsername(otpRequest.getTelephone());
    	Assert.notNull(user, "Mobile number is not registered");
    	
    	VerificationToken verificationToken = verificationTokenRepository.findByUser(user);
    	Assert.notNull(verificationToken, "VerificationToken is not found.");
    	
    	String token = null;
    	if(otpRequest.getToken().trim().equals(verificationToken.getToken().trim())) {
    		token = generateOTP(user);
    	}
    	else {
    		throw new RuntimeException("Token is invalid.");
    	}
    	
    	return "{\"token\": \"" + token + "\"}";
	}
	
	@Transactional
	@Override
	public String generateOTP(User user) {
		String secret = Base32.random();
	    Totp totp = new Totp(secret);
	    String otp = totp.now();  
	    
	    //remove leading zero from otp and replace by 9
	    otp = otp.replaceFirst("^0", "9");
	    	         
        String token = UUID.randomUUID().toString();
        Map<String, String> response = saveVerificationTokenForUser(user, token, otp);
        otp = response.get("otp");
        token = response.get("token");

        LOGGER.info("New OTP generated for USername {}: OTP : {} Token : {}", user.getUsername(), otp, token);
        
		SMSRequestInfo smsRequestInfo = smsService.buildRegistrationConfirmationSMSRequestInfo(user.getTelephone(), otp);
		if(smsRequestInfo != null){
			try {
				Object responseBody = smsService.sendSMS(smsRequestInfo);
				LOGGER.debug("OTP sent. Response: {}", responseBody);
			} catch (Exception e) {
				LOGGER.error("Failed to send OTP:", e.getMessage());
			}
		}
	    return token;        
	}
	
	private Date generateTokenExpiryDate(int expiryTimeInMinutes) {
        Calendar cal = Calendar.getInstance();
        return new Date(cal.getTimeInMillis() + (1000 * 60 * expiryTimeInMinutes));
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
				
				String password = UUID.randomUUID().toString();
				if(userItr.getPassword() != null) {
					password = userItr.getPassword();
				}
				newUser.setPassword(bCryptPasswordEncoder.encode(password));
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
	
	
	
	@Override
	@Transactional
	public User register(UserDto user) {
		User existUser = userRepository.findByUsername(user.getUsername());
		
		if(existUser == null) {
			existUser = new User();
			
			existUser.setUsername(user.getUsername());			
			existUser.setTelephone(user.getTelephone());
			if(user.getEmail() != null) {
				existUser.setEmail(user.getEmail());
			}
			if(user.getFirstName() != null) {
				existUser.setFirstName(user.getFirstName());	
			}
			if(user.getLastName() != null) {
				existUser.setLastName(user.getLastName());
			}
			
			
			String password = bCryptPasswordEncoder.encode(UUID.randomUUID().toString());
			if(user.getPassword() != null) { 
				password = bCryptPasswordEncoder.encode(user.getPassword()); 
			}
			existUser.setPassword(password);	
			
			existUser.setEnabled(false);
		}
		else {
			if(existUser.isEnabled()) {
				throw new RuntimeException("Account is already created.");
			}
		}
		
		for(String itr : user.getRoles()) {			
			if(existUser.getRoles() != null && existUser.getRoles().size() != 0) {
				Iterator<Role> itrRole = existUser.getRoles().iterator();
				
				Boolean flag = false;
				while (itrRole.hasNext()) {
					Role role = (Role) itrRole.next();
					if(role.getName().equals(itr)) { flag = true; }
				}	
				if(!flag) {
					Role existRole = roleRepository.findByName(itr);
					if(existRole == null) {
						existRole = new Role();
						existRole.setName(itr);
						roleRepository.save(existRole);
					}
					existUser.getRoles().add(existRole);				
				}				
			}
			else {
				existUser.setRoles(new ArrayList<>());
				Role existRole = roleRepository.findByName(itr);
				if(existRole == null) {
					existRole = new Role();
					existRole.setName(itr);
					roleRepository.save(existRole);
				}
				existUser.getRoles().add(existRole);					
			}
		}			
				
		existUser = userRepository.save(existUser);
		return existUser;
	}

	@Override
	public User registerCustomer(UserDto user) {
		try {
			LOGGER.info("registerCustomer : {}", new ObjectMapper().writeValueAsString(user));
		} catch (IOException e) {e.printStackTrace();
		}
		
		User existUser = userRepository.findByUsername(user.getUsername());
		
		if(existUser == null) {
			existUser = new User();
			existUser.setFirstName(user.getFirstName());
			existUser.setUsername(user.getUsername());
			existUser.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
			existUser.setTelephone(user.getTelephone());
			existUser.setEmail(user.getEmail());
			existUser.setRoles(new ArrayList<>());
			existUser.setEnabled(true);		
			

		}
		else {
			existUser.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
		}
		
		//List<Role> collection = new ArrayList<>();
		for(String itr : user.getRoles()) {
			Role existRole = roleRepository.findByName(itr);
			if(existRole == null) {
				existRole = new Role();
				existRole.setName(itr);
				roleRepository.save(existRole);
			}
			//collection.add(existRole);
			existUser.getRoles().add(existRole);
		}	


		userRepository.save(existUser);
		
		return existUser;
	} 
	
	
    
}