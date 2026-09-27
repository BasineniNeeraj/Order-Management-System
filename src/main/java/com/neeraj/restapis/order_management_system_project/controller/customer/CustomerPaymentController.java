package com.neeraj.restapis.order_management_system_project.controller.customer;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neeraj.restapis.order_management_system_project.dto.PaymentRequestDto;
import com.neeraj.restapis.order_management_system_project.dto.PaymentResponseDto;
import com.neeraj.restapis.order_management_system_project.dto.PaymentVerificationRequestDto;
import com.neeraj.restapis.order_management_system_project.service.PaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/customer/payments")
public class CustomerPaymentController {

	private final PaymentService service;

	public CustomerPaymentController(PaymentService service) {
		this.service = service;
	}
	
	// Creates a payment for the specified order belonging to the authenticated customer.
	@PostMapping("/orders/{orderId}")
    public ResponseEntity<PaymentResponseDto> createPayment(Principal principal, @PathVariable Long orderId,
            @Valid @RequestBody PaymentRequestDto request) {
        PaymentResponseDto payment = service.createPayment(principal.getName(), orderId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

	// Verifies the Razorpay payment signature and updates the payment status.
	@PostMapping("/verify")
    public ResponseEntity<PaymentResponseDto> verifyPayment(Principal principal,
            @Valid @RequestBody PaymentVerificationRequestDto request) {
        PaymentResponseDto verifiedPayment = service.verifyPayment(principal.getName(), request);
        return ResponseEntity.ok(verifiedPayment);
    }

	// Retrieves a payment by ID for the authenticated customer.
	@GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponseDto> retrievePayment(Principal principal, @PathVariable Long paymentId) {
        return ResponseEntity.ok(service.findPaymentByIdForUser(principal.getName(), paymentId));
    }
}