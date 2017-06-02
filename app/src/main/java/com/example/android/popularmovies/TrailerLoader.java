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

public class TrailerLoader extends AsyncTaskLoader<List<Trailer>> {
    private final String LOG_TAG = TrailerLoader.class.getSimpleName();
    private final Context mContext;
    private final long mMovieId;
    List<Trailer> mTrailers;


    public TrailerLoader(Context context, long movieId) {
        super(context);
        mContext = context;
        mMovieId = movieId;

    }
    @Override
    public List<Trailer> loadInBackground() {


        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String videoJsonStr = null;

        try {

            final String MOVIE_BASE_URL = "https://api.themoviedb.org/3/movie/"+mMovieId+"/videos?";
            final String APPID_PARAM = "api_key";
            Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                    .appendQueryParameter(APPID_PARAM,BuildConfig.movie_db_api_key)
                    .build();
            URL url = new URL(builtUri.toString());
            // Create the request to moviedb, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                videoJsonStr = null;
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
                videoJsonStr = null;
            }
            videoJsonStr = buffer.toString();
            return getVideoDataFromJson(videoJsonStr);
            // Log.v(LOG_TAG, "Movie JSON string: " + movieJsonStr);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
            videoJsonStr = null;
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

    private List<Trailer> getVideoDataFromJson(String videoJsonStr)
            throws JSONException {


        // These are the names of the JSON objects that need to be extracted.
        final String RESULTS = "results";
        final String KEY = "key";
        final String NAME = "name";


        try {
            JSONObject videoJson = new JSONObject(videoJsonStr);
            JSONArray videoArray = videoJson.getJSONArray(RESULTS);

            List<Trailer> trailers = new ArrayList<Trailer>();

            for (int i = 0; i < videoArray.length(); i++) {
                String key;
                String name;

                // Get the JSON object representing the movie result
                JSONObject resultVideo = videoArray.getJSONObject(i);
                key = resultVideo.getString(KEY);
                name = String.format(mContext.getString(R.string.format_movie_trailer),i+1);
                // name = resultVideo.getString(NAME);

                trailers.add(new Trailer(key,name));

            }


            Log.d(LOG_TAG, "FetchVideoData Complete.");
            mTrailers = trailers;
            return trailers;

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
    public void deliverResult(List<Trailer> trailers) {

       mTrailers = trailers;
        super.deliverResult(trailers);
    }
    /**
     * Handles a request to start the Loader.
     */
    @Override
    protected void onStartLoading() {
        if (mTrailers != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(mTrailers);
        }
        if (takeContentChanged() || mTrailers == null) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }

}
