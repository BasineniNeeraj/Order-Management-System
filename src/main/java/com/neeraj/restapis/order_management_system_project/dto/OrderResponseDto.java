package com.neeraj.restapis.order_management_system_project.dto;

import java.math.BigDecimal;
import java.util.List;

import com.neeraj.restapis.order_management_system_project.common.AppEnums.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDto {

    private Long id;
    private Long userId;
    private String userName;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private List<OrderItemResponseDto> items;
}