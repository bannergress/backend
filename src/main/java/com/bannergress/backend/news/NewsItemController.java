package com.bannergress.backend.news;

import com.bannergress.backend.security.Roles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * REST endpoint for news items.
 */
@RestController
class NewsItemController {
    private final NewsItemRepository newsItemRepository;

    NewsItemController(final NewsItemRepository newsItemRepository) {
        this.newsItemRepository = newsItemRepository;
    }

    /**
     * Returns all available news items.
     *
     * @return News items.
     */
    @GetMapping("/news")
    Collection<NewsItemDto> list() {
        Collection<NewsItem> items = newsItemRepository.findAll();
        return items.stream().map(NewsItemController::toDto).toList();
    }

    /**
     * Returns a single news item by ID.
     *
     * @param id ID.
     * @return News item.
     */
    @GetMapping("/news/{uuid}")
    ResponseEntity<NewsItemDto> get(@PathVariable final UUID uuid) {
        Optional<NewsItem> item = newsItemRepository.findById(uuid);
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
    NewsItemDto post(@Valid @RequestBody final NewsItemDto item) {
        NewsItem newsItem = new NewsItem();
        newsItem.setContent(item.content());
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
    @PutMapping("/news/{uuid}")
    ResponseEntity<NewsItemDto> put(@PathVariable final UUID uuid, @Valid @RequestBody final NewsItemDto item) {
        NewsItem newsItem = newsItemRepository.findById(uuid)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
        newsItem.setContent(item.content());
        newsItem = newsItemRepository.save(newsItem);
        return ResponseEntity.ok(toDto(newsItem));
    }

    /**
     * Deletes an existing news item.
     *
     * @param id News item ID.
     */
    @RolesAllowed(Roles.MANAGE_NEWS)
    @DeleteMapping("/news/{uuid}")
    void delete(@PathVariable final UUID uuid) {
        newsItemRepository.deleteById(uuid);
    }

    private static NewsItemDto toDto(final NewsItem newsItem) {
        return new NewsItemDto(newsItem.getUuid(), newsItem.getContent(), newsItem.getCreated());
    }
}
