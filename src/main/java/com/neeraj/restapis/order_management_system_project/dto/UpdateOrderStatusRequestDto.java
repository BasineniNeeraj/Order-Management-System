package com.neeraj.restapis.order_management_system_project.dto;

import com.neeraj.restapis.order_management_system_project.common.AppEnums.OrderStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusRequestDto {

    @NotNull(message = "Order status cannot be null")
    private OrderStatus status;

}