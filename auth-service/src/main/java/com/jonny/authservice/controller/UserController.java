package com.jonny.authservice.controller;


import com.jonny.authservice.dto.CredentialsDto;
import com.jonny.authservice.dto.UserCreationDto;
import com.jonny.authservice.dto.UserDto;
import com.jonny.authservice.service.MailService;
import com.jonny.authservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth/users")
@Slf4j
public class UserController {

    private final UserService userService;
    private final MailService mailService;

    @PostMapping("/signIn")
    public ResponseEntity<UserDto> signIn(@RequestBody CredentialsDto credentialsDto) {
        log.info("Trying to login {}", credentialsDto.getLogin());
        return ResponseEntity.ok(userService.signIn(credentialsDto));
    }

    @PostMapping("/signUp")
    public ResponseEntity<UserDto> signUp(@RequestBody UserCreationDto userCreationDto) {
        log.info("Creating new user {}", userCreationDto.getLogin());
        UserDto user = userService.signUp(userCreationDto);
        mailService.sendUserWelcomeMail(user);
        return ResponseEntity.ok(user);
    }
}
