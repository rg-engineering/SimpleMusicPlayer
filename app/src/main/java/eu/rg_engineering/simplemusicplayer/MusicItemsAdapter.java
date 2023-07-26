package eu.rg_engineering.simplemusicplayer;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import eu.rg_engineering.simplemusicplayer.utils.ItemTouchHelperAdapter;
import eu.rg_engineering.simplemusicplayer.utils.OnDeleteMusicitemListener;

// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
public class MusicItemsAdapter extends
        RecyclerView.Adapter<MusicItemsAdapter.ViewHolder> implements
        Filterable,
        ItemTouchHelperAdapter {
    private String TAG = "MusicItemsAdapter";
    private List<MusicItem> mItemsFiltered;
    private List<MusicItem> mItemsAll;
    private ItemTouchHelper mTouchHelper;
    private OnDeleteMusicitemListener deleteListener;
    private int mFilterIdx = 0;
    private int currentPlayedMusicPosition = -1;
    Context mContext;
    MusicItemsAdapterListener mCommunication;

    //Interface for communication
    public interface MusicItemsAdapterListener {
        void messageFromMusicItemsAdapter(String msg, String params);
    }

    // Pass in the contact array into the constructor
    public MusicItemsAdapter(List<MusicItem> items, OnDeleteMusicitemListener deleteListener) {
        this.deleteListener = deleteListener;
        mItemsFiltered = MusicItem.createItemsList(0);

        mItemsAll = items;
        UpdateData();

        //todo load playlist
    }

    public void UpdateData() {

        Log.d(TAG, "UpdateData called ");
        mItemsFiltered.clear();
        for (MusicItem item : mItemsAll) {

            //todo filter beachten???
            mItemsFiltered.add(item);
        }
    }

    @NonNull
    @Override
    public MusicItemsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View itemView = inflater.inflate(R.layout.music_item, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(itemView);

        mCommunication = (MusicItemsAdapterListener) context;
        mContext = context;
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
// Get the data model based on position
        MusicItem item = mItemsFiltered.get(position);

        Log.d(TAG, "onBindViewHolder called, position " + position);
        TextView titleTextView = viewHolder.titleTextView;
        if (titleTextView != null) {
            titleTextView.setText(item.getTitle());
        }
        TextView artistTextView = viewHolder.artistTextView;
        if (artistTextView != null) {
            artistTextView.setText(item.getArtist());
        }
        TextView albumTextView = viewHolder.albumTextView;
        if (albumTextView != null) {
            albumTextView.setText(item.getAlbum());
        }
        TextView durationTextView = viewHolder.durationTextView;
        if (durationTextView != null) {
            durationTextView.setText(item.getDuration());
        }
        TextView currentPlaytimeTextView = viewHolder.currentPlaytimeTextView;
        if (currentPlaytimeTextView != null) {
            currentPlaytimeTextView.setText(item.getCurrentPlaytime());
        }
        ProgressBar currentProgress = viewHolder.currentProgressbar;
        if (currentProgress != null) {
            currentProgress.setMin(0);
            currentProgress.setMax(100);
            currentProgress.setProgress(item.getProgress());
        }
        SeekBar currentSeek = viewHolder.currentSeekbar;
        if (currentSeek != null) {
            currentSeek.setMin(0);
            currentSeek.setMax(100);
            currentSeek.setProgress(item.getProgress());
        }


        Button btnPlaySong = viewHolder.btnPlaySong;
        btnPlaySong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "PlayMusic pressed position " + position);
                String filename = mItemsFiltered.get(position).getFilename();
                currentPlayedMusicPosition = position;
                mCommunication.messageFromMusicItemsAdapter("PlayMusic", filename);
            }
        });

        if (currentPlayedMusicPosition==position) {

            viewHolder.titleTextView.setTextColor(Color.RED);

            //viewHolder.albumTextView.setBackgroundColor(Color.parseColor( String.format("#%06X", (0xFFFFFF & R.color.HappySun))));
            //viewHolder.artistTextView.setBackgroundColor(Color.parseColor( String.format("#%06X", (0xFFFFFF & R.color.HappySun))));
            //viewHolder.titleTextView.setBackgroundColor(Color.parseColor( String.format("#%06X", (0xFFFFFF & R.color.HappySun))));
            //viewHolder.itemView.setBackgroundColor(Color.parseColor( String.format("#%06X", (0xFFFFFF & R.color.HappySun))));
        }
        else {
            viewHolder.titleTextView.setTextColor(Color.BLACK);
        }
    }

    public void GetNextSong() {

        int maxMusic = mItemsFiltered.size() - 1;
        if (currentPlayedMusicPosition >= 0 && currentPlayedMusicPosition < maxMusic) {
            currentPlayedMusicPosition++;
        } else {
            currentPlayedMusicPosition = 0;
        }
        String filename = mItemsFiltered.get(currentPlayedMusicPosition).getFilename();
        mCommunication.messageFromMusicItemsAdapter("PlayMusic", filename);
    }

    public void SetCurrentPlaytime(long playtime) {

        Log.d(TAG, "got current playtime " + playtime + " " + mItemsFiltered.size());

        if (currentPlayedMusicPosition > 0 && currentPlayedMusicPosition < mItemsFiltered.size()) {
            mItemsFiltered.get(currentPlayedMusicPosition).setCurrentPlaytime(playtime);
            notifyItemChanged(currentPlayedMusicPosition);
        } else {
            Log.e(TAG, "index out of range ");
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
            case 1: // artist filter
                oRet = filterArtist;
                break;
            case 2: // title filter
                oRet = filterTitle;
                break;
            case 3: // album filter
                oRet = filterAlbum;
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

            List<MusicItem> filteredList = new ArrayList<>();

            if (constraint.toString().isEmpty()) {
                Log.d(TAG, "no filter set " + mItemsAll.size());
                filteredList.addAll(mItemsAll);

            } else {
                Log.d(TAG, "artist filter set " + constraint.toString() + " " + mItemsAll.size());

                for (MusicItem item : mItemsAll) {

                    Log.d(TAG, "artist filter compare " + item.getArtist().toLowerCase() + " ?= " + constraint.toString().toLowerCase());
//todo combination of all filter
                    if (item.getArtist().toLowerCase().contains(constraint.toString().toLowerCase())) {
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

            mItemsFiltered.addAll((Collection<? extends MusicItem>) results.values);

            notifyDataSetChanged();
        }
    };

    Filter filterAlbum = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            List<MusicItem> filteredList = new ArrayList<>();

            if (constraint.toString().isEmpty()) {
                Log.d(TAG, "no filter set " + mItemsAll.size());
                filteredList.addAll(mItemsAll);

            } else {
                Log.d(TAG, "album filter set " + constraint.toString() + " " + mItemsAll.size());

                for (MusicItem item : mItemsAll) {

                    Log.d(TAG, "album filter compare " + item.getAlbum().toLowerCase() + " ?= " + constraint.toString().toLowerCase());
//todo combination of all filter
                    if (item.getAlbum().toLowerCase().contains(constraint.toString().toLowerCase())) {
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

            mItemsFiltered.addAll((Collection<? extends MusicItem>) results.values);

            notifyDataSetChanged();
        }
    };

    Filter filterTitle = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            List<MusicItem> filteredList = new ArrayList<>();

            if (constraint.toString().isEmpty()) {
                Log.d(TAG, "no filter set " + mItemsAll.size());
                filteredList.addAll(mItemsAll);

            } else {
                Log.d(TAG, "title filter set " + constraint.toString() + " " + mItemsAll.size());

                for (MusicItem item : mItemsAll) {

                    Log.d(TAG, "title filter compare " + item.getTitle().toLowerCase() + " ?= " + constraint.toString().toLowerCase());

                    //todo combination of all filter

                    if (item.getTitle().toLowerCase().contains(constraint.toString().toLowerCase())) {
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

            mItemsFiltered.addAll((Collection<? extends MusicItem>) results.values);

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
        public TextView titleTextView;
        public TextView artistTextView;
        public TextView albumTextView;
        public TextView durationTextView;
        public TextView currentPlaytimeTextView;
        public ProgressBar currentProgressbar;
        public SeekBar currentSeekbar;
        public Button btnPlaySong;

        GestureDetector mGestureDetector;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            titleTextView = (TextView) itemView.findViewById(R.id.item_title);
            artistTextView = (TextView) itemView.findViewById(R.id.item_artist);
            albumTextView = (TextView) itemView.findViewById(R.id.item_album);
            durationTextView = (TextView) itemView.findViewById(R.id.item_duration);
            currentPlaytimeTextView = (TextView) itemView.findViewById(R.id.item_currentPlaytime);
            currentProgressbar = (ProgressBar) itemView.findViewById(R.id.item_progressBar);
            currentSeekbar = (SeekBar) itemView.findViewById(R.id.item_seekBar);
            btnPlaySong = (Button) itemView.findViewById(R.id.PlaySong);

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

    public void ChangeItem(int index, MusicItem item) {
        Log.d(TAG, "change item " + item.getTitle());
        mItemsFiltered.set(index, item);

        int pos = FindItemInList(mItemsFiltered, item.getTitle());

        if (pos > -1) {
            mItemsFiltered.set(pos, item);
        }


    }

    public void AddItem(MusicItem item) {
        Log.d(TAG, "add item " + item.getTitle());

        mItemsAll.add(item);


        getFilter().filter("");

    }


    private int FindItemInList(List<MusicItem> list, String id) {
        int nRet = -1;

        try {
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {

                    Log.d(TAG, "check item " + list.get(i).getTitle() + " =? " + id);

                    if (list.get(i).getTitle().toLowerCase().contains(id.toLowerCase())) {

                        Log.d(TAG, "item found at " + i);
                        nRet = i;
                    }
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "exception in FindItemInList " + ex.toString());
        }
        return nRet;
    }

}
