package eu.rg_engineering.simplemusicplayer;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.UUID;

public class TrackItem implements Parcelable  {

    private String mID;
    private String mName;


    protected TrackItem(Parcel in) {
        mID = in.readString();
        mName = in.readString();
    }
    public TrackItem(String name ) {
        mID = UUID.randomUUID().toString();
        mName = name;
    }

    public String getId() {
        return mID;
    }
    public String getName() {
        return mName;
    }


    public static final Creator<TrackItem> CREATOR = new Creator<TrackItem>() {
        @Override
        public TrackItem createFromParcel(Parcel in) {
            return new TrackItem(in);
        }
        @Override
        public TrackItem[] newArray(int size) {
            return new TrackItem[size];
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
    }

    public static ArrayList<TrackItem> createItemsList(int numItems) {
        ArrayList<TrackItem> items = new ArrayList<>();

        for (int i = 1; i <= numItems; i++) {
            items.add(new TrackItem("track"+i));
        }
        return items;
    }

    public String Serialize2() {
        String sRet = "";

        sRet += mID;
        sRet += System.getProperty("line.separator");
        sRet += mName;
        sRet += System.getProperty("line.separator");

        return sRet;
    }

    public String Serialize(Boolean all) {
        String sRet = "";

        sRet += "," + mID;
        sRet += "," + mName;
        sRet += System.getProperty("line.separator");
        return sRet;
    }
}
