package io.sjostrom.webresources;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by Nick on 11/22/2016.
 */

public class ResourceListAdapter extends RecyclerView.Adapter<ResourceListAdapter.ViewHolder> {

    private static String TAG = ResourceListAdapter.class.getSimpleName();

    Context mContext;
    private JSONArray mResourceList;

    ResourceListAdapter(Context context) {
        mContext = context;
        mResourceList = new JSONArray();
    }

    ResourceListAdapter(Context context, JSONArray resources) {
        mContext = context;
        mResourceList = resources;
    }

    public void setList(JSONArray resources) {
        mResourceList = resources;
    }

    public void clearList() {
        mResourceList = new JSONArray();
        this.notifyDataSetChanged();
    }

    @Override
    public ResourceListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.resource_item, parent, false); //Inflating the layout

        ViewHolder vhItem = new ViewHolder(v, viewType); //Creating ViewHolder and passing the object of type view

        return vhItem;
    }

    @Override
    public void onBindViewHolder(ResourceListAdapter.ViewHolder holder, int position) {
        if (position < this.mResourceList.length()) {
            try {
                holder.textName.setText(this.mResourceList.getJSONObject(position).getString("name"));
            } catch (JSONException e) {
                Log.w(TAG, e.toString());
            }
        }
    }

    @Override
    public int getItemCount() {
        return mResourceList.length();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public int viewType = -1;
        public View view;

        TextView textName;

        public ViewHolder(View itemView, int viewType) {
            super(itemView);

            textName = (TextView) itemView.findViewById(R.id.resource_name);
        }
    }
}
