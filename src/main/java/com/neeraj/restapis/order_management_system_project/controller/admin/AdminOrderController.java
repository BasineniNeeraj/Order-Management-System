package com.neeraj.restapis.order_management_system_project.controller.admin;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neeraj.restapis.order_management_system_project.dto.OrderResponseDto;
import com.neeraj.restapis.order_management_system_project.dto.UpdateOrderStatusRequestDto;
import com.neeraj.restapis.order_management_system_project.service.OrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final OrderService service;

    public AdminOrderController(OrderService service) {
        this.service = service;
    }

    // Retrieve all orders
    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> retrieveAllOrders() {
        return ResponseEntity.ok(service.findAllOrders());
    }

    // Retrieve order by ID
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> retrieveOrderByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(service.findOrderById(orderId));
    }
    
    // Retrieve orders by User ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponseDto>> retrieveOrdersByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(service.findOrdersByUserId(userId));
    }
    
    // Update order status
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(@PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequestDto request) {
        OrderResponseDto updatedOrder = service.updateOrderStatus(orderId, request.getStatus());
        return ResponseEntity.ok(updatedOrder);
    }
    
}