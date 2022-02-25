package com.databasket.auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class HeaderInterceptorRegister extends WebMvcConfigurerAdapter{
	@Autowired
	HeaderInterceptor headerInterceptor;
	
    @Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(headerInterceptor);
		//super.addInterceptors(registry);
	}
}
