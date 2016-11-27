package io.sjostrom.sitesource.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

import java.util.Date;

import io.realm.RealmObject;

/**
 * Created by Nick on 11/27/2016.
 */

public class History extends RealmObject implements SearchSuggestion {
    public String url;
    public Date lastsearched;

    public History(){}

    @Override
    public String getBody() {
        return url;
    }

    // Parcelling part
    public History(Parcel in){
        String[] data = new String[3];

        in.readStringArray(data);
        this.url = data[0];
        this.lastsearched = new Date(data[1]);
    }

    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {this.url,
                this.lastsearched.toString()});
    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public History createFromParcel(Parcel in) {
            return new History(in);
        }

        public History[] newArray(int size) {
            return new History[size];
        }
    };
}
