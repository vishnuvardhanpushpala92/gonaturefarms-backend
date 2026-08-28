package com.gonaturefarms.service;

import com.gonaturefarms.dto.common.ApiResponse;
import com.gonaturefarms.dto.product.ProductRequest;
import com.gonaturefarms.entity.Product;
import com.gonaturefarms.entity.ProductVariant;
import com.gonaturefarms.exception.ApiException;
import com.gonaturefarms.exception.ResourceNotFoundException;
import com.gonaturefarms.repository.CategoryRepository;
import com.gonaturefarms.repository.ProductRepository;
import com.gonaturefarms.repository.ProductVariantRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Business logic for browsing and (admin) managing products. */
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository productVariantRepository;

    // Utility for generating random file names
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789";

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    public ProductService(ProductRepository productRepository,
                         CategoryRepository categoryRepository,
                         ProductVariantRepository productVariantRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productVariantRepository = productVariantRepository;
    }

    // ──────────────────────────────────────────────
    //  PUBLIC/READ-ONLY ENDPOINTS
    // ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ApiResponse listProducts(String cat, String status, String search) {
        Specification<Product> spec = buildSpecification(cat, status, search);
        List<Product> products = productRepository.findAll(
                spec, org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));

        for (Product product : products) {
            List<ProductVariant> variants = productVariantRepository.findByProductId(product.getId());
            product.setVariants(variants);
        }

        return ApiResponse.ok().with("products", products);
    }

    @Transactional(readOnly = true)
    public ApiResponse listCategories() {
        List<String> names = categoryRepository.findAllByOrderByNameAsc()
                .stream()
                .map(com.gonaturefarms.entity.Category::getName)
                .collect(Collectors.toList());
        return ApiResponse.ok().with("categories", names);
    }

    @Transactional(readOnly = true)
    public ApiResponse getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        List<ProductVariant> variants = productVariantRepository.findByProductId(id);
        product.setVariants(variants);

        return ApiResponse.ok().with("product", product);
    }

    // ──────────────────────────────────────────────
    //  ADMIN CREATE / UPDATE / DELETE
    // ──────────────────────────────────────────────

    @Transactional
    public ApiResponse createProduct(ProductRequest req) {
        if (req.getName() == null || req.getName().isBlank() || req.getPrice() == null) {
            throw new ApiException("Name and price are required");
        }

        // Prevent duplicate product names
        boolean productExists = productRepository.findAll().stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(req.getName()));
        if (productExists) {
            throw new ApiException("Product with name '" + req.getName() + "' already exists");
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
                .additionalImages(req.getAdditionalImages() == null ? "" : req.getAdditionalImages())
                .status(parseStatus(req.getStatus()))
                .build();
        product = productRepository.save(product);

        saveUniqueVariants(product, req.getVariants());

        return ApiResponse.ok("Product added").with("id", product.getId());
    }

    @Transactional
    public ApiResponse updateProduct(Long id, ProductRequest req) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Update basic fields
        product.setName(req.getName());
        product.setDescription(req.getDescription() == null ? "" : req.getDescription());
        product.setPrice(req.getPrice());
        product.setMrp(req.getMrp() == null ? BigDecimal.ZERO : req.getMrp());
        product.setGst(req.getGst() == null ? BigDecimal.ZERO : req.getGst());
        product.setHsn(req.getHsn() == null ? "" : req.getHsn());
        product.setCat(req.getCat() == null ? "" : req.getCat());
        product.setImgUrl(req.getImgUrl() == null ? "" : req.getImgUrl());
        product.setAdditionalImages(req.getAdditionalImages() == null ? "" : req.getAdditionalImages());
        product.setStatus(parseStatus(req.getStatus()));
        productRepository.save(product);

        // ✅ CRITICAL: Delete ALL old variants first so we don't create duplicates
        productVariantRepository.deleteByProductId(id);

        // ✅ Re-save only the unique variants from the request
        saveUniqueVariants(product, req.getVariants());

        return ApiResponse.ok("Product updated");
    }

    @Transactional
    public ApiResponse deleteProduct(Long id) {
        if (productRepository.existsById(id)) {
            productVariantRepository.deleteByProductId(id);
            productRepository.deleteById(id);
        }
        return ApiResponse.ok("Product deleted");
    }

    // ──────────────────────────────────────────────
    //  HELPER METHODS
    // ──────────────────────────────────────────────

    /** 
     * Saves variants while preventing any duplicate names. 
     * Also auto-syncs price to MRP if price is missing or 0.
     */
    private void saveUniqueVariants(Product product, List<com.gonaturefarms.dto.product.ProductVariantRequest> variantReqs) {
        if (variantReqs == null || variantReqs.isEmpty()) return;

        Map<String, Boolean> seenNames = new HashMap<>();

        for (com.gonaturefarms.dto.product.ProductVariantRequest variantReq : variantReqs) {
            if (variantReq.getVariantName() == null || variantReq.getVariantName().isBlank()) continue;

            // 🛑 BLOCK: Skip if duplicate name
            if (seenNames.containsKey(variantReq.getVariantName())) continue;
            seenNames.put(variantReq.getVariantName(), true);

            // ✅ FIX: Auto-sync Price with MRP if Price is 0 or missing
            BigDecimal variantPrice = (variantReq.getPrice() != null && variantReq.getPrice().compareTo(BigDecimal.ZERO) > 0)
                    ? variantReq.getPrice()
                    : (variantReq.getMrp() != null ? variantReq.getMrp() : BigDecimal.ZERO);

            ProductVariant variant = ProductVariant.builder()
                    .product(product)
                    .productName(product.getName())
                    .variantName(variantReq.getVariantName())
                    .price(variantPrice)
                    .mrp(variantReq.getMrp() == null ? variantPrice : variantReq.getMrp())
                    .stock(variantReq.getStock() == null ? 100 : variantReq.getStock())
                    .build();

            productVariantRepository.save(variant);
        }
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

    private Product.ProductStatus parseStatus(String status) {
        if (status == null || status.isBlank()) return Product.ProductStatus.current;
        try {
            return Product.ProductStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return Product.ProductStatus.current;
        }
    }

    // ──────────────────────────────────────────────
    //  FILE UPLOAD
    // ──────────────────────────────────────────────

    @Transactional
    public ApiResponse uploadProductImage(MultipartFile file) {
        if (file == null || file.isEmpty()) return ApiResponse.fail("No file uploaded");
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) throw new ApiException("Only image files allowed");

        try {
            Path dir = Path.of(uploadDir + "/products");
            Files.createDirectories(dir);
            String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
            String ext = original.contains(".") ? original.substring(original.lastIndexOf('.')) : "";
            String filename = System.currentTimeMillis() + "_" + randomSuffix(6) + ext;

            Path target = dir.resolve(filename);
            file.transferTo(target);

            return ApiResponse.ok("Image uploaded successfully").with("url", "/uploads/products/" + filename);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    private String randomSuffix(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        return sb.toString();
    }
}