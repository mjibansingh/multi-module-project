package com.databasket.auth.dto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

import com.databasket.auth.dto.PrivilegeInfo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthPrincipal extends OAuth2Authentication{

	private static final long serialVersionUID = 1L;
	
	public AuthPrincipal(OAuth2Request storedRequest, Authentication userAuthentication) {
		super(storedRequest, userAuthentication);
	}
	
	private String fullName;
	private Long id;
	private List<PrivilegeInfo> privileges = new ArrayList<>();
		
}
