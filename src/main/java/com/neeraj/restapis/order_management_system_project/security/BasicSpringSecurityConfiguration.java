package com.neeraj.restapis.order_management_system_project.security;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

//@Configuration
public class BasicSpringSecurityConfiguration {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) {
		
		 // Every request must be authenticated
        http.authorizeHttpRequests(auth -> auth
        	.requestMatchers("/customer/users/v1/register").permitAll()
        	.requestMatchers("/admin/users/v1/admin").permitAll()
            .anyRequest().authenticated()
        );

        // Enable HTTP Basic authentication
        http.httpBasic(withDefaults());
        http.sessionManagement(session -> session
        		.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        	);
        // Disable CSRF for REST API PPST,PUT testing
        http.csrf(csrf -> csrf.disable());

        return http.build();
	}
	
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}