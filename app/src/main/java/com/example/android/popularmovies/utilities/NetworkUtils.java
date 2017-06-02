package com.example.android.popularmovies.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import com.example.android.popularmovies.BuildConfig;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by Deep on 5/2/2017.
 */

public final class NetworkUtils {
    private static final String TAG = NetworkUtils.class.getSimpleName();
    //Url to call themoviedb api.
    private static final String MOVIE_BASE_URL = "https://api.themoviedb.org/3/movie/";
    private static final String APPID_PARAM = "api_key";



    /**
     *Builds the url using movie sort by.
     * @param movieSorting
     * @param context
     * @return
     */
    public static URL buildUrl(String movieSorting,Context context) {

        String uri = MOVIE_BASE_URL + movieSorting + "?";
        Uri builtUri = Uri.parse(uri).buildUpon()
                .appendQueryParameter(APPID_PARAM, BuildConfig.movie_db_api_key)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "Built URI " + url);

        return url;
    }

    /**
     *This method returns the entire result from the HTTP response.
     * @param url
     * @return
     * @throws IOException
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
