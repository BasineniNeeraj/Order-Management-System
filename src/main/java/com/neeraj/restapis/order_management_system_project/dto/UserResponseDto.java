package com.neeraj.restapis.order_management_system_project.dto;

import com.neeraj.restapis.order_management_system_project.common.AppEnums.UserRole;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
	
    private Long id;
    private String name;
    private String email;
    private String phoneNo;
    private UserRole role;
    private String address;
    
}