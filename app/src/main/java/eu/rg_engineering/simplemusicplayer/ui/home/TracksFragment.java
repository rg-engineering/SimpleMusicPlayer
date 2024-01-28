package eu.rg_engineering.simplemusicplayer.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.util.ArrayList;

import eu.rg_engineering.simplemusicplayer.MainActivity;
import eu.rg_engineering.simplemusicplayer.R;
import eu.rg_engineering.simplemusicplayer.TrackItem;
import eu.rg_engineering.simplemusicplayer.TrackItemsAdapter;
import eu.rg_engineering.simplemusicplayer.utils.MyItemTouchHelper;
import eu.rg_engineering.simplemusicplayer.utils.OnDeleteTrackitemListener;
import io.sentry.Sentry;

public class TracksFragment extends Fragment implements
        OnDeleteTrackitemListener {

    private final String TAG = "TracksFragment";
    TracksFragmentListener mCommunication;
    Context mContext;
    private RecyclerView rvTrackItems = null;
    private TrackItemsAdapter TrackItemsAdapter = null;
    ArrayList<TrackItem> mTracks;
    String mArtistName = "";
    String mAlbumName = "";
    String mPath2ImageArtist = "";
    String mPath2ImageAlbum = "";
    TextView artistName;
    TextView albumName;
    TextView noOfTracks;
    ImageView artistImage;
    ImageView albumImage;
    private String IP = "";
    private String Port = "";
    private String Token = "";

    @Override
    public void ItemDeleted() {
        //SaveData();
    }
    public interface TracksFragmentListener {
        void messageFromTracksFragment(String msg, String params);
    }
    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
        mCommunication = (TracksFragmentListener) context;
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCommunication = null;
    }
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_tracks, container, false);

        try {
            rvTrackItems = (RecyclerView) root.findViewById(R.id.rvTracks);

            MainActivity activity = (MainActivity) getActivity();
            mTracks=activity.mMusicData.getTrackData();

            // Create adapter passing in the sample user data
            TrackItemsAdapter = new TrackItemsAdapter(mTracks,this);


            ItemTouchHelper.Callback callback = new MyItemTouchHelper(TrackItemsAdapter);
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
            TrackItemsAdapter.setTouchHelper(itemTouchHelper);
            itemTouchHelper.attachToRecyclerView(rvTrackItems);
            // Attach the adapter to the recyclerview to populate items
            rvTrackItems.setAdapter(TrackItemsAdapter);

            // Set layout manager to position the items
            rvTrackItems.setLayoutManager(new LinearLayoutManager(getActivity()));
            rvTrackItems.setItemAnimator(null);
            // That's all!
            AutoCompleteTextView editFilterTracks = (AutoCompleteTextView ) root.findViewById(R.id.filter_tracks);
            ArrayList <String> TrackList = new ArrayList<>();

            for (int i = 0; i < mTracks.size(); i++) {

                String track = mTracks.get(i).getName();
                if (!TrackList.contains(track)) {
                    TrackList.add(track);
                }
            }

            ArrayAdapter<String> TrackListAdapter = new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_dropdown_item_1line, TrackList);

            editFilterTracks.setThreshold(1);
            editFilterTracks.setAdapter(TrackListAdapter);

            editFilterTracks.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    Log.d(TAG, "track filter text changed " + s);
                    TrackItemsAdapter.setFilterIdx(1);
                    TrackItemsAdapter.getFilter().filter(s);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            Button btnBack = (Button) root.findViewById(R.id.btnBackTracks);
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "back button pressed");
                    mCommunication.messageFromTracksFragment("btnBack", "");
                }
            });



            artistName = (TextView) root.findViewById(R.id.artistName4Tracks);
            albumName = (TextView) root.findViewById(R.id.albumName4Tracks);

            noOfTracks = (TextView) root.findViewById(R.id.numberOfTracks);
            artistImage = (ImageView) root.findViewById(R.id.artistImage4Tracks);
            albumImage = (ImageView) root.findViewById(R.id.albumImage4Tracks);


            UpdateInfo();




        } catch (Exception ex) {
            Log.e(TAG, "exception in onCreateView " + ex);
            Sentry.captureException(ex);
        }
        return root;


    }
    public void ReadPlexTrackData(){

        MainActivity activity = (MainActivity) getActivity();
        if (activity.mMusicData!=null) {
            activity.mMusicData.ReadPlexTrackData();
            Log.d(TAG, "plex data read ");

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mTracks != null) {
                        TrackItemsAdapter.updateItems(mTracks);
                        TrackItemsAdapter.notifyDatasetChanged();
                    }
                    UpdateInfo();
                }
            });

            mCommunication.messageFromTracksFragment("PlexDataRead", "");
            Log.d(TAG, "adapter notified ");
        }
    }

    public void SetCurrentplaytime(int index, long duration){
        TrackItemsAdapter.SetCurrentPlaytime(index, duration);
    }

    /*
    public void GetNextSong(){
        TrackItemsAdapter.GetNextSong();
    }

    public void GetCurrentSong(){
        TrackItemsAdapter.GetCurrentSong();
    }
     */

    public void GetSongs(){
        TrackItemsAdapter.GetSongs();
    }


    private void UpdateInfo() {
        MainActivity activity = (MainActivity) getActivity();
        mArtistName = activity.mMusicData.GetArtist4Album();
        mPath2ImageArtist = activity.mMusicData.GetPath2Image4Album();

        mAlbumName = activity.mMusicData.GetAlbum4Track();
        mPath2ImageAlbum = activity.mMusicData.GetPath2Image4Track();

        if (artistName != null) {
            artistName.setText(mArtistName);
        }
        if (albumName != null) {
            albumName.setText(mAlbumName);
        }
        if (noOfTracks != null) {
            String counts = "" + mTracks.size() + " " + getString(R.string.tracks);
            noOfTracks.setText(counts);
        }

        if (artistImage != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            IP = sharedPreferences.getString("plex_server_ip", "");
            Port = sharedPreferences.getString("plex_server_port", "");
            Token = sharedPreferences.getString("plex_server_token", "");

            if (mPath2ImageArtist != null && mPath2ImageArtist.length() > 0) {
                Log.d(TAG, "image view should be used ");
                new DownloadImageTask(artistImage).execute(mPath2ImageArtist);
            } else {
                Log.d(TAG, "image view shouldn't be used ");
            }
        }
        if (albumImage != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            IP = sharedPreferences.getString("plex_server_ip", "");
            Port = sharedPreferences.getString("plex_server_port", "");
            Token = sharedPreferences.getString("plex_server_token", "");

            if (mPath2ImageAlbum != null && mPath2ImageAlbum.length() > 0) {
                Log.d(TAG, "image view should be used ");
                new DownloadImageTask(albumImage).execute(mPath2ImageAlbum);
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



}
