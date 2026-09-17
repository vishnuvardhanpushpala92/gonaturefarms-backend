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
import com.gonaturefarms.entity.ProductVariant;

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
        System.out.println("=== PUBLIC PRODUCT LIST DEBUG ===");
        System.out.println("cat: " + cat + ", status: " + status + ", search: " + search);
        
        try {
            Specification<Product> spec = buildSpecification(cat, status, search);
            List<Product> products = productRepository.findAll(
                    spec, org.springframework.data.domain.Sort.by(
                            org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));

            System.out.println("Products found (before variant loading): " + products.size());
            for (Product p : products) {
                System.out.println("  - ID: " + p.getId() + ", Name: " + p.getName());
            }

            // Filter out pending products for public view (treat NULL as false)
            products = products.stream()
                    .filter(p -> p.getPending() == null || !p.getPending())
                    .collect(Collectors.toList());
            
            System.out.println("Products after pending filter: " + products.size());

            // Use batch query to fetch all variants at once (prevent N+1)
            if (!products.isEmpty()) {
                try {
                    List<Long> productIds = products.stream()
                        .filter(p -> p != null && p.getId() != null)
                        .map(Product::getId)
                        .collect(Collectors.toList());
                    
                    System.out.println("Fetching variants for product IDs: " + productIds);
                    
                    if (!productIds.isEmpty()) {
                        List<Product> productsWithVariants = productRepository.findAllByIdWithVariants(productIds);
                        System.out.println("Products with variants fetched: " + productsWithVariants.size());
                        
                        // Use merge function to handle potential duplicates
                        Map<Long, List<ProductVariant>> variantsMap = productsWithVariants.stream()
                            .filter(p -> p != null && p.getId() != null)
                            .collect(Collectors.toMap(
                                Product::getId, 
                                p -> p.getVariants() != null ? p.getVariants() : new ArrayList<>(),
                                (existing, replacement) -> existing
                            ));
                        
                        products.forEach(p -> {
                            if (p != null) {
                                p.setVariants(variantsMap.getOrDefault(p.getId(), new ArrayList<>()));
                            }
                        });
                    }
                } catch (Exception e) {
                    System.out.println("ERROR during variant loading: " + e.getMessage());
                    e.printStackTrace();
                    // If variant loading fails, set empty variants and continue
                    products.forEach(p -> {
                        if (p != null) {
                            p.setVariants(new ArrayList<>());
                        }
                    });
                }
            }

            System.out.println("Returning " + products.size() + " products");
            System.out.println("=== END PUBLIC PRODUCT LIST DEBUG ===");
            return ApiResponse.ok().with("products", products);
        } catch (Exception e) {
            System.out.println("ERROR in listProducts: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public ApiResponse listProductsAdmin(String cat, String status, String search) {
        System.out.println("=== ADMIN PRODUCT LIST DEBUG ===");
        System.out.println("cat: " + cat + ", status: " + status + ", search: " + search);
        
        try {
            Specification<Product> spec = buildSpecification(cat, status, search);
            List<Product> products = productRepository.findAll(
                    spec, org.springframework.data.domain.Sort.by(
                            org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
            
            System.out.println("Products found: " + products.size());
            for (Product p : products) {
                System.out.println("  - ID: " + p.getId() + ", Name: " + p.getName());
            }

            // Simplify: Don't load variants in admin list view
            // Variants are only needed when editing a product, not in the list
            // Set empty variants for all products to avoid serialization issues
            products.forEach(p -> {
                if (p != null) {
                    p.setVariants(new ArrayList<>());
                }
            });

            System.out.println("Returning " + products.size() + " products");
            System.out.println("=== END ADMIN PRODUCT LIST DEBUG ===");
            return ApiResponse.ok().with("products", products);
        } catch (Exception e) {
            System.out.println("ERROR in listProductsAdmin: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
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
                .pending(true)
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
        product.setPending(true);
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

    @Transactional
    public ApiResponse addDemoVariantsToAllProducts() {
        List<Product> allProducts = productRepository.findAll();
        int updatedCount = 0;
        
        for (Product product : allProducts) {
            List<ProductVariant> existingVariants = productVariantRepository.findByProductId(product.getId());
            
            // Only add demo variants if product has no variants
            if (existingVariants == null || existingVariants.isEmpty()) {
                System.out.println("Adding demo variants to product: " + product.getName());
                
                // Create demo variants: Small, Medium, Large
                BigDecimal basePrice = product.getPrice() != null ? product.getPrice() : BigDecimal.valueOf(100);
                BigDecimal baseMrp = product.getMrp() != null ? product.getMrp() : basePrice;
                
                ProductVariant smallVariant = ProductVariant.builder()
                        .product(product)
                        .productName(product.getName())
                        .variantName("Small")
                        .price(basePrice.multiply(BigDecimal.valueOf(0.8))) // 20% discount
                        .mrp(baseMrp.multiply(BigDecimal.valueOf(0.8)))
                        .stock(50)
                        .build();
                
                ProductVariant mediumVariant = ProductVariant.builder()
                        .product(product)
                        .productName(product.getName())
                        .variantName("Medium")
                        .price(basePrice)
                        .mrp(baseMrp)
                        .stock(100)
                        .build();
                
                ProductVariant largeVariant = ProductVariant.builder()
                        .product(product)
                        .productName(product.getName())
                        .variantName("Large")
                        .price(basePrice.multiply(BigDecimal.valueOf(1.2))) // 20% premium
                        .mrp(baseMrp.multiply(BigDecimal.valueOf(1.2)))
                        .stock(75)
                        .build();
                
                productVariantRepository.save(smallVariant);
                productVariantRepository.save(mediumVariant);
                productVariantRepository.save(largeVariant);
                
                updatedCount++;
            }
        }
        
        return ApiResponse.ok("Demo variants added to " + updatedCount + " products");
    }

    @Transactional
    public ApiResponse removeDemoVariantsFromAllProducts() {
        List<Product> allProducts = productRepository.findAll();
        int removedCount = 0;
        
        // Demo variant names that should be removed
        java.util.Set<String> demoVariantNames = new java.util.HashSet<>();
        demoVariantNames.add("Small");
        demoVariantNames.add("Medium");
        demoVariantNames.add("Large");
        
        try {
            for (Product product : allProducts) {
                List<ProductVariant> existingVariants = productVariantRepository.findByProductId(product.getId());
                
                if (existingVariants != null && !existingVariants.isEmpty()) {
                    for (ProductVariant variant : existingVariants) {
                        // Only remove if it's a demo variant (Small, Medium, Large)
                        if (demoVariantNames.contains(variant.getVariantName())) {
                            System.out.println("Removing demo variant: " + variant.getVariantName() + " from product: " + product.getName());
                            productVariantRepository.deleteById(variant.getId());
                            removedCount++;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error removing demo variants: " + e.getMessage());
            e.printStackTrace();
            throw new ApiException("Failed to remove demo variants: " + e.getMessage());
        }
        
        return ApiResponse.ok("Demo variants removed from " + removedCount + " products");
    }

    // ──────────────────────────────────────────────
    //  HELPER METHODS
    // ──────────────────────────────────────────────

    private void saveUniqueVariants(Product product, List<com.gonaturefarms.dto.product.ProductVariantRequest> variantReqs) {
        if (variantReqs == null || variantReqs.isEmpty()) return;

        System.out.println("=== SAVE VARIANTS DEBUG ===");
        System.out.println("Product: " + product.getName());
        System.out.println("Number of variant requests: " + variantReqs.size());
        for (com.gonaturefarms.dto.product.ProductVariantRequest variantReq : variantReqs) {
            System.out.println("Received variantReq - variantName: [" + variantReq.getVariantName() + "], price: " + variantReq.getPrice());
        }

        Map<String, Boolean> seenNames = new HashMap<>();

        for (com.gonaturefarms.dto.product.ProductVariantRequest variantReq : variantReqs) {
            // Log variant details for debugging
            System.out.println("Processing variant: " + variantReq.getVariantName() + ", price: " + variantReq.getPrice());

            // Do NOT skip variants with empty variantName - save them as-is
            // The admin panel should ensure variantName is populated

            // 🛑 BLOCK: Skip if duplicate name
            if (seenNames.containsKey(variantReq.getVariantName())) {
                System.out.println("SKIPPING duplicate variant: " + variantReq.getVariantName());
                continue;
            }
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

            System.out.println("Saving variant to DB: " + variant.getVariantName() + ", price: " + variant.getPrice());
            productVariantRepository.save(variant);
        }
        System.out.println("=== END SAVE VARIANTS DEBUG ===");
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