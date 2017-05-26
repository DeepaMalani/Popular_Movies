package com.example.android.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Deep on 5/3/2017.
 */

public class Movie implements Parcelable {
    public final long movieId;
    public final String movieTitle;
    public final String movieOverView;
    public final  double movieUserRating;
    public final String movieReleaseDate;
    public final String movieImageThumbNailPath;
    public final String moviePosterPath;
    /**
     * Create a new movie from discrete values
     */
    public Movie(long movieId,String moviePosterPath,String movieTitle,String movieOverView,double movieUserRating,String movieReleaseDate,String movieImageThumbNailPath) {
        this.movieId = movieId;
        this.moviePosterPath = moviePosterPath;
        this.movieTitle = movieTitle;
        this.movieOverView = movieOverView;
        this.movieUserRating= movieUserRating;
        this.movieReleaseDate = movieReleaseDate;
        this.movieImageThumbNailPath = movieImageThumbNailPath;

    }
    /**
     * Create a new movie from a data Parcel
     */
    protected Movie(Parcel in) {

        this.movieId = in.readLong();
        this.movieTitle = in.readString();
        this.movieOverView = in.readString();
        this.movieUserRating= in.readDouble();
        this.movieReleaseDate = in.readString();
        this.movieImageThumbNailPath = in.readString();
        this.moviePosterPath = in.readString();
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeLong(movieId);
        dest.writeString(movieTitle);
        dest.writeString(movieOverView);
        dest.writeDouble(movieUserRating);
        dest.writeString(movieReleaseDate);
        dest.writeString(movieImageThumbNailPath);
        dest.writeString(moviePosterPath);
    }


    @Override
    public int describeContents() {
        return 0;
    }
    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
