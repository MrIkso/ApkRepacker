package com.mrikso.apkrepacker.fragment;


import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.google.android.material.button.MaterialButton;
import com.mrikso.apkrepacker.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class LibraresFragment extends Fragment {


    public LibraresFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_librares, container, false);
      ////  final Dialog dialog = new Dialog(this, R.style.DialogFullscreenWithTitle);
       // dialog.setTitle(getString(R.string.about_source_licenses));
      //  dialog.setContentView(R.layout.dialog_source_licenses);
        WebView webView = view.findViewById(R.id.web_source_licenses);
        webView.loadUrl("file:///android_asset/open_source_license.html");
     //   MaterialButton btn_source_licenses_close = dialog.findViewById(R.id.btn_close);
       // btn_source_licenses_close.setOnClickListener(v -> dialog.dismiss());
       // dialog.show();
        // Inflate the layout for this fragment
        return view;
    }

}
