package eu.rg_engineering.simplemusicplayer;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.UUID;

public class AlbumItem implements Parcelable {

    private final String mID;
    private final String mName;
    private ArrayList<String> mArtists;
    private int mYear;
    private String mPath2Image;
    private String mSummery;
    private int mPlex_ratingKey = -1;

    protected AlbumItem(Parcel in) {
        mID = in.readString();
        mName = in.readString();
    }

    public AlbumItem(String name, String artist, int year, String path2image, String summery, int plex_ratingkey) {
        mID = UUID.randomUUID().toString();
        mName = name;
        if (mArtists==null) {
            mArtists = new ArrayList<>();
        }
        mArtists.add(artist);
        mYear = year;
        mPath2Image = path2image;
        mSummery = summery;
        if (plex_ratingkey > 0) {
            mPlex_ratingKey = plex_ratingkey;
        }
    }

    public String getId() {
        return mID;
    }

    public String getName() {
        return mName;
    }

    public void AddArtist(String artist){

        if (!mArtists.contains(artist)){
            mArtists.add(artist);
        }
    }

    public Boolean containArtist(String artist){
        Boolean ret=false;

        ret = mArtists.contains(artist);

        return ret;
    }
    public String getArtist(int idx) {

        String artist="";
        if (idx<mArtists.size()){
            artist=mArtists.get(idx);
        }
        return artist;
    }

    public String getArtist() {

        String artist="";
        if (mArtists.size()>1){
            artist="various";
        }
        else {
            artist=mArtists.get(0);
        }

        return artist;
    }

    public Boolean ArtistExists(String artist){
        return mArtists.contains(artist);
    }

    public int getYear() {
        return mYear;
    }

    public String getPath2Image() {
        return mPath2Image;
    }

    public String getInfo() {
        return mSummery;
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
        parcel.writeArray(mArtists.toArray());
        parcel.writeInt(mYear);
        parcel.writeString(mPath2Image);
        parcel.writeString(mSummery);
        parcel.writeInt(mPlex_ratingKey);
    }

    public static ArrayList<AlbumItem> createItemsList(int numItems) {
        ArrayList<AlbumItem> items = new ArrayList<>();

        for (int i = 1; i <= numItems; i++) {
            items.add(new AlbumItem("album" + i, "test artist", 2011, "", "", -1));
        }
        return items;
    }

    public String Serialize2() {
        String sRet = "";

        sRet += mID;
        sRet += System.getProperty("line.separator");
        sRet += mName;
        sRet += System.getProperty("line.separator");
        sRet += mArtists.toString();
        sRet += System.getProperty("line.separator");
        sRet += mYear;
        sRet += System.getProperty("line.separator");
        sRet += mPath2Image;
        sRet += System.getProperty("line.separator");
        sRet += mSummery;
        sRet += System.getProperty("line.separator");
        sRet += mPlex_ratingKey;
        sRet += System.getProperty("line.separator");

        return sRet;
    }

    public String Serialize(Boolean all) {
        String sRet = "";

        sRet += "," + mID;
        sRet += "," + mName;
        sRet += "," + mArtists.toString();
        sRet += "," + mYear;
        sRet += "," + mPath2Image;
        sRet += "," + mSummery;
        sRet += "," + mPlex_ratingKey;
        sRet += System.getProperty("line.separator");
        return sRet;
    }
}
