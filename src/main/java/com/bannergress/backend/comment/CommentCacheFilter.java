package com.bannergress.backend.comment;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** No shared caches may retain author-only bodies, capabilities or moderation responses. */
@Component
public class CommentCacheFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (path.startsWith("/comments/") || path.startsWith("/user/comments")
            || path.startsWith("/moderation/comments/") || path.matches("/bnrs/[^/]+/comments")) {
            response.setHeader("Cache-Control", "private, no-store");
            response.addHeader("Vary", "Authorization");
        }
        chain.doFilter(request, response);
    }
}
