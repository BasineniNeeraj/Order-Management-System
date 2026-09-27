package com.neeraj.restapis.order_management_system_project.controller.customer;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neeraj.restapis.order_management_system_project.common.AppEnums.UserRole;
import com.neeraj.restapis.order_management_system_project.dto.PasswordRequestDto;
import com.neeraj.restapis.order_management_system_project.dto.UserRequestDto;
import com.neeraj.restapis.order_management_system_project.dto.UserResponseDto;
import com.neeraj.restapis.order_management_system_project.dto.UserUpdateDto;
import com.neeraj.restapis.order_management_system_project.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/customer/users")
public class CustomerUserController {

	private final UserService service;

	public CustomerUserController(UserService service) {
		this.service = service;
	}

	// Register a customer
	@PostMapping("/register")
    public ResponseEntity<UserResponseDto> registerCustomer(@Valid @RequestBody UserRequestDto userRequest) {
        UserResponseDto savedUser = service.saveUser(userRequest, UserRole.USER);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

	// Get customer profile
	@GetMapping("/profile")
    public ResponseEntity<UserResponseDto> retrieveMyProfile(Principal principal) {
        return ResponseEntity.ok(service.findUserByEmail(principal.getName()));
    }
	
	// Update customer profile
	@PutMapping("/profile")
    public ResponseEntity<UserResponseDto> updateMyProfile(Principal principal, @Valid @RequestBody UserUpdateDto userRequest) {
        return ResponseEntity.ok(service.updateUserByEmail(principal.getName(), userRequest));
    }
	// Change customer password
	@PatchMapping("/profile/password")
    public ResponseEntity<UserResponseDto> changeMyPassword(Principal principal,
            @Valid @RequestBody PasswordRequestDto passwordRequest) {
        return ResponseEntity.ok(service.updateUserPasswordByEmail(principal.getName(), passwordRequest));
    }
	
	// Delete customer
	@DeleteMapping("/profile")
    public ResponseEntity<Void> deleteMyAccount(Principal principal) {
        service.deleteUserByEmail(principal.getName());
        return ResponseEntity.noContent().build();
    }
	
}