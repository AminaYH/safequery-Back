package com.safequery.back.projectdemo.Util;

import io.jsonwebtoken.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    private final String SECRET_KEY = "your_secret_key"; // Replace with a stronger secret key
    private final long EXPIRATION_TIME = 86400000L; // 24 hours in milliseconds

    // Generate JWT token
    public String generateToken(String username, List<String> authorities) {
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", authorities)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }

    // Extract username from the JWT token
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    // Extract the roles/authorities from the JWT token
    public List<SimpleGrantedAuthority> getAuthorities(String token) {
        List<String> roles = (List<String>) extractClaims(token).get("roles");
        return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    // Validate the JWT token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Helper method to extract claims from JWT token
    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
