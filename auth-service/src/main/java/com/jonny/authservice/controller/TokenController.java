package com.jonny.authservice.controller;


import com.jonny.authservice.dto.UserDto;
import com.jonny.authservice.service.TokenService;
import com.jonny.authservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/token")
@Slf4j
public class TokenController {

    private final TokenService tokenService;

    @PostMapping("/validate")
    public ResponseEntity<UserDto> validateToken(@RequestParam String token) {
        log.info("Trying to validate token {}", token);
        return ResponseEntity.ok(tokenService.validateToken(token));
    }

    /*
    @PostMapping("/role")
    public ResponseEntity<?> getRoleFromToken(@RequestParam String token, @RequestParam String role) {
        log.info("Trying to get role from token {}", token);
        var tokenRole = tokenService.extractRoleFromToken(token);
        if(tokenRole.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if(tokenRole.get() == role) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.ok(false);
        }
    }
    */
}
