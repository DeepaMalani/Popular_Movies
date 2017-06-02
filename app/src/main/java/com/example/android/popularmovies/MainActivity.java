package com.example.android.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmovies.data.MovieContract;
import com.example.android.popularmovies.utilities.NetworkUtils;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private MovieAdapter mMovieAdapter;
    private TextView mErrorMessageDisplay;
    private Button mButtonRefresh;
    private int mPosition = RecyclerView.NO_POSITION;
    private String mSortBy;
    private String mSortByOnCallBacks = "";
    private static final String LIFECYCLE_CALLBACKS_TEXT_KEY = "callbacks";
    private static final int MOVIES_LOADER_ID = 0;
    private static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_USER_RATING,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE

    };

    /*
    * We store the indices of the values in the array of Strings above to more quickly be able to
    * access the data from our query. If the order of the Strings above changes, these indices
    * must be adjusted to match the order of the Strings.
    */
    public static final int INDEX_ID = 0;
    public static final int INDEX_MOVIE_ID = 1;
//    public static final int INDEX_MOVIE_SORT_BY = 2;
    public static final int INDEX_POSTER_PATH = 2;
    public static final int INDEX_TITLE = 3;
    public static final int INDEX_OVERVIEW = 4;
    public static final int INDEX_USER_RATING = 5;
    public static final int INDEX_RELEASE_DATE = 6;


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
        mButtonRefresh = (Button)findViewById(R.id.button_refresh);
        mSortBy = setupSharedPreferences();
      //Set recycler view number of column based on Orientation
      int numberOfColumns ;
      if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
          numberOfColumns = 2;
      }
      else{
          numberOfColumns = 3;
      }


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
        mMovieAdapter = new MovieAdapter(MainActivity.this,new ArrayList<Movie>(),mSortBy);

        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        mRecyclerView.setAdapter(mMovieAdapter);
        mMovieAdapter.setOnItemClickListener(new MovieAdapter.MovieAdapterOnClickHandler() {
            @Override
            public void onClick(Movie movie) {
                Intent intent = new Intent(MainActivity.this,MovieDetail.class);
                intent.putExtra(Intent.EXTRA_TEXT,movie);
                intent.putExtra("SortBy",mSortBy);
                startActivity(intent);
            }
        });


//      if (mSortBy.equals(getResources().getString(R.string.pref_sorting_favorite))) {
//          getSupportLoaderManager().initLoader(MOVIES_LOADER_ID, null, MainActivity.this);
//      }
//      else {
//          if (NetworkUtils.isOnline(MainActivity.this)) {
//              getSupportLoaderManager().initLoader(MOVIES_LOADER_ID, null, MainActivity.this);
//          }
//      }
      //Set activity label based on sort by
      setActivityLabel(mSortBy);

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
       Toast.makeText(MainActivity.this,"On start",Toast.LENGTH_LONG).show();
       if (!mSortBy.equals(getResources().getString(R.string.pref_sorting_favorite))) {
           if (NetworkUtils.isOnline(MainActivity.this)) {
               getSupportLoaderManager().initLoader(MOVIES_LOADER_ID, null, MainActivity.this);
           /* Once all of our views are setup, we can load the movie data. */
               loadMovieData();
           }
           else {
               Toast.makeText(MainActivity.this,"Error",Toast.LENGTH_LONG).show();
               showErrorMessage(getString(R.string.network_msg));
           }
       }
       else {
           getSupportLoaderManager().initLoader(MOVIES_LOADER_ID, null, MainActivity.this);
           showMovieDataView();
       }
   }
    private void loadMovieData()
    {
        //If device is rotated and shared preference is not change then FetchMoviesData will not execute.
        if(!mSortByOnCallBacks.equals(mSortBy)) {
//            if (!mSortBy.equals(getResources().getString(R.string.pref_sorting_favorite))) {
                FetchMoviesData movieData = new FetchMoviesData(MainActivity.this);
                movieData.execute(mSortBy);
           // }
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

//    public boolean isOnline() {
//        ConnectivityManager cm =
//                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo netInfo = cm.getActiveNetworkInfo();
//        return netInfo != null && netInfo.isConnectedOrConnecting();
//    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (mSortBy.equals(getResources().getString(R.string.pref_sorting_favorite)))
        {
            Uri favoritesUri = MovieContract.FavoriteMoviesEntry.CONTENT_URI;
            return new CursorLoader(MainActivity.this,
                    favoritesUri,
                    null,
                    null,
                    null,
                    null);
        }
        else {
            Uri movieUri = MovieContract.MovieEntry.CONTENT_URI;
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
     mMovieAdapter.swapCursor(data,mSortBy);
        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
        mRecyclerView.smoothScrollToPosition(mPosition);
        if (data.getCount() != 0) showMovieDataView();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mMovieAdapter.swapCursor(null,mSortBy);
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
        /* First, make sure the error and refresh button is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mButtonRefresh.setVisibility(View.INVISIBLE);
        /* Then, make sure the movie data is visible */
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
        /* Then, show the error and refresh button*/
        mErrorMessageDisplay.setText(message);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
        mButtonRefresh.setVisibility(View.VISIBLE);
        //Set refresh button click event
        mButtonRefresh.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                onStart();
            }
        });
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
       setActivityLabel(mSortBy);
    }

    private void setActivityLabel(String sortBy)
    {
        if (sortBy.equals(getString(R.string.pref_sorting_popularity)))
       {
        this.setTitle(getString(R.string.popular_movies));
       }
        else if(sortBy.equals(getString(R.string.pref_sorting_rating)))
       {
           this.setTitle(getString(R.string.top_rated_movies));
       }
        else if (sortBy.equals(getString(R.string.pref_sorting_favorite)))
       {
           this.setTitle(getString(R.string.favorite_movies));
       }

    }
}
