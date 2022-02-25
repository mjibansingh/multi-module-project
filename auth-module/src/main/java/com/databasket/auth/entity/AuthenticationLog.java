package com.databasket.auth.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationLog {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;   
 
    private String username;      
    private Date timestamp;
    private String event;

}