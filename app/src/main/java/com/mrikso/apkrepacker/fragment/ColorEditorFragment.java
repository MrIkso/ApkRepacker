package com.mrikso.apkrepacker.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.activity.CodeEditorActivity;
import com.mrikso.apkrepacker.fragment.dialogs.bottomsheet.ColorOptionsDialogFragment;
import com.mrikso.apkrepacker.ui.colorslist.ColorMeta;
import com.mrikso.apkrepacker.ui.colorslist.ColorsAdapter;
import com.mrikso.apkrepacker.ui.colorslist.ColorsViewModel;
import com.mrikso.apkrepacker.utils.ScrollingViewOnApplyWindowInsetsListener;

import java.io.File;
import java.util.Objects;

import me.zhanghai.android.fastscroll.FastScroller;
import me.zhanghai.android.fastscroll.FastScrollerBuilder;

public class ColorEditorFragment extends Fragment implements ColorsAdapter.OnItemInteractionListener, ColorOptionsDialogFragment.ItemClickListener {

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
            this.colors = new File(Objects.requireNonNull(requireArguments().getString("colorsFile")));
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
            ColorOptionsDialogFragment.newInstance(null, Color.BLACK, false).show(getChildFragmentManager(), ColorOptionsDialogFragment.TAG);
        });

        fabMenu.setClosedOnTouchOutside(true);
        colorsAdapter.setInteractionListener(this);
        FastScroller fastScroller = new FastScrollerBuilder(colorsList).useMd2Style().build();
        colorsList.setOnApplyWindowInsetsListener(new ScrollingViewOnApplyWindowInsetsListener(colorsList, fastScroller));


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
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

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

        ColorOptionsDialogFragment.newInstance(colorName, colorsAdapter.getColor(color.value), true).show(getChildFragmentManager(), ColorOptionsDialogFragment.TAG);
    }

    @Override
    public void onColorClick(View view, String colorName, int colorValue, boolean isChange) {
        if (view.getId() == R.id.delete_color) {
            mViewModel.deleteColor(pos);
            colorsAdapter.deleteColor(pos);
        } else if (isChange) {
            mViewModel.setNewColor(pos, colorName, String.format("#%08X", colorValue));
            colorsAdapter.setNewValue(pos, colorName, String.format("#%08X", colorValue));
        } else {
            mViewModel.addNewColor(colorName, String.format("#%08X", colorValue));
            colorsAdapter.addNewColor(colorName, String.format("#%08X", colorValue));
            colorsList.smoothScrollToPosition(colorsAdapter.getItemCount());
        }
    }
}