package com.neeraj.restapis.order_management_system_project.common;

public class AppEnums {

	// Converts the input string to a UserRole enum value.
	public enum UserRole {

		ADMIN, USER;
	}

	// Converts the input string to an OrderStatus enum value.
	public enum OrderStatus {
		CREATED {
			@Override
			public boolean canTransitionTo(OrderStatus next) {
				return next == PAID || next == CANCELLED;
			}
		},
		PAID {
			@Override
			public boolean canTransitionTo(OrderStatus next) {
				return next == SHIPPED || next == CANCELLED;
			}
		},
		SHIPPED {
			@Override
			public boolean canTransitionTo(OrderStatus next) {
				return next == DELIVERED;
			}
		},
		DELIVERED, CANCELLED;

		public boolean canTransitionTo(OrderStatus next) {
			return false;
		}
	}

	// Converts the input string to a PaymentStatus enum value.
	public enum PaymentStatus {

		PENDING, SUCCESS, FAILED;

	}

	// Converts the input string to a PaymentMethod enum value.
	public enum PaymentMethod {

		UPI, CARD;

	}
}