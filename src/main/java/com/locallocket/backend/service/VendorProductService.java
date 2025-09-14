// src/main/java/com/locallocket/backend/service/VendorProductService.java
package com.locallocket.backend.service;

import com.locallocket.backend.dto.product.*;
import com.locallocket.backend.entity.Product;
import com.locallocket.backend.entity.Vendor;
import com.locallocket.backend.exception.BadRequestException;
import com.locallocket.backend.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VendorProductService {
    private final ProductRepository productRepository;

    public VendorProductService(ProductRepository pr) { this.productRepository = pr; }

    @Transactional
    public ProductResponse createProduct(Vendor vendor, ProductCreateRequest req) {
        Product p = new Product();
        p.setVendor(vendor);
        p.setName(req.getName());
        p.setDescription(req.getDescription());
        p.setPrice(req.getPrice());
        p.setImageUrl(req.getImageUrl());
        p.setStock(req.getStock());
        p.setIsActive(req.getIsActive() != null ? req.getIsActive() : true);
        productRepository.save(p);
        return new ProductResponse(p);
    }

    @Transactional(readOnly=true)
    public Page<ProductResponse> listProducts(Vendor vendor, String query, Pageable pageable) {
        Page<Product> page = (query == null || query.isBlank())
                ? productRepository.findByVendor(vendor, pageable)
                : productRepository.findByVendorAndNameContainingIgnoreCase(vendor, query.trim(), pageable);
        return page.map(ProductResponse::new);
    }

    @Transactional
    public ProductResponse updateProduct(Vendor vendor, Long productId, ProductUpdateRequest req) {
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new BadRequestException("Product not found"));
        if (!p.getVendor().getId().equals(vendor.getId())) {
            throw new BadRequestException("Not allowed to modify this product");
        }
        p.setName(req.getName());
        p.setDescription(req.getDescription());
        p.setPrice(req.getPrice());
        p.setImageUrl(req.getImageUrl());
        p.setStock(req.getStock());
        p.setIsActive(req.getIsActive());
        productRepository.save(p);
        return new ProductResponse(p);
    }

    @Transactional
    public void deleteProduct(Vendor vendor, Long productId) {
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new BadRequestException("Product not found"));
        if (!p.getVendor().getId().equals(vendor.getId())) {
            throw new BadRequestException("Not allowed to delete this product");
        }
        productRepository.delete(p);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Vendor vendor, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BadRequestException("Product not found"));

        if (!product.getVendor().getId().equals(vendor.getId())) {
            throw new BadRequestException("Not allowed to access this product");
        }

        return new ProductResponse(product);
    }

    @Transactional
    public ProductResponse toggleProductStatus(Vendor vendor, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BadRequestException("Product not found"));

        if (!product.getVendor().getId().equals(vendor.getId())) {
            throw new BadRequestException("Not allowed to modify this product");
        }

        product.setIsActive(!product.getIsActive());
        productRepository.save(product);
        return new ProductResponse(product);
    }

    @Transactional
    public ProductResponse updateStock(Vendor vendor, Long productId, Integer newStock) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BadRequestException("Product not found"));

        if (!product.getVendor().getId().equals(vendor.getId())) {
            throw new BadRequestException("Not allowed to modify this product");
        }

        product.setStock(newStock);
        productRepository.save(product);
        return new ProductResponse(product);
    }



}
