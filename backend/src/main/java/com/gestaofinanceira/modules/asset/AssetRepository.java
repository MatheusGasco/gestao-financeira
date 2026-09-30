package com.gestaofinanceira.modules.asset;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssetRepository extends JpaRepository<Asset, UUID> {
    Optional<Asset> findByTickerIgnoreCase(String ticker);
    boolean existsByTickerIgnoreCase(String ticker);
}
