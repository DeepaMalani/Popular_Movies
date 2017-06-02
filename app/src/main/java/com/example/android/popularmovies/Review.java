package com.example.android.popularmovies;

/**
 * Created by Deep on 5/30/2017.
 */

public class Review {
    public String Author;
    public String Content;
    public String ReviewUrl;

    public Review(String Author, String Content, String ReviewUrl) {
        this.Author = Author;
        this.Content = Content;
        this.ReviewUrl = ReviewUrl;
    }
}
