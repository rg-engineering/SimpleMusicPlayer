package eu.rg_engineering.simplemusicplayer;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.UUID;

public class ArtistItem implements Parcelable {

    private String mID;
    private String mName;
    private String mGenre;
    private String mCountry;
    private String mPath2Image;
    private String mSummery;
    private int mPlex_ratingKey = -1;

    protected ArtistItem(Parcel in) {
        mID = in.readString();
        mName = in.readString();
    }

    public ArtistItem(String name, String genre, String country, String path2image, String summery, int plex_ratingkey) {
        mID = UUID.randomUUID().toString();
        mName = name;
        mGenre = genre;
        mCountry = country;
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

    public String getGenre() {
        return mGenre;
    }

    public String getCountry() {
        return mCountry;
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
        parcel.writeString(mGenre);
        parcel.writeString(mCountry);
        parcel.writeString(mPath2Image);
        parcel.writeString(mSummery);
        parcel.writeInt(mPlex_ratingKey);
    }

    public static ArrayList<ArtistItem> createItemsList(int numItems) {
        ArrayList<ArtistItem> items = new ArrayList<>();

        for (int i = 1; i <= numItems; i++) {
            items.add(new ArtistItem("artist" + i, "Jazz", "Germany", "", "no info", -1));
        }
        return items;
    }

    public String Serialize2() {
        String sRet = "";

        sRet += mID;
        sRet += System.getProperty("line.separator");
        sRet += mName;
        sRet += System.getProperty("line.separator");
        sRet += mGenre;
        sRet += System.getProperty("line.separator");
        sRet += mCountry;
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
        sRet += "," + mGenre;
        sRet += "," + mCountry;
        sRet += "," + mPath2Image;
        sRet += "," + mSummery;
        sRet += "," + mPlex_ratingKey;
        sRet += System.getProperty("line.separator");
        return sRet;
    }
}
