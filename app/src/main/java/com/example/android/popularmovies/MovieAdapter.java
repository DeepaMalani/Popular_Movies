package com.example.android.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Deep on 5/2/2017.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder>{
    private String[] mMoviePosterPath;
    private Context mContex;
    private List<Movie> mMovies = new ArrayList<Movie>();
    private Cursor mCursor;

    /*
     * An on-click handler that we've defined to make it easy for an Activity to interface with
     * our RecyclerView
     */
    private static MovieAdapterOnClickHandler mClickHandler;

    public MovieAdapter(Context context, List<Movie> movies) {
        mContex = context;
        mMovies = movies ;

    }

    // Define the method that allows the parent activity  to define the listener
    public void setOnItemClickListener(MovieAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    /**
     * The interface that receives onClick messages.
     */
    public interface MovieAdapterOnClickHandler {
        void onClick(Movie movie);
    }


    /**
     * Cache of the children views for a Movie grid item.
     */
    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private final ImageView mMovieImageView;
        MovieAdapterViewHolder(View view)
        {
            super(view);
            mMovieImageView = (ImageView) view.findViewById(R.id.image_view_movie_poster);
            view.setOnClickListener(this);
        }

        /**
         * This get called when clicks on movie poster.
         * @param view
         */
        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
             long movieId = mCursor.getLong(MainActivity.INDEX_MOVIE_ID);
             String movieTitle =  mCursor.getString(MainActivity.INDEX_TITLE);
             String movieOverView = mCursor.getString(MainActivity.INDEX_OVERVIEW);
             double movieUserRating = mCursor.getDouble(MainActivity.INDEX_USER_RATING);
             String movieReleaseDate = mCursor.getString(MainActivity.INDEX_RELEASE_DATE);
             String movieImageThumbNailPath = mCursor.getString(MainActivity.INDEX_IMAGE_THUMBNAIL_PATH);
             String moviePosterPath = mCursor.getString(MainActivity.INDEX_POSTER_PATH);

            Movie movie = new Movie(movieId,moviePosterPath,movieTitle,movieOverView,movieUserRating,movieReleaseDate,movieImageThumbNailPath);
            mClickHandler.onClick(movie);


        }
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position.
     * @param movieAdapterViewHolder
     * @param position
     */
    @Override
    public void onBindViewHolder(MovieAdapterViewHolder movieAdapterViewHolder, int position) {

       mCursor.moveToPosition(position);
       String moviePosterPath = mCursor.getString(MainActivity.INDEX_POSTER_PATH);
        Picasso
                .with(mContex)
                .load(moviePosterPath)
                .fit() // will explain later
                .into((ImageView) movieAdapterViewHolder.mMovieImageView);

    }

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.

     * @param viewGroup
     * @param viewType
     * @return
     */
    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
         mContex = viewGroup.getContext();
        int layoutIdForGridItem = R.layout.movie_list_item;
        LayoutInflater inflater = LayoutInflater.from(mContex);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForGridItem, viewGroup, shouldAttachToParentImmediately);
        return new MovieAdapterViewHolder(view);
    }

    /**
     * Swaps the cursor used by the MovieAdapter for its movie data. This method is called by
     * MainActivity after a load has finished, as well as when the Loader responsible for loading
     * the movie data is reset. When this method is called, we assume we have a completely new
     * set of data, so we call notifyDataSetChanged to tell the RecyclerView to update.
     *
     * @param newCursor the new cursor to use as ForecastAdapter's data source
     */
    void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }
}
