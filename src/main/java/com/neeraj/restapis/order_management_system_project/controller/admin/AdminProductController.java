package com.neeraj.restapis.order_management_system_project.controller.admin;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neeraj.restapis.order_management_system_project.dto.ProductRequestDto;
import com.neeraj.restapis.order_management_system_project.dto.ProductResponseDto;
import com.neeraj.restapis.order_management_system_project.service.ProductService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin/products")
public class AdminProductController {

	private final ProductService service;

	public AdminProductController(ProductService service) {
		this.service = service;
	}

	// Get all products
	@GetMapping
	public ResponseEntity<List<ProductResponseDto>> retrieveAllProducts() {
		return ResponseEntity.ok(service.findAllProducts());
	}

	// Get product by Id
	@GetMapping("/{id}")
	public ResponseEntity<ProductResponseDto> retrieveProductById(@PathVariable Long id) {
		return ResponseEntity.ok(service.findProductById(id));
	}

	// Create a product
	@PostMapping
	public ResponseEntity<ProductResponseDto> createProduct(@Valid @RequestBody ProductRequestDto productRequest) {
		ProductResponseDto savedProduct = service.createProduct(productRequest);
		return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
	}

	// Update a product
	@PutMapping("/{id}")
	public ResponseEntity<ProductResponseDto> updateProductById(@PathVariable Long id, @Valid @RequestBody ProductRequestDto productRequest) {
		return ResponseEntity.ok(service.updateProductById(id, productRequest));
	}

	// Delete a product
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteProductById(@PathVariable Long id) {
		service.deleteProductById(id);
		return ResponseEntity.noContent().build();
	}
}