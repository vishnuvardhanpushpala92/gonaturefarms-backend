package com.gonaturefarms.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.dto.product.ProductRequest;
import com.gonaturefarms.service.ProductService;

import jakarta.validation.Valid;

/** REST controller for the product catalog. Mirrors routes/products.js. */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ApiResponse list(@RequestParam(required = false) String cat,
                             @RequestParam(required = false) String status,
                             @RequestParam(required = false) String search) {
        return productService.listProducts(cat, status, search);
    }

    @GetMapping("/categories")
    public ApiResponse categories() {
        return productService.listCategories();
    }

    @GetMapping("/{id}")
    public ApiResponse get(@PathVariable Long id) {
        return productService.getProduct(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.createProduct(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return productService.updateProduct(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse delete(@PathVariable Long id) {
        return productService.deleteProduct(id);
    }

    @PostMapping("/add-demo-variants")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse addDemoVariants() {
        return productService.addDemoVariantsToAllProducts();
    }
}
