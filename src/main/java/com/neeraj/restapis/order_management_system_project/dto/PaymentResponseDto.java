package com.neeraj.restapis.order_management_system_project.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.neeraj.restapis.order_management_system_project.common.AppEnums.PaymentMethod;
import com.neeraj.restapis.order_management_system_project.common.AppEnums.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDto {

	private Long paymentId;
    private Long orderId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private LocalDateTime paymentDate;

}