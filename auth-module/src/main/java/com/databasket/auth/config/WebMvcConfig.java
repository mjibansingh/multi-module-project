package com.databasket.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
		
	@Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/tt/users/avatars/**", "/tt/category/image/**", "/tt/disposal/image/**",
				"/tt/infocenter/image/**", "/tt/partner/image/**", "/tt/product/image/**", "/tt/image/**")
				.addResourceLocations("file:images/avatars/", "file:images/category/", "file:images/disposal/",
						"file:images/infocenter/", "file:images/partner/", "file:images/product/");

		registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");

		registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");    
    }
	
	@Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/tt/users/avatars/**")
            .allowedOrigins("*");
    }
	

	

}