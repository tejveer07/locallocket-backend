package com.locallocket.backend.service;

import com.locallocket.backend.dto.cart.*;
import com.locallocket.backend.entity.*;
import com.locallocket.backend.exception.BadRequestException;
import com.locallocket.backend.exception.VendorConflictException;
import com.locallocket.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private FeeCalculationService feeCalculationService;

//    @Transactional
//    public CartResponse addToCart(User user, AddToCartRequest request) {
//        // Get product
//        Product product = productRepository.findById(request.getProductId())
//                .orElseThrow(() -> new BadRequestException("Product not found"));
//
//        if (!product.getIsActive()) {
//            throw new BadRequestException("Product is not available");
//        }
//
//        if (product.getStock() < request.getQuantity()) {
//            throw new BadRequestException("Insufficient stock. Available: " + product.getStock());
//        }
//
//        // **KEY CHANGE: Check for vendor conflict**
//        Optional<Cart> existingCart = cartRepository.findByUser(user);
//
//        if (existingCart.isPresent()) {
//            Cart cart = existingCart.get();
//
//            // If trying to add from different vendor, throw specific exception
//            if (!cart.getVendor().getId().equals(product.getVendor().getId())) {
//                throw new VendorConflictException(
//                        "Your cart contains items from " + cart.getVendor().getShopName() +
//                                ". You can only add products from one vendor at a time. Please complete your current order or clear your cart to shop from " +
//                                product.getVendor().getShopName() + "."
//                );
//            }
//        }
//
//        // Get or create cart for this vendor (will create new if no existing cart)
//        Cart cart = existingCart.orElse(createNewCart(user, product.getVendor()));
//
//        // Check if item already exists in cart
//        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);
//
//        if (existingItem.isPresent()) {
//            // Update existing item
//            CartItem item = existingItem.get();
//            int newQuantity = item.getQuantity() + request.getQuantity();
//
//            if (product.getStock() < newQuantity) {
//                throw new BadRequestException("Cannot add more. Available stock: " + product.getStock());
//            }
//
//            item.setQuantity(newQuantity);
//            cartItemRepository.save(item);
//        } else {
//            // Add new item
//            CartItem newItem = new CartItem(cart, product, request.getQuantity(), product.getPrice());
//            cartItemRepository.save(newItem);
//            cart.addItem(newItem);
//        }
//
//        // Recalculate cart totals
//        recalculateCartTotals(cart);
//        cartRepository.save(cart);
//
//        return new CartResponse(cart, "Item added successfully to your cart from " + product.getVendor().getShopName());
//    }

    @Transactional
    public CartResponse addToCart(User user, AddToCartRequest request) {
        // Get product
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BadRequestException("Product not found"));

        if (!product.getIsActive()) {
            throw new BadRequestException("Product is not available");
        }

        if (product.getStock() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock. Available: " + product.getStock());
        }

        // Use the same safe method for getting/creating cart
        Cart cart = getOrCreateCartSafely(user, product.getVendor());

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);

        if (existingItem.isPresent()) {
            // Update existing item
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();

            if (product.getStock() < newQuantity) {
                throw new BadRequestException("Cannot add more. Available stock: " + product.getStock());
            }

            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            // Add new item
            CartItem newItem = new CartItem(cart, product, request.getQuantity(), product.getPrice());
            cartItemRepository.save(newItem);
            cart.addItem(newItem);
        }

        // Recalculate cart totals
        recalculateCartTotals(cart);
        cartRepository.save(cart);

        return new CartResponse(cart, "Item added successfully to your cart from " + product.getVendor().getShopName());
    }



    @Transactional(readOnly = true)
    public CartResponse getCart(User user) {
        Optional<Cart> cart = cartRepository.findByUser(user);
        return cart.map(CartResponse::new).orElse(null);
    }

    @Transactional
    public CartResponse updateCartItem(User user, Long cartItemId, UpdateCartItemRequest request) {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new BadRequestException("Cart not found"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BadRequestException("Cart item not found"));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Cart item does not belong to your cart");
        }

        // Check stock availability
        if (cartItem.getProduct().getStock() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock. Available: " + cartItem.getProduct().getStock());
        }

        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);

        // Recalculate totals
        recalculateCartTotals(cart);
        cartRepository.save(cart);

        return new CartResponse(cart);
    }

    @Transactional
    public void removeCartItem(User user, Long cartItemId) {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new BadRequestException("Cart not found"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BadRequestException("Cart item not found"));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Cart item does not belong to your cart");
        }

        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);

        if (cart.getItems().isEmpty()) {
            // Delete empty cart
            cartRepository.delete(cart);
        } else {
            // Recalculate totals
            recalculateCartTotals(cart);
            cartRepository.save(cart);
        }
    }

    @Transactional
    public void clearCart(User user) {
        Optional<Cart> cart = cartRepository.findByUser(user);
        cart.ifPresent(cartRepository::delete);
    }

    @Transactional
    public CartResponse switchVendor(User user, Long newVendorId) {
        Vendor newVendor = vendorRepository.findById(newVendorId)
                .orElseThrow(() -> new BadRequestException("Vendor not found"));

        // Clear existing cart
        clearCart(user);

        // Create new cart for new vendor
        Cart newCart = createNewCart(user, newVendor);

        return new CartResponse(newCart, "Switched to " + newVendor.getShopName() + ". Your previous cart was cleared.");
    }

    private Cart createNewCart(User user, Vendor vendor) {
        Cart cart = new Cart(user, vendor);
        return cartRepository.save(cart);
    }

    private void recalculateCartTotals(Cart cart) {
        // Calculate subtotal
        BigDecimal subtotal = cart.getItems().stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate fees
        BigDecimal platformFee = feeCalculationService.calculatePlatformFee(subtotal);
        BigDecimal deliveryFee = feeCalculationService.calculateDeliveryFee();
        BigDecimal totalAmount = subtotal.add(platformFee).add(deliveryFee);

        // Update cart totals
        cart.setSubtotal(subtotal);
        cart.setPlatformFee(platformFee);
        cart.setDeliveryFee(deliveryFee);
        cart.setTotalAmount(totalAmount);
    }

//    @Transactional
//    public CartResponse addMultipleProductsToCart(User user, AddMultipleProductsRequest request) {
//        List<AddMultipleProductsRequest.ProductItem> products = request.getProducts();
//
//        if (products == null || products.isEmpty()) {
//            throw new BadRequestException("Products list cannot be empty");
//        }
//
//        // Step 1: Validate all products exist and belong to the same vendor
//        Vendor vendor = null;
//        List<Product> validatedProducts = new ArrayList<>();
//
//        for (AddMultipleProductsRequest.ProductItem item : products) {
//            Product product = productRepository.findById(item.getProductId())
//                    .orElseThrow(() -> new BadRequestException("Product not found: " + item.getProductId()));
//
//            if (!product.getIsActive()) {
//                throw new BadRequestException("Product is not available: " + product.getName());
//            }
//
//            if (product.getStock() < item.getQuantity()) {
//                throw new BadRequestException("Insufficient stock for " + product.getName() +
//                        ". Available: " + product.getStock() + ", Requested: " + item.getQuantity());
//            }
//
//            // Ensure all products are from the same vendor
//            if (vendor == null) {
//                vendor = product.getVendor();
//            } else if (!vendor.getId().equals(product.getVendor().getId())) {
//                throw new BadRequestException("All products must be from the same vendor. " +
//                        "Expected: " + vendor.getShopName() + ", Found: " + product.getVendor().getShopName());
//            }
//
//            validatedProducts.add(product);
//        }
//
//        // Step 2: Check for vendor conflict with existing cart
//        Optional<Cart> existingCart = cartRepository.findByUser(user);
//
//        if (existingCart.isPresent()) {
//            Cart cart = existingCart.get();
//
//            if (!cart.getVendor().getId().equals(vendor.getId())) {
//                throw new VendorConflictException(
//                        "Your cart contains items from " + cart.getVendor().getShopName() +
//                                ". You can only add products from one vendor at a time. Please complete your current order or clear your cart to shop from " +
//                                vendor.getShopName() + "."
//                );
//            }
//        }
//
//        // Step 3: Get or create cart for this vendor
//        Cart cart = existingCart.orElse(createNewCart(user, vendor));
//
//        // Step 4: Add or update each product in the cart
//        int totalItemsAdded = 0;
//        for (int i = 0; i < products.size(); i++) {
//            AddMultipleProductsRequest.ProductItem item = products.get(i);
//            Product product = validatedProducts.get(i);
//
//            Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);
//
//            if (existingItem.isPresent()) {
//                // Update existing item
//                CartItem cartItem = existingItem.get();
//                int newQuantity = cartItem.getQuantity() + item.getQuantity();
//
//                // Double-check stock after combining quantities
//                if (product.getStock() < newQuantity) {
//                    throw new BadRequestException("Cannot add " + item.getQuantity() + " more " + product.getName() +
//                            ". You already have " + cartItem.getQuantity() + " in cart. Available stock: " + product.getStock());
//                }
//
//                cartItem.setQuantity(newQuantity);
//                cartItemRepository.save(cartItem);
//                totalItemsAdded += item.getQuantity();
//            } else {
//                // Add new item
//                CartItem newItem = new CartItem(cart, product, item.getQuantity(), product.getPrice());
//                cartItemRepository.save(newItem);
//                cart.addItem(newItem);
//                totalItemsAdded += item.getQuantity();
//            }
//        }
//
//        // Step 5: Recalculate cart totals
//        recalculateCartTotals(cart);
//        cartRepository.save(cart);
//
//        return new CartResponse(cart,
//                totalItemsAdded + " items added successfully to your cart from " + vendor.getShopName());
//    }

    @Transactional
    public CartResponse addMultipleProductsToCart(User user, AddMultipleProductsRequest request) {
        List<AddMultipleProductsRequest.ProductItem> products = request.getProducts();

        if (products == null || products.isEmpty()) {
            throw new BadRequestException("Products list cannot be empty");
        }

        // Step 1: Validate all products exist and belong to the same vendor
        Vendor vendor = null;
        List<Product> validatedProducts = new ArrayList<>();

        for (AddMultipleProductsRequest.ProductItem item : products) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new BadRequestException("Product not found: " + item.getProductId()));

            if (!product.getIsActive()) {
                throw new BadRequestException("Product is not available: " + product.getName());
            }

            if (product.getStock() < item.getQuantity()) {
                throw new BadRequestException("Insufficient stock for " + product.getName());
            }

            // Ensure all products are from the same vendor
            if (vendor == null) {
                vendor = product.getVendor();
            } else if (!vendor.getId().equals(product.getVendor().getId())) {
                throw new BadRequestException("All products must be from the same vendor");
            }

            validatedProducts.add(product);
        }

        // Step 2: Get or handle existing cart - THIS IS THE KEY FIX
        Cart cart = getOrCreateCartSafely(user, vendor);

        // Step 3: Add or update each product in the cart
        int totalItemsAdded = 0;
        for (int i = 0; i < products.size(); i++) {
            AddMultipleProductsRequest.ProductItem item = products.get(i);
            Product product = validatedProducts.get(i);

            Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);

            if (existingItem.isPresent()) {
                // Update existing item
                CartItem cartItem = existingItem.get();
                int newQuantity = cartItem.getQuantity() + item.getQuantity();

                if (product.getStock() < newQuantity) {
                    throw new BadRequestException("Cannot add " + item.getQuantity() + " more " + product.getName());
                }

                cartItem.setQuantity(newQuantity);
                cartItemRepository.save(cartItem);
                totalItemsAdded += item.getQuantity();
            } else {
                // Add new item
                CartItem newItem = new CartItem(cart, product, item.getQuantity(), product.getPrice());
                cartItemRepository.save(newItem);
                cart.addItem(newItem);
                totalItemsAdded += item.getQuantity();
            }
        }

        // Step 4: Recalculate cart totals
        recalculateCartTotals(cart);
        cartRepository.save(cart);

        return new CartResponse(cart,
                totalItemsAdded + " items added successfully to your cart from " + vendor.getShopName());
    }

    // NEW METHOD: Safe cart creation/retrieval
    private Cart getOrCreateCartSafely(User user, Vendor vendor) {
        Optional<Cart> existingCart = cartRepository.findByUser(user);

        if (existingCart.isPresent()) {
            Cart cart = existingCart.get();

            // Check vendor conflict
            if (!cart.getVendor().getId().equals(vendor.getId())) {
                throw new VendorConflictException(
                        "Your cart contains items from " + cart.getVendor().getShopName() +
                                ". You can only add products from one vendor at a time. Please complete your current order or clear your cart to shop from " +
                                vendor.getShopName() + "."
                );
            }

            return cart; // Return existing cart
        } else {
            // Create new cart only if none exists
            Cart newCart = new Cart(user, vendor);
            return cartRepository.save(newCart); // Use save() instead of manual insert
        }
    }


}
