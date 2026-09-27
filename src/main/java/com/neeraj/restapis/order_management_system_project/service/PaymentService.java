package com.neeraj.restapis.order_management_system_project.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neeraj.restapis.order_management_system_project.common.AppEnums.OrderStatus;
import com.neeraj.restapis.order_management_system_project.common.AppEnums.PaymentStatus;
import com.neeraj.restapis.order_management_system_project.dto.PaymentRequestDto;
import com.neeraj.restapis.order_management_system_project.dto.PaymentResponseDto;
import com.neeraj.restapis.order_management_system_project.dto.PaymentVerificationRequestDto;
import com.neeraj.restapis.order_management_system_project.entity.Order;
import com.neeraj.restapis.order_management_system_project.entity.Payment;
import com.neeraj.restapis.order_management_system_project.exception.OrderNotFoundException;
import com.neeraj.restapis.order_management_system_project.exception.PaymentNotFoundException;
import com.neeraj.restapis.order_management_system_project.exception.PaymentVerificationException;
import com.neeraj.restapis.order_management_system_project.repository.OrderRepository;
import com.neeraj.restapis.order_management_system_project.repository.PaymentRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;

@Service
@Transactional
public class PaymentService {

	private final PaymentRepository paymentRepository;
	private final OrderRepository orderRepository;
	private final RazorpayClient razorpayClient;

	@Value("${razorpay.key.secret}")
	private String razorpayKeySecret;

	public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository,
			RazorpayClient razorpayClient) {
		this.paymentRepository = paymentRepository;
		this.orderRepository = orderRepository;
		this.razorpayClient = razorpayClient;
	}

	// Converts a Payment entity into a PaymentResponseDto.
	private PaymentResponseDto mapToResponseDto(Payment payment) {
        PaymentResponseDto response = new PaymentResponseDto();
        response.setPaymentId(payment.getPaymentId());
        response.setOrderId(payment.getOrder().getOrderId());
        response.setAmount(payment.getAmount());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setPaymentStatus(payment.getPaymentStatus());
        response.setRazorpayOrderId(payment.getRazorpayOrderId());
        response.setRazorpayPaymentId(payment.getRazorpayPaymentId());
        response.setPaymentDate(payment.getPaymentDate());
        return response;
    }
	
	// Creates a Razorpay order and saves a pending payment for the specified customer order.
	public PaymentResponseDto createPayment(String email, Long orderId, PaymentRequestDto request) {
		Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
		if (!order.getUser().getEmail().equalsIgnoreCase(email)) {
			throw new AccessDeniedException("You cannot make payment for this order");
		}
		if (order.getStatus() != OrderStatus.CREATED) {
			throw new IllegalStateException("Payment can only be initiated for an order in CREATED status");
		}
		// 2. Check whether a payment already exists for this order
		Optional<Payment> existingPaymentOpt = paymentRepository.findByOrder(order);
        if (existingPaymentOpt.isPresent()) {
            Payment existing = existingPaymentOpt.get();
            if (existing.getPaymentStatus() == PaymentStatus.SUCCESS || existing.getPaymentStatus() == PaymentStatus.PENDING) {
                return mapToResponseDto(existing);
            }
            // If previous attempt was FAILED, reuse and update the existing record
        }
		BigDecimal amount = order.getTotalAmount();
		long amountInPaise = amount.multiply(BigDecimal.valueOf(100)).longValue();

		JSONObject orderRequest = new JSONObject();
		orderRequest.put("amount", amountInPaise);
		orderRequest.put("currency", "INR");
		orderRequest.put("receipt", "order_" + order.getOrderId());

		try {

			com.razorpay.Order razorpayOrder = razorpayClient.orders.create(orderRequest);
			String razorpayOrderId = razorpayOrder.get("id");

			Payment payment = new Payment();
			payment.setOrder(order);
			payment.setAmount(amount);
			payment.setPaymentMethod(request.getPaymentMethod());
			payment.setPaymentStatus(PaymentStatus.PENDING);
			payment.setRazorpayOrderId(razorpayOrderId);
			payment.setRazorpayPaymentId(null);
            payment.setPaymentDate(null);

			Payment savedPayment = paymentRepository.save(payment);
			return mapToResponseDto(savedPayment);

		} catch (RazorpayException e) {
			throw new IllegalStateException("Unable to initialize gateway order: " + e.getMessage(), e);
		}
	}

	// Verifies the Razorpay payment signature and updates the payment and order statuses.
	@Transactional(noRollbackFor = PaymentVerificationException.class)
	public PaymentResponseDto verifyPayment(String email, PaymentVerificationRequestDto request) {
		Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new PaymentVerificationException(
                        "Payment not found for Razorpay order ID: " + request.getRazorpayOrderId()));
		Order order = payment.getOrder();

		if (!order.getUser().getEmail().equalsIgnoreCase(email)) {
			throw new AccessDeniedException("You cannot verify payment for this order");
		}
		if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
			return mapToResponseDto(payment);
		}

		// Important: Use the Razorpay order ID stored in our database.
		JSONObject verificationRequest = new JSONObject();
		verificationRequest.put("razorpay_order_id", payment.getRazorpayOrderId());
		verificationRequest.put("razorpay_payment_id", request.getRazorpayPaymentId());
		verificationRequest.put("razorpay_signature", request.getRazorpaySignature());

		try {

			boolean verified = Utils.verifyPaymentSignature(verificationRequest, razorpayKeySecret);
			if (!verified) {
				payment.setPaymentStatus(PaymentStatus.FAILED);
				throw new PaymentVerificationException("Payment signature verification failed");
			}
			payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
			payment.setPaymentStatus(PaymentStatus.SUCCESS);
			payment.setPaymentDate(LocalDateTime.now());

			order.setStatus(OrderStatus.PAID);
			return mapToResponseDto(payment);

		} catch (RazorpayException e) {
			payment.setPaymentStatus(PaymentStatus.FAILED);
			throw new PaymentVerificationException("Unable to verify Razorpay payment");
		}
	}

	// Retrieves a payment by ID after verifying that it belongs to the specified customer.
	@Transactional(readOnly = true)
	public PaymentResponseDto findPaymentByIdForUser(String email, Long paymentId) {
		Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + paymentId));
		if (!payment.getOrder().getUser().getEmail().equalsIgnoreCase(email)) {
			throw new IllegalStateException("You cannot access this payment");
		}
		return mapToResponseDto(payment);
	}

	// Retrieves a payment by ID.
	@Transactional(readOnly = true)
	public PaymentResponseDto findPaymentById(Long paymentId) {
		Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + paymentId));
        return mapToResponseDto(payment);
	}

	// Retrieves all payments and converts them into response DTOs.
	@Transactional(readOnly = true)
	public java.util.List<PaymentResponseDto> findAllPayments() {
		List<Payment> payments = paymentRepository.findAll();
		List<PaymentResponseDto> responseList = new java.util.ArrayList<>();
		for (Payment payment : payments) {
			responseList.add(mapToResponseDto(payment));
		}
		return responseList;
	}

}