package com.locallocket.backend.service;

import com.locallocket.backend.dto.product.ProductResponse;
import com.locallocket.backend.dto.vendor.VendorResponse;
import com.locallocket.backend.entity.Product;
import com.locallocket.backend.entity.Vendor;
import com.locallocket.backend.exception.BadRequestException;
import com.locallocket.backend.repository.ProductRepository;
import com.locallocket.backend.repository.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerProductService {

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Page<VendorResponse> getNearbyVendors(Double lat, Double lon, Integer radius, Pageable pageable) {
        // Get all active vendors
        List<Vendor> allVendors = vendorRepository.findByIsActiveTrue();

        // Filter vendors within radius and calculate distance
        List<VendorResponse> nearbyVendors = allVendors.stream()
                .filter(vendor -> vendor.getLatitude() != null && vendor.getLongitude() != null)
                .map(vendor -> {
                    double distance = calculateDistance(lat, lon, vendor.getLatitude(), vendor.getLongitude());
                    return new VendorResponse(vendor, distance);
                })
                .filter(vendorResponse -> vendorResponse.getDistance() <= radius)
                .sorted((v1, v2) -> Double.compare(v1.getDistance(), v2.getDistance()))
                .collect(Collectors.toList());

        // Manual pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), nearbyVendors.size());

        List<VendorResponse> pageContent = nearbyVendors.subList(start, end);
        return new PageImpl<>(pageContent, pageable, nearbyVendors.size());
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getVendorProducts(Long vendorId, String query, Pageable pageable) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new BadRequestException("Vendor not found"));

        if (!vendor.getIsActive()) {
            throw new BadRequestException("Vendor is not active");
        }

        Page<Product> productsPage;
        if (query == null || query.trim().isEmpty()) {
            productsPage = productRepository.findByVendorAndIsActiveTrue(vendor, pageable);
        } else {
            productsPage = productRepository.findByVendorAndIsActiveTrueAndNameContainingIgnoreCase(
                    vendor, query.trim(), pageable);
        }

        return productsPage.map(ProductResponse::new);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String query, Double lat, Double lon, Integer radius, Pageable pageable) {
        if (query == null || query.trim().isEmpty()) {
            throw new BadRequestException("Search query is required");
        }

        Page<Product> allProducts;

        if (lat != null && lon != null) {
            // Location-based search: first get nearby vendors, then search their products
            List<Vendor> nearbyVendors = vendorRepository.findByIsActiveTrue().stream()
                    .filter(vendor -> vendor.getLatitude() != null && vendor.getLongitude() != null)
                    .filter(vendor -> calculateDistance(lat, lon, vendor.getLatitude(), vendor.getLongitude()) <= radius)
                    .collect(Collectors.toList());

            allProducts = productRepository.findByVendorInAndIsActiveTrueAndNameContainingIgnoreCase(
                    nearbyVendors, query.trim(), pageable);
        } else {
            // Global search across all vendors
            allProducts = productRepository.findByIsActiveTrueAndNameContainingIgnoreCase(query.trim(), pageable);
        }

        return allProducts.map(ProductResponse::new);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductDetails(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BadRequestException("Product not found"));

        if (!product.getIsActive()) {
            throw new BadRequestException("Product is not available");
        }

        if (!product.getVendor().getIsActive()) {
            throw new BadRequestException("Vendor is not active");
        }

        return new ProductResponse(product);
    }

    @Transactional(readOnly = true)
    public VendorResponse getVendorDetails(Long vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new BadRequestException("Vendor not found"));

        if (!vendor.getIsActive()) {
            throw new BadRequestException("Vendor is not active");
        }

        return new VendorResponse(vendor);
    }

    // Haversine formula to calculate distance between two points
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        return distance;
    }
}
