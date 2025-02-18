package com.example.Shares.QRcode;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QRCodeRepository extends JpaRepository<QRCodeEntity, Long> {
    Optional<QRCodeEntity> findByTransactionId(String transactionId);
}
