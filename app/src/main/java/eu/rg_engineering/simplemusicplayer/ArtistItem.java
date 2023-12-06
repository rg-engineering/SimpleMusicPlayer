package eu.rg_engineering.simplemusicplayer;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.UUID;

public class ArtistItem implements Parcelable  {

    private String mID;
    private String mName;


    protected ArtistItem(Parcel in) {
        mID = in.readString();
        mName = in.readString();
    }
    public ArtistItem(String name) {
        mID = UUID.randomUUID().toString();
        mName = name;
    }

    public String getId() {
        return mID;
    }

    public String getName() {
        return mName;
    }

    public static final Creator<ArtistItem> CREATOR = new Creator<ArtistItem>() {
        @Override
        public ArtistItem createFromParcel(Parcel in) {
            return new ArtistItem(in);
        }

        @Override
        public ArtistItem[] newArray(int size) {
            return new ArtistItem[size];
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

    public static ArrayList<ArtistItem> createItemsList(int numItems) {
        ArrayList<ArtistItem> items = new ArrayList<>();

        for (int i = 1; i <= numItems; i++) {
            items.add(new ArtistItem("test"));
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
