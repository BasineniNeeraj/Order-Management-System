package com.neeraj.restapis.order_management_system_project.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neeraj.restapis.order_management_system_project.common.AppEnums.OrderStatus;
import com.neeraj.restapis.order_management_system_project.dto.OrderItemRequestDto;
import com.neeraj.restapis.order_management_system_project.dto.OrderItemResponseDto;
import com.neeraj.restapis.order_management_system_project.dto.OrderRequestDto;
import com.neeraj.restapis.order_management_system_project.dto.OrderResponseDto;
import com.neeraj.restapis.order_management_system_project.entity.Order;
import com.neeraj.restapis.order_management_system_project.entity.OrderItem;
import com.neeraj.restapis.order_management_system_project.entity.Product;
import com.neeraj.restapis.order_management_system_project.entity.User;
import com.neeraj.restapis.order_management_system_project.exception.OrderNotFoundException;
import com.neeraj.restapis.order_management_system_project.exception.ProductNotFoundException;
import com.neeraj.restapis.order_management_system_project.exception.UserNotFoundException;
import com.neeraj.restapis.order_management_system_project.repository.OrderRepository;
import com.neeraj.restapis.order_management_system_project.repository.ProductRepository;
import com.neeraj.restapis.order_management_system_project.repository.UserRepository;

@Service
@Transactional
public class OrderService {

	private final OrderRepository orderRepository;
	private final UserRepository userRepository;
	private final ProductRepository productRepository;

	public OrderService(OrderRepository orderRepository, UserRepository userRepository,
			ProductRepository productRepository) {
		this.orderRepository = orderRepository;
		this.userRepository = userRepository;
		this.productRepository = productRepository;
	}

	// Helper method to convert Order Entity to OrderResponseDto
	private OrderResponseDto mapToResponseDto(Order order) {
		List<OrderItemResponseDto> itemResponses = new ArrayList<>();

		for (OrderItem orderItem : order.getOrderItems()) {
			BigDecimal subTotal = orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()));
			OrderItemResponseDto itemResponse = new OrderItemResponseDto();
			itemResponse.setId(orderItem.getOrderItemId());
			itemResponse.setProductId(orderItem.getProduct().getProductId());
			itemResponse.setProductName(orderItem.getProduct().getName());
			itemResponse.setQuantity(orderItem.getQuantity());
			itemResponse.setPrice(orderItem.getPrice());
			itemResponse.setSubTotal(subTotal);

			itemResponses.add(itemResponse);
		}

		OrderResponseDto response = new OrderResponseDto();
		response.setId(order.getOrderId());
		response.setUserId(order.getUser().getUserId());
		response.setUserName(order.getUser().getName());
		response.setTotalAmount(order.getTotalAmount());
		response.setStatus(order.getStatus());
		response.setItems(itemResponses);
		return response;
	}

	// Retrieve all orders
	@Transactional(readOnly = true)
	public List<OrderResponseDto> findAllOrders() {
		List<Order> orders = orderRepository.findAllWithItems();
		List<OrderResponseDto> responseList = new ArrayList<>();
		for (Order order : orders) {
			OrderResponseDto response = mapToResponseDto(order);
			responseList.add(response);
		}
		return responseList;
	}

	// Retrieve order by ID
	@Transactional(readOnly = true)
	public OrderResponseDto findOrderById(Long orderId) {
		Order order = orderRepository.findByIdWithItems(orderId)
				.orElseThrow(() -> new OrderNotFoundException("Order with Id " + orderId + " not found"));
		return mapToResponseDto(order);
	}

	// Retrieve orders by User ID
	@Transactional(readOnly = true)
	public List<OrderResponseDto> findOrdersByUserId(Long userId) {
		if (!userRepository.existsById(userId)) {
			throw new UserNotFoundException("User not found with id: " + userId);
		}
		List<Order> userOrders = orderRepository.findByUserIdWithItems(userId);
		List<OrderResponseDto> responseList = new ArrayList<>();
		for (Order order : userOrders) {
			OrderResponseDto response = mapToResponseDto(order);
			responseList.add(response);
		}
		return responseList;
	}

	// Retrieve orders by customer email
	public List<OrderResponseDto> findOrdersByUserEmail(String email) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
		List<Order> userOrders = orderRepository.findByUserEmailWithItems(user.getEmail());
		List<OrderResponseDto> responseList = new ArrayList<>();
		for (Order order : userOrders) {
			responseList.add(mapToResponseDto(order));
		}
		return responseList;
	}

	// Retrieve order by ID after verifying customer ownership
	public OrderResponseDto findOrderByIdForUser(Long orderId, String email) {
		Order order = orderRepository.findByIdWithItems(orderId)
				.orElseThrow(() -> new OrderNotFoundException("Order with Id " + orderId + " not found"));
		if (!order.getUser().getEmail().equals(email)) {
			throw new AccessDeniedException("You cannot access this order");
		}
		return mapToResponseDto(order);
	}

	// Create new order with inventory check and stock deduction
	public OrderResponseDto createOrder(String email, OrderRequestDto request) {
		// Find User
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
		// Validate Order Items
		if (request.getOrderItems() == null || request.getOrderItems().isEmpty()) {
			throw new IllegalArgumentException("Order must contain at least one item");
		}

		Order order = new Order();
		order.setUser(user);
		order.setStatus(OrderStatus.CREATED);

		BigDecimal totalAmount = BigDecimal.ZERO;
		// Process every item/product
		for (OrderItemRequestDto itemRequest : request.getOrderItems()) {
			Product product = productRepository.findById(itemRequest.getProductId()).orElseThrow(
					() -> new ProductNotFoundException("Product not found with id: " + itemRequest.getProductId()));
			// Validate stock availability
			if (!product.getActive()) {
				throw new IllegalStateException("Product is not available: " + product.getName());
			}
			if (product.getQuantity() < itemRequest.getQuantity()) {
				throw new IllegalStateException("Insufficient stock for product: " + product.getName());
			}
			// Decrement inventory stock
			product.setQuantity(product.getQuantity() - itemRequest.getQuantity());
			// Create OrderItem
			OrderItem orderItem = new OrderItem();
			orderItem.setProduct(product);
			orderItem.setQuantity(itemRequest.getQuantity());
			orderItem.setPrice(product.getPrice());
			// Calculate Subtotal and add subtotal to total
			BigDecimal subTotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
			totalAmount = totalAmount.add(subTotal);
			// Add OrderItem to Order
			order.addOrderItem(orderItem);
		}
		// Set final total
		order.setTotalAmount(totalAmount);
		Order savedOrder = orderRepository.save(order);
		return mapToResponseDto(savedOrder);
	}

	// Update order status with strict lifecycle validation and stock restoration
	public OrderResponseDto updateOrderStatus(Long orderId, OrderStatus newStatus) {
		Order order = orderRepository.findByIdWithItems(orderId)
				.orElseThrow(() -> new OrderNotFoundException("Order with Id " + orderId + " not found"));
		OrderStatus currentStatus = order.getStatus();

		// Prevent modification of completed or cancelled orders
		if (currentStatus == OrderStatus.CANCELLED || currentStatus == OrderStatus.DELIVERED) {
			throw new IllegalStateException("Order cannot be modified further once " + currentStatus);
		}
		// No change required
		if (currentStatus == newStatus) {
			return mapToResponseDto(order);
		}

		boolean isValidTransition = false;

		switch (currentStatus) {
		case CREATED:
			if (newStatus == OrderStatus.PAID || newStatus == OrderStatus.CANCELLED) {
				isValidTransition = true;
			}
			break;
		case PAID:
			if (newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.CANCELLED) {
				isValidTransition = true;
			}
			break;
		case SHIPPED:
			if (newStatus == OrderStatus.DELIVERED) {
				isValidTransition = true;
			}
			break;
		default:
			isValidTransition = false;
			break;
		}

		// Reject invalid transitions
		if (!isValidTransition) {
			throw new IllegalStateException(
					"Invalid order status transition from " + currentStatus + " to " + newStatus);
		}

		// Restore stock only when cancelling an unpaid order
		if (newStatus == OrderStatus.CANCELLED) {
			for (OrderItem item : order.getOrderItems()) {
				Product product = item.getProduct();
				product.setQuantity(product.getQuantity() + item.getQuantity());
			}
		}

		order.setStatus(newStatus);
		return mapToResponseDto(order);
	}

	// Cancel an order after verifying customer ownership
	public OrderResponseDto cancelOrderForUser(Long orderId, String email) {
		Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order with Id " + orderId + " not found"));

        if (!order.getUser().getEmail().equalsIgnoreCase(email)) {
            throw new AccessDeniedException("You cannot cancel this order");
        }
		// Allow cancellation only before payment
		if (order.getStatus() != OrderStatus.CREATED) {
			throw new IllegalStateException("Only orders with CREATED status can be cancelled");
		}
		return updateOrderStatus(orderId, OrderStatus.CANCELLED);
	}
}