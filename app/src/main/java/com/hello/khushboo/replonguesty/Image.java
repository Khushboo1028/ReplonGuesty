package com.hello.khushboo.replonguesty;

import android.graphics.Bitmap;

public class Image {

    Bitmap image;
    String imageUrl;

    public Image(String imageUrl,Bitmap image) {
        this.imageUrl = imageUrl;
        this.image=image;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
