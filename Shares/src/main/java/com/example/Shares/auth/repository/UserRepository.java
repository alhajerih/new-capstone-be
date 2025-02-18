package com.example.Shares.auth.repository;

import com.example.Shares.auth.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsernameIgnoreCase(String username);

Optional<UserEntity> findFirstByOrderByIdAsc();
    UserEntity findByUsername(String username);

    UserEntity findByOtp(String otp);

    Optional<UserEntity> findByCivilId(String civilId);
}
