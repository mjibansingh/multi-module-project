package com.databasket.auth.events;

import java.util.Locale;

import org.springframework.context.ApplicationEvent;

import com.databasket.auth.dto.UserDto;

@SuppressWarnings("serial")
public class OnPasswordResetCompleteEvent extends ApplicationEvent {

    private final String appUrl;
    private final Locale locale;
    private final UserDto userInfo;

    public OnPasswordResetCompleteEvent(UserDto userInfo, Locale locale, String appUrl) {
    	super(userInfo);
        this.locale = locale;
        this.appUrl = appUrl;
        this.userInfo = userInfo;
	}

	public String getAppUrl() {
        return appUrl;
    }

    public Locale getLocale() {
        return locale;
    }

    public UserDto getUserInfo(){
    	return userInfo;
    }
}
