package io.sjostrom.webresources;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONObject;


public class ResourceDetailDialog extends DialogFragment {
    private static final String RESOURCE_TIMING = "io.sjostrom.webresources.RESOURCE_TIMING";

    private String resourceTiming;

    private TextView mResourceName;
    private TextView mResourceDuration;

    public ResourceDetailDialog() {
        // Required empty public constructor
    }

    public static ResourceDetailDialog newInstance(String timing) {
        ResourceDetailDialog fragment = new ResourceDetailDialog();
        Bundle args = new Bundle();
        args.putString(RESOURCE_TIMING, timing);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            resourceTiming = getArguments().getString(RESOURCE_TIMING);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_resource_detail_dialog, container, false);

        mResourceDuration = (TextView) view.findViewById(R.id.resource_duration);
        mResourceName = (TextView) view.findViewById(R.id.resource_name);

        try {
            JSONObject jsonResource = new JSONObject(resourceTiming);
            mResourceName.setText(jsonResource.getString("name"));
            mResourceDuration.setText(jsonResource.getInt("duration") + " ms");
        } catch(Exception e) {

        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ResourceDetailDialog.this.dismiss();
            }
        });

        return view;
    }
}
