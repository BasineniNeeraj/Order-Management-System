package com.neeraj.restapis.order_management_system_project.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.neeraj.restapis.order_management_system_project.dto.ProductRequestDto;
import com.neeraj.restapis.order_management_system_project.dto.ProductResponseDto;
import com.neeraj.restapis.order_management_system_project.entity.Product;
import com.neeraj.restapis.order_management_system_project.exception.ProductAlreadyExistsException;
import com.neeraj.restapis.order_management_system_project.exception.ProductNotFoundException;
import com.neeraj.restapis.order_management_system_project.repository.ProductRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ProductService {

	private final ProductRepository productRepository;

	public ProductService(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	// Convert Product Entity to ProductResponseDto
	private ProductResponseDto mapToResponseDto(Product product) {
        ProductResponseDto response = new ProductResponseDto();
        response.setId(product.getProductId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setQuantity(product.getQuantity());
        response.setCategory(product.getCategory());
        response.setActive(product.getActive());
        return response;
    }
	
	// Get all products - Admin
	public List<ProductResponseDto> findAllProducts() {
		List<Product> products = productRepository.findAll();
		List<ProductResponseDto> responseList = new ArrayList<>();
		for (Product product : products) {
			ProductResponseDto response = mapToResponseDto(product);
			responseList.add(response);
		}
		return responseList;
	}

	// Get all active products - Customer
	public List<ProductResponseDto> findAllActiveProducts() {
        List<Product> products = productRepository.findByActiveTrue();
		List<ProductResponseDto> responseList = new ArrayList<>();
		for (Product product : products) {
			ProductResponseDto response = mapToResponseDto(product);
			responseList.add(response);
		}
		return responseList;
    }
	
	// Get any Product By Id - Admin
	public ProductResponseDto findProductById(Long id) {
		Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product with Id " + id + " not found"));
		return mapToResponseDto(product);
	}
	
	// Retrieve active product by ID - Customer
    public ProductResponseDto findActiveProductById(Long id) {
        Product product = productRepository.findByProductIdAndActiveTrue(id)
                .orElseThrow(() -> new ProductNotFoundException("Product with Id " + id + " not found"));
        return mapToResponseDto(product);
    }

	// Create a product - Admin
	public ProductResponseDto createProduct(ProductRequestDto productRequest) {
		if (productRepository.existsByNameIgnoreCase(productRequest.getName())) {
			throw new ProductAlreadyExistsException("Product already exists with name: " + productRequest.getName());
		}
		
		Product product = new Product();
		product.setName(productRequest.getName());
		product.setDescription(productRequest.getDescription());
		product.setPrice(productRequest.getPrice());
		product.setQuantity(productRequest.getQuantity());
		product.setCategory(productRequest.getCategory());
		product.setActive(productRequest.getActive());
		
		Product savedProduct = productRepository.save(product);
		return mapToResponseDto(savedProduct);
	}

	// Update a product - Admin
	public ProductResponseDto updateProductById(Long id, ProductRequestDto productRequest) {
		Product existingProduct = productRepository.findByProductIdAndActiveTrue(id)
                .orElseThrow(() -> new ProductNotFoundException("Product with Id " + id + " not found"));

		// Check name collision only when name is modified
		if (!existingProduct.getName().equalsIgnoreCase(productRequest.getName())) {
			if (productRepository.existsByNameIgnoreCase(productRequest.getName())) {
				throw new ProductAlreadyExistsException("Product with this name already exists");
			}
			existingProduct.setName(productRequest.getName());
		}
		existingProduct.setDescription(productRequest.getDescription());
		existingProduct.setPrice(productRequest.getPrice());
		existingProduct.setQuantity(productRequest.getQuantity());
		existingProduct.setCategory(productRequest.getCategory());
		existingProduct.setActive(productRequest.getActive());
		return mapToResponseDto(existingProduct);
	}

	// Delete a product - Admin
	public void deleteProductById(Long id) {
		Product existingProduct = productRepository.findByProductIdAndActiveTrue(id)
                .orElseThrow(() -> new ProductNotFoundException("Product with Id " + id + " not found"));
		existingProduct.setActive(false);
	}

}