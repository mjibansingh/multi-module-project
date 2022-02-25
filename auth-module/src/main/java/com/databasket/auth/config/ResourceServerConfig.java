package com.databasket.auth.config;


import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.context.ShutdownEndpoint;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;

@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    private static final String RESOURCE_ID = "resource_id";

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources.resourceId(RESOURCE_ID).stateless(false);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
        .authorizeRequests()
        .requestMatchers(EndpointRequest.to(ShutdownEndpoint.class))
        .hasRole("Super Admin")
        .requestMatchers(EndpointRequest.toAnyEndpoint())
        .permitAll()        
        .antMatchers("/", "/index.html", "/**.css", "/**.js", "/**.woff2", "/assets/**", "/**/public/**", "/**/setting/**", "/get/image/**", "/laboratory/**", "/v2/api-docs/**", "/swagger-ui.html", "/webjars/**", "/swagger-resources/**")
        .permitAll()
        .anyRequest()
        .authenticated()
        .and().exceptionHandling().accessDeniedHandler(new OAuth2AccessDeniedHandler());
    }

}