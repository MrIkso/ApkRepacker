package com.mrikso.apkrepacker.ui.imageviewer;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.activity.CodeEditorActivity;
import com.mrikso.apkrepacker.utils.BundleUtils;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.mrikso.apkrepacker.utils.ViewUtils;
import com.sdsmdg.harjot.vectormaster.VectorMasterView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageViewerFragment extends Fragment {

    private static final String KEY_PREFIX = ImageViewerFragment.class.getName() + '.';

    private static final String STATE_PATHS = KEY_PREFIX + "PATHS";

    private List<String> mExtraPaths;
    private int mExtraPosition;
    private int switchState = 0;

    private int mToolbarAnimationDuration = 400;

    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private FloatingActionButton fabEdit;

    private ArrayList<String> mPaths;
//    private SystemUiHelper mSystemUiHelper;
    private ImageViewerAdapter mAdapter;

    @NonNull
    public static ImageViewerFragment newInstance(@NonNull List<String> paths, int position) {
        //noinspection deprecation
        ImageViewerFragment fragment = new ImageViewerFragment();
        fragment.mExtraPaths = paths;
        fragment.mExtraPosition = position;
        return fragment;
    }

    public ImageViewerFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_viewer, container, false);
        mToolbar = view.findViewById(R.id.toolbar);
        mViewPager = view.findViewById(R.id.view_pager);
        fabEdit = view.findViewById(R.id.fab_go_editor);
        fabEdit.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CodeEditorActivity.class);
            intent.putExtra("filePath", getCurrentPath());
            startActivity(intent);
        });
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState == null) {
            mPaths = new ArrayList<>(mExtraPaths);
        } else {
            //noinspection unchecked
            mPaths = (ArrayList<String>) (ArrayList<?>) BundleUtils.getParcelableArrayList(savedInstanceState, STATE_PATHS);
        }
        if (mPaths.isEmpty()) {
            // TODO: Show a toast.
            finish();
            return;
        }

        ((AppCompatActivity) requireActivity()).setSupportActionBar(mToolbar);

/*        mSystemUiHelper = new SystemUiHelper(requireActivity(), SystemUiHelper.LEVEL_IMMERSIVE, SystemUiHelper.FLAG_LAYOUT_IN_SCREEN_OLDER_DEVICES, visible -> {
            mToolbar.animate()
                    .alpha(visible ? 1 : 0)
                    .translationY(visible ? 0 : -mToolbar.getBottom())
                    .setDuration(mToolbarAnimationDuration)
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .start();

            Resources resources = requireContext().getResources();
            int orientation = resources.getConfiguration().orientation;
            boolean navBarExists = getResources().getBoolean(getResources().getIdentifier("config_showNavigationBar", "bool", "android"));

            if (!navBarExists) {
                if (isTablet(requireContext()) || orientation == Configuration.ORIENTATION_PORTRAIT) {
                    fabEdit.animate()
                            .translationY(visible ? -getNavBarHeight(getContext()) : 0)
                            .setDuration(mToolbarAnimationDuration)
                            .setInterpolator(new FastOutSlowInInterpolator())
                            .start();
                } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    fabEdit.animate()
                            .translationX(visible ? -getNavBarHeight(getContext()) : 0)
                            .setDuration(mToolbarAnimationDuration)
                            .setInterpolator(new FastOutSlowInInterpolator())
                            .start();
                }
                Toast.makeText(requireContext(), "NavBarHeight = " + getNavBarHeight(requireContext()), Toast.LENGTH_SHORT).show();
            }
        });
        // This will set up window flags.
        mSystemUiHelper.show();*/

        mAdapter = new ImageViewerAdapter(/*view -> mSystemUiHelper.toggle()*/);
        mAdapter.replace(mPaths);
        mViewPager.setAdapter(mAdapter);
        // ViewPager saves its position and will restore it later.
        mViewPager.setCurrentItem(mExtraPosition);
        mViewPager.setPageTransformer(true, ViewPagerTransformers.DEPTH);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                updateTitle();
            }
        });
        updateTitle();
    }

/*    private int getNavBarHeight(Context c) {
        int result = 0;
        boolean navBarExists = getResources().getBoolean(getResources().getIdentifier("config_showNavigationBar", "bool", "android"));

        if (!navBarExists) {
            //The device has a navigation bar
            Resources resources = c.getResources();

            int orientation = resources.getConfiguration().orientation;
            int resourceId;
            if (isTablet(c)) {
                resourceId = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_height_landscape", "dimen", "android");
            } else {
                resourceId = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_width", "dimen", "android");
            }

            if (resourceId > 0) {
                return resources.getDimensionPixelSize(resourceId);
            }
        }
        return result;
    }*/


/*    private boolean isTablet(Context c) {
        return (c.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }*/

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //noinspection unchecked
        outState.putParcelableArrayList(STATE_PATHS, (ArrayList<Parcelable>) (ArrayList<?>) mPaths);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem switch_item = menu.add(0, 0, 0, "Switch Background");
        switch_item.setIcon(R.drawable.theme_light_dark);
        switch_item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case 0:
                switchBackground();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void finish() {
        requireActivity().finish();
    }

    private void switchBackground() {
        if (switchState == 0) {
            switchState++;
            mViewPager.setBackgroundColor(Color.WHITE);
        } else if (switchState == 1) {
            switchState++;
            mViewPager.setBackgroundColor(Color.GRAY);
        } else if (switchState == 2) {
            switchState++;
            mViewPager.setBackgroundColor(Color.BLACK);
        } else if (switchState == 3) {
            switchState = 0;
            mViewPager.setBackgroundResource(R.drawable.alpha);
        }
    }

    private void updateTitle() {

        FileUtil.FileType fileType = FileUtil.FileType.getFileType(new File(getCurrentPath()));
        int width = -1;
        int height = -1;

        if (getCurrentPath().endsWith(".xml")) {
            VectorMasterView vectorMasterView = new VectorMasterView(getContext(), new File(getCurrentPath()));
            width = (int) vectorMasterView.vectorModel.getWidth();
            height = (int) vectorMasterView.vectorModel.getHeight();
            ViewUtils.fadeToVisibility(fabEdit, true);
        } else if (fileType.equals(FileUtil.FileType.IMAGE)) {
            Bitmap bitmap = BitmapFactory.decodeFile(getCurrentPath());
            width = bitmap.getWidth();
            height = bitmap.getHeight();
            ViewUtils.fadeToVisibility(fabEdit, false);
        } /*else if (fileType.equals(FileUtil.FileType.TTF)) {
            ViewUtils.fadeToVisibility(fabEdit, false);
        }*/

        File path = new File(getCurrentPath());
        int size = mPaths.size();
        requireActivity().setTitle(path.getName());
        mToolbar.setSubtitle(size > 1 ? getString(R.string.image_viewer_subtitle_format, mViewPager.getCurrentItem() + 1, size) + ((width != -1 && height != -1) ? " (" + width + "×" + height + ")" : "") : null);
    }

    @NonNull
    private String getCurrentPath() {
        return mPaths.get(mViewPager.getCurrentItem());
    }
}
