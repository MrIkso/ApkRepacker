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
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.activity.CodeEditorActivity;
import com.mrikso.apkrepacker.ui.dimenslist.DimensAdapter;
import com.mrikso.apkrepacker.ui.dimenslist.DimensMeta;
import com.mrikso.apkrepacker.ui.dimenslist.DimensViewModel;

import java.io.File;

import me.zhanghai.android.fastscroll.FastScrollerBuilder;

public class DimensEditorFragment extends Fragment implements DimensAdapter.OnItemInteractionListener {

    public static final String TAG = "DimensEditorFragment";
    private RecyclerView dimensList;
    private DimensViewModel mViewModel;
    private DimensAdapter dimensAdapter;
    private EditText mEditTextSearch;
    private File dimens;
    private String dimensName;
    private int pos;

    public DimensEditorFragment() {
        // Required empty public constructor
    }

    public static DimensEditorFragment newInstance(String dimens) {
        DimensEditorFragment fragment = new DimensEditorFragment();
        Bundle args = new Bundle();
        args.putString("dimensFile", dimens);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.dimens = new File(requireArguments().getString("dimensFile"));
        }
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dimen_editor, container, false);
        // Inflate the layout for this fragment
        Context mContext = view.getContext();
        mViewModel = new ViewModelProvider(this).get(DimensViewModel.class);
        //  loading = view.findViewById(R.id.app_packages_loading);
        mViewModel.setDimensFile(dimens);
        dimensList = view.findViewById(R.id.dimens);
        dimensList.setLayoutManager(new LinearLayoutManager(mContext));
        dimensList.getRecycledViewPool().setMaxRecycledViews(0, 24);
        dimensAdapter = new DimensAdapter(mContext);
        // mViewModel.getPackages().observe(getViewLifecycleOwner(), appsAdapter::setData);

        dimensAdapter.setInteractionListener(this);
        new FastScrollerBuilder(dimensList).useMd2Style().build();

        FloatingActionButton fabEdit = view.findViewById(R.id.fab_go_editor);
        fabEdit.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CodeEditorActivity.class);
            intent.putExtra("filePath", dimens.getAbsolutePath());
            startActivity(intent);
        });
        initData();
        setupToolbar(view);
        return view;
    }

    private void initData() {
        mViewModel.getDimens().observe(getViewLifecycleOwner(), dimensAdapter::setData);
        dimensList.setAdapter(dimensAdapter);
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
    }

    private void setupToolbar(View view) {
        //Search
        mEditTextSearch = view.findViewById(R.id.dimens_search);
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
    public void onDimensClicked(DimensMeta dimensMeta, int id) {
        dimensName = dimensMeta.label;
        pos = id;

        UIUtils.showInputDialog(getContext(), "Title", "Hint", "Value", 0,  new UIUtils.OnShowInputCallback() {
            @Override
            public void onConfirm(CharSequence input) {
                Toast.makeText(getContext(), "Ok", Toast.LENGTH_SHORT).show();
            }
        });
    }

  /*  @Override
    public void onColorSelected(int dialogId, int color) {
        mViewModel.setNewDimens(pos, String.format("#%08X", color), dimensName);
        dimensAdapter.newValue(pos, String.format("#%08X", color), dimensName);
        //mViewModel.setNewColor(color, colorName);
        //Toast.makeText(App.getContext(), String.format("#%08X", color), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDialogDismissed(int dialogId) {}*/
}
