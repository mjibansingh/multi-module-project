package com.databasket.auth.dto;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.databasket.auth.entity.Role;
import com.databasket.auth.entity.User;

import lombok.Getter;
import lombok.Setter;

@SuppressWarnings("serial")
@Getter
@Setter
public class AuthUserInfo extends UsernamePasswordAuthenticationToken{

	public AuthUserInfo(User user) {
		super(null, null);
		BeanUtils.copyProperties(user, this);
		for (Role role : user.getRoles()) {
			this.getAuthorities().add(new SimpleGrantedAuthority(role.getName()));
		}
		
		user.setSecret(null);
		user.setPassword(null);
		setUser(user);
	}
	
	public AuthUserInfo() {
		super(null, null);
	}

	private User user;
	private Collection<GrantedAuthority> authorities = new ArrayList<>();
	private Object principal;
	private boolean authenticated;

}
