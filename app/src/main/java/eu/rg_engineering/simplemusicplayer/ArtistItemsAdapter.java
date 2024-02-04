package eu.rg_engineering.simplemusicplayer;

import android.content.Context;
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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import eu.rg_engineering.simplemusicplayer.utils.ImageDownloader;
import eu.rg_engineering.simplemusicplayer.utils.ItemTouchHelperAdapter;
import eu.rg_engineering.simplemusicplayer.utils.OnDeleteArtistitemListener;
import io.sentry.Sentry;

//todo swipe verhindern

public class ArtistItemsAdapter extends
        RecyclerView.Adapter<ArtistItemsAdapter.ViewHolder> implements
        Filterable,
        ItemTouchHelperAdapter {

    private final String TAG = "ArtistItemsAdapter";
    private final List<ArtistItem> mItemsFiltered;
    private List<ArtistItem> mItemsAll;
    private ItemTouchHelper mTouchHelper;
    private final OnDeleteArtistitemListener deleteListener;
    private int mFilterIdx = 0;
    Context mContext;
    ArtistItemsAdapter.ArtistItemsAdapterListener mCommunication;
    private ImageDownloader imageDownloader=null;

    public void notifyDatasetChanged() {
        notifyDataSetChanged();
    }

    public void updateItems(ArrayList<ArtistItem> mArtists) {
        mItemsAll = mArtists;
        UpdateData();
    }

    public interface ArtistItemsAdapterListener {
        void messageFromArtistItemsAdapter(String msg, String params, ArrayList<String> data);
    }

    public ArtistItemsAdapter(List<ArtistItem> items, OnDeleteArtistitemListener deleteListener) {
        this.deleteListener = deleteListener;
        mItemsFiltered = ArtistItem.createItemsList(0);

        mItemsAll = items;

        UpdateData();

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

        File path = mContext.getFilesDir();
        imageDownloader = new ImageDownloader(path.getAbsolutePath());


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistItemsAdapter.ViewHolder viewHolder, int position) {
// Get the data model based on position
        ArtistItem item = mItemsFiltered.get(position);

        //Log.d(TAG, "onBindViewHolder called, position " + position);
        TextView nameTextView = viewHolder.nameTextView;
        TextView genreTextView = viewHolder.genreTextView;
        TextView countryTextView = viewHolder.countryTextView;
        Button infoButton = viewHolder.infoButton;
        ImageView imageImageView = viewHolder.imageImageView;
        viewHolder.Plex_RatingKey = item.getPlexRatingKey();
        viewHolder.Path2Image = item.getPath2Image();

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
                Log.d(TAG, item.getName()+": info Button should be visible " + position);
                infoButton.setVisibility(View.VISIBLE);

                infoButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "infoButton pressed position " + position);
                        String info = mItemsFiltered.get(position).getInfo();
                        mCommunication.messageFromArtistItemsAdapter("ShowInfo", info, null);
                    }
                });
            } else {
                Log.d(TAG, item.getName()+": info Button should be invisible " + position);
            }
        }
        if (imageImageView != null) {
            String path2image = item.getPath2Image();

            if (path2image != null && path2image.length() > 0) {
                Log.d(TAG, item.getName() + ": image view should be used ");
                //new DownloadImageTask(imageImageView, item.getName()).execute(path2image);
                imageDownloader.loadBitmap(path2image, imageImageView);
            } else {
                Log.d(TAG, item.getName() + ": image view shouldn't be used ");
                imageImageView.setImageBitmap(null);
            }
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

        if (mFilterIdx == 1) { // artist name filter
            oRet = filterArtist;
        } else {
            Log.e(TAG, "no Filter #" + mFilterIdx);
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
                Log.d(TAG, "artist filter set " + constraint + " " + mItemsAll.size());

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
        Log.d(TAG, "item moved, from " + fromPosition + " to " + toPosition);
    }

    @Override
    public void onItemSwiped(int position) {
        Log.d(TAG, "item swiped, position " + position);


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
        public String Path2Image="";

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

            ArrayList<String> data = new ArrayList<>();
            data.add(nameTextView.getText().toString());
            data.add(Path2Image);
            data.add(ratingKey);

            mCommunication.messageFromArtistItemsAdapter("ArtistSelected", "" , data);
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
            Log.e(TAG, "exception in FindItemInList " + ex);
            Sentry.captureException(ex);
        }
        return nRet;
    }

}
