package com.example.Shares.auth.repository;

import com.example.Shares.auth.entity.BankCardEntity;
import com.example.Shares.auth.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankCardRepository extends JpaRepository<BankCardEntity, Long> {

    Optional<BankCardEntity> findByCardNumber(String cardNumber);
    List<BankCardEntity> findByUser(UserEntity user);

}
