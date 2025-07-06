package com.nori.personal_finance.repository;

import com.nori.personal_finance.model.Category;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
  List<Category> findByUserEmail(String userEmail);

  @Transactional
  void deleteAllByUserEmail(String userEmail);
}
