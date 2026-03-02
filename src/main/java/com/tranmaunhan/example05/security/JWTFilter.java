// package com.tonquoctuan.example05.security;

// import java.io.IOException;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.stereotype.Service;
// import org.springframework.web.filter.OncePerRequestFilter;

// import com.auth0.jwt.exceptions.JWTVerificationException;
// import com.tonquoctuan.example05.service.impl.UserDetailsServiceImpl;

// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;

// @Service
// public class JWTFilter extends OncePerRequestFilter {

//     @Autowired
//     private JwtUtil jwtUtil;

//     @Autowired
//     private UserDetailsServiceImpl userDetailsServiceImpl;

//     @Override
//     protected void doFilterInternal( @SuppressWarnings("null") HttpServletRequest request, @SuppressWarnings("null")
//         HttpServletResponse response, @SuppressWarnings("null") FilterChain filterChain)
//         throws ServletException, IOException {

//         String authHeader = request.getHeader("Authorization");

//         if (authHeader != null && !authHeader.isBlank() && authHeader.startsWith("Bearer ")) {
//             String jwt = authHeader.substring(7);

//             if (jwt == null || jwt.isBlank()) {
//                 response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invlaid JWT token in Bearer Header");
//             } else {
//                 try {
//                     String email = jwtUtil.validateTokenAndRetrieveSubject(jwt);

//                     UserDetails userDetails = userDetailsServiceImpl.loadUserByEmail(email);

//                     UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
//                         new UsernamePasswordAuthenticationToken(email, userDetails.getPassword(),
//                             userDetails.getAuthorities());

//                     if (SecurityContextHolder.getContext().getAuthentication() == null) {
//                         SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
//                     }
//                 } catch (JWTVerificationException e) {
//                     response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JWT Token");
//                 }
//             }
//         }

//         filterChain.doFilter(request, response);
//     }
// }

package com.tranmaunhan.example05.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.exceptions.JWTVerificationException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component

public class JWTFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JWTFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);

        try {
            String email = jwtUtil.validateTokenAndRetrieveSubject(jwt);

            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(email);

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails.getUsername(),
                                null,
                                userDetails.getAuthorities()
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (JWTVerificationException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT Token");
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/api/login")
                || path.equals("/api/register")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.equals("/error");
    }
}
