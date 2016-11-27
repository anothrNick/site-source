package io.sjostrom.sitesource;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class LoadResourceDialog extends DialogFragment {

    private TextView mLoadingText;

    public LoadResourceDialog() {
        // Required empty public constructor
    }

    public static LoadResourceDialog newInstance() {
        LoadResourceDialog fragment = new LoadResourceDialog();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.load_resource_dialog_fragment, container, false);

        mLoadingText = (TextView) view.findViewById(R.id.loadingText);

        return view;
    }

    public void setLoadingText(String text) {
        if (mLoadingText != null) {
            mLoadingText.setText(text);
        }
    }
}
