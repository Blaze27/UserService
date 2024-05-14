package com.example.userservice.controllers;

import com.example.userservice.dtos.LoginRequestDto;
import com.example.userservice.dtos.LogoutRequestDto;
import com.example.userservice.dtos.SignupRequestDto;
import com.example.userservice.dtos.UserDto;
import com.example.userservice.models.Token;
import com.example.userservice.models.User;
import com.example.userservice.services.UserService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private UserService userService;

    public UserController(@Qualifier("UserServiceBean") UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public UserDto signUp(@RequestBody SignupRequestDto signupRequestDto) {
        User user = userService.signup(
                signupRequestDto.getEmail(),
                signupRequestDto.getName(),
                signupRequestDto.getPassword()
        );
        return UserDto.from(user);

    }

    @PostMapping("/login")
    public ResponseEntity<Token> login(@RequestBody LoginRequestDto loginDto) {
        Token token = userService.login(
                loginDto.getEmail(),
                loginDto.getPassword()
        );
        if (token == null) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(token, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequestDto logoutRequestDto) {
        userService.logout(logoutRequestDto.getToken());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/validate/{token}")
    public UserDto validateToken(@PathVariable("token") String token ) {
        User user = userService.validateToken(token);
        return UserDto.from(user);
    }
}
