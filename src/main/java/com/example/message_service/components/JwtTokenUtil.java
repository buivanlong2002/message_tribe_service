package com.example.message_service.components;

import com.example.message_service.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtTokenUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);

    private final KeyProvider keyProvider;

    @Value("${jwt.expiration:3600}")
    private int expiration;

    /**
     * Tạo JWT token từ thông tin người dùng, ký bằng private key (RS256)
     */
    public String generateToken(User user) throws Exception {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("name", user.getUsername());
        claims.put("email", user.getEmail()); // Optional: vẫn có thể lưu trong claims

        PrivateKey privateKey = keyProvider.getPrivateKey();

        try {
            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(user.getUsername()) // Đặt username làm subject
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000L))
                    .signWith(privateKey, SignatureAlgorithm.RS256)
                    .compact();
        } catch (Exception e) {
            logger.error("Error generating token: {}", e.getMessage());
            throw new Exception("Error generating token: " + e.getMessage(), e);
        }
    }

    /**
     * Trích xuất toàn bộ claims từ token, xác thực bằng public key
     */
    public Claims extractClaims(String token) {
        PublicKey publicKey = keyProvider.getPublicKey();

        try {
            return Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            logger.error("Error extracting claims from token: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid JWT token");
        }
    }

    /**
     * Trích xuất một claim cụ thể từ token.
     */
    public <T> T extractClaims(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Kiểm tra token có hết hạn không.
     */
    public boolean isTokenExpired(String token) {
        Date expirationDate = extractClaims(token, Claims::getExpiration);
        return expirationDate.before(new Date());
    }

    /**
     * Lấy username từ token (lưu trong subject)
     */
    public String extractUsername(String token) {
        try {
            return extractClaims(token, Claims::getSubject);
        } catch (Exception e) {
            logger.error("Error extracting username from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Kiểm tra token hợp lệ (username trùng và chưa hết hạn)
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String email = extractUsername(token);
        return email != null && email.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public long getExpirationTime(String token) {
        return extractClaims(token, Claims::getExpiration).getTime();
    }

}
