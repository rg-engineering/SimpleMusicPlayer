package eu.rg_engineering.simplemusicplayer.ui.home;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import eu.rg_engineering.simplemusicplayer.Constants;
import eu.rg_engineering.simplemusicplayer.R;
import eu.rg_engineering.simplemusicplayer.MusicItem;
import eu.rg_engineering.simplemusicplayer.MusicItemsAdapter;
import eu.rg_engineering.simplemusicplayer.utils.MyItemTouchHelper;
import eu.rg_engineering.simplemusicplayer.utils.OnDeleteMusicitemListener;
import io.sentry.Sentry;


public class HomeFragment_old extends Fragment implements
        OnDeleteMusicitemListener {

    private HomeViewModel homeViewModel;
    ArrayList<MusicItem> items;
    private RecyclerView rvMusicItems = null;
    private MusicItemsAdapter MusicItemsAdapter = null;
    private final SearchView searchView = null;
    private final String TAG = "HomeFragment";
    private final String filename = "ShoppingList";
    private final int REQUEST_CODE_EDIT = 101;
    HomeFragmentListener mCommunication;
    Context mContext;

    @Override
    public void ItemDeleted() {
        SaveData();
    }

    public void GetNextSong(){
        MusicItemsAdapter.GetNextSong();
    }

    public void SetCurrentplaytime(long duration){
        MusicItemsAdapter.SetCurrentPlaytime(duration);
    }

    //Interface for communication
    public interface HomeFragmentListener {
        void messageFromHomeFragment(String msg, String params);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        inflater.inflate(R.menu.main_menu, menu);
        /*
        MenuItem item = null;
        item = menu.findItem(R.id.action_item_search);

        if (item != null) {
            searchView = (SearchView) item.getActionView();
        }

        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {

                    Log.d(TAG, "filter text changed " + newText);

                    MusicItemsAdapter.setFilterIdx(0);
                    MusicItemsAdapter.getFilter().filter(newText);
                    return false;
                }
            });
        }
*/
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCommunication = (HomeFragmentListener) context;
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCommunication = null;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home_old, container, false);

        try {
            // Lookup the recyclerview in activity layout
            rvMusicItems = (RecyclerView) root.findViewById(R.id.rvItems);

            // Initialize items
            items = MusicItem.createItemsList(0);

            LoadData();

            // Create adapter passing in the sample user data
            MusicItemsAdapter = new MusicItemsAdapter(items,this);


            ItemTouchHelper.Callback callback = new MyItemTouchHelper(MusicItemsAdapter);
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
            MusicItemsAdapter.setTouchHelper(itemTouchHelper);
            itemTouchHelper.attachToRecyclerView(rvMusicItems);
            // Attach the adapter to the recyclerview to populate items
            rvMusicItems.setAdapter(MusicItemsAdapter);

            // Set layout manager to position the items
            rvMusicItems.setLayoutManager(new LinearLayoutManager(getActivity()));
            rvMusicItems.setItemAnimator(null);
            // That's all!

            AutoCompleteTextView editFilterArtist = (AutoCompleteTextView ) root.findViewById(R.id.filter_artist);
            EditText editFilterTitle = (EditText) root.findViewById(R.id.filter_title);
            EditText editFilterAlbum = (EditText) root.findViewById(R.id.filter_album);

            ArrayList <String> ArtistList = new ArrayList<>();

            for (int i = 0; i < items.size(); i++) {

                String artist = items.get(i).getArtist();
                if (!ArtistList.contains(artist)) {
                    ArtistList.add(artist);
                }
            }

            ArrayAdapter<String> ArtistListAdapter = new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_dropdown_item_1line, ArtistList);

            editFilterArtist.setThreshold(1);
            editFilterArtist.setAdapter(ArtistListAdapter);

            //ImageButton btnFilterArtistSearch = (ImageButton) root.findViewById(R.id.filter);
            /*btnFilterArtistSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "Search Artist pressed");
                    mCommunication.messageFromHomeFragment("ReplaceFragment", "SearchArtist");
                }
            });
*/

            editFilterArtist.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    Log.d(TAG, "artist filter text changed " + s);
                    MusicItemsAdapter.setFilterIdx(1);
                    MusicItemsAdapter.getFilter().filter(s);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }

            });

            editFilterTitle.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    Log.d(TAG, "title filter text changed " + s);
                    MusicItemsAdapter.setFilterIdx(2);
                    MusicItemsAdapter.getFilter().filter(s);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }

            });

            editFilterAlbum.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    Log.d(TAG, "album filter text changed " + s);
                    MusicItemsAdapter.setFilterIdx(3);
                    MusicItemsAdapter.getFilter().filter(s);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }

            });

            Button btnPauseMusic = (Button) root.findViewById(R.id.PauseMusic);
            btnPauseMusic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "PauseMusic pressed");
                    mCommunication.messageFromHomeFragment("PauseMusic", "");
                }
            });

            Button btnPlayMusic = (Button) root.findViewById(R.id.PlayMusic);
            btnPlayMusic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "PlayMusic pressed");
                    mCommunication.messageFromHomeFragment("PlayMusic", "");
                }
            });

            Button btnStopMusic = (Button) root.findViewById(R.id.StopMusic);
            btnStopMusic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "StopMusic pressed");
                    mCommunication.messageFromHomeFragment("StopMusic", "");
                }
            });


            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String backGroundColor = sharedPreferences.getString("BackgroundColor", "");
            String backGroundImage = sharedPreferences.getString("BackgroundImage", "");
            Boolean useBackGroundColor = sharedPreferences.getBoolean("useBackgroundColor", false);

            Log.d(TAG, "set background color to " + backGroundColor);

            if (useBackGroundColor && backGroundColor != null && backGroundColor.length() > 1) {
                rvMusicItems.setBackgroundColor(Color.parseColor(backGroundColor));

            } else if (!useBackGroundColor && backGroundImage != null && backGroundImage.length() > 1) {

                try {
                    InputStream inputStream = getActivity().getContentResolver().openInputStream(Uri.parse(backGroundImage));
                    Drawable myPic = Drawable.createFromStream(inputStream, backGroundImage);

                    rvMusicItems.setBackground(myPic);

                } catch (FileNotFoundException e) {
                    Log.e(TAG, "image not found " + backGroundImage);
                    Sentry.captureException(e);
                }
            }


            setHasOptionsMenu(true);

            FillFilters();
        } catch (Exception ex) {
            Log.e(TAG, "exception in onCreateView " + ex);
            Sentry.captureException(ex);
        }
        return root;

        //return inflater.inflate(R.layout.fragment_home, container, false);
    }


    private void FillFilters() {

        //todo fill filtes
    }


    private String AddDisclaimer() {
        String sRet = System.getProperty("line.separator") + "sent by simplemusicplayer by rg-engineering" + System.getProperty("line.separator");

        return sRet;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EDIT) {
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) {
                    return;
                }
                boolean isEdit = data.getBooleanExtra(Constants.ITEM_INTENT_EDIT, false);
                MusicItem item = data.getParcelableExtra(Constants.ITEM_INTENT_OBJECT);
                if (isEdit) {
                    int index = data.getIntExtra(Constants.ITEM_INTENT_INDEX, -1);
                    if (index == -1) {
                        return;
                    }
                    MusicItemsAdapter.ChangeItem(index, item);

                } else {
                    //items.add(item);
                    MusicItemsAdapter.AddItem(item);
                }
                MusicItemsAdapter.notifyDataSetChanged();

                MusicItemsAdapter.getFilter().filter("");

                SaveData();
            }
        }
    }


    //save nach delete item


    public void LoadData() {
        /*
        Log.d(TAG,"load data ");

        readFromFile(filename);

        Log.d(TAG,"data loaded,  " + items.size() + " items " );

         */
        ContentResolver cr = getActivity().getContentResolver();
        getAllMusic(cr);


    }

    public void SaveData() {
        Log.d(TAG, "save date ");
        String Contents = "";
        try {

            //File oriFile = new File(getFilesDir(), filename);

            //OutputStream outStream = new FileOutputStream(oriFile);

            for (int i = 0; i < items.size(); i++) {
                Contents += items.get(i).Serialize(true);
            }


            writeToFile(Contents, filename);

        } catch (Exception ex) {
            Log.e(TAG, "exception in save file " + ex);
            Sentry.captureException(ex);
        }
    }


    private void writeToFile(String data, String filename) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getActivity().openFileOutput(filename, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e);
            Sentry.captureException(e);
        }


    }

    private String readFromFile(String filename) {

        String ret = "";

        try {
            InputStream inputStream = getActivity().openFileInput(filename);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                //StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    //stringBuilder.append("\n").append(receiveString);

                    MusicItem item = new MusicItem(receiveString);


                    items.add(item);

                }

                inputStream.close();
                //ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + e);
            Sentry.captureException(e);
        } catch (IOException e) {
            Log.e(TAG, "Can not read file: " + e);
            Sentry.captureException(e);
        }


        return ret;
    }


    public final Uri AUDIO_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    public void getAllMusic(ContentResolver cr) {
        Log.d(TAG, "check for audio files ");
        String[] projection = {
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ARTIST_ID,

                //MediaStore.Audio.Media.DATA
        };
        String sortOrder = MediaStore.Audio.Media._ID + " desc";
        Cursor cur = cr.query(AUDIO_URI, projection, null, null, sortOrder);
        List<String> pathNameList = new ArrayList<>();
        if (cur != null) {

            while (cur.moveToNext()) {
                String Album = cur.getString(0);
                String AlbumId = cur.getString(5);
                String Artist = cur.getString(1);
                String ArtistId = cur.getString(6);
                String Title = cur.getString(2);
                String fileName = cur.getString(3);
                int nDuration=0;
                try {
                nDuration = Integer.parseInt(cur.getString(4));
                }
                catch (NumberFormatException ex){

                }
                String sDuration = String.format("%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(nDuration),
                        TimeUnit.MILLISECONDS.toMinutes(nDuration) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(nDuration)),
                        TimeUnit.MILLISECONDS.toSeconds(nDuration) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(nDuration)));


                String Line = Artist + " - " + Title + " " + sDuration;

                Log.d(TAG, "Audio " + Line);

                //https://stackoverflow.com/questions/23043123/retrive-the-track-image-from-android-mediastore-audio-media-content-provider
                String[] projection2 = {
                        MediaStore.Audio.AlbumColumns.ALBUM_ART
                };
                String selection = MediaStore.Audio.Media._ID + " =?";
                String[] selectionArgs = {
                        String.valueOf(AlbumId)
                };
                Cursor artCursor = cr.query(
                        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                        projection2,
                        selection,
                        selectionArgs,
                        null);
                String albumArt;
                if (artCursor.moveToNext()) {
                    albumArt = "file://" + artCursor.getString(0);
                } else {
                    albumArt = null;
                }
                Log.d(TAG, "Image " + albumArt);
                artCursor.close();

                MusicItem song = new MusicItem(Title, Album, Artist, fileName, nDuration);

                items.add(song);
            }
            cur.close();
        }

    }
}