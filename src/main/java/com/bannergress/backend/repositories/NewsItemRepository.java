package com.bannergress.backend.repositories;

import com.bannergress.backend.entities.NewsItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repository for news items.
 */
public interface NewsItemRepository extends JpaRepository<NewsItem, UUID> {

}
