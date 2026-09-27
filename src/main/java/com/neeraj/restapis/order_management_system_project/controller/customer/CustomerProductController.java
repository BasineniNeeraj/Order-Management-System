package com.neeraj.restapis.order_management_system_project.controller.customer;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neeraj.restapis.order_management_system_project.dto.ProductResponseDto;
import com.neeraj.restapis.order_management_system_project.service.ProductService;

@RestController
@RequestMapping("/customer/products")
public class CustomerProductController {

    private final ProductService service; 

    public CustomerProductController(ProductService service) {
        this.service = service;
    }

    // Get all products
    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> retrieveAllProducts() {
        return ResponseEntity.ok(service.findAllActiveProducts());
    }

    // Get product by Id
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> retrieveProductById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findActiveProductById(id));
    }
    
}