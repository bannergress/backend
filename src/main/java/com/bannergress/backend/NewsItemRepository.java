package com.bannergress.backend;

import com.bannergress.backend.entities.NewsItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for news items.
 */
@Repository
public interface NewsItemRepository extends JpaRepository<NewsItem, Long> {

}
