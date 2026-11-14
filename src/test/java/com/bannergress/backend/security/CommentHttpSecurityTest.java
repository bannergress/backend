package com.bannergress.backend.security;

import com.bannergress.backend.comment.*;
import jakarta.servlet.ServletContext;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CommentHttpSecurityTest {
    private AnnotationConfigWebApplicationContext context;
    private MockMvc mvc;

    @Configuration
    @EnableWebMvc
    @org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
    @Import({KeycloakSecurityConfig.class, CommentController.class, CommentModerationController.class})
    static class Config {
        @Bean
        CommentService comments() {
            return mock(CommentService.class);
        }

        @Bean
        CommentOutbox outbox() {
            return mock(CommentOutbox.class);
        }

        @Bean
        JwtDecoder decoder() {
            return mock(JwtDecoder.class);
        }

        @Bean
        CommentSettings settings() {
            return new CommentSettings(true, "TEST", true, true, 3, 10, 100,
                "bot", Set.of(123L), "https://login.bannergress.com/auth/realms/bannergress-test");
        }
    }

    @BeforeEach
    void setup() {
        ServletContext servletContext = new MockServletContext();
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(servletContext);
        context.register(Config.class);
        context.refresh();
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity())
            .addFilters(new CommentCacheFilter()).build();
    }

    @AfterEach
    void close() {
        context.close();
    }

    @Test
    void anonymousCannotReadOrSubmitAndRegularUserCannotModerate() throws Exception {
        mvc.perform(get("/bnrs/banner/comments")).andExpect(status().isUnauthorized());
        mvc.perform(post("/bnrs/banner/comments").contentType("application/json")
            .content("{\"requestId\":\"00000000-0000-0000-0000-000000000001\",\"text\":\"test\"}"))
            .andExpect(status().isUnauthorized());
        mvc.perform(get("/moderation/comments/TEST").with(jwt())).andExpect(status().isForbidden());
    }

    @Test
    void signedInReadIsUncacheableAndInvalidWritesAreRejected() throws Exception {
        mvc.perform(get("/bnrs/banner/comments").with(jwt())).andExpect(status().isOk())
            .andExpect(header().string("Cache-Control", "private, no-store"));
        mvc.perform(post("/bnrs/banner/comments").with(jwt()).contentType("application/json")
            .content("{\"text\":\" \"}")).andExpect(status().isBadRequest());
    }

    @Test
    void testCallbackCannotSelectProductionAndModeratorReasonIsRequired() throws Exception {
        var bot = jwt().authorities(new SimpleGrantedAuthority("ROLE_moderate-comments"));
        mvc.perform(get("/moderation/comments/PROD").with(bot)).andExpect(status().isForbidden());
        mvc.perform(post("/moderation/comments/TEST/00000000-0000-0000-0000-000000000001/decisions")
            .with(bot).contentType("application/json")
            .content("{\"requestId\":\"00000000-0000-0000-0000-000000000002\",\"expectedVersion\":0,\"action\":\"REMOVE\",\"moderatorId\":123}"))
            .andExpect(status().isBadRequest());
    }
}
