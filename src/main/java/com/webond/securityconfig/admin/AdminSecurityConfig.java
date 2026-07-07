package com.webond.securityconfig.admin;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;


@Configuration
@EnableWebSecurity 
public class AdminSecurityConfig {

	private final DataSource dataSource;
	private final AdminUserDetailService userDetailsService;
	private final AdminLoginSuccessHandler successHandler;
	

	public AdminSecurityConfig(DataSource dataSource, AdminUserDetailService userDetailsService,
			AdminLoginSuccessHandler successHandler) {
		this.dataSource = dataSource;
		this.userDetailsService = userDetailsService;
		this.successHandler = successHandler;
	}


	@Bean
	@Order(1) 
	public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception{
		http
			.securityMatcher("/admin/**")
		
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/admin/login").permitAll()
				.requestMatchers("/admin/employees/updatePassword").authenticated() 
				
				.requestMatchers("/admin/employees/**").hasAuthority("管理員工")
				.requestMatchers("/admin/service-orders/**").hasAuthority("管理服務訂單")
				.requestMatchers("/admin/activity-orders/**").hasAuthority("管理活動訂單")
				.requestMatchers("/admin/venue-orders/**").hasAuthority("管理場地訂單")
				.requestMatchers("/admin/members/**").hasAuthority("管理會員")
				.requestMatchers("/admin/frontend/**").hasAuthority("管理前台")
				
				.anyRequest().authenticated()					
			)
            .userDetailsService(userDetailsService)	
            
            	.formLogin(form -> form
            	.loginPage("/admin/login")       
            	.loginProcessingUrl("/admin/login")  
            	
            	.usernameParameter("username")	 
            	.passwordParameter("password") 
            	
            	.successHandler(successHandler)
            	
            	.failureUrl("/admin/login?error=true")      
            )
            .logout(logout -> logout
            	.logoutUrl("/admin/logout")
            	.logoutSuccessUrl("/admin/login?logout") 
            	.invalidateHttpSession(true)              
            	.deleteCookies("JSESSIONID")				 		
            )
            .rememberMe(remember -> remember
            	.key("adminSecretKeyUnique")			 		
            	.rememberMeCookieName("remember-me-admin")		
            	.tokenRepository(persistentTokenRepository())   
            	.tokenValiditySeconds(32400)					
            	.userDetailsService(userDetailsService)		
            	.rememberMeParameter("remember-me")			 	
            )
            .requestCache(cache -> cache
            	.requestCache(new HttpSessionRequestCache())		
            );
            
		return http.build();
	}
	

	@Bean
	public PersistentTokenRepository persistentTokenRepository() {
		JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
		tokenRepository.setDataSource(dataSource);
		return tokenRepository;
	}
	
}
