package com.jonny.authservice.service;

import com.jonny.authservice.dto.CredentialsDto;
import com.jonny.authservice.dto.UserCreationDto;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.CharBuffer;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public UserDto signIn(CredentialsDto credentialsDto) {
        var user = userRepository.findByLogin(credentialsDto.getLogin())
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        if (passwordEncoder.matches(CharBuffer.wrap(credentialsDto.getPassword()), user.getPassword())) {
            return userMapper.toUserDto(user, tokenService.createToken(user));
        }

        throw new AppException("Invalid password", HttpStatus.BAD_REQUEST);
    }

    public UserDto signUp(UserCreationDto userCreationDto) {
        var userOptional = userRepository.findByLogin(userCreationDto.getLogin());
        if (userOptional.isPresent()) {
            throw new AppException("User already in database", HttpStatus.BAD_REQUEST);
        }

        var user = userMapper.toShopUser(
                userCreationDto, passwordEncoder.encode(CharBuffer.wrap(userCreationDto.getPassword())));
        userRepository.save(user);

        return UserDto.builder()
                .login(user.getLogin())
                .build();
    }
}
