package com.neeraj.restapis.order_management_system_project.controller.admin;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neeraj.restapis.order_management_system_project.dto.PaymentResponseDto;
import com.neeraj.restapis.order_management_system_project.service.PaymentService;

@RestController
@RequestMapping("/admin/payments")
public class AdminPaymentController {

	private final PaymentService service;

	public AdminPaymentController(PaymentService service) {
		this.service = service;
	}

	// Retrieves all payment records.
	@GetMapping
    public ResponseEntity<List<PaymentResponseDto>> retrieveAllPayments() {
        return ResponseEntity.ok(service.findAllPayments());
    }

	// Retrieves a payment by its payment ID.
	@GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponseDto> retrievePayment(@PathVariable Long paymentId) {
        return ResponseEntity.ok(service.findPaymentById(paymentId));
    }
}