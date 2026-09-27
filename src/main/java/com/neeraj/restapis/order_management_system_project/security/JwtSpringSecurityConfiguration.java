package com.neeraj.restapis.order_management_system_project.security;

import static org.springframework.security.config.Customizer.withDefaults;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
public class JwtSpringSecurityConfiguration {

	// Configure HTTP security, authorization, and JWT authentication
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		
		http.authorizeHttpRequests(auth -> auth
	            .requestMatchers("/customer/users/register", "/swagger-ui.html", "/payment.html").permitAll()
				.requestMatchers("/customer/**").hasAuthority("SCOPE_ROLE_USER")
				.requestMatchers("/admin/**").hasAuthority("SCOPE_ROLE_ADMIN")
				.requestMatchers("/authenticate").authenticated()
				.anyRequest().authenticated());
		http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		http.httpBasic(withDefaults());
		http.oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()));
		http.csrf(csrf -> csrf.disable());

		return http.build();
	}

	// Create BCrypt password encoder for password hashing
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	// Generate RSA key pair for signing and verifying JWT tokens
	@Bean
	public KeyPair keyPair() throws NoSuchAlgorithmException {
		var keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		return keyPairGenerator.generateKeyPair();

	}

	// Create RSA key configuration using the generated key pair
	@Bean
	public RSAKey rsaKey(KeyPair keyPair) {
		return new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
				.privateKey(keyPair.getPrivate())
				.keyID(UUID.randomUUID().toString())
				.build();
	}

	// Provide the RSA key as a JSON Web Key source
	@Bean
	public JWKSource<SecurityContext> jwkSource(RSAKey rsaKey) {
	    var jwkSet = new JWKSet(rsaKey);
	    return (jwkSelector, context) -> jwkSelector.select(jwkSet);
	}
	
	// Configure JWT decoder to verify tokens using the RSA public key
	@Bean
	public JwtDecoder jwtDecoder(RSAKey rsaKey) throws JOSEException {
		return NimbusJwtDecoder
			.withPublicKey(rsaKey.toRSAPublicKey())
			.build();
	}
	
	// Configure JWT encoder to generate signed tokens
	@Bean
	public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
		return new NimbusJwtEncoder(jwkSource) ;
	}
}