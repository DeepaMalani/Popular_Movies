package com.example.android.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Deep on 5/30/2017.
 */

public class ReviewLoader extends AsyncTaskLoader<List<Review>> {
    private final String LOG_TAG = ReviewLoader.class.getSimpleName();
    private final Context mContext;
    private final long mMovieId;
    List<Review> mReviews;

    public ReviewLoader(Context context, long movieId) {
        super(context);
        mContext = context;
        mMovieId = movieId;
    }

    @Override
    public List<Review> loadInBackground() {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String reviewJsonStr = null;

        try {
            // Construct the URL for the themoviedb api
            //https://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=21129ccd694a516f78fb06c6fae5f076

            final String MOVIE_BASE_URL = "https://api.themoviedb.org/3/movie/"+mMovieId+"/reviews?";
            final String SORT_PARAM = "sort_by";
            final String APPID_PARAM = "api_key";
            Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                    .appendQueryParameter(APPID_PARAM, BuildConfig.movie_db_api_key)
                    .build();
            URL url = new URL(builtUri.toString());
            // Log.v(LOG_TAG, "Built URI " + builtUri.toString());


            // Create the request to moviedb, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                reviewJsonStr = null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                reviewJsonStr = null;
            }
            reviewJsonStr = buffer.toString();
            return getReviewDataFromJson(reviewJsonStr);
            // Log.v(LOG_TAG, "Movie JSON string: " + movieJsonStr);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
            reviewJsonStr = null;
        }catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return  null;
    }
    private List<Review> getReviewDataFromJson(String reviewJsonStr)
            throws JSONException {


        // These are the names of the JSON objects that need to be extracted.
        final String RESULTS = "results";
        final String AUTHOR = "author";
        final String CONTENT = "content";
        final String REVIEW_URL = "url";


        try {
            JSONObject reviewJson = new JSONObject(reviewJsonStr);
            JSONArray reviewArray = reviewJson.getJSONArray(RESULTS);

            List<Review> listReview = new ArrayList<Review>();

            for (int i = 0; i < reviewArray.length(); i++) {
                String author;
                String content;
                String reviewUrl;

                // Get the JSON object representing the movie result
                JSONObject resultReview = reviewArray.getJSONObject(i);
                author = String.format(mContext.getString(R.string.format_movie_review),resultReview.getString(AUTHOR));
                content = resultReview.getString(CONTENT);
                reviewUrl = resultReview.getString(REVIEW_URL);
                listReview.add(new Review(author,content,reviewUrl));

            }


            Log.d(LOG_TAG, "Fetch Reviews Complete.");
            mReviews = listReview;
            return listReview;

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override
    public void deliverResult(List<Review> reviews) {
        mReviews = reviews;
        super.deliverResult(reviews);

    }

    /**
     * Handles a request to start the Loader.
     */
    @Override
    protected void onStartLoading() {
        if (mReviews != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(mReviews);
        }
        if (takeContentChanged() || mReviews == null) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }

}

