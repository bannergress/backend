package com.bannergress.backend.banner.picture;

public enum PictureType {
    jpeg("image/jpeg"), webp("image/webp");

    private final String mediaType;

    private PictureType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getMediaType() {
        return mediaType;
    }
}
