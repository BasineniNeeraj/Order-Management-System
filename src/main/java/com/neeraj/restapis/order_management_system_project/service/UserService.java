package com.neeraj.restapis.order_management_system_project.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.neeraj.restapis.order_management_system_project.common.AppEnums.UserRole;
import com.neeraj.restapis.order_management_system_project.dto.PasswordRequestDto;
import com.neeraj.restapis.order_management_system_project.dto.UserRequestDto;
import com.neeraj.restapis.order_management_system_project.dto.UserResponseDto;
import com.neeraj.restapis.order_management_system_project.dto.UserUpdateDto;
import com.neeraj.restapis.order_management_system_project.entity.User;
import com.neeraj.restapis.order_management_system_project.exception.EmailAlreadyExistsException;
import com.neeraj.restapis.order_management_system_project.exception.InvalidPasswordException;
import com.neeraj.restapis.order_management_system_project.exception.UserNotFoundException;
import com.neeraj.restapis.order_management_system_project.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	// Convert User Entity to UserResponseDto
	private UserResponseDto mapToResponseDto(User user) {
        UserResponseDto response = new UserResponseDto();
        response.setId(user.getUserId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhoneNo(user.getPhoneNo());
        response.setRole(user.getRole());
        response.setAddress(user.getAddress());
        return response;
    }

	// Get all users - Admin
	public List<UserResponseDto> findAllUsers() {
		List<User> users = userRepository.findAll();
		List<UserResponseDto> userResponseDtos = new ArrayList<>();
		for (User user : users) {
			UserResponseDto response = mapToResponseDto(user);
			userResponseDtos.add(response);
		}
		return userResponseDtos;
	}

	// Get user by ID - Admin
	public UserResponseDto findUserById(Long id) {
		User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found."));
		return mapToResponseDto(user);
	}

	// Delete user - Admin
	public void deleteUserById(Long id) {
		if (!userRepository.existsById(id)) {
			throw new UserNotFoundException("User with id " + id + " not found.");
		}
		userRepository.deleteById(id);
	}

	// Create user - Admin and Customer
    public UserResponseDto saveUser(UserRequestDto request, UserRole role) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists: " + request.getEmail());
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNo(request.getPhoneNo());
        user.setRole(role);
        user.setAddress(request.getAddress());

        User savedUser = userRepository.save(user);
        return mapToResponseDto(savedUser);
    }

	// Get user by email - Customer
	public UserResponseDto findUserByEmail(String email) {
		User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email " + email + " not found."));
		return mapToResponseDto(user);
	}

	// Update user - Customer
	public UserResponseDto updateUserByEmail(String email, UserUpdateDto request) {
		User existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email " + email + " not found."));
		// Authenticated user's email must match request email
		if (!existingUser.getEmail().equalsIgnoreCase(request.getEmail())) {
			throw new IllegalArgumentException("Authenticated user email and request email do not match.");
		}
		existingUser.setName(request.getName());
		existingUser.setPhoneNo(request.getPhoneNo());
		existingUser.setAddress(request.getAddress());
		return mapToResponseDto(existingUser);
	}

	// Update password - Customer
	public UserResponseDto updateUserPasswordByEmail(String email, PasswordRequestDto passwordRequest) {
		User existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email " + email + " not found."));
		// Verify current password before permitting update
        if (!passwordEncoder.matches(passwordRequest.getCurrentPassword(), existingUser.getPassword())) {
            throw new InvalidPasswordException("Current password provided is incorrect.");
        }
		existingUser.setPassword(passwordEncoder.encode(passwordRequest.getNewPassword()));
		return mapToResponseDto(existingUser);
	}

	// Delete user - Customer
	public void deleteUserByEmail(String email) {
		User existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email " + email + " not found."));
		userRepository.delete(existingUser);
	}

}