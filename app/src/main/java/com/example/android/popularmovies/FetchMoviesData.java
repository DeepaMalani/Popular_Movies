package com.example.android.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;

import com.example.android.popularmovies.data.MovieContract;
import com.example.android.popularmovies.utilities.NetworkUtils;
import com.example.android.popularmovies.utilities.OpenMovieJsonUtils;

import java.net.URL;

/**
 * Created by Deep on 5/25/2017.
 */

public class FetchMoviesData extends AsyncTask<String, Void, Void> {
    private Context mContext ;

    FetchMoviesData(Context context)
    {
        mContext = context;
    }
    @Override
    protected Void doInBackground(String... params) {

        String sortBy = params[0];
        URL movieRequestUrl = NetworkUtils.buildUrl(sortBy,mContext);

        try {
            String jsonMovieResponse = NetworkUtils
                    .getResponseFromHttpUrl(movieRequestUrl);
            ContentValues[] movieValues = OpenMovieJsonUtils.getMovieDataFromJson(jsonMovieResponse,sortBy,mContext);

           if(movieValues!=null && movieValues.length !=0)
           {
               // delete movie old data based on sort by
               mContext.getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI,MovieContract.MovieEntry.COLUMN_MOVIE_SORT_BY + " = ?",new String[]{sortBy});

               /* Insert our new movie data into movie ContentProvider */
               mContext.getContentResolver().bulkInsert(MovieContract.MovieEntry.CONTENT_URI, movieValues);

           }
        }
       catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }
}
