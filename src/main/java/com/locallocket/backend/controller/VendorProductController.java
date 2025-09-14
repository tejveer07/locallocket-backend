package com.locallocket.backend.controller;

import com.locallocket.backend.dto.product.*;
import com.locallocket.backend.entity.User;
import com.locallocket.backend.entity.Vendor;
import com.locallocket.backend.service.VendorAuthService;
import com.locallocket.backend.service.VendorProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vendor/products")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('VENDOR')")
public class VendorProductController {

    @Autowired
    private VendorProductService productService;

    @Autowired
    private VendorAuthService vendorAuthService;

    private Vendor getCurrentVendor(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return vendorAuthService.currentVendor(currentUser);
    }

    // Create new product
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            Authentication authentication,
            @Valid @RequestBody ProductCreateRequest request) {

        Vendor vendor = getCurrentVendor(authentication);
        ProductResponse response = productService.createProduct(vendor, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // List all products for the vendor with pagination and search
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> listProducts(
            Authentication authentication,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Vendor vendor = getCurrentVendor(authentication);

        // Create Pageable with sorting
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProductResponse> products = productService.listProducts(vendor, query, pageable);
        return ResponseEntity.ok(products);
    }

    // Get single product by ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(
            Authentication authentication,
            @PathVariable Long id) {

        Vendor vendor = getCurrentVendor(authentication);
        ProductResponse product = productService.getProduct(vendor, id);
        return ResponseEntity.ok(product);
    }

    // Update existing product
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request) {

        Vendor vendor = getCurrentVendor(authentication);
        ProductResponse response = productService.updateProduct(vendor, id, request);
        return ResponseEntity.ok(response);
    }

    // Delete product
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            Authentication authentication,
            @PathVariable Long id) {

        Vendor vendor = getCurrentVendor(authentication);
        productService.deleteProduct(vendor, id);
        return ResponseEntity.noContent().build();
    }

    // Toggle product active status
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ProductResponse> toggleProductStatus(
            Authentication authentication,
            @PathVariable Long id) {

        Vendor vendor = getCurrentVendor(authentication);
        ProductResponse response = productService.toggleProductStatus(vendor, id);
        return ResponseEntity.ok(response);
    }

    // Update product stock
    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductResponse> updateStock(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody StockUpdateRequest request) {

        Vendor vendor = getCurrentVendor(authentication);
        ProductResponse response = productService.updateStock(vendor, id, request.getStock());
        return ResponseEntity.ok(response);
    }
}
