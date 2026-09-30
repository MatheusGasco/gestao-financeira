package com.gestaofinanceira.modules.investment;

import com.gestaofinanceira.modules.asset.Asset;
import com.gestaofinanceira.modules.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InvestmentTransactionRepository extends JpaRepository<InvestmentTransaction, UUID> {
    List<InvestmentTransaction> findByUserOrderByTransactionDateAscCreatedAtAsc(User user);
    List<InvestmentTransaction> findByUserAndAssetOrderByTransactionDateAscCreatedAtAsc(User user, Asset asset);
    List<InvestmentTransaction> findByUserOrderByTransactionDateDescCreatedAtDesc(User user);
}
