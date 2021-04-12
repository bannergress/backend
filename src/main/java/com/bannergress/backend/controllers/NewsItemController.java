package com.bannergress.backend.controllers;

import com.bannergress.backend.dto.NewsItemDto;
import com.bannergress.backend.entities.NewsItem;
import com.bannergress.backend.repositories.NewsItemRepository;
import com.bannergress.backend.security.Roles;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST endpoint for news items.
 */
@RestController
@CrossOrigin
public class NewsItemController {
    private final NewsItemRepository newsItemRepository;

    public NewsItemController(final NewsItemRepository newsItemRepository) {
        this.newsItemRepository = newsItemRepository;
    }

    /**
     * Returns all available news items.
     *
     * @return News items.
     */
    @GetMapping("/news")
    public Collection<NewsItemDto> list() {
        Collection<NewsItem> items = newsItemRepository.findAll();
        return items.stream().map(NewsItemController::toDto).collect(Collectors.toUnmodifiableList());
    }

    /**
     * Returns a single news item by ID.
     *
     * @param id ID.
     * @return News item.
     */
    @GetMapping("/news/{id}")
    public ResponseEntity<NewsItemDto> get(@PathVariable final long id) {
        Optional<NewsItem> item = newsItemRepository.findById(id);
        return ResponseEntity.of(item.map(NewsItemController::toDto));
    }

    /**
     * Creates a new news item.
     *
     * @param item News item.
     * @return Created news item.
     */
    @RolesAllowed(Roles.MANAGE_NEWS)
    @PostMapping("/news")
    public NewsItemDto post(@Valid @RequestBody final NewsItemDto item) {
        NewsItem newsItem = new NewsItem();
        newsItem.setContent(item.content);
        newsItem.setCreated(Instant.now());
        newsItem = newsItemRepository.save(newsItem);
        return toDto(newsItem);
    }

    /**
     * Updates an existing news item.
     *
     * @param id   News item ID.
     * @param item News item.
     * @return Updated news item.
     */
    @RolesAllowed(Roles.MANAGE_NEWS)
    @PutMapping("/news/{id}")
    public ResponseEntity<NewsItemDto> put(@PathVariable final long id, @Valid @RequestBody final NewsItemDto item) {
        Optional<NewsItem> newsItem = newsItemRepository.findById(id);
        if (newsItem.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        newsItem.get().setContent(item.content);
        newsItem = Optional.of(newsItemRepository.save(newsItem.get()));
        return ResponseEntity.of(newsItem.map(NewsItemController::toDto));
    }

    /**
     * Deletes an existing news item.
     *
     * @param id News item ID.
     */
    @RolesAllowed(Roles.MANAGE_NEWS)
    @DeleteMapping("/news/{id}")
    public void delete(@PathVariable final long id) {
        try {
            newsItemRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    private static NewsItemDto toDto(final NewsItem newsItem) {
        NewsItemDto result = new NewsItemDto();
        result.id = newsItem.getId();
        result.content = newsItem.getContent();
        result.created = newsItem.getCreated();
        return result;
    }
}
