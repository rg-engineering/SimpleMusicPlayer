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
import android.widget.TextView;

import androidx.annotation.NonNull;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import eu.rg_engineering.simplemusicplayer.utils.ItemTouchHelperAdapter;

// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
public class MusicItemsAdapter extends
        RecyclerView.Adapter<MusicItemsAdapter.ViewHolder> implements
        Filterable,
        ItemTouchHelperAdapter
{

    private  String TAG = "MusicItemsAdapter";
    private List<MusicItem> mItems;
    private ItemTouchHelper mTouchHelper;
    private int mFilterIdx=0;
    Context mContext;
    MusicItemsAdapterListener mCommunication;
    //Interface for communication
    public interface MusicItemsAdapterListener {
        void messageFromMusicItemsAdapter(String msg, String params);
    }
    // Pass in the contact array into the constructor
    public MusicItemsAdapter(List<MusicItem> items) {

        mItems = items;
        UpdateData();

    }


    public void UpdateData(){
        Log.d(TAG,"UpdateData called "  );
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
        mContext=context;
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
// Get the data model based on position
        MusicItem item = mItems.get(position);

        Log.d(TAG,"onBindViewHolder called, position " +position  );
        TextView titleTextView = viewHolder.titleTextView;
        if (titleTextView!=null) {
            titleTextView.setText(item.getTitle());
        }
        TextView artistTextView = viewHolder.artistTextView;
        if (artistTextView!=null) {
            artistTextView.setText(item.getArtist());
        }
        TextView albumTextView = viewHolder.albumTextView;
        if (albumTextView!=null) {
            albumTextView.setText(item.getAlbum());
        }
        TextView durationTextView = viewHolder.durationTextView;
        if (durationTextView!=null) {
            durationTextView.setText(item.getDuration());
        }

        Button btnPlaySong = viewHolder.btnPlaySong;
        btnPlaySong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "PlayMusic pressed position " + position);
                String filename = mItems.get(position).getFilename();
                mCommunication.messageFromMusicItemsAdapter("PlayMusic",filename);
            }
        });
    }

    @Override
    public int getItemCount() {
        int cnt=0;

        if (mItems!=null){
            cnt=mItems.size();
        }
        Log.d(TAG,"items " + cnt  );
        return cnt;
    }

    public void setTouchHelper (ItemTouchHelper touchHelper)
    {
        this.mTouchHelper = touchHelper;
    }

    public void setFilterIdx(int idx){
        mFilterIdx=idx;
    }

    @Override
    public Filter getFilter() {
        Log.d(TAG,"getFilter #" + mFilterIdx );

        Filter oRet = null;

        switch (mFilterIdx)
        {

            default:
                oRet= filterItemname;
                break;

        }

        return oRet;
    }

    Filter filterItemname = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            List<MusicItem> filteredList = new ArrayList<>();

            if (constraint.toString().isEmpty()){

                Log.d(TAG,"no filter set " + mItems.size());

                filteredList.addAll(mItems);

            } else {

                Log.d(TAG,"filter set " + constraint.toString() + " " + mItems.size() );
                

                for (MusicItem item:mItems) {

                    Log.d(TAG,"compare " +  item.getTitle().toLowerCase()  + " ?= " + constraint.toString().toLowerCase());

                    if (item.getTitle().toLowerCase().contains(constraint.toString().toLowerCase())){
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
            mItems.clear();

            mItems.addAll((Collection<? extends MusicItem>) results.values);

            notifyDataSetChanged();
        }
    };



    @Override
    public void onItemMoved(int fromPosition, int toPosition) {

    }

    @Override
    public void onItemSwiped(int position) {

    }


    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnTouchListener,

            GestureDetector.OnGestureListener
    {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView titleTextView;
        public TextView artistTextView;
        public TextView albumTextView;
        public TextView durationTextView;
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
            btnPlaySong = (Button) itemView.findViewById(R.id.PlaySong);

            mGestureDetector = new GestureDetector(itemView.getContext(),this);



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
            Log.d(TAG,"onSingleTapUp position " + this.getAdapterPosition());


            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (mTouchHelper!=null) {
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

    public String GetSerializedData()
    {
        String sRet="";

        for (int i =0; i< mItems.size(); i++) {
            sRet += mItems.get(i).Serialize2();
        }
        return sRet;
    }

    public void ChangeItem(int index, MusicItem item)
    {
        Log.d(TAG,"change item " + item.getTitle() );
        mItems.set(index, item);

        int pos = FindItemInList(mItems, item.getTitle());

        if (pos > -1) {
            mItems.set(pos, item);
        }


    }

    public void AddItem(MusicItem item)
    {
        Log.d(TAG,"add item " + item.getTitle());

        mItems.add(item);


        getFilter().filter("");

    }


    private int FindItemInList(List<MusicItem> list, String id){
        int nRet=-1;

        try {
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {

                    Log.d(TAG, "check item " + list.get(i).getTitle() +  " =? " + id);

                    if (list.get(i).getTitle().toLowerCase().contains(id.toLowerCase())) {

                        Log.d(TAG, "item found at " + i);
                        nRet = i;
                    }
                }
            }
        }
        catch (Exception ex){
            Log.e(TAG, "exception in FindItemInList " + ex.toString());
        }
        return nRet;
    }

}
