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

import eu.rg_engineering.simplemusicplayer.AlbumItem;
import eu.rg_engineering.simplemusicplayer.AlbumItemsAdapter;
import eu.rg_engineering.simplemusicplayer.MainActivity;
import eu.rg_engineering.simplemusicplayer.MusicData.MusicData;
import eu.rg_engineering.simplemusicplayer.R;
import eu.rg_engineering.simplemusicplayer.utils.MyItemTouchHelper;
import eu.rg_engineering.simplemusicplayer.utils.OnDeleteAlbumitemListener;

public class AlbumsFragment extends Fragment implements
        OnDeleteAlbumitemListener {

    private String TAG = "AlbumsFragment";
    AlbumsFragmentListener mCommunication;
    Context mContext;
    private RecyclerView rvAlbumItems = null;
    private AlbumItemsAdapter AlbumItemsAdapter = null;
    ArrayList<AlbumItem> mAlbums;

    @Override
    public void ItemDeleted() {
        //SaveData();
    }

    public interface AlbumsFragmentListener {
        void messageFromAlbumsFragment(String msg, String params);
    }
    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
        mCommunication = (AlbumsFragmentListener) context;
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCommunication = null;
    }
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_albums, container, false);

        try {
            rvAlbumItems = (RecyclerView) root.findViewById(R.id.rvAlbums);

            MainActivity activity = (MainActivity) getActivity();
            mAlbums=activity.mMusicData.getAlbumData();

            // Create adapter passing in the sample user data
            AlbumItemsAdapter = new AlbumItemsAdapter(mAlbums,this);


            ItemTouchHelper.Callback callback = new MyItemTouchHelper(AlbumItemsAdapter);
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
            AlbumItemsAdapter.setTouchHelper(itemTouchHelper);
            itemTouchHelper.attachToRecyclerView(rvAlbumItems);
            // Attach the adapter to the recyclerview to populate items
            rvAlbumItems.setAdapter(AlbumItemsAdapter);

            // Set layout manager to position the items
            rvAlbumItems.setLayoutManager(new LinearLayoutManager(getActivity()));
            rvAlbumItems.setItemAnimator(null);
            // That's all!
            AutoCompleteTextView editFilterAlbum = (AutoCompleteTextView ) root.findViewById(R.id.filter_albums);
            ArrayList <String> AlbumList = new ArrayList<>();

            for (int i = 0; i < mAlbums.size(); i++) {

                String album = mAlbums.get(i).getName();
                if (!AlbumList.contains(album)) {
                    AlbumList.add(album);
                }
            }

            ArrayAdapter<String> AlbumListAdapter = new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_dropdown_item_1line, AlbumList);

            editFilterAlbum.setThreshold(1);
            editFilterAlbum.setAdapter(AlbumListAdapter);

            editFilterAlbum.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    Log.d(TAG, "album filter text changed " + s);
                    AlbumItemsAdapter.setFilterIdx(1);
                    AlbumItemsAdapter.getFilter().filter(s);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }

            });
            Button btnBack = (Button) root.findViewById(R.id.btnBackAlbums);
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "back button pressed");
                    mCommunication.messageFromAlbumsFragment("btnBack", "");
                }
            });

        } catch (Exception ex) {
            Log.e(TAG, "exception in onCreateView " + ex.toString());
        }
        return root;


    }
    public void ReadPlexAlbumData(){

        MainActivity activity = (MainActivity) getActivity();
        if (activity.mMusicData!=null) {
            activity.mMusicData.ReadPlexAlbumData();
            Log.d(TAG, "plex data read ");

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mAlbums != null) {
                        AlbumItemsAdapter.updateItems(mAlbums);
                        AlbumItemsAdapter.notifyDatasetChanged();
                    }
                }
            });

            Log.d(TAG, "adapter notified ");
        }
    }
}
