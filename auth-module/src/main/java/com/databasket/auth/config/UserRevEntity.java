package com.databasket.auth.config;

import javax.persistence.Entity;

import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;

@Entity
@RevisionEntity(UserRevisionListener.class)
public class UserRevEntity extends DefaultRevisionEntity {
    private String username;
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}