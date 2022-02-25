package com.databasket.auth.events;

import java.util.Locale;

import org.springframework.context.ApplicationEvent;

import com.databasket.auth.dto.UserDto;
import com.databasket.auth.entity.User;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;
	
	private final Locale locale;
    private final User user;
    private final String eventSubtype;
    private String message;    
    private final String appUrl;


    
    public UserEvent(User user, Locale locale, String eventSubtype, String message, String appUrl) {
    	super(user);
        this.locale = locale;
        this.user = user;
        this.eventSubtype = eventSubtype;
        this.message=message;
        this.appUrl = appUrl;
	}

}
