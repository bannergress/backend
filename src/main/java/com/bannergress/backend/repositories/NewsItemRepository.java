package com.bannergress.backend.repositories;

import com.bannergress.backend.entities.NewsItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for news items.
 */
@Repository
public interface NewsItemRepository extends JpaRepository<NewsItem, Long> {

}
