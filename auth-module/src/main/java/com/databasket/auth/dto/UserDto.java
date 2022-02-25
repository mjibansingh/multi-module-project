package com.databasket.auth.dto;

import java.util.ArrayList;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.beans.BeanUtils;

import com.databasket.auth.entity.Role;
import com.databasket.auth.entity.User;
//import com.databasket.smarthotel.commons.AddressInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
public class UserDto {
	private Long id;
	   
	//@S(message="Name is required")
    private String firstName;
    private String lastName;

    //@ValidEmail(message="Valid email is required")
    private String email;
    private Integer age;
    private String sex;
    
    //@NotNull(message="Mobile number is required")
    private String telephone;
    
    //@NotNull(message="Address is required")
	//List<AddressInfo> addresses = new ArrayList<>(); 
 
    private String username;
    //@ValidPassword
    private String password;
    private String matchingPassword;    
    
    private boolean enabled;

	private String role;	
	
	ArrayList<String> roles = new ArrayList<String>();
	
	private Date createTimestamp;
	private String status;
	
	private String system;
	private String accountActivationEndpoint;
	private String loginURL;
	private String token;
	
	public UserDto(User user) {
		BeanUtils.copyProperties(user, this);
		user.setPassword(null);
		for(Role role : user.getRoles()) this.getRoles().add(role.getName());        
	}
	
	public UserDto() {}

}
