package com.gonaturefarms.service;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.dto.product.ProductRequest;
import com.gonaturefarms.entity.Product;
import com.gonaturefarms.exception.ApiException;
import com.gonaturefarms.exception.ResourceNotFoundException;
import com.gonaturefarms.repository.CategoryRepository;
import com.gonaturefarms.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Business logic for browsing and (admin) managing products. Mirrors routes/products.js. */
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public ApiResponse listProducts(String cat, String status, String search) {
        Specification<Product> spec = buildSpecification(cat, status, search);
        List<Product> products = productRepository.findAll(
                spec, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
        return ApiResponse.ok().with("products", products);
    }

    private Specification<Product> buildSpecification(String cat, String status, String search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (cat != null && !cat.isBlank() && !cat.equals("All")) {
                predicates.add(cb.equal(root.get("cat"), cat));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), Product.ProductStatus.valueOf(status)));
            }
            if (search != null && !search.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional(readOnly = true)
    public ApiResponse listCategories() {
        List<String> names = categoryRepository.findAllByOrderByNameAsc()
                .stream().map(com.gonaturefarms.entity.Category::getName).collect(Collectors.toList());
        return ApiResponse.ok().with("categories", names);
    }

    @Transactional(readOnly = true)
    public ApiResponse getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return ApiResponse.ok().with("product", product);
    }

    @Transactional
    public ApiResponse createProduct(ProductRequest req) {
        if (req.getName() == null || req.getName().isBlank() || req.getPrice() == null) {
            throw new ApiException("Name and price are required");
        }
        Product product = Product.builder()
                .name(req.getName())
                .description(req.getDescription() == null ? "" : req.getDescription())
                .price(req.getPrice())
                .mrp(req.getMrp() == null ? BigDecimal.ZERO : req.getMrp())
                .gst(req.getGst() == null ? BigDecimal.ZERO : req.getGst())
                .hsn(req.getHsn() == null ? "" : req.getHsn())
                .cat(req.getCat() == null ? "" : req.getCat())
                .imgUrl(req.getImgUrl() == null ? "" : req.getImgUrl())
                .status(parseStatus(req.getStatus()))
                .build();
        product = productRepository.save(product);
        return ApiResponse.ok("Product added").with("id", product.getId());
    }

    @Transactional
    public ApiResponse updateProduct(Long id, ProductRequest req) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        product.setName(req.getName());
        product.setDescription(req.getDescription() == null ? "" : req.getDescription());
        product.setPrice(req.getPrice());
        product.setMrp(req.getMrp() == null ? BigDecimal.ZERO : req.getMrp());
        product.setGst(req.getGst() == null ? BigDecimal.ZERO : req.getGst());
        product.setHsn(req.getHsn() == null ? "" : req.getHsn());
        product.setCat(req.getCat() == null ? "" : req.getCat());
        product.setImgUrl(req.getImgUrl() == null ? "" : req.getImgUrl());
        product.setStatus(parseStatus(req.getStatus()));
        productRepository.save(product);
        return ApiResponse.ok("Product updated");
    }

    @Transactional
    public ApiResponse deleteProduct(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
        }
        return ApiResponse.ok("Product deleted");
    }

    private Product.ProductStatus parseStatus(String status) {
        if (status == null || status.isBlank()) return Product.ProductStatus.current;
        try {
            return Product.ProductStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return Product.ProductStatus.current;
        }
    }
}
