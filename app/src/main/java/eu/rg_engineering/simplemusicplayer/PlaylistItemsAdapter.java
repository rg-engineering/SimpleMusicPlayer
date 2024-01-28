package eu.rg_engineering.simplemusicplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import android.widget.ProgressBar;
import android.widget.SeekBar;
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
import eu.rg_engineering.simplemusicplayer.utils.OnDeletePlaylistitemListener;
import io.sentry.Sentry;


//todo playlist muss sortierbar werden
//todo playlist automatisch speichern, wenn Seite verlassen wird
//todo entfernter track muss auch aus Media-Liste im Service gelöscht werden, sonst wird er trotzdem gespielt


public class PlaylistItemsAdapter extends
        RecyclerView.Adapter<PlaylistItemsAdapter.ViewHolder> implements
        Filterable,
        ItemTouchHelperAdapter {

    private final String TAG = "PlaylistItemsAdapter";
    private final List<TrackItem> mItemsFiltered;
    private List<TrackItem> mItemsAll;
    private ItemTouchHelper mTouchHelper;
    private final OnDeletePlaylistitemListener deleteListener;
    private int mFilterIdx = 0;
    private int currentPlayedMusicPosition = -1;
    Context mContext;
    PlaylistItemsAdapterListener mCommunication;
    private boolean mNeed2SendSongs=false;
    //private String IP = "";
    //private String Port = "";
    //private String Token = "";

    public void notifyDatasetChanged() {
        notifyDataSetChanged();
    }

    public void updateItems(ArrayList<TrackItem> mTracks) {
        mItemsAll = mTracks;
        UpdateData();
    }

    public interface PlaylistItemsAdapterListener {
        void messageFromPlaylistItemsAdapter(String msg, ArrayList<String> params, ArrayList<TrackData> tracks);
    }
    public PlaylistItemsAdapter(List<TrackItem> items, OnDeletePlaylistitemListener deleteListener) {

        Log.d(TAG, "PlaylistItemsAdapter contructor ");

        this.deleteListener = deleteListener;
        mItemsFiltered = TrackItem.createItemsList(0);

        mItemsAll = items;
        UpdateData();

    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        Log.d(TAG, "onAttachedToRecyclerView");

        Context context = recyclerView.getContext();
        mCommunication = (PlaylistItemsAdapterListener) context;
        mContext = context;


        if (mCommunication != null) {
            mCommunication.messageFromPlaylistItemsAdapter("IsReady", null, null);
        }
        else {
            Log.w(TAG, "mCommunication == null ");
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        Log.d(TAG, "onViewRecycled");
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        Log.d(TAG, "onViewAttachedToWindow");
    }

    public void UpdateData() {

        Log.d(TAG, "UpdateData Tracks called ");
        mItemsFiltered.clear();
        for (TrackItem item : mItemsAll) {

            //todo filter beachten???
            mItemsFiltered.add(item);
        }

        //send songs here to main since plexe needs longer then internal data
        if (mNeed2SendSongs) {
            GetSongs();
        }
    }
    @NonNull
    @Override
    public PlaylistItemsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View itemView = inflater.inflate(R.layout.playlisttrack_item, parent, false);

        // Return a new holder instance
        PlaylistItemsAdapter.ViewHolder viewHolder = new PlaylistItemsAdapter.ViewHolder(itemView);

        //SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        //IP = sharedPreferences.getString("plex_server_ip", "");
        //Port = sharedPreferences.getString("plex_server_port", "");
        //Token = sharedPreferences.getString("plex_server_token", "");

        Log.d(TAG, "onCreateViewHolder done");
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistItemsAdapter.ViewHolder viewHolder, int position) {
// Get the data model based on position
        TrackItem item = mItemsFiltered.get(position);

        Log.d(TAG, "onBindViewHolder playlist called, position " + position);
        TextView nameTextView = viewHolder.nameTextView;

        TextView albumTextView = viewHolder.albumTextView;
        TextView artistTextView = viewHolder.artistTextView;
        TextView durationTextView = viewHolder.durationTextView;
        TextView currentPlaytimeTextView = viewHolder.currentPlaytimeTextView;

        ImageView imageViewAlbum = viewHolder.imageViewAlbum;
        ImageView imageViewArtist = viewHolder.imageViewArtist;

        if (nameTextView != null) {
            nameTextView.setText(item.getName());
        }
        if (albumTextView != null) {
            albumTextView.setText(item.getAlbum());
        }
        if (artistTextView != null) {
            artistTextView.setText(item.getArtist());
        }
        if (durationTextView != null) {
            durationTextView.setText(item.getDuration());
        }
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
                PlayCurrentSong(position);
            }
        });

        if (currentPlayedMusicPosition==position) {
            viewHolder.nameTextView.setTextColor(Color.RED);
        }
        else {
            viewHolder.nameTextView.setTextColor(Color.BLACK);
        }

        if (imageViewAlbum != null) {
            String path2image = item.GetPath2AlbumImage();

            if (path2image != null && path2image.length() > 0) {
                Log.d(TAG, "image view should be used ");
                new DownloadImageTask(imageViewAlbum).execute(path2image);
            } else {
                Log.d(TAG, "image view shouldn't be used ");
            }
        }

        if (imageViewArtist != null) {
            String path2image = item.GetPath2ArtistImage();

            if (path2image != null && path2image.length() > 0) {
                Log.d(TAG, "image view should be used ");
                new DownloadImageTask(imageViewArtist).execute(path2image);
            } else {
                Log.d(TAG, "image view shouldn't be used ");
            }
        }

    }

    //todo DownloadImageTask global verfügbar machen
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView mImage;

        public DownloadImageTask(ImageView image) {
            this.mImage = image;
        }

        protected Bitmap doInBackground(String... urls) {
            String fullURL = urls[0];
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

    public void SetCurrentPlaytime(int index, long playtime) {

        Log.d(TAG, "got current playtime " + index + " / " + playtime + " " + mItemsFiltered.size());

        if (index >= 0 && index < mItemsFiltered.size()) {
            mItemsFiltered.get(index).setCurrentPlaytime(playtime);

            currentPlayedMusicPosition = index;

            notifyItemChanged(index);

        } else {
            Log.e(TAG, "index out of range " + index);
        }
    }

    public void GetSongs() {
        Log.d(TAG, "GetSongs  " );

        if (mItemsFiltered.size()>0) {
            mNeed2SendSongs=false;
            ArrayList<TrackData> tracks = new ArrayList<>();

            for (TrackItem item : mItemsFiltered) {

                TrackData track = new TrackData();
                track.Artist = item.getArtist();
                track.TrackName = item.getName();
                track.Url = item.getFileName();
                track.Path2Image = item.GetPath2ArtistImage();

                tracks.add(track);
            }

            mCommunication.messageFromPlaylistItemsAdapter("UpdatePlayList", null, tracks);
        }
        else {
            mNeed2SendSongs=true;
        }
    }

    private void PlayCurrentSong(int pos){

        Log.d(TAG, "PLayfromcurrent position  " + pos );

        ArrayList<String> items = new ArrayList<>();

        items.add(String.valueOf(pos));

        mCommunication.messageFromPlaylistItemsAdapter("PlayFromCurrentPos", items, null);
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

        if (mFilterIdx == 1) { // track name filter
            oRet = filterTrack;
        } else {
            Log.e(TAG, "no Filter #" + mFilterIdx);
        }

        return oRet;
    }

    Filter filterTrack = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            List<TrackItem> filteredList = new ArrayList<>();

            if (constraint.toString().isEmpty()) {
                Log.d(TAG, "no filter set " + mItemsAll.size());
                filteredList.addAll(mItemsAll);

            } else {
                Log.d(TAG, "track filter set " + constraint + " " + mItemsAll.size());

                for (TrackItem item : mItemsAll) {

                    Log.d(TAG, "track filter compare " + item.getName().toLowerCase() + " ?= " + constraint.toString().toLowerCase());

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

            mItemsFiltered.addAll((Collection<? extends TrackItem>) results.values);

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
        public TextView artistTextView;
        public TextView albumTextView;
        public TextView durationTextView;
        public TextView currentPlaytimeTextView;
        public ProgressBar currentProgressbar;
        public SeekBar currentSeekbar;
        public Button btnPlaySong;
        public ImageView imageViewAlbum;
        public ImageView imageViewArtist;

        GestureDetector mGestureDetector;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            nameTextView = (TextView) itemView.findViewById(R.id.track_Name);
            artistTextView = (TextView) itemView.findViewById(R.id.track_Artist);
            albumTextView = (TextView) itemView.findViewById(R.id.track_Album);
            durationTextView = (TextView) itemView.findViewById(R.id.track_Duration);
            currentPlaytimeTextView = (TextView) itemView.findViewById(R.id.track_CurrentPlaytime);
            currentProgressbar = (ProgressBar) itemView.findViewById(R.id.track_ProgressBar);
            currentSeekbar = (SeekBar) itemView.findViewById(R.id.track_SeekBar);
            btnPlaySong = (Button) itemView.findViewById(R.id.track_PlaySong);

            imageViewAlbum = (ImageView) itemView.findViewById(R.id.track_AlbumImage);
            imageViewArtist = (ImageView) itemView.findViewById(R.id.track_ArtistImage);

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

            int pos =this.getAdapterPosition();

            ArrayList<String> items = new ArrayList<>();

            items.add(String.valueOf(pos));

            mCommunication.messageFromPlaylistItemsAdapter("TrackSelected",items, null );

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

    public void ChangeItem(int index, TrackItem item) {
        Log.d(TAG, "change item " + item.getName());
        mItemsFiltered.set(index, item);

        int pos = FindItemInList(mItemsFiltered, item.getName());

        if (pos > -1) {
            mItemsFiltered.set(pos, item);
        }


    }

    public void AddItem(TrackItem item) {
        Log.d(TAG, "add item " + item.getName());

        mItemsAll.add(item);


        getFilter().filter("");

    }


    private int FindItemInList(List<TrackItem> list, String id) {
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
