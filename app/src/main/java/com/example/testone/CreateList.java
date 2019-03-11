package com.example.testone;

import android.graphics.Bitmap;

class CreateList {
    private String image_title;
    private String image_path;
    private Bitmap image;

    public String getImage_title() {
        return image_title;
    }

    public void setImage_title(String android_version_name) {
        this.image_title = android_version_name;
    }

    public String getImage_ID() {
        return image_path;
    }

    public void setImage_ID(String image_path) {
        this.image_path = image_path;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }
}