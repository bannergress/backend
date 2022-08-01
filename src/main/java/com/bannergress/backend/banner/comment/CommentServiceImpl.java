package com.bannergress.backend.banner.comment;

import com.bannergress.backend.banner.Banner;
import com.bannergress.backend.banner.BannerService;
import com.bannergress.backend.dto.CommentDto;
import com.bannergress.backend.user.User;
import com.bannergress.backend.user.UserService;
import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/** Default implementation of {@link CommentService}. */
@Service
@Transactional
public class CommentServiceImpl implements CommentService {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private BannerService bannerService;

    @Override
    public Comment createOrUpdate(UUID uuid, Banner banner, CommentDto commentDto) {
        User user = userService.getOrCreateCurrentUser();
        Comment comment = commentRepository.findById(uuid).orElseGet(() -> createComment(uuid, banner, user));
        Preconditions.checkArgument(user.equals(comment.getUser()));
        comment.setRatingAccessibility(commentDto.ratingAccessibility);
        comment.setRatingRoundTheClock(commentDto.ratingRoundTheClock);
        comment.setComment(commentDto.comment);
        comment.setRatingOverall(commentDto.ratingOverall);
        comment.setRatingPassphrases(commentDto.ratingPassphrases);
        comment.setType(commentDto.type);
        comment = commentRepository.save(comment);
        banner.getComments().add(comment);
        bannerService.calculateData(banner);
        return comment;
    }

    private Comment createComment(UUID uuid, Banner banner, User user) {
        Comment result = new Comment();
        result.setUuid(uuid);
        result.setBanner(banner);
        result.setCreated(Instant.now());
        result.setUser(user);
        return result;
    }

    @Override
    public void delete(UUID uuid) {
        Comment comment = commentRepository.findById(uuid).orElseThrow();
        Banner banner = comment.getBanner();
        banner.getComments().remove(comment);
        commentRepository.delete(comment);
        bannerService.calculateData(banner);
    }
}
