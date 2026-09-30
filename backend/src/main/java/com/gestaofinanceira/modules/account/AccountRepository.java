package com.gestaofinanceira.modules.account;

import com.gestaofinanceira.modules.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    List<Account> findByUserOrderByCreatedAtAsc(User user);
    Optional<Account> findByIdAndUser(UUID id, User user);
}
