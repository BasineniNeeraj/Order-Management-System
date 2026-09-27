package com.neeraj.restapis.order_management_system_project.security;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.neeraj.restapis.order_management_system_project.entity.User;
import com.neeraj.restapis.order_management_system_project.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    // Load user details by email for Spring Security authentication
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		Optional<User> foundUser = userRepository.findByEmail(email);
		if (foundUser.isEmpty()) {
			throw new UsernameNotFoundException("Invalid email or password");
		}
		
		User user = foundUser.get();
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())// Adds "ROLE_" prefix automatically (e.g. ROLE_USER, ROLE_ADMIN)
                .build();
    }
    
}