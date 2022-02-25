package com.databasket.auth.dto;

import java.util.ArrayList;
import java.util.Date;

import org.springframework.beans.BeanUtils;

import com.databasket.auth.entity.Role;
import com.databasket.auth.entity.User;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfo {

	private Long id;
	private String firstName;
    private String lastName;
    private String email;
    private Integer age;
    private String sex;
    private String telephone;
	private String username;
    private String password;
    private String matchingPassword;    
	private Integer role;	
	ArrayList<String> roles = new ArrayList<>();
	private String createdBy;
	private Date dateCreated;
	private String updatedBy;
	private Date lastUpdated;
	private String status;
	private String system;
	private String accountActivationEndpoint;
	private String loginURL;
	private String token;
	private boolean enabled;
	
	public UserInfo() {}
	
	public UserInfo(User user) {
		BeanUtils.copyProperties(user, this);
		user.setPassword(null);
		for(Role r : user.getRoles()) this.getRoles().add(r.getName());
	}	
}
