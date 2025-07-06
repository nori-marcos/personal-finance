package com.nori.personal_finance.repository;

import com.nori.personal_finance.model.Category;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

  @Query("SELECT c FROM Category c WHERE c.user.email = :email AND c.name <> 'Transferência'")
  List<Category> findByUserEmailExcludingTransferencia(@Param("email") String email);

  @Transactional
  void deleteAllByUserEmail(String userEmail);

  Optional<Category> findByNameAndUserEmail(String name, String userEmail);

  boolean existsByNameAndUserEmail(String name, String userEmail);
}
