package com.databasket.auth.config;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.databasket.auth.config.SecurityContext;
import com.databasket.auth.dto.UserDto;
import com.databasket.auth.repository.UserRepo;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class HeaderInterceptor extends HandlerInterceptorAdapter  {
	Logger logger = LoggerFactory.getLogger(HeaderInterceptor.class);
		

    @Autowired
    UserRepo userRepository;

	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {	
		
		//Spring – Log Incoming Requests
		//logging.level.org.springframework.web.filter.CommonsRequestLoggingFilter: DEBUG
	    HttpServletRequest requestCacheWrapperObject = new ContentCachingRequestWrapper(request);
	    requestCacheWrapperObject.getParameterMap();
		
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    if (!(auth instanceof AnonymousAuthenticationToken)) {			

			UserDetails userDetails = (UserDetails) auth.getPrincipal();			
			UserDto user = new UserDto();
			user.setUsername(userDetails.getUsername());
			
			Iterator<GrantedAuthority> itrRole = (Iterator<GrantedAuthority>) userDetails.getAuthorities().iterator();
			
			while (itrRole.hasNext()) {
				String role = itrRole.next().getAuthority();
				user.getRoles().add(role);
			}
			
			SecurityContext.setCurrentUser(userRepository.findByUsername(userDetails.getUsername()));
			

	    }
	    else {
			log.info("Requesting '{} {}' by anonymous user...", request.getMethod(), request.getRequestURI());
		}
	
		long startTime = System.currentTimeMillis();
		request.setAttribute("startTime", startTime);

		return true;
	}
	
	
	
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {		
		long startTime = (Long) request.getAttribute("startTime");
		long endTime = System.currentTimeMillis();
		long executeTime = endTime - startTime;
		logger.info("*** Execution Time > {} ms", executeTime);
	}	
	
}
