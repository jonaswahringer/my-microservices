package com.jonny.authservice.service;

import com.jonny.authservice.dto.UserDto;
import com.jonny.authservice.entities.ShopUser;
import com.jonny.authservice.exception.AppException;
import com.jonny.authservice.mappers.UserMapper;
import com.jonny.authservice.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Value("${security.jwt.token.secret-key:secret-key}")
    private String secretKey;

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public String createToken(ShopUser user) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + 3600000); // 1 hour

        return Jwts.builder()
                .setSubject(user.getLogin())
                .claim("role", user.getRole())
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public UserDto validateToken(String token) {
        String login = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        Optional<ShopUser> userOptional = userRepository.findByLogin(login);

        if (userOptional.isEmpty()) {
            throw new AppException("User not found", HttpStatus.NOT_FOUND);
        }

        ShopUser user = userOptional.get();
        return userMapper.toUserDto(user, createToken(user));
    }

    public Optional<String> extractRoleFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
            return Optional.ofNullable((String) claims.get("role"));
        } catch (Exception e) {
            // Handle exception (e.g., token validation error)
            return Optional.empty();
        }
    }
}
