package com.neeraj.restapis.order_management_system_project.security;

import java.time.Instant;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

record JwtResponse(String token) {}

@RestController
public class JwtAuthenticationResource {

	private final JwtEncoder jwtEncoder;
		
	public JwtAuthenticationResource(JwtEncoder jwtEncoder) {
		this.jwtEncoder = jwtEncoder;
	}

//	// basic authentication
//	@PostMapping("/authenticate")
//	public Authentication authenticate(Authentication authentication) {
//		return authentication;
//	}
	
	// Authenticate the user and return a JWT token
	@PostMapping("/authenticate")
	public ResponseEntity<JwtResponse> authenticate(Authentication authentication) {
        return ResponseEntity.ok(new JwtResponse(createToken(authentication)));
    }	

	// Create a JWT token with issuer, timestamps, subject, and authorities
	private String createToken(Authentication authentication) {
		JwtClaimsSet jwtClaimsSet = JwtClaimsSet .builder()
		.issuer("self")
		.issuedAt(Instant.now())
		.expiresAt(Instant.now().plusSeconds(60*15))
		.subject(authentication.getName())
		.claim("scope",createScope(authentication))
		.build();
				
		JwtEncoderParameters parameters = JwtEncoderParameters.from(jwtClaimsSet);
		return jwtEncoder.encode(parameters).getTokenValue();
	}

	// Extract the user's authorities and combine them into a scope string
	private String createScope(Authentication authentication) {
		String scope="";
		for(var authority: authentication.getAuthorities()) {
			scope += authority.getAuthority() + " ";
		}
		return scope.trim();
	}
}