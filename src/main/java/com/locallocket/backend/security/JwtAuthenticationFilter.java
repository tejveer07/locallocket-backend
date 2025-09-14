package com.locallocket.backend.security;

import com.locallocket.backend.entity.User;
import com.locallocket.backend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        // Debug: Print all headers
        System.out.println("🔍 REQUEST: " + method + " " + requestURI);
        System.out.println("🔍 Headers:");
        java.util.Collections.list(request.getHeaderNames()).forEach(headerName -> {
            System.out.println("   " + headerName + ": " + request.getHeader(headerName));
        });

        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                System.out.println("✅ JWT Token found: " + jwt.substring(0, Math.min(jwt.length(), 20)) + "...");

                if (jwtTokenProvider.validateToken(jwt)) {
                    Long userId = jwtTokenProvider.getUserIdFromToken(jwt);
                    User user = userRepository.findById(userId).orElse(null);

                    if (user != null && user.getIsActive()) {
                        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                                .collect(Collectors.toList());

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(user, null, authorities);

                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        System.out.println("✅ User authenticated: " + user.getEmail());
                        System.out.println("✅ Authorities: " + authorities);
                    } else {
                        System.out.println("❌ User not found or inactive for ID: " + userId);
                    }
                } else {
                    System.out.println("❌ JWT token validation failed");
                }
            } else {
                System.out.println("❌ No JWT token found for: " + requestURI);
            }
        } catch (Exception ex) {
            System.out.println("❌ JWT processing error: " + ex.getMessage());
            ex.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }


    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}






//package com.locallocket.backend.security;
//
//import com.locallocket.backend.entity.User;
//import com.locallocket.backend.repository.UserRepository;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Component
//public class JwtAuthenticationFilter extends OncePerRequestFilter {
//
//    @Autowired
//    private JwtTokenProvider jwtTokenProvider;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//
//        try {
//            String jwt = getJwtFromRequest(request);
//
//            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
//                Long userId = jwtTokenProvider.getUserIdFromToken(jwt);
//
//                User user = userRepository.findById(userId).orElse(null);
//                if (user != null && user.getIsActive()) {
//                    // Convert roles to authorities with ROLE_ prefix
//                    List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
//                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
//                            .collect(Collectors.toList());
//
//                    UsernamePasswordAuthenticationToken authentication =
//                            new UsernamePasswordAuthenticationToken(user, null, authorities);
//
//                    SecurityContextHolder.getContext().setAuthentication(authentication);
//
//                    // Debug log
//                    System.out.println("User authenticated: " + user.getEmail() + " with roles: " + authorities);
//                }
//            }
//        } catch (Exception ex) {
//            logger.error("Could not set user authentication in security context", ex);
//        }
//
//        filterChain.doFilter(request, response);
//    }
//
//    private String getJwtFromRequest(HttpServletRequest request) {
//        String bearerToken = request.getHeader("Authorization");
//        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
//            return bearerToken.substring(7);
//        }
//        return null;
//    }
//}
