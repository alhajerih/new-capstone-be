package com.example.Shares.savings.repository;

import com.example.Shares.savings.entity.SavingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavingsRepository extends JpaRepository<SavingsEntity, Long> {
}
