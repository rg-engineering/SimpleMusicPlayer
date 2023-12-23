package eu.rg_engineering.simplemusicplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import eu.rg_engineering.simplemusicplayer.utils.ItemTouchHelperAdapter;
import eu.rg_engineering.simplemusicplayer.utils.OnDeleteArtistitemListener;
import io.sentry.Sentry;


public class ArtistItemsAdapter extends
        RecyclerView.Adapter<ArtistItemsAdapter.ViewHolder> implements
        Filterable,
        ItemTouchHelperAdapter {

    private String TAG = "ArtistItemsAdapter";
    private List<ArtistItem> mItemsFiltered;
    private List<ArtistItem> mItemsAll;
    private ItemTouchHelper mTouchHelper;
    private OnDeleteArtistitemListener deleteListener;
    private int mFilterIdx = 0;
    Context mContext;
    private String IP = "";
    private String Port = "";
    private String Token = "";
    ArtistItemsAdapter.ArtistItemsAdapterListener mCommunication;

    public void notifyDatasetChanged() {
        notifyDataSetChanged();
    }

    public void updateItems(ArrayList<ArtistItem> mArtists) {
        mItemsAll = mArtists;
        UpdateData();
    }

    public interface ArtistItemsAdapterListener {
        void messageFromArtistItemsAdapter(String msg, String params, String artistName);
    }

    public ArtistItemsAdapter(List<ArtistItem> items, OnDeleteArtistitemListener deleteListener) {
        this.deleteListener = deleteListener;
        mItemsFiltered = ArtistItem.createItemsList(0);

        mItemsAll = items;
        UpdateData();

        //todo load playlist
    }

    public void UpdateData() {

        Log.d(TAG, "UpdateData called ");
        mItemsFiltered.clear();
        for (ArtistItem item : mItemsAll) {

            //todo filter beachten???
            mItemsFiltered.add(item);
        }
    }

    @NonNull
    @Override
    public ArtistItemsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View itemView = inflater.inflate(R.layout.artist_item, parent, false);

        // Return a new holder instance
        ArtistItemsAdapter.ViewHolder viewHolder = new ArtistItemsAdapter.ViewHolder(itemView);

        mCommunication = (ArtistItemsAdapter.ArtistItemsAdapterListener) context;
        mContext = context;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        IP = sharedPreferences.getString("plex_server_ip", "");
        Port = sharedPreferences.getString("plex_server_port", "");
        Token = sharedPreferences.getString("plex_server_token", "");

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistItemsAdapter.ViewHolder viewHolder, int position) {
// Get the data model based on position
        ArtistItem item = mItemsFiltered.get(position);

        Log.d(TAG, "onBindViewHolder called, position " + position);
        TextView nameTextView = viewHolder.nameTextView;
        TextView genreTextView = viewHolder.genreTextView;
        TextView countryTextView = viewHolder.countryTextView;
        Button infoButton = viewHolder.infoButton;
        ImageView imageImageView = viewHolder.imageImageView;
        viewHolder.Plex_RatingKey = item.getPlexRatingKey();

        if (nameTextView != null) {
            nameTextView.setText(item.getName());
        }
        if (genreTextView != null) {
            genreTextView.setText(item.getGenre());
        }
        if (countryTextView != null) {
            countryTextView.setText(item.getCountry());
        }
        if (infoButton != null) {
            String infoSummery = item.getInfo();

            if (infoSummery != null && infoSummery.length() > 0) {
                Log.d(TAG, "info Button should be visible ");
                infoButton.setVisibility(View.VISIBLE);

                infoButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "infoButton pressed position " + position);
                        String info = mItemsFiltered.get(position).getInfo();
                        mCommunication.messageFromArtistItemsAdapter("ShowInfo", info, "");
                    }
                });
            } else {
                Log.d(TAG, "info Button should be invisible ");
            }
        }
        if (imageImageView != null) {
            String path2image = item.getPath2Image();

            if (path2image != null && path2image.length() > 0) {
                Log.d(TAG, "image view should be used ");
                new DownloadImageTask(imageImageView).execute(path2image);
            } else {
                Log.d(TAG, "image view shouldn't be used ");
            }
        }


    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView mImage;

        public DownloadImageTask(ImageView image) {
            this.mImage = image;
        }

        protected Bitmap doInBackground(String... urls) {
            String fullURL = "http://" + IP + ":" + Port + urls[0] + "?X-Plex-Token=" + Token;
            Bitmap icon = null;

            Log.d(TAG, "get image from " + fullURL);

            try {
                InputStream in = new java.net.URL(fullURL).openStream();
                icon = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e(TAG, "exception in DownloadImageTask " + e.getMessage());
                e.printStackTrace();
                Sentry.captureException(e);
            }
            return icon;
        }

        protected void onPostExecute(Bitmap result) {
            mImage.setImageBitmap(result);
        }
    }


    @Override
    public int getItemCount() {
        int cnt = 0;

        if (mItemsFiltered != null) {
            cnt = mItemsFiltered.size();
        }
        //Log.d(TAG,"items " + cnt  );
        return cnt;
    }

    public void setTouchHelper(ItemTouchHelper touchHelper) {
        this.mTouchHelper = touchHelper;
    }

    public void setFilterIdx(int idx) {
        mFilterIdx = idx;
    }

    @Override
    public Filter getFilter() {
        Log.d(TAG, "getFilter #" + mFilterIdx);

        Filter oRet = null;

        switch (mFilterIdx) {
            case 1: // artist name filter
                oRet = filterArtist;
                break;

            default:
                Log.e(TAG, "no Filter #" + mFilterIdx);
                break;

        }

        return oRet;
    }

    Filter filterArtist = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            List<ArtistItem> filteredList = new ArrayList<>();

            if (constraint.toString().isEmpty()) {
                Log.d(TAG, "no filter set " + mItemsAll.size());
                filteredList.addAll(mItemsAll);

            } else {
                Log.d(TAG, "artist filter set " + constraint.toString() + " " + mItemsAll.size());

                for (ArtistItem item : mItemsAll) {

                    Log.d(TAG, "artist filter compare " + item.getName().toLowerCase() + " ?= " + constraint.toString().toLowerCase());

                    if (item.getName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mItemsFiltered.clear();

            mItemsFiltered.addAll((Collection<? extends ArtistItem>) results.values);

            notifyDataSetChanged();
        }
    };


    @Override
    public void onItemMoved(int fromPosition, int toPosition) {

    }

    @Override
    public void onItemSwiped(int position) {
        Log.d(TAG, "item swiped, position " + position);

        int pos = FindItemInList(mItemsAll, mItemsFiltered.get(position).getId());

        if (pos > -1) {
            mItemsAll.remove(pos);
        }
        mItemsFiltered.remove(position);

        notifyItemRemoved(position);
        //just inform parent class
        if (deleteListener != null) {
            deleteListener.ItemDeleted();
        }
    }


    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnTouchListener,

            GestureDetector.OnGestureListener {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView nameTextView;
        public TextView genreTextView;
        public TextView countryTextView;
        public Button infoButton;
        public ImageView imageImageView;
        public int Plex_RatingKey;

        GestureDetector mGestureDetector;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            nameTextView = (TextView) itemView.findViewById(R.id.artist_Name);
            genreTextView = (TextView) itemView.findViewById(R.id.artist_Genre);
            countryTextView = (TextView) itemView.findViewById(R.id.artist_Country);
            infoButton = (Button) itemView.findViewById(R.id.artist_info_button);
            imageImageView = (ImageView) itemView.findViewById(R.id.artist_image);


            mGestureDetector = new GestureDetector(itemView.getContext(), this);


            itemView.setOnTouchListener(this);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.d(TAG, "onSingleTapUp position " + this.getAdapterPosition());
            int pos = this.getAdapterPosition();

            String ratingKey = "-1";
            if (Plex_RatingKey > 0) {
                ratingKey = String.valueOf(Plex_RatingKey);
            }
            String ArtistName = nameTextView.getText().toString();

            mCommunication.messageFromArtistItemsAdapter("ArtistSelected", ratingKey, ArtistName);
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (mTouchHelper != null) {
                mTouchHelper.startDrag(this);
            }
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }


        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mGestureDetector.onTouchEvent(event);
            return true;
        }
    }

    public String GetSerializedData() {
        String sRet = "";

        for (int i = 0; i < mItemsAll.size(); i++) {
            sRet += mItemsAll.get(i).Serialize2();
        }
        return sRet;
    }

    public void ChangeItem(int index, ArtistItem item) {
        Log.d(TAG, "change item " + item.getName());
        mItemsFiltered.set(index, item);

        int pos = FindItemInList(mItemsFiltered, item.getName());

        if (pos > -1) {
            mItemsFiltered.set(pos, item);
        }


    }

    public void AddItem(ArtistItem item) {
        Log.d(TAG, "add item " + item.getName());

        mItemsAll.add(item);


        getFilter().filter("");

    }


    private int FindItemInList(List<ArtistItem> list, String id) {
        int nRet = -1;

        try {
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {

                    Log.d(TAG, "check item " + list.get(i).getName() + " =? " + id);

                    if (list.get(i).getName().toLowerCase().contains(id.toLowerCase())) {

                        Log.d(TAG, "item found at " + i);
                        nRet = i;
                    }
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "exception in FindItemInList " + ex.toString());
            Sentry.captureException(ex);
        }
        return nRet;
    }

}
