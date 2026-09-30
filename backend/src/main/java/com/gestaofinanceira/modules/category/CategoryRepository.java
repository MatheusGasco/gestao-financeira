package com.gestaofinanceira.modules.category;

import com.gestaofinanceira.modules.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    @Query("SELECT c FROM Category c WHERE c.user = :user OR c.isDefault = true ORDER BY c.name ASC")
    List<Category> findAllAvailableForUser(@Param("user") User user);

    List<Category> findByUserAndType(User user, CategoryType type);
}
