package com.example.android.popularmovies;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.android.popularmovies.data.MovieContract;
import com.example.android.popularmovies.databinding.ActivityMovieDetailBinding;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MovieDetail extends AppCompatActivity {

    private Movie mMovie;
    boolean mIsFavoriteMovie;
    private ActivityMovieDetailBinding mMovieDetailBinding;
    //Declare loader Ids for trailers ans reviews
    private static final int TRAILERS_RESULT_LOADER_ID = 1;
    private static final int REVIEWS_RESULT_LOADER_ID = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_movie_detail);

        mMovieDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_movie_detail);


        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {

            //Retrieve movie object
            mMovie = (Movie) intent.getParcelableExtra(Intent.EXTRA_TEXT);
            //Set Movie Title using databinding
            mMovieDetailBinding.textViewTitle.setText(mMovie.movieTitle);
            //Set ThumbNail image
            Picasso.with(MovieDetail.this).load(mMovie.moviePosterPath).into(mMovieDetailBinding.movieInfo.imageViewThumbnail);
            //Set Movie overview,release date and user rating text using data binding.
            mMovieDetailBinding.textViewOverview.setText(mMovie.movieOverView);
            mMovieDetailBinding.movieInfo.textViewReleaseDate.setText(getYearFromReleaseDate(mMovie.movieReleaseDate));
            mMovieDetailBinding.movieInfo.textViewUserRating.setText(String.valueOf(mMovie.movieUserRating)+ getResources().getString(R.string.ten_rating));
            setFavoriteButton();

        }

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getSupportLoaderManager().initLoader(TRAILERS_RESULT_LOADER_ID, null, trailersResultLoaderListener);
        getSupportLoaderManager().initLoader(REVIEWS_RESULT_LOADER_ID, null, reviewsResultLoaderListener);
    }

    private LoaderManager.LoaderCallbacks<List<Trailer>> trailersResultLoaderListener;
    {
        trailersResultLoaderListener = new LoaderManager.LoaderCallbacks<List<Trailer>>()

        {
            @Override
            public Loader<List<Trailer>> onCreateLoader(int id, Bundle args) {
                return new TrailerLoader(MovieDetail.this, mMovie.movieId);
            }

            @Override
            public void onLoadFinished(Loader<List<Trailer>> loader, List<Trailer> trailers) {
                TextView textViewTrailersLabel =(TextView) findViewById(R.id.text_view_trailer_header);

                //Set header for trailers list.
                if(trailers.size() > 0)
                {
                    textViewTrailersLabel.setVisibility(View.VISIBLE);
                }
                else
                {
                    textViewTrailersLabel.setVisibility(View.GONE);
                }

               LayoutInflater inflater = (LayoutInflater) MovieDetail.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                for (final Trailer trailer : trailers) {
                    View v = inflater.inflate(R.layout.trailer_list, null);

                    // fill in any details dynamically here
                    TextView textView = (TextView) v.findViewById(R.id.textTrailerName);
                    textView.setText(trailer.videoName);
                    //ImageView imgPlay = (ImageView) v.findViewById(R.id.imgPlay);
                    //imgPlay.setOnClickListener(new ClickPlayListener(trailer.getSource()));

                    mMovieDetailBinding.layoutTrailer.addView(v);
                    //Set OnClickListener for trailers
                    mMovieDetailBinding.layoutTrailer.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view) {
                            String videoKey = trailer.videoKey;
                            Uri youtubeApp = Uri.parse("vnd.youtube:" + videoKey) ;
                            Uri youtubeWeb = Uri.parse("http://www.youtube.com/watch?v=" + videoKey) ;
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            try {
                                intent.setData(youtubeApp);
                                startActivity(intent);
                            } catch (ActivityNotFoundException ex) {
                                intent.setData(youtubeWeb);
                                startActivity(intent);
                            }
                        }
                    });
                }

            }

            @Override
            public void onLoaderReset(Loader<List<Trailer>> loader) {

            }
        };
    }

    private LoaderManager.LoaderCallbacks<List<Review>> reviewsResultLoaderListener
            = new LoaderManager.LoaderCallbacks<List<Review>>()
    {
        @Override
        public Loader<List<Review>> onCreateLoader(int id, Bundle args) {
            return new ReviewLoader(MovieDetail.this,mMovie.movieId);
        }


        @Override
        public void onLoadFinished(Loader<List<Review>> loader, List<Review> reviews) {

            TextView textViewReviewsLabel =(TextView) findViewById(R.id.text_view_review_header);

            //Set header for reviews list.
            if(reviews.size() > 0)
            {
                textViewReviewsLabel.setVisibility(View.VISIBLE);
            }
            else
            {
                textViewReviewsLabel.setVisibility(View.GONE);
            }

            LayoutInflater inflater = (LayoutInflater) MovieDetail.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            for (final Review review : reviews) {
                View v = inflater.inflate(R.layout.review_list, null);

                // fill in any details dynamically here
                TextView textViewAuthor = (TextView) v.findViewById(R.id.tv_review_author);
                textViewAuthor.setText(review.Author);

                TextView textViewContent = (TextView) v.findViewById(R.id.tv_review_content);
                textViewContent.setText(getShortMovieReview(review.Content));

                mMovieDetailBinding.layoutReview.addView(v);
                //Set OnClickListener for reviews
                mMovieDetailBinding.layoutReview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String url = review.ReviewUrl;
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    }
                });
            }
        }
        @Override
        public void onLoaderReset(Loader<List<Review>> loader) {

        }

    };
    private void setFavoriteButton()
    {
        //If movie is already marked as a favorite, then set red star else set gray star.
        if(checkFavorites(mMovie.movieId))
        {
            mIsFavoriteMovie = true;
            mMovieDetailBinding.movieInfo.toggleButtonFavorites.setBackgroundDrawable(ContextCompat.getDrawable(MovieDetail.this, R.drawable.img_star_red));

        }
        else
        {
            mIsFavoriteMovie = false;
            mMovieDetailBinding.movieInfo.toggleButtonFavorites.setBackgroundDrawable(ContextCompat.getDrawable(MovieDetail.this, R.drawable.img_star_gray));

        }

       // attach an OnClickListener
        mMovieDetailBinding.movieInfo.toggleButtonFavorites.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // If movie is marked as a favorite, insert new row in favorites table.

                if(!mIsFavoriteMovie)
                {
                    insertFavorites(mMovie);
                    mMovieDetailBinding.movieInfo.toggleButtonFavorites.setBackgroundDrawable(ContextCompat.getDrawable(MovieDetail.this, R.drawable.img_star_red));
                    mIsFavoriteMovie = true;
                }
                // If user wants to un mark favorite movie, delete row from favorites table.
                else
                {
                    deleteFavorite(mMovie.movieId);
                    mMovieDetailBinding.movieInfo.toggleButtonFavorites.setBackgroundDrawable(ContextCompat.getDrawable(MovieDetail.this, R.drawable.img_star_gray));
                    mIsFavoriteMovie = false;
                }
            }
        });
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



    private void insertFavorites(Movie movie)
    {
        ContentValues favoritesValues = new ContentValues();
        favoritesValues.put(MovieContract.FavoriteMoviesEntry.COLUMN_MOVIE_ID, movie.movieId);
        favoritesValues.put(MovieContract.FavoriteMoviesEntry.COLUMN_POSTER_PATH, movie.moviePosterPath);
        favoritesValues.put(MovieContract.FavoriteMoviesEntry.COLUMN_TITLE, movie.movieTitle);
        favoritesValues.put(MovieContract.FavoriteMoviesEntry.COLUMN_OVERVIEW, movie.movieOverView);
        favoritesValues.put(MovieContract.FavoriteMoviesEntry.COLUMN_USER_RATING, movie.movieUserRating);
        favoritesValues.put(MovieContract.FavoriteMoviesEntry.COLUMN_RELEASE_DATE, movie.movieReleaseDate);

        Uri uri = MovieDetail.this.getContentResolver().insert(
                MovieContract.FavoriteMoviesEntry.CONTENT_URI,
                favoritesValues);


    }

    private void deleteFavorite(long movieId)
    {

        MovieDetail.this.getContentResolver().delete(
                MovieContract.FavoriteMoviesEntry.CONTENT_URI,
                MovieContract.FavoriteMoviesEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{String.valueOf(movieId)}
        );
    }

    private boolean checkFavorites(long movieId)
    {

        // Check if the movie with this movie id exists in the db
        Cursor movieCursor = MovieDetail.this.getContentResolver().query(
                MovieContract.FavoriteMoviesEntry.CONTENT_URI,
                null,
                MovieContract.FavoriteMoviesEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{String.valueOf(movieId)},
                null);


       int count = movieCursor.getCount();

        if(count > 0)
            return true;
        else
            return false;
    }

    private String getYearFromReleaseDate (String releaseDate) {

        String releaseYear ="";
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            Date date = format.parse(releaseDate);
            SimpleDateFormat dateFormate = new SimpleDateFormat("yyyy");
            releaseYear = dateFormate.format(date);


       } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
       return releaseYear;
    }

    private String getShortMovieReview(String strReview)
    {
        int MAX_CHAR = 100;
        int maxLength = (strReview.length() < MAX_CHAR)?strReview.length():MAX_CHAR;
        String review = strReview.substring(0, maxLength);

        return review+ "..." + "\n";
    }
}
