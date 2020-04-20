package com.mrikso.apkrepacker.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;
import com.jaredrummler.android.colorpicker.ColorShape;
import com.mrikso.apkrepacker.App;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.activity.CodeEditorActivity;
import com.mrikso.apkrepacker.fragment.dialogs.AddColorDialogFragment;
import com.mrikso.apkrepacker.ui.colorslist.ColorMeta;
import com.mrikso.apkrepacker.ui.colorslist.ColorsAdapter;
import com.mrikso.apkrepacker.ui.colorslist.ColorsViewModel;
import com.mrikso.apkrepacker.utils.ThemeWrapper;

import java.io.File;
import java.util.Objects;

import me.zhanghai.android.fastscroll.FastScrollerBuilder;

public class ColorEditorFragment extends Fragment implements ColorsAdapter.OnItemInteractionListener, AddColorDialogFragment.ItemClickListener, ColorPickerDialogListener {

    public static final String TAG = "ColorEditorFragment";
    private RecyclerView colorsList;
    private ColorsViewModel mViewModel;
    private ColorsAdapter colorsAdapter;
    private EditText mEditTextSearch;
    private File colors;
    private String colorName;
    private Context mContext;
    private int pos;

    public ColorEditorFragment() {
        // Required empty public constructor
    }

    public static ColorEditorFragment newInstance(String colors) {
        ColorEditorFragment fragment = new ColorEditorFragment();
        Bundle args = new Bundle();
        args.putString("colorsFile", colors);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.colors = new File(Objects.requireNonNull(getArguments().getString("colorsFile")));
        }
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_color_editor, container, false);
        mContext = view.getContext();
        // Inflate the layout for this fragment
//        Context mContext = view.getContext();
        mViewModel = new ViewModelProvider(this).get(ColorsViewModel.class);
        //  loading = view.findViewById(R.id.app_packages_loading);
        mViewModel.setColorsFile(colors);
        colorsList = view.findViewById(R.id.colors);
        colorsList.setLayoutManager(new LinearLayoutManager(mContext));
        colorsList.getRecycledViewPool().setMaxRecycledViews(0, 24);
        colorsAdapter = new ColorsAdapter(mContext);
        // mViewModel.getPackages().observe(getViewLifecycleOwner(), appsAdapter::setData);

        FloatingActionMenu fabMenu = view.findViewById(R.id.fab);
        FloatingActionButton fabOpenInEditor = view.findViewById(R.id.fab_go_editor);
        FloatingActionButton fabAddColor = view.findViewById(R.id.fab_add_color);

        fabOpenInEditor.setOnClickListener(v -> {
            fabMenu.close(true);
            Intent intent = new Intent(getActivity(), CodeEditorActivity.class);
            intent.putExtra("filePath", colors.getAbsolutePath());
            startActivity(intent);
        });
        fabAddColor.setOnClickListener(v -> {
            fabMenu.close(true);
            AddColorDialogFragment fragment = AddColorDialogFragment.newInstance();
            fragment.show(getChildFragmentManager(), AddColorDialogFragment.TAG);
        });

        fabMenu.setClosedOnTouchOutside(true);
        colorsAdapter.setInteractionListener(this);
        new FastScrollerBuilder(colorsList).useMd2Style().build();

        initData();
        setupToolbar(view);
        return view;
    }

    private void initData() {
        mViewModel.getColors().observe(getViewLifecycleOwner(), colorsAdapter::setData);
        colorsList.setAdapter(colorsAdapter);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
    }

    private void setupToolbar(View view) {
        //Search
        mEditTextSearch = view.findViewById(R.id.et_search);
        view.findViewById(R.id.button_clear).setOnClickListener(v -> mEditTextSearch.setText(""));
        mEditTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                view.findViewById(R.id.button_clear).setVisibility(s.toString().isEmpty() ? View.GONE : View.VISIBLE);
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

        ColorPickerDialog dialog = ColorPickerDialog.newBuilder()
                .setDialogTitle(colorName)
                .setDialogType(ColorPickerDialog.TYPE_PRESETS)
                .setColorShape(ColorShape.CIRCLE)
                .setPresets(ColorPickerDialog.MATERIAL_COLORS)
                .setAllowPresets(true)
                .setAllowCustom(true)
                .setShowAlphaSlider(true)
                .setShowColorShades(true)
                .setColor(colorsAdapter.getColor(color.value))
                .setDialogWidth(0.95f)
                .setDialogTheme(ThemeWrapper.isLightTheme() ? R.style.ColorPickerDialog : R.style.ColorPickerDialog_Dark)
                .create();
        dialog.setColorPickerDialogListener(this);
        ((AppCompatActivity) Objects.requireNonNull(getContext())).getSupportFragmentManager().beginTransaction().add(dialog, "color_picker_dialog").commitAllowingStateLoss();
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        mViewModel.setNewColor(pos, String.format("#%08X", color), colorName);
        colorsAdapter.newValue(pos, String.format("#%08X", color), colorName);
    }

    @Override
    public void onDialogDismissed(int dialogId) {}

    @Override
    public void onAddColorClick(String colorName, int colorValue) {
        mViewModel.addNewColor(colorName, String.format("#%08X", colorValue));
        colorsAdapter.addNewColor(colorName, String.format("#%08X", colorValue));
    }
}