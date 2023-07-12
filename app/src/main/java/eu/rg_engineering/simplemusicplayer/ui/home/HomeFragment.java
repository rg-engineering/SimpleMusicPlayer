package eu.rg_engineering.simplemusicplayer.ui.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import eu.rg_engineering.simplemusicplayer.Constants;
import eu.rg_engineering.simplemusicplayer.R;
import eu.rg_engineering.simplemusicplayer.MusicItem;
import eu.rg_engineering.simplemusicplayer.MusicItemsAdapter;
import eu.rg_engineering.simplemusicplayer.utils.MyItemTouchHelper;


public class HomeFragment extends Fragment  {

    private HomeViewModel homeViewModel;
    ArrayList<MusicItem> items;
    private RecyclerView rvShoppingItems= null;
    private MusicItemsAdapter ShoppingItemsAdapter= null;
    private SearchView searchView= null;;
    private String TAG = "HomeFragment";
    private String filename = "ShoppingList";
    private final int REQUEST_CODE_EDIT = 101;


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        inflater.inflate(R.menu.main_menu, menu);
        MenuItem item = null;
        item = menu.findItem(R.id.action_item_search);

        if (item != null) {
            searchView = (SearchView) item.getActionView();
        }

        if (searchView!=null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {

                    Log.d(TAG, "filter text changed " + newText);

                    ShoppingItemsAdapter.setFilterIdx(0);
                    ShoppingItemsAdapter.getFilter().filter(newText);
                    return false;
                }
            });
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        try {
            // Lookup the recyclerview in activity layout
            rvShoppingItems = (RecyclerView) root.findViewById(R.id.rvItems);


            // Initialize items
            items = MusicItem.createItemsList(0);

            LoadData();

            // Create adapter passing in the sample user data
            ShoppingItemsAdapter = new MusicItemsAdapter(items);


            ItemTouchHelper.Callback callback = new MyItemTouchHelper(ShoppingItemsAdapter);
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
            ShoppingItemsAdapter.setTouchHelper(itemTouchHelper);
            itemTouchHelper.attachToRecyclerView(rvShoppingItems);
            // Attach the adapter to the recyclerview to populate items
            rvShoppingItems.setAdapter(ShoppingItemsAdapter);

            // Set layout manager to position the items
            rvShoppingItems.setLayoutManager(new LinearLayoutManager(getActivity()));

            // That's all!

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String backGroundColor = sharedPreferences.getString("BackgroundColor", "");
            String backGroundImage = sharedPreferences.getString("BackgroundImage", "");
            Boolean useBackGroundColor = sharedPreferences.getBoolean("useBackgroundColor", false);

            Log.d(TAG, "set background color to " + backGroundColor);

            if (useBackGroundColor && backGroundColor != null && backGroundColor.length() > 1) {
                rvShoppingItems.setBackgroundColor(Color.parseColor(backGroundColor));

            } else if (!useBackGroundColor && backGroundImage != null && backGroundImage.length() > 1) {

                try {
                    InputStream inputStream = getActivity().getContentResolver().openInputStream(Uri.parse(backGroundImage));
                    Drawable myPic = Drawable.createFromStream(inputStream, backGroundImage);

                    rvShoppingItems.setBackground(myPic);

                } catch (FileNotFoundException e) {
                    Log.e(TAG, "image not found " + backGroundImage);
                }
            }




            setHasOptionsMenu(true);

            FillFilters();
        } catch (Exception ex) {
            Log.e(TAG, "exception in onCreateView " + ex.toString());
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
        if(requestCode == REQUEST_CODE_EDIT){
            if(resultCode == Activity.RESULT_OK){
                if(data == null){
                    return;
                }
                boolean isEdit = data.getBooleanExtra(Constants.ITEM_INTENT_EDIT, false);
                MusicItem item = data.getParcelableExtra(Constants.ITEM_INTENT_OBJECT);
                if(isEdit){
                    int index = data.getIntExtra(Constants.ITEM_INTENT_INDEX, -1);
                    if(index == -1){
                        return;
                    }
                    ShoppingItemsAdapter.ChangeItem(index, item);

                }else{
                    //items.add(item);
                    ShoppingItemsAdapter.AddItem(item);
                }
                ShoppingItemsAdapter.notifyDataSetChanged();

                ShoppingItemsAdapter.getFilter().filter("");

                SaveData();
            }
        }
    }


    //save nach delete item




    public void LoadData() {

        Log.d(TAG,"load data ");

        readFromFile(filename);

        Log.d(TAG,"data loaded,  " + items.size() + " items " );
    }

    public void SaveData() {
        Log.d(TAG,"save date ");
        String Contents = "";
        try {

            //File oriFile = new File(getFilesDir(), filename);

            //OutputStream outStream = new FileOutputStream(oriFile);

            for (int i =0; i< items.size(); i++) {
                Contents += items.get(i).Serialize(true);
            }


            writeToFile(Contents,filename);

        }
        catch (Exception ex){
            Log.e(TAG,"exception in save file " + ex.toString());
        }
    }


    private void writeToFile(String data,String filename) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getActivity().openFileOutput(filename, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }


    }

    private String readFromFile(String filename) {

        String ret = "";

        try {
            InputStream inputStream = getActivity().openFileInput(filename);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                //StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    //stringBuilder.append("\n").append(receiveString);

                    MusicItem item = new MusicItem(receiveString);


                        items.add(item);

                }

                inputStream.close();
                //ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, "Can not read file: " + e.toString());
        }


        return ret;
    }



}