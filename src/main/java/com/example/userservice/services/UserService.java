package com.example.userservice.services;

import com.example.userservice.exceptions.PasswordMisMatchException;
import com.example.userservice.exceptions.UserNotFoundException;
import com.example.userservice.models.Token;
import com.example.userservice.models.User;
import com.example.userservice.repositories.TokenRepository;
import com.example.userservice.repositories.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service("UserServiceBean")
public class UserService {
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;

    public UserService(BCryptPasswordEncoder bCryptPasswordEncoder, UserRepository userRepository,
                       TokenRepository tokenRepository) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
    }
    public User signup(String email, String name, String password) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setHashedPassword(bCryptPasswordEncoder.encode(password));
        user.setIsEmailVerified(true);
        user.setDeleted(false);
//        Save the user object to the database

        return userRepository.save(user);
    }

    public Token login(String email, String password) {
        if (email == null)
            return null;

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException("No user with this email " + email + " found");
        }

        User user = optionalUser.get();
        String hashedPassword = user.getHashedPassword();

        if (!bCryptPasswordEncoder.matches(password, hashedPassword)) {
//            Throuws new exception that password didn't match
//            throw new PasswordMisMatchException("Provided Password is incorrect for the user with email " + email);
            return null;
        }

//        Check if there's a valid token in the Token table for this user
        Token token = checkAndReturnTokenIfExist(user);
        return tokenRepository.save(token);
    }

    private Token checkAndReturnTokenIfExist(User user) {
        List<Optional<Token>> optionalToken = tokenRepository.findByUser(user);
        if (!optionalToken.isEmpty()) {
            LocalDate currentTime = LocalDate.now();
            for (Optional<Token> token : optionalToken) {
                if (token.isEmpty()) {
                    continue;
                }
                Token token1 = token.get();
                LocalDate expiryDate = token1.getExpiryAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                if (!token1.getDeleted() && expiryDate.isAfter(currentTime)) {
                    return token1;
                }
            }
        }

        Token token = generateToken(user);
        return tokenRepository.save(token);
    }

    private Token generateToken(User user) {
        LocalDate currentTime = LocalDate.now();
        LocalDate thirtyDaysAhead = currentTime.plusDays(30);
        Date expiryDate = Date.from(thirtyDaysAhead.atStartOfDay(ZoneId.systemDefault()).toInstant());
//        Generate a token value Alphanumeric String

        Token token = new Token();
        token.setExpiryAt(expiryDate);
        token.setValue(RandomStringUtils.randomAlphanumeric(128));
        token.setUser(user);
        token.setDeleted(false);
        return token;
    }
    public void logout(String tokenValue) {
        Optional<Token> optionalToken = tokenRepository.findByValueAndDeleted(tokenValue, false);
        if (optionalToken.isPresent()) {
            Token token = optionalToken.get();
            token.setDeleted(true);
            tokenRepository.save(token);
        }
    }

    public User validateToken(String tokenValue) {
        Optional<Token> optionalToken = tokenRepository.findByValueAndDeletedAndExpiryAtGreaterThan(
                tokenValue,
                false,
                new Date()
        );

        if (optionalToken.isEmpty()) {
//            Token not valid, throw new exception
            return null;
        }
        return optionalToken.get().getUser();
    }
}
