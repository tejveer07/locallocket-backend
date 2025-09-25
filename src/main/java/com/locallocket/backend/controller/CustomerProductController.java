package com.locallocket.backend.controller;

import com.locallocket.backend.dto.product.ProductResponse;
import com.locallocket.backend.dto.vendor.VendorResponse;
import com.locallocket.backend.service.CustomerProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerProductController {

    @Autowired
    private CustomerProductService customerProductService;

    // List nearby vendors based on location
    @GetMapping("/vendors/nearby")
    public ResponseEntity<Page<VendorResponse>> getNearbyVendors(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam(defaultValue = "5") Integer radius, // Change to KM
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "shopName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<VendorResponse> vendors = customerProductService.getNearbyVendors(lat, lon, radius, pageable);
        return ResponseEntity.ok(vendors);
    }


    // Get products from specific vendor
    @GetMapping("/vendors/{vendorId}/products")
    public ResponseEntity<Page<ProductResponse>> getVendorProducts(
            @PathVariable Long vendorId,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProductResponse> products = customerProductService.getVendorProducts(vendorId, query, pageable);
        return ResponseEntity.ok(products);
    }

    // Search products across all vendors
    @GetMapping("/products/search")
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @RequestParam String query,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(defaultValue = "5000") Integer radius,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProductResponse> products = customerProductService.searchProducts(
                query, lat, lon, radius, pageable);
        return ResponseEntity.ok(products);
    }

    // Get single product details (for product detail page)
    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductResponse> getProductDetails(@PathVariable Long productId) {
        ProductResponse product = customerProductService.getProductDetails(productId);
        return ResponseEntity.ok(product);
    }

    // Get vendor details
    @GetMapping("/vendors/{vendorId}")
    public ResponseEntity<VendorResponse> getVendorDetails(@PathVariable Long vendorId) {
        VendorResponse vendor = customerProductService.getVendorDetails(vendorId);
        return ResponseEntity.ok(vendor);
    }
}
