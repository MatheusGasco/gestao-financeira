package com.gestaofinanceira.modules.transaction;

import com.gestaofinanceira.modules.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    @Query("SELECT t FROM Transaction t WHERE t.user = :user " +
           "AND (:startDate IS NULL OR t.transactionDate >= :startDate) " +
           "AND (:endDate IS NULL OR t.transactionDate <= :endDate) " +
           "AND (:type IS NULL OR t.type = :type) " +
           "AND (:accountId IS NULL OR t.account.id = :accountId OR t.destinationAccount.id = :accountId) " +
           "ORDER BY t.transactionDate DESC, t.createdAt DESC")
    List<Transaction> findFiltered(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("type") TransactionType type,
            @Param("accountId") UUID accountId
    );

    List<Transaction> findTop5ByUserOrderByTransactionDateDescCreatedAtDesc(User user);

    List<Transaction> findByUserAndTransactionDateBetween(User user, LocalDate start, LocalDate end);

    Optional<Transaction> findByIdAndUser(UUID id, User user);
}
