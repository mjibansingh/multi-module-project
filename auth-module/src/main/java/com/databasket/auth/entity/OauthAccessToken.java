package com.databasket.auth.entity;

import java.sql.Blob;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class OauthAccessToken {
    @Id
    private String authenticationId;   
 
    private String tokenId;
    
    @Lob
    private Blob token;
    
    private String userName;
    
    private String clientId;
    
    @Lob
    private Blob authentication;
    
    private String refreshToken;
}

//CREATE TABLE oauth_access_token (
//	    authentication_id varchar(255) NOT NULL PRIMARY KEY,
//	    token_id varchar(255) NOT NULL,
//	    token blob NOT NULL,
//	    user_name varchar(255) NOT NULL,
//	    client_id varchar(255) NOT NULL,
//	    authentication blob NOT NULL,
//	    refresh_token varchar(255) NOT NULL
//	) ENGINE=InnoDB DEFAULT CHARSET=utf8;
