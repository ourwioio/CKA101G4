package com.webond.securityconfig.admin;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;


@Configuration
@EnableWebSecurity 
public class AdminSecurityConfig {

	private final AdminUserDetailService userDetailsService;
	private final AdminLoginSuccessHandler successHandler;
	

	public AdminSecurityConfig(AdminUserDetailService userDetailsService,
			AdminLoginSuccessHandler successHandler) {
		this.userDetailsService = userDetailsService;
		this.successHandler = successHandler;
	}


	@Bean
	@Order(1) 
	public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception{
		http
			.securityMatcher("/admin/**")
		
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/admin/login", "/admin/login?error=true").permitAll()
				.requestMatchers("/admin/employees/updatePassword").authenticated() 
				
				.requestMatchers("/admin/employees/**").hasAuthority("員工管理")
				.requestMatchers("/admin/services/**").hasAuthority("服務管理")
				.requestMatchers("/admin/activity/**").hasAuthority("活動管理")
				.requestMatchers("/admin/venue/**").hasAuthority("場地管理")
				.requestMatchers("/admin/members/**").hasAuthority("會員管理")
				.requestMatchers("/admin/order/**").hasAuthority("訂單交易管理")
				.requestMatchers("/admin/platform/**").hasAuthority("平台管理")
				
				.anyRequest().authenticated()					
			)
            .userDetailsService(userDetailsService)	
            
            	.formLogin(form -> form
            	.loginPage("/admin/login")       
            	.loginProcessingUrl("/admin/login")  
            	
            	.usernameParameter("username")	 
            	.passwordParameter("password") 
            	
            	.successHandler(successHandler)
            	
            	.failureHandler(new AdiminFailureHandler())      
            )
            .logout(logout -> logout
            	.logoutUrl("/admin/logout")
            	.logoutSuccessUrl("/admin/login?logout") 
            	.invalidateHttpSession(true)
            	.clearAuthentication(true)
            	.deleteCookies("JSESSIONID")				 				 	
            )
            .requestCache(cache -> cache
            	.requestCache(new HttpSessionRequestCache())		
            )
            .exceptionHandling(exception -> exception
            	 .accessDeniedHandler((request, response, accessDeniedException) -> {
            	      	response.sendRedirect("/admin/adminPage?error=no_permission"); 
            	    })
            );
		
            
		return http.build();
	}
	


	
}
