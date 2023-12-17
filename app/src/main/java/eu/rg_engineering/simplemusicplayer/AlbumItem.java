package eu.rg_engineering.simplemusicplayer;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.UUID;

public class AlbumItem implements Parcelable  {

    private String mID;
    private String mName;
    private int mPlex_ratingKey=47872;


    protected AlbumItem(Parcel in) {
        mID = in.readString();
        mName = in.readString();
    }
    public AlbumItem(String name, int plex_ratingkey ) {
        mID = UUID.randomUUID().toString();
        mName = name;
        if (plex_ratingkey>0){
            mPlex_ratingKey=plex_ratingkey;
        }
    }

    public String getId() {
        return mID;
    }
    public String getName() {
        return mName;
    }
    public int getPlexRatingKey() {
        return mPlex_ratingKey;
    }

    public static final Creator<AlbumItem> CREATOR = new Creator<AlbumItem>() {
        @Override
        public AlbumItem createFromParcel(Parcel in) {
            return new AlbumItem(in);
        }
        @Override
        public AlbumItem[] newArray(int size) {
            return new AlbumItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(mID);
        parcel.writeString(mName);
        parcel.writeInt(mPlex_ratingKey);
    }

    public static ArrayList<AlbumItem> createItemsList(int numItems) {
        ArrayList<AlbumItem> items = new ArrayList<>();

        for (int i = 1; i <= numItems; i++) {
            items.add(new AlbumItem("album"+i,-1));
        }
        return items;
    }

    public String Serialize2() {
        String sRet = "";

        sRet += mID;
        sRet += System.getProperty("line.separator");
        sRet += mName;
        sRet += System.getProperty("line.separator");
        sRet += mPlex_ratingKey;
        sRet += System.getProperty("line.separator");

        return sRet;
    }

    public String Serialize(Boolean all) {
        String sRet = "";

        sRet += "," + mID;
        sRet += "," + mName;
        sRet += "," + mPlex_ratingKey;
        sRet += System.getProperty("line.separator");
        return sRet;
    }
}
