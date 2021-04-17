package com.bannergress.backend.services.impl;

import com.bannergress.backend.entities.Banner;
import com.bannergress.backend.entities.Mission;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.SortedMap;
import java.util.TreeMap;

class TestBannerPictureServiceImpl {

    @Test
    void testCreatePicture() throws IOException {
        BannerPictureServiceImpl bannerPictureService = new BannerPictureServiceImpl(null, 0.92f);
        Banner banner = new Banner();
        banner.setNumberOfMissions(6);
        SortedMap<Integer, Mission> missions = new TreeMap<>();
        for (String urlSpec : Arrays.asList(
            "https://lh3.googleusercontent.com/dEd_m6qhPJYxl1U4zkf4Jyhud524gAgiWzsMzQ7iZZ3Cz-kVWAEMzjO7gt13I8D9mE7zmz9npVE__AuWSK_EWg",
            "https://lh3.googleusercontent.com/bRPMh-uuJKs4jJmT0lMcQYXf9aRfNS-Td7j7hChq35wHllR3NazzH__S-Ngxyl88fu_g6gu-1tOasWHNnYrz",
            "https://lh3.googleusercontent.com/0hAQj7kaN1aXWqvmo9OQ5SL80NXRCgqU7j9QukALEdhsfMz5ooMoHEf6TaeBpMO0XxqhnRn-ZphIu5MOXQb9",
            "https://lh3.googleusercontent.com/eTUvqm4aLzHFd40egxDpVIPnvToFF_WhGlvQTBpEsO-UXjUMVu_p30M3aIZN3p5JhUXDO7gbNJr_MLRsBsI",
            "https://lh3.googleusercontent.com/hmOH79KZ5_to3GYJmTyA5YNxgmzHuaAUJS-Lvgd8Vq_jgaGbjQU8xSrLI8JauTu_2LFXcjYz-oOxjiISn3k",
            "https://lh3.googleusercontent.com/5mhMU3Ax5G4McyxfFJy15_tnQ7scjChNawrj7qFUZWVruRB8qf8GhjwhJPwrmPEFJRihMZfByq43HMYzD0OS")) {
            Mission m = new Mission();
            m.setTitle("" + missions.size());
            m.setPicture(new URL(urlSpec));
            m.setOnline(true);
            missions.put(Integer.valueOf(missions.size()), m);
        }
        banner.setMissions(missions);
        banner.getMissions().get(3).setOnline(false);

        byte[] pngData = bannerPictureService.createPicture(banner);

        try (OutputStream os = new FileOutputStream(new File("banner.jpg"))) {
            os.write(pngData);
        }
    }

}
