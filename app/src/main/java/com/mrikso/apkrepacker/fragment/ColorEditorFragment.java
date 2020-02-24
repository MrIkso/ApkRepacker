package com.mrikso.apkrepacker.fragment;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jecelyin.editor.v2.ui.activities.MainActivity;
import com.mrikso.apkrepacker.App;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.ui.colorslist.ColorMeta;
import com.mrikso.apkrepacker.ui.colorslist.ColorsAdapter;
import com.mrikso.apkrepacker.ui.colorslist.ColorsViewModel;
import com.mrikso.apkrepacker.utils.ThemeWrapper;

import java.io.File;

import me.jfenn.colorpickerdialog.dialogs.ColorPickerDialog;
import me.jfenn.colorpickerdialog.interfaces.OnColorPickedListener;
import me.zhanghai.android.fastscroll.FastScrollerBuilder;

public class ColorEditorFragment extends Fragment implements ColorsAdapter.OnItemInteractionListener,
        OnColorPickedListener<ColorPickerDialog> {

    public static final String TAG = "ColorEditorFragment";
    private RecyclerView colorsList;
    private ColorsViewModel mViewModel;
    private ColorsAdapter colorsAdapter;
    private EditText mEditTextSearch;
    private Context mContext;
    private File colors;
    private String colorName;
    private int pos;

    public ColorEditorFragment(File colors) {
        this.colors = colors;
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_color_editor, container, false);
        // Inflate the layout for this fragment
        mContext = view.getContext();
        mViewModel = ViewModelProviders.of(this).get(ColorsViewModel.class);
        //  loading = view.findViewById(R.id.app_packages_loading);
        mViewModel.setColorsFile(colors);
        colorsList = view.findViewById(R.id.colors);
        colorsList.setLayoutManager(new LinearLayoutManager(App.getContext()));
        colorsList.getRecycledViewPool().setMaxRecycledViews(0, 24);
        colorsAdapter = new ColorsAdapter(App.getContext());
        // mViewModel.getPackages().observe(getViewLifecycleOwner(), appsAdapter::setData);

        colorsAdapter.setInteractionListener(this);
        new FastScrollerBuilder(colorsList).build();

        FloatingActionButton fabEdit = view.findViewById(R.id.fab_go_editor);
        fabEdit.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.putExtra("filePath", colors.getAbsolutePath());
            startActivity(intent);
        });
        initData();
        setupToolbar(view);
        return view;
    }

    private void initData() {
        mViewModel.getColors().observe(getViewLifecycleOwner(), colorsAdapter::setData);
        colorsList.setAdapter(colorsAdapter);
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
    }

    private void setupToolbar(View view) {
        //Search
        mEditTextSearch = view.findViewById(R.id.et_search);
        mEditTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filterColors();
            }
        });

        filterColors();
    }

    private void filterColors() {
        mViewModel.filter(mEditTextSearch.getText().toString());
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onColorClicked(ColorMeta color, int id) {
        colorName = color.label;
        pos = id;
        new ColorPickerDialog()
                .withColor(colorsAdapter.getColor(color.value))
                .withAlphaEnabled(true)
                .withTitle(this.getResources().getString(R.string.dialog_color_picker))
                .withTheme(ThemeWrapper.isLightTheme() ? R.style.ColorPickerDialog : R.style.ColorPickerDialog_Dark)
                .withPresets(new int[]{})
                .withListener(this)
                .show(getChildFragmentManager(), null);
    }

    @Override
    public void onColorPicked(@Nullable ColorPickerDialog pickerView, int color) {
        mViewModel.setNewColor(pos, String.format("#%08X", color), colorName);
        colorsAdapter.newValue(pos, String.format("#%08X", color), colorName);
        //mViewModel.setNewColor(color, colorName);
        //Toast.makeText(App.getContext(), String.format("#%08X", color), Toast.LENGTH_LONG).show();
    }
}
