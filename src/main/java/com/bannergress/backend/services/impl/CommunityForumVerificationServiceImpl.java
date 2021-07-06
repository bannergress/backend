package com.bannergress.backend.services.impl;

import com.bannergress.backend.dto.RSS;
import com.bannergress.backend.services.VerificationService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
public class CommunityForumVerificationServiceImpl implements VerificationService {
    private final OkHttpClient client;

    private final String url;

    public CommunityForumVerificationServiceImpl(
        @Value(value = "${verification.url:https://community.ingress.com/en/activity/feed.rss}") String url) {
        this.client = new OkHttpClient.Builder().cache(null).build();
        this.url = url;
    }

    @Override
    public Optional<String> verify(String agent, UUID verificationToken) {
        RSS rss = loadRssFeed();
        return verify(agent, verificationToken, rss);
    }

    private RSS loadRssFeed() {
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            XmlMapper xmlMapper = new XmlMapper();
            xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            RSS rss = xmlMapper.readValue(response.body().charStream(), RSS.class);
            return rss;
        } catch (IOException ex) {
            throw new RuntimeException("failed to load RSS feed", ex);
        }
    }

    private Optional<String> verify(String agent, UUID verificationToken, RSS rss) {
        String tokenString = verificationToken.toString();
        for (RSS.Channel channel : rss.channels) {
            for (RSS.Item item : channel.items) {
                if (agent.equalsIgnoreCase(item.creator) && item.description != null
                    && item.description.toLowerCase().contains(tokenString)) {
                    return Optional.of(item.creator);
                }
            }
        }
        return Optional.empty();
    }
}
