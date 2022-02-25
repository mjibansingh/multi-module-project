package com.databasket.auth.config;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.databasket.auth.entity.Role;
import com.databasket.auth.entity.VerificationToken;
import com.databasket.auth.repository.RoleRepo;
import com.databasket.auth.repository.UserRepo;
import com.databasket.auth.repository.VerificationTokenRepo;

@Component
public class UserAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    UserRepo userRepo;
    
    @Autowired
    RoleRepo repo;
    
    @Autowired
    VerificationTokenRepo verificationTokenRepo;
    
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    
    public Locale locale;
    
    public UserAuthenticationProvider() {
        super();
    }

    @Override
    public Authentication authenticate(final Authentication authentication) {
    	
    	final String name = authentication.getName();
        final String password = authentication.getCredentials().toString();
  
        com.databasket.auth.entity.User user = userRepo.findByUsername(name);
    	
    	if(user == null || !user.isEnabled()){
        	SecurityContextHolder.clearContext();
        	return null;
    	}    

    	
        if (name.equals(user.getUsername()) && bCryptPasswordEncoder.matches(password, user.getPassword())) {
            final List<GrantedAuthority> grantedAuths = new ArrayList<>();
            for (Role role : user.getRoles()){
            	grantedAuths.add(new SimpleGrantedAuthority(role.getName()));
            	
            	if(role.getName().equals("Customer")) {
                	HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
                	String verification = request.getHeader("Verification-Token");
                	if(verification != null) {
                		validateOTPLogin(user);
                	}           		
            	}
            }            
            final UserDetails principal = new User(name, password, grantedAuths);
            return new UsernamePasswordAuthenticationToken(principal, password, grantedAuths);
        } else {
        	throw new RuntimeException("Credential mismatched.");
        }
    }
    
    private void validateOTPLogin(com.databasket.auth.entity.User user) {
    	HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
    	String verification = request.getHeader("Verification-Token");

    	if(verification != null) {
    		VerificationToken verificationToken = verificationTokenRepo.findByUser(user);
    		Assert.notNull(verificationToken, "Verification token is not found"); 
    		
    		if (verification.equals(verificationToken.getToken())) {
    			Date now = new Date();
    			
    			Date expiryVerification = new Date(verificationToken.getExpiryDate().getTime());
    			   			
    			if(now.after(expiryVerification)) {
    				throw new RuntimeException("Verification token expired.");
    			}
    		}
    		else {
    			throw new RuntimeException("Verification token mismatched.");
    		}
    	}
    	else {
    		throw new RuntimeException("verification header is missing.");
    	}

		user.setPassword(bCryptPasswordEncoder.encode(UUID.randomUUID().toString()));
		userRepo.save(user);
	  	
    }

    @Override
    public boolean supports(final Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

}

