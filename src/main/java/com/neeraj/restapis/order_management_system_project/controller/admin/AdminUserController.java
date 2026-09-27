package com.neeraj.restapis.order_management_system_project.controller.admin;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neeraj.restapis.order_management_system_project.common.AppEnums.UserRole;
import com.neeraj.restapis.order_management_system_project.dto.UserRequestDto;
import com.neeraj.restapis.order_management_system_project.dto.UserResponseDto;
import com.neeraj.restapis.order_management_system_project.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

	private final UserService service;

	public AdminUserController(UserService service) {
		this.service = service;
	}

	// Get all users
	@GetMapping
    public ResponseEntity<List<UserResponseDto>> retrieveAllUsers() {
        return ResponseEntity.ok(service.findAllUsers());
    }
    
    // Get user by ID
	@GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> retrieveUserById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findUserById(id));
    }
	
    // Delete a user
	@DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable Long id) {
        service.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

	// Create an admin
	@PostMapping("/register")
    public ResponseEntity<UserResponseDto> registerAdmin(@Valid @RequestBody UserRequestDto adminRequest) {
        UserResponseDto savedUser = service.saveUser(adminRequest, UserRole.ADMIN);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }
	
}