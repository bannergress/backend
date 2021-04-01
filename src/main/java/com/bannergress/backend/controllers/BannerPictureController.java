package com.bannergress.backend.controllers;

import com.bannergress.backend.entities.BannerPicture;
import com.bannergress.backend.services.BannerPictureService;
import com.google.common.io.ByteStreams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

@Controller
public class BannerPictureController {
    @Autowired
    private BannerPictureService bannerPictureService;

    @RequestMapping(value = "/banners/pictures/{hash}", method = RequestMethod.GET, produces = {"image/jpeg"})
    public void getFile(@PathVariable String hash, HttpServletResponse response) throws IOException {
        Optional<BannerPicture> bannerPicture = bannerPictureService.findByHash(hash);
        if (bannerPicture.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        ByteStreams.copy(new ByteArrayInputStream(bannerPicture.get().getPicture()), response.getOutputStream());
    }
}
