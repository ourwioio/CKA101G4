package com; 
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class PasswordEncoderConfig {

    // 🏆 1. 密碼加密器 Bean (供 Controller 進行 @Autowired)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 🛡️ 2. 全站放行設定，徹底把預設的藍色登入畫面擊退！
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 關閉 CSRF 保護
            .csrf(csrf -> csrf.disable())
            
            // 允許所有 HTTP 請求完全通行，解除 Spring Security 的預設全站封鎖
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            
            // 徹底關閉 Spring Security 內建的預設登入與 HTTP Basic 畫面
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());

        return http.build();
    }
}