package com.mrikso.apkrepacker.fragment.dialogs.bottomsheet;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.adapter.ThemeAdapter;
import com.mrikso.apkrepacker.fragment.dialogs.base.BaseBottomSheetDialogFragment;
import com.mrikso.apkrepacker.utils.FragmentUtils;
import com.mrikso.apkrepacker.utils.Theme;

public class ThemeSelectionDialogFragment extends BaseBottomSheetDialogFragment implements ThemeAdapter.OnThemeInteractionListener {

    @IntDef(flag = true, value = {MODE_APPLY, MODE_CHOOSE})
    public @interface Mode {
    }

    public static final int MODE_APPLY = 0;
    public static final int MODE_CHOOSE = 1;

    private static final String EXTRA_MODE = "mode";

    private int mMode = MODE_APPLY;

    /**
     * Same as {@link #newInstance(int)} with MODE_APPLY
     */
    public static ThemeSelectionDialogFragment newInstance(Context context) {
        return newInstance(MODE_APPLY);
    }

    public static ThemeSelectionDialogFragment newInstance(@Mode int mode) {
        ThemeSelectionDialogFragment fragment = new ThemeSelectionDialogFragment();

        Bundle args = new Bundle();
        args.putInt(EXTRA_MODE, mode);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            mMode = args.getInt(EXTRA_MODE, MODE_APPLY);
        }

    }

    @Nullable
    @Override
    protected View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = new RecyclerView(requireContext());
        recyclerView.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        return recyclerView;
    }

    @Override
    protected void onContentViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onContentViewCreated(view, savedInstanceState);

        setTitle(R.string.dialog_select_theme);
        getPositiveButton().setVisibility(View.GONE);
        getNegativeButton().setOnClickListener(v -> dismiss());

        RecyclerView recycler = (RecyclerView) view;
        recycler.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        ThemeAdapter adapter = new ThemeAdapter(requireContext());
        adapter.setThemes(Theme.getInstance(requireContext()).getThemes());
        adapter.setOnThemeInteractionListener(this);
        recycler.setAdapter(adapter);

        revealBottomSheet();
    }

    @Override
    public void onThemeClicked(Theme.ThemeDescriptor theme) {
        switch (mMode) {
            case MODE_APPLY:
                Theme.getInstance(getContext()).setConcreteTheme(theme);
                dismiss();
                break;
            case MODE_CHOOSE:
                OnThemeChosenListener listener = FragmentUtils.getParentAs(this, OnThemeChosenListener.class);
                if (listener != null)
                    listener.onThemeChosen(getTag(), theme);

                dismiss();
                break;
            default:
                throw new IllegalStateException("Unknown mode");
        }
    }

    public interface OnThemeChosenListener {
        void onThemeChosen(@Nullable String tag, Theme.ThemeDescriptor theme);
    }
}

