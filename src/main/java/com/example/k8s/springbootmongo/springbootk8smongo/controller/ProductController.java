package com.example.k8s.springbootmongo.springbootk8smongo.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.k8s.springbootmongo.springbootk8smongo.entity.Product;
import com.example.k8s.springbootmongo.springbootk8smongo.repository.ProductRepository;

@RestController
public class ProductController {
	
	@Autowired
	private ProductRepository productRepository;
	
	@PostMapping("/addProduct")
	public String saveProduct(@RequestBody Product product) {
		productRepository.save(product);
		return "Product added successfully::"+product.getId();
		
	}
	
	@GetMapping("/findAllProducts")
	public List<Product> getProducts() {
		return productRepository.findAll();
	}
}
