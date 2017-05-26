package com.example.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmovies.data.MovieContract;

import java.util.ArrayList;

import static com.example.android.popularmovies.R.string.pref_sorting_favorite;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private MovieAdapter mMovieAdapter;
    private TextView mErrorMessageDisplay;
    private String mSortBy;
    private String mSortByOnCallBacks = "";
    private static final String LIFECYCLE_CALLBACKS_TEXT_KEY = "callbacks";
    private static final int MOVIES_LOADER_ID = 0;
    private static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_SORT_BY,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_USER_RATING,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_IMAGE_THUMBNAIL_PATH
    };

    /*
    * We store the indices of the values in the array of Strings above to more quickly be able to
    * access the data from our query. If the order of the Strings above changes, these indices
    * must be adjusted to match the order of the Strings.
    */
    public static final int INDEX_ID = 0;
    public static final int INDEX_MOVIE_ID = 1;
    public static final int INDEX_MOVIE_SORT_BY = 2;
    public static final int INDEX_POSTER_PATH = 3;
    public static final int INDEX_TITLE = 4;
    public static final int INDEX_OVERVIEW = 5;
    public static final int INDEX_USER_RATING = 6;
    public static final int INDEX_RELEASE_DATE = 7;
    public static final int INDEX_IMAGE_THUMBNAIL_PATH = 8;

  @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


      // If savedInstanceState is not null and contains LIFECYCLE_CALLBACKS_TEXT_KEY, set that text on our TextView

      if (savedInstanceState!=null)
      {
          if(savedInstanceState.containsKey(LIFECYCLE_CALLBACKS_TEXT_KEY)) {
              mSortByOnCallBacks = savedInstanceState.getString(LIFECYCLE_CALLBACKS_TEXT_KEY);
          }
      }

        /*
         * Using findViewById, we get a reference to our RecyclerView from xml. This allows us to
         * do things like set the adapter of the RecyclerView and toggle the visibility.
         */
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_movie);

        /* This TextView is used to display errors and will be hidden if there are no errors */
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);


        /*
         * LinearLayoutManager can support HORIZONTAL or VERTICAL orientations. The reverse layout
         * parameter is useful mostly for HORIZONTAL layouts that should reverse for right to left
         * languages.
         */
        int numberOfColumns = 2;
        GridLayoutManager layoutManager
                = new GridLayoutManager(MainActivity.this,numberOfColumns);

        mRecyclerView.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mRecyclerView.setHasFixedSize(true);

        /*
         * The MovieAdapter is responsible for linking our movie data with the Views.
         */
        mMovieAdapter = new MovieAdapter(MainActivity.this,new ArrayList<Movie>());

        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        mRecyclerView.setAdapter(mMovieAdapter);
        mMovieAdapter.setOnItemClickListener(new MovieAdapter.MovieAdapterOnClickHandler() {
            @Override
            public void onClick(Movie movie) {
                Intent intent = new Intent(MainActivity.this,MovieDetail.class);
                intent.putExtra(Intent.EXTRA_TEXT,movie);
                startActivity(intent);
            }
        });
        Bundle bundleForLoader = null;
        mSortBy = setupSharedPreferences();
        Toast.makeText(MainActivity.this,mSortBy,Toast.LENGTH_SHORT).show();
        getSupportLoaderManager().initLoader(MOVIES_LOADER_ID, null, MainActivity.this);

      if(isOnline())
           /* Once all of our views are setup, we can load the movie data. */
          loadMovieData();
      else {
          showErrorMessage(getString(R.string.network_msg));
      }

    }
    private String setupSharedPreferences() {
        String sortBy;
        // Get all of the values from shared preferences to set it up
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        sortBy =sharedPreferences.getString(getString(R.string.pref_sorting_key),
                getString(R.string.pref_sorting_popularity));
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
       return  sortBy;

    }
        @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }


   @Override
    protected void onStart() {
        super.onStart();
   }
    private void loadMovieData()
    {
        //If device is rotated and shared preference is not change then FetchMoviesData will not execute.
        if(!mSortByOnCallBacks.equals(mSortBy)) {
            if (!mSortBy.equals(pref_sorting_favorite)) {
                FetchMoviesData movieData = new FetchMoviesData(MainActivity.this);
                movieData.execute(mSortBy);
            }
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(LIFECYCLE_CALLBACKS_TEXT_KEY,mSortBy);
    }
    @Override
    protected void onResume() {
        super.onResume();

    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri movieUri = MovieContract.MovieEntry.CONTENT_URI;
        if (mSortBy.equals(R.string.pref_sorting_favorite))
        {
            return new CursorLoader(MainActivity.this,
                    movieUri,
                    MOVIE_COLUMNS,
                    MovieContract.MovieEntry.COLUMN_IS_FAVORITE + " = ?",
                    new String[]{String.valueOf(MovieContract.FAVORITE_MOVIE) },
                    null);
        }
        else {
            return new CursorLoader(MainActivity.this,
                    movieUri,
                    MOVIE_COLUMNS,
                    MovieContract.MovieEntry.COLUMN_MOVIE_SORT_BY + " = ?",
                    new String[]{mSortBy},
                    null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
     mMovieAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mMovieAdapter.swapCursor(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    private void showMovieDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the weather data is visible */
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the error message visible and hide the weather
     * View.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showErrorMessage(String message) {
        /* First, hide the currently visible data */
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setText(message);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(getString(R.string.pref_sorting_key)))
        {
         mSortBy = sharedPreferences.getString(getString(R.string.pref_sorting_key),
                 getString(R.string.pref_sorting_popularity));
        }
       loadMovieData();
       getSupportLoaderManager().restartLoader(MOVIES_LOADER_ID, null, MainActivity.this);
    }
}
