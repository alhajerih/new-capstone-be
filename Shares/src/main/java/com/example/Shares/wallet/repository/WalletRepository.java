package com.example.Shares.wallet.repository;

import com.example.Shares.hub.entity.HubEntity;
import com.example.Shares.wallet.entity.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface WalletRepository extends JpaRepository<WalletEntity, Long> {
Optional<WalletEntity> findFirstByOrderByIdAsc();

}




