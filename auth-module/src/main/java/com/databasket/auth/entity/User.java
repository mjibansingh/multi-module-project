package com.databasket.auth.entity;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.jboss.aerogear.security.otp.api.Base32;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.databasket.auth.config.Auditable;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Audited
@EntityListeners(AuditingEntityListener.class)
public class User extends Auditable {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;   
 

    
    @Column(unique=true, nullable=false)
    private String username;  
    
    @Column(nullable=false, length = 60)
    private String password;
    
    private String firstName;
    private String lastName;
    private String email;        
    private String telephone;
    private boolean enabled; 
    private String secret;
  

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable (name="user_role", joinColumns=@JoinColumn(name="user_id"), inverseJoinColumns = @JoinColumn(name="role_id"))
    @JsonIgnore
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Collection<Role> roles;
    
    public User() {
        super();
        this.secret = Base32.random();
        this.enabled = false;
    }

    public boolean isEnabled() {
        return enabled;
    }
    public Boolean getEnabled() {
        return enabled;
    }    

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((email == null) ? 0 : email.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final User user = (User) obj;
        if (!email.equals(user.email)) {
            return false;
        }
        return true;
    }
    
    public Map<String, String> compareFields(User user) throws IllegalAccessException {
    	Map<String, String> resultObject = new HashMap<>();     
        Field[] fields = this.getClass().getDeclaredFields();
        
        for(Field field : fields){
            if(field.get(this) != null && field.get(user) != null && !field.get(this).equals(field.get(user))){            	
            	String fieldName = field.getName();
            	String fieldValue;
            	if(fieldName.equals("password")){
            		fieldValue = "*****" + " > " + "*****";
            	}
            	else{
            		fieldValue = field.get(this).toString() + " > " + field.get(user).toString();
            	}
            	
               	if(fieldName.equals("enabled")){
            		fieldValue = (field.get(this).equals(true) ? "Enabled" : "Disabled") + " > " + (field.get(user).equals(true) ? "Enabled" : "Disabled");
            	}            	
            	 
            	resultObject.put(fieldName, fieldValue);
            }
        }
        return resultObject;
    }    
 
}