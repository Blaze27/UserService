package com.example.userservice.repositories;

import com.example.userservice.models.Token;
import com.example.userservice.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    @Override
    Token save(Token token);

    List<Optional<Token>> findByUser(User user);

    Optional<Token> findByValueAndDeleted(String tokenValue, boolean deleted);

    Optional<Token> findByValueAndDeletedAndExpiryAtGreaterThan(String tokenValue, boolean deleted, Date date);
}
