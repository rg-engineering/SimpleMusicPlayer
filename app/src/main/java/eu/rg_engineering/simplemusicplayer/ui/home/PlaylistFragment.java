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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import eu.rg_engineering.simplemusicplayer.R;
import eu.rg_engineering.simplemusicplayer.TrackItem;
import eu.rg_engineering.simplemusicplayer.PlaylistItemsAdapter;
import eu.rg_engineering.simplemusicplayer.utils.MyItemTouchHelper;
import eu.rg_engineering.simplemusicplayer.utils.OnPlaylistitemListener;
import io.sentry.Sentry;


public class PlaylistFragment extends Fragment implements
        OnPlaylistitemListener {

    private final String TAG = "PlaylistFragment";
    PlaylistFragmentListener mCommunication;
    Context mContext;
    private RecyclerView rvPlaylistItems = null;
    private PlaylistItemsAdapter playlistItemsAdapter = null;
    ArrayList<TrackItem> mPlaylistTracks;
    TextView noOfTracks;
    //todo playlist file einstellbar
    private final String filename = "playlist.txt";
    @Override
    public void ItemDeleted(int position) {
        Log.d(TAG, "item removed " + position);
    }

    @Override
    public void ItemMoved(int oldPos, int newPos) {
        Log.d(TAG, "moved from " + oldPos + " to " + newPos);
    }

    @Override
    public void PlayList2Save() {
        SavePlaylist();
    }

    public interface PlaylistFragmentListener {
        void messageFromPlaylistFragment(String msg, String params);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCommunication = (PlaylistFragmentListener) context;
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCommunication = null;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_playlist, container, false);

        try {
            rvPlaylistItems = (RecyclerView) root.findViewById(R.id.rvPlaylistTracks);

            //MainActivity activity = (MainActivity) getActivity();

            ReadPlaylist();

            // Create adapter passing in the sample user data
            playlistItemsAdapter = new PlaylistItemsAdapter(mPlaylistTracks, this);

            ItemTouchHelper.Callback callback = new MyItemTouchHelper(playlistItemsAdapter);
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
            playlistItemsAdapter.setTouchHelper(itemTouchHelper);
            itemTouchHelper.attachToRecyclerView(rvPlaylistItems);
            // Attach the adapter to the recyclerview to populate items
            rvPlaylistItems.setAdapter(playlistItemsAdapter);

            // Set layout manager to position the items
            rvPlaylistItems.setLayoutManager(new LinearLayoutManager(getActivity()));
            rvPlaylistItems.setItemAnimator(null);
            // That's all!
            AutoCompleteTextView editFilterTracks = (AutoCompleteTextView) root.findViewById(R.id.filter_playlisttracks);
            ArrayList<String> TrackList = new ArrayList<>();

            for (int i = 0; i < mPlaylistTracks.size(); i++) {

                String track = mPlaylistTracks.get(i).getName();
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
                    playlistItemsAdapter.setFilterIdx(1);
                    playlistItemsAdapter.getFilter().filter(s);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            Button btnDeletePlaylist = (Button) root.findViewById(R.id.playlist_deleteAll);
            btnDeletePlaylist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "delete playlist button pressed");
                    DeletePlaylist();
                }
            });

            noOfTracks = (TextView) root.findViewById(R.id.numberOfTracksInPlaylist);

            UpdateInfo();

        } catch (Exception ex) {
            Log.e(TAG, "exception in onCreateView " + ex);
            Sentry.captureException(ex);
        }
        return root;
    }

    public void SetCurrentplaytime(int index, long duration) {
        playlistItemsAdapter.SetCurrentPlaytime(index, duration);
    }

    public void GetSongs() {
        playlistItemsAdapter.GetSongs();
    }

    private void ReadPlaylist() {

        if (mPlaylistTracks == null) {
            mPlaylistTracks = new ArrayList<>();
        }
        else {
            mPlaylistTracks.clear();
        }
        try {
            Log.d(TAG, "read play list");
            readFromFile(filename);
        } catch (Exception ex) {
            Log.e(TAG, "Exception in SavePlaylist " + ex);
        }
    }
    private String readFromFile(String filename) {

        String ret = "";

        try {
            File path = mContext.getFilesDir();
            //File newDir = new File(path + "/" + filename);

            FileInputStream fileInputStream = new FileInputStream(new File(path, filename));
            InputStreamReader  inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {

                if (line.length()>5) {
                    Log.d(TAG, "read " + line);
                    TrackItem item = new TrackItem(line);
                    mPlaylistTracks.add(item);
                }
            }


        }
        catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + e);
            mCommunication.messageFromPlaylistFragment("ShowInfo", "playlist not found");
        } catch (IOException e) {
            Log.e(TAG, "Can not read file: " + e);
        }

        return ret;
    }
    private void SavePlaylist() {

        try {
            StringBuilder Contents = new StringBuilder();
            //todo save play list
            Log.d(TAG, "save play list "+ mPlaylistTracks.size());

            for (int i =0; i< mPlaylistTracks.size(); i++) {
                Log.d(TAG, "save "+ mPlaylistTracks.get(i).getName());
                Contents.append(mPlaylistTracks.get(i).Serialize(true));
            }
            writeToFile(Contents.toString(),filename);

        } catch (Exception ex) {
            Log.e(TAG, "Exception in SavePlaylist " + ex);
        }
    }

    private void writeToFile(String data,String filename) {
        try {
            File path = mContext.getFilesDir();
            File file = new File(path, filename);
            if (!file.exists()) {
                file.getParentFile().mkdirs(); // Will create parent directories if not exists
                file.createNewFile();
            }
            FileOutputStream writer = new FileOutputStream(file,false);
            writer.write(data.getBytes());
            writer.close();
            Log.d("TAG", "Wrote to file: " + filename);

        }

        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e);
        }
    }

    public void Add2Playlist(String trackData) {

        ReadPlaylist();

        try {
            TrackItem track = new TrackItem(trackData);
            if (mPlaylistTracks != null) {
                mPlaylistTracks.add(track);
                Log.d(TAG, "track added to playlist " +track.getName());
            }
            if (playlistItemsAdapter != null) {
                playlistItemsAdapter.notifyDatasetChanged();
            }
        } catch (Exception ex) {
            Log.e(TAG, "Exception in Add2Playlist " + ex);
        }
        SavePlaylist();
    }

    private void DeletePlaylist(){
        try {
            File path = mContext.getFilesDir();
            File newDir = new File(path + "/" + filename);

            if (!newDir.exists()) {
                newDir.mkdir();
            }
            FileOutputStream writer = new FileOutputStream(new File(path, filename),false);

            writer.close();
            Log.d("TAG", "Wrote empty to file: " + newDir);

        }

        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e);
        }

        ReadPlaylist();

        if (playlistItemsAdapter != null) {
            playlistItemsAdapter.notifyDatasetChanged();
        }
    }

    private void UpdateInfo() {
        //MainActivity activity = (MainActivity) getActivity();

        if (noOfTracks != null) {
            String counts = "" + mPlaylistTracks.size() + " " + getString(R.string.tracks);
            noOfTracks.setText(counts);
        }



    }

}
