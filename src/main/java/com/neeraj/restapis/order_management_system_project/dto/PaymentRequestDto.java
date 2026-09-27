package com.neeraj.restapis.order_management_system_project.dto;

import com.neeraj.restapis.order_management_system_project.common.AppEnums.PaymentMethod;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDto {

	@NotNull(message = "Payment method is required")
	private PaymentMethod paymentMethod;

}