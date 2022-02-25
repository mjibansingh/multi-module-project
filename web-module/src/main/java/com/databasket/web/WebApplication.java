package com.databasket.web;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.TimeZone;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.databasket.auth.config.AuditorAwareImpl;
import com.databasket.auth.dto.AuthPrincipal;
import com.databasket.auth.entity.User;
import com.databasket.auth.service.UserService;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class, DataSourceAutoConfiguration.class})
@ComponentScan("com.databasket")
@EntityScan(basePackages = "com.databasket")
@EnableJpaRepositories("com.databasket")
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@Controller
@Slf4j
public class WebApplication {
	
	@Autowired
	UserService userService;
    
    @Autowired
    private ConsumerTokenServices consumerTokenServices;
    
    @Autowired
    private AuthorizationServerTokenServices authorizationServerTokenServices;
    
    @Autowired
    Environment environment;

	public static void main(String[] args) {
		SpringApplication.run(WebApplication.class, args);
		System.err.println("Web Application works...");
	}
	
	@PostConstruct
	public void setTimezone() {
		TimeZone.setDefault(TimeZone
				.getTimeZone(environment.getProperty("app.timezone") != null ? environment.getProperty("app.timezone") : "UTC"));
		log.info("Initializaed app at {} timezone.", TimeZone.getDefault().getID());
	}
	
	@PostConstruct
	public void postConstruct() {
		Path rootLocationImage = Paths.get("images");
		Path rootLocationData = Paths.get("data");

		try {
			Files.createDirectory(rootLocationImage);
		} catch (IOException e) {}			
		try {
			Files.createDirectory(rootLocationData);
		} catch (IOException e) {}			
	}
		
	
    @RequestMapping(value = "/oauth/logout", method = RequestMethod.GET)
    public ResponseEntity<String> logout(Principal principal, HttpServletRequest request, HttpServletResponse response) {    	
    	OAuth2Authentication oAuth2Authentication = (OAuth2Authentication) principal;
        OAuth2AccessToken accessToken = authorizationServerTokenServices.getAccessToken(oAuth2Authentication);
        consumerTokenServices.revokeToken(accessToken.getValue());
        return ResponseEntity.ok().body("Logout successfully.");
    }

	@GetMapping("/oauth/user")	
	public @ResponseBody Principal user(Principal user) {
		AuthPrincipal principal = new AuthPrincipal(((OAuth2Authentication)user).getOAuth2Request(), ((OAuth2Authentication)user).getUserAuthentication());
		User loaded = userService.get(user.getName());
		String fullName = "";
		if (loaded.getFirstName()!=null && !loaded.getFirstName().isEmpty()) fullName += loaded.getFirstName();
		if (loaded.getLastName()!=null && !loaded.getLastName().isEmpty()) fullName += " " + loaded.getLastName();
		principal.setFullName(fullName);
		principal.setId(loaded.getId());
		return principal;
	}
	

	@GetMapping({ 
		"/laboratory/"
		 })
	public String angularRoute() {
		return "forward:/laboratory/index.html"; 
	}	

}
