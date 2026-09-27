package com.neeraj.restapis.order_management_system_project.controller.customer;

import java.net.URI;
import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.neeraj.restapis.order_management_system_project.dto.OrderRequestDto;
import com.neeraj.restapis.order_management_system_project.dto.OrderResponseDto;
import com.neeraj.restapis.order_management_system_project.service.OrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/customer/orders")
public class CustomerOrderController {

	private final OrderService service;

	public CustomerOrderController(OrderService service) {
		this.service = service;
	}

	// Retrieve orders of the authenticated customer
	@GetMapping("/user")
    public ResponseEntity<List<OrderResponseDto>> retrieveMyOrders(Principal principal) {
        return ResponseEntity.ok(service.findOrdersByUserEmail(principal.getName()));
    }

	// Retrieve order by ID for the authenticated customer
	@GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> retrieveOrderByOrderId(@PathVariable Long orderId, Principal principal) {
        return ResponseEntity.ok(service.findOrderByIdForUser(orderId, principal.getName()));
    }
	
	// Create a new order for the authenticated customer
	@PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(Principal principal,
            @Valid @RequestBody OrderRequestDto request) {
        OrderResponseDto savedOrder = service.createOrder(principal.getName(), request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(savedOrder.getId()).toUri();
        return ResponseEntity.created(location).body(savedOrder);
    }

	// Cancel an order for the authenticated customer
	@PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponseDto> cancelOrder(@PathVariable Long orderId, Principal principal) {
        return ResponseEntity.ok(service.cancelOrderForUser(orderId, principal.getName()));
    }
}