package eu.rg_engineering.simplemusicplayer.ui.home;

import android.content.Context;
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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import eu.rg_engineering.simplemusicplayer.MainActivity;
import eu.rg_engineering.simplemusicplayer.R;
import eu.rg_engineering.simplemusicplayer.TrackItem;
import eu.rg_engineering.simplemusicplayer.TrackItemsAdapter;
import eu.rg_engineering.simplemusicplayer.utils.MyItemTouchHelper;
import eu.rg_engineering.simplemusicplayer.utils.OnDeleteTrackitemListener;

public class TracksFragment extends Fragment implements
        OnDeleteTrackitemListener {

    private String TAG = "TracksFragment";
    TracksFragmentListener mCommunication;
    Context mContext;
    private RecyclerView rvTrackItems = null;
    private TrackItemsAdapter TrackItemsAdapter = null;
    ArrayList<TrackItem> mTracks;

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

        } catch (Exception ex) {
            Log.e(TAG, "exception in onCreateView " + ex.toString());
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
                }
            });

            Log.d(TAG, "adapter notified ");
        }
    }

    public void SetCurrentplaytime(long duration){
        TrackItemsAdapter.SetCurrentPlaytime(duration);
    }
}
