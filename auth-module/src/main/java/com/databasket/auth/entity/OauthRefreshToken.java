package com.databasket.auth.entity;

import java.sql.Blob;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class OauthRefreshToken {
	@Id
    private String tokenId;
    
    @Lob
    private Blob token;

    @Lob
    private Blob authentication;
    
}

//CREATE TABLE oauth_refresh_token (
//	    token_id varchar(255) NOT NULL,
//	    token blob NOT NULL,
//	    authentication blob NOT NULL
//	) ENGINE=InnoDB DEFAULT CHARSET=utf8;
