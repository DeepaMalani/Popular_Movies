package com.example.android.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popularmovies.data.MovieContract;
import com.squareup.picasso.Picasso;

public class MovieDetail extends AppCompatActivity {

    private TextView mMovieTitle;
    private ImageView mMovieImageThumbnail;
    private TextView mMovieOverview;
    private TextView mUserRating;
    private TextView mReleaseDate;
    private Movie mMovie;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
       // Get a reference to textView from xml.
        mMovieTitle = (TextView)findViewById(R.id.text_view_title);
        mMovieImageThumbnail = (ImageView)findViewById(R.id.image_view_thumbnail);
        mMovieOverview = (TextView)findViewById(R.id.text_view_overview);
        mUserRating = (TextView)findViewById(R.id.text_view_user_rating);
        mReleaseDate = (TextView)findViewById(R.id.text_view_release_date);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {

            //Retrieve movie object
            mMovie = (Movie) intent.getParcelableExtra(Intent.EXTRA_TEXT);
           // Toast.makeText(MovieDetail.this,"Title:" + movie.movieTitle,Toast.LENGTH_SHORT).show();
            mMovieTitle.setText(mMovie.movieTitle);
            Picasso.with(MovieDetail.this).load(mMovie.movieImageThumbNailPath).into(mMovieImageThumbnail);
            mMovieOverview.setText(mMovie.movieOverView);
            mReleaseDate.setText(mMovie.movieReleaseDate);
            mUserRating.setText(String.valueOf(mMovie.movieUserRating));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id)
        {
            case android.R.id.home:
                // This takes the user 'back', as if they pressed the left-facing triangle icon on the main android toolbar.
                MovieDetail.this.onBackPressed();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    public void onClickAddFavoriteMovies(View view)
    {
        //Update movie table isfavorite column
        ContentValues movieValues = new ContentValues();
        movieValues.put(MovieContract.MovieEntry.COLUMN_IS_FAVORITE, 1);

        MovieDetail.this.getContentResolver().update(
                MovieContract.MovieEntry.CONTENT_URI,
                movieValues,
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{String.valueOf(mMovie.movieId)}
        );

    }
}
