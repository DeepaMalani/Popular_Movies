package com.example.android.popularmovies.utilities;

import android.content.ContentValues;
import android.content.Context;

import com.example.android.popularmovies.data.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility functions to handle TheMovieDb JSON data.
 */

public final class OpenMovieJsonUtils {

    private static final String LOG_TAG = OpenMovieJsonUtils.class.getSimpleName();


    /**
     * Take the String representing the complete movie data in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    public static ContentValues[] getMovieDataFromJson(String movieJsonStr, String movieSortBy, Context context)
            throws JSONException {


        // These are the names of the JSON objects that need to be extracted.
        final String MOVIE_RESULTS = "results";
        final String MOVIE_ID = "id";
        final String MOVIE_POSTER_PATH = "poster_path";
        final String MOVIE_TITLE = "original_title";
        final String MOVIE_OVERVIEW = "overview";
        final String MOVER_USER_RATING = "vote_average";
        final String MOVIE_RELEASE_DATE = "release_date";
        final String OWM_MESSAGE_CODE = "cod";

        JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(MOVIE_RESULTS);
           ContentValues[] movieContentValues = new ContentValues[movieArray.length()];

        for (int i = 0; i < movieArray.length(); i++) {

                long movieId;
                String posterPath;
                String title;
                String overview;
                double userRating;
                String releaseDate;

                // Get the JSON object representing the movie result
                JSONObject resultMovie = movieArray.getJSONObject(i);
                movieId = resultMovie.getInt(MOVIE_ID);
                posterPath = resultMovie.getString(MOVIE_POSTER_PATH);
                title = resultMovie.getString(MOVIE_TITLE);
                overview = resultMovie.getString(MOVIE_OVERVIEW);
                userRating = resultMovie.getDouble(MOVER_USER_RATING);
                releaseDate = resultMovie.getString(MOVIE_RELEASE_DATE);

                final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/";
                final String SIZE = "w185";

                 ContentValues movieValues = new ContentValues();
                  movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movieId);
                  movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_SORT_BY,movieSortBy);
                  movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, IMAGE_BASE_URL + SIZE + posterPath);
                  movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, title);
                  movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, overview);
                  movieValues.put(MovieContract.MovieEntry.COLUMN_USER_RATING, userRating);
                  movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, releaseDate);

                  movieContentValues[i] = movieValues;
              }
              return  movieContentValues;
    }

}
