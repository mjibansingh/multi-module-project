package com.databasket.auth.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

	 @Autowired
	    private BCryptPasswordEncoder bCryptPasswordEncoder;
	 private static final String CLIENT_ID = "smclient";
	    // encoding method prefix is required for DelegatingPasswordEncoder which is default since Spring Security 5.0.0.RC1
	    // you can use one of bcrypt/noop/pbkdf2/scrypt/sha256
	    // you can change default behaviour by providing a bean with the encoder you want
	    // more: https://spring.io/blog/2017/11/01/spring-security-5-0-0-rc1-released#password-encoding
	    static final String CLIENT_SECRET = "smclient00ll";

	    private static final String GRANT_TYPE_PASSWORD = "password";
	    private static final String AUTHORIZATION_CODE = "authorization_code";
	    private static final String REFRESH_TOKEN = "refresh_token";
	    private static final String SCOPE_READ = "read";
	    private static final String SCOPE_WRITE = "write";
	    private static final String TRUST = "trust";
	    private static final int VALID_FOREVER = -1;

	    @Autowired
	    private AuthenticationManager authManager;

//	    @Autowired
//	    private DataSource dataSource;
	    
	    @Autowired
	    private TokenStore tokenStore;

//	    @Bean
//	    public TokenStore tokenStore() {
//	        return new JdbcTokenStore(dataSource);
//	    }
	    @Bean
	    @Primary
	    public DefaultTokenServices tokenServices() {
	        DefaultTokenServices tokenServices = new       DefaultTokenServices();
	        tokenServices.setSupportRefreshToken(true);
	        tokenServices.setTokenStore(this.tokenStore);
	        return tokenServices;
	    }
	    @Override
	    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
	        clients
	                .inMemory()
	                .withClient(CLIENT_ID)
	                .secret(bCryptPasswordEncoder.encode(CLIENT_SECRET))
	                .authorizedGrantTypes(GRANT_TYPE_PASSWORD, AUTHORIZATION_CODE, REFRESH_TOKEN)
	                .scopes(SCOPE_READ, SCOPE_WRITE, TRUST)
	                .accessTokenValiditySeconds(VALID_FOREVER)
	                .refreshTokenValiditySeconds(VALID_FOREVER);
	    }

	    @Override
	    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
	        endpoints.tokenStore(this.tokenStore)
	                .authenticationManager(authManager);
	    }
}
