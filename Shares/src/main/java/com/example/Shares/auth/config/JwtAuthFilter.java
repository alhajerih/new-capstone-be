package com.example.Shares.auth.config;


import com.example.Shares.auth.exception.UserNotFoundException;
import com.example.Shares.auth.service.auth.CustomUserDetailsService;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Configuration
public class JwtAuthFilter extends OncePerRequestFilter {

    /*
    The JwtAuthFilter class defines two instance variables: jwtUtil and userDetailsService. These are required for JWT (JSON Web Token) authentication. The class has a constructor that receives these dependencies, allowing them to be injected when the filter is created.
     */
    private static final String BEARER = "Bearer ";

    private final JWTUtil jwtUtil;

    private final CustomUserDetailsService userDetailsService;

    public JwtAuthFilter(JWTUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }


    /*
    This method doFilterInternal is the heart of the filter. It is called for each HTTP request to process JWT authentication.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // Retrieve the Authorization header from the HTTP request
        String authorizationHeader = request.getHeader(AUTHORIZATION);

        // Check if the Authorization header exists and starts with "Bearer "
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER)) {
            String token = authorizationHeader.substring(7); // Extract token without "Bearer "

            // Validate the token
            if (jwtUtil.isTokenValid(token)) {

                // Extract civilId from the token
                String civilId = jwtUtil.extractCivilId(token);
                if (civilId == null) {
                    throw new UserNotFoundException("Civil ID not found in token");
                }

                // Load user details using civilId
                UserDetails userDetails = userDetailsService.loadUserByCivilId(civilId);
                if (userDetails == null) {
                    throw new UserNotFoundException("User not found for Civil ID: " + civilId);
                }

                // Create an Authentication object
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                // Set additional details for the authentication object
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set the authentication object in the security context
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // Allow the request to continue processing
        filterChain.doFilter(request, response);
    }

}