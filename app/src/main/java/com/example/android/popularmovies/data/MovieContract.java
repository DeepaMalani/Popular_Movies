package com.example.android.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Deep on 5/23/2017.
 */

public class MovieContract {
    public static final String CONTENT_AUTHORITY = "com.example.android.popularmovies";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.example.android.popularmovies.app/movie/ is a valid path for

    public static final String PATH_MOVIE = "movie";

    public static final  int FAVORITE_MOVIE = 1;

    public static final int NOT_FAVORITE_MOVIE = 0;

    /* Inner class that defines the table contents of the movie table */
    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        // These are special type prefixes that specify if a URI returns a list or a specific item
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;


        // Define a function to build a URI to find a specific movie by it's identifier
        public static Uri buildMovieUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static final String TABLE_NAME = "movie";


        // Movie id as returned by API, to identify the video to be used
        public static final String COLUMN_MOVIE_ID = "movie_id";

        // The movie setting string is what will be sent to API
        // as the sort order(popular movie or top rated movies).
        public static final String COLUMN_MOVIE_SORT_BY= "sort_by";

        // Poster path of the movie, as provided by API.
        public static final String COLUMN_POSTER_PATH = "poster_path";

        // Movie title
        public static final String COLUMN_TITLE = "original_title";
        //Movie overview(A plot synopsis)
        public static final String COLUMN_OVERVIEW = "overview";

        //Vote average(user rating) of movie
        public static final String COLUMN_USER_RATING = "user_rating";

        // Date, stored as long in milliseconds since the epoch
        public static final String COLUMN_RELEASE_DATE = "release_date";

        //Backdrop path(image thumbnail path)
        public static final String COLUMN_IMAGE_THUMBNAIL_PATH = "image_thumbnail_path";

        //Integer value to store favorite movie value,
        //possible values are 0 or 1
        public static final String COLUMN_IS_FAVORITE = "is_favorite";
    }
}
