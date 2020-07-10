package com.mrikso.apkrepacker.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.jecelyin.common.utils.DLog;
import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.App;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.activity.CodeEditorActivity;
import com.mrikso.apkrepacker.filepicker.FilePickerDialog;
import com.mrikso.apkrepacker.fragment.dialogs.FileOptionsDialogFragment;
import com.mrikso.apkrepacker.ui.filelist.FileAdapter;
import com.mrikso.apkrepacker.ui.filelist.PathButtonAdapter;
import com.mrikso.apkrepacker.ui.imageviewer.ImageViewerActivity;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.mrikso.apkrepacker.utils.FragmentUtils;
import com.mrikso.apkrepacker.utils.IntegerArray;
import com.mrikso.apkrepacker.utils.IntentUtils;
import com.mrikso.apkrepacker.utils.StringUtils;
import com.sdsmdg.harjot.vectormaster.VectorMasterDrawable;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import me.zhanghai.android.fastscroll.FastScrollerBuilder;

public class FilesFragment extends Fragment implements FileOptionsDialogFragment.FileItemClickListener, OnBackPressedListener {
    private File currentDirectory;
    private Context mContext;
    private FileAdapter adapter;
    private RecyclerView recyclerView, patchRecyclerView;
    private String projectPatch;
    private PathButtonAdapter pathAdapter;
    private FloatingActionMenu actionMenu;
    private File selectedFile;

    private IntegerArray integerArray = new IntegerArray();
    private int lastFirstVisiblePosition = 0;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public FilesFragment() {
        // Required empty public constructor
    }

    private static String downDir(int levels, String oldPath) {
        String[] splitterPathArray = oldPath.split("/");
        levels = splitterPathArray.length - levels;
        List<String> splitedPathList = Arrays.asList(splitterPathArray);
        splitedPathList = splitedPathList.subList(0, levels);
        return TextUtils.join("/", splitedPathList);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            projectPatch = bundle.getString("prjPatch");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // DataBindingUtil binding = DataBindingUtil.inflate(inflater, R.layout.file_explorer_fragment, container, false);
        View result = inflater.inflate(R.layout.fragment_files, container, false);
        mContext = result.getContext();
        initViews(result);
        adapter = new FileAdapter(mContext);
        recyclerView = result.findViewById(R.id.resource_list);
        //fastScroller = result.findViewById(R.id.fast_scroll_files);
        patchRecyclerView = result.findViewById(R.id.pathScrollView);
        //  binding.pathScrollView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        pathAdapter = new PathButtonAdapter();
        // binding.pathScrollView.setAdapter(pathAdapter);
        initRecyclerView();
        setPath(new File(projectPatch));
        return result;
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    private void setPath(File directory) {
        if (!directory.exists()) {
            UIUtils.toast(App.getContext(), R.string.toast_error_directory_not_exits);
            return;
        }
        pathAdapter.setPath(directory);
        currentDirectory = directory;
        if (adapter != null) {
            if (adapter.anySelected()) {
                adapter.clear();
                adapter.clearSelection();
                adapter.addAll(FileUtil.getChildren(directory));
            } else {
                adapter.clear();
                adapter.addAll(FileUtil.getChildren(directory));
            }
        }

        //invalidateTitle();

    }

    private void savePosition(boolean save) {
        if (save) {
            integerArray.add(((LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager())).findFirstCompletelyVisibleItemPosition());
        } else {
            lastFirstVisiblePosition = integerArray.getSize();
            lastFirstVisiblePosition--;
            if (lastFirstVisiblePosition >= 0) {
                ((LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager())).scrollToPositionWithOffset(integerArray.get(lastFirstVisiblePosition), 0);
            } else {
                lastFirstVisiblePosition = 0;
                integerArray.clear();
            }
        }
    }

    private void initRecyclerView() {
        patchRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        pathAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                patchRecyclerView.scrollToPosition(pathAdapter.getItemCount() - 1);
            }
        });
        pathAdapter.setPath(new File(projectPatch));
        pathAdapter.setOnItemClickListener((position, view) -> {
            File file = pathAdapter.getItem(position);
            setPath(file);
            if (Objects.equals(file.getParent(), currentDirectory.getParent())) {
                savePosition(false);
            }
        });
        patchRecyclerView.setAdapter(pathAdapter);
        adapter.setOnItemClickListener(new OnItemClickListener(getContext()));
        adapter.setItemLayout(R.layout.item_project_file);
        adapter.setSpanCount(getResources().getInteger(R.integer.span_count0));

        if (recyclerView != null) {
            recyclerView.setHasFixedSize(true);
//            recyclerView.buildDrawingCache(true);
//            recyclerView.setDrawingCacheEnabled(true);
//            recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
//            recyclerView.getRecycledViewPool().setMaxRecycledViews(0, 24);
            new FastScrollerBuilder(recyclerView).useMd2Style().build();
            recyclerView.setAdapter(adapter);
        }
    }

    private void initViews(View view) {
        actionMenu = view.findViewById(R.id.fab_menu);
        FloatingActionButton addFile = view.findViewById(R.id.fab_add_file);
        FloatingActionButton addFolder = view.findViewById(R.id.fab_add_folder);
        FloatingActionButton searchFab = view.findViewById(R.id.fab_search);
        AppCompatImageButton home_folder = view.findViewById(R.id.home_folder_app);
        home_folder.setOnClickListener(v -> setPath(new File(projectPatch)));
        searchFab.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("curDirect", currentDirectory.getAbsolutePath());
            actionMenu.close(true);
            SearchFragment searchFragment = new SearchFragment();
            searchFragment.setArguments(bundle);

            FragmentUtils.replace(searchFragment, getChildFragmentManager(), R.id.fragment_container, SearchFragment.TAG);
        });
        addFolder.setOnClickListener(v -> {
            actionMenu.close(true);
            new FilePickerDialog(getContext())
                    .setTitleText(this.getResources().getString(R.string.select_directory))
                    .setSelectMode(FilePickerDialog.MODE_SINGLE)
                    .setSelectType(FilePickerDialog.TYPE_DIR)
                    .setRootDir(Environment.getExternalStorageDirectory().getAbsolutePath())
                    .setBackCancelable(true)
                    .setOutsideCancelable(true)
                    .setDialogListener(this.getResources().getString(R.string.choose_button_label), this.getResources().getString(R.string.cancel_button_label), new FilePickerDialog.FileDialogListener() {
                        @Override
                        public void onSelectedFilePaths(String[] filePaths) {
                            for (String dir : filePaths) {
                                try {
                                    FileUtil.copyFile(new File(dir), new File(currentDirectory.getAbsolutePath()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            setPath(new File(currentDirectory.getAbsolutePath()));
                        }

                        @Override
                        public void onCanceled() {
                        }
                    })
                    .show();

        });
        addFile.setOnClickListener(V -> {
            actionMenu.close(true);
            new FilePickerDialog(getContext())
                    .setTitleText(this.getResources().getString(R.string.select_file))
                    .setSelectMode(FilePickerDialog.MODE_MULTI)
                    .setSelectType(FilePickerDialog.TYPE_FILE)
                    .setRootDir(Environment.getExternalStorageDirectory().getAbsolutePath())
                    .setBackCancelable(true)
                    .setOutsideCancelable(true)
                    .setDialogListener(this.getResources().getString(R.string.choose_button_label), this.getResources().getString(R.string.cancel_button_label), new FilePickerDialog.FileDialogListener() {
                        @Override
                        public void onSelectedFilePaths(String[] filePaths) {
                            for (String dir : filePaths) {
                                try {
                                    FileUtil.copyFile(new File(dir), new File(currentDirectory.getAbsolutePath()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            setPath(new File(currentDirectory.getAbsolutePath()));
                        }

                        @Override
                        public void onCanceled() {
                        }
                    })
                    .show();

        });

        actionMenu.setClosedOnTouchOutside(true);
    }

    @Override
    public void onResume() {
        if (adapter != null) adapter.refresh();
        super.onResume();
    }

    /*@Override
    public void onBackPressed() {
        //currentDirName is dependent on dirName
        String currentDirName = currentDirectory.getName();
        File f = new File(projectPatch);
        //  File currLoc = new File(currentDirName);
        if (currentDirName.equals(f.getName())) {
            getActivity().onBackPressed();
            //  getActivity().getSupportFragmentManager().popBackStack();
        } else {
            setPath(new File(downDir(1, currentDirectory.getAbsolutePath())));
        }
    }*/

    @Override
    public void onFileItemClick(Integer item) {
        switch (item) {
            case R.id.open_with:
                startActivity(IntentUtils.openFileWithIntent(new File(selectedFile.getAbsolutePath())));
                break;
            case R.id.open_in_editor:
                Intent intent = new Intent(getActivity(), CodeEditorActivity.class);
                intent.putExtra("filePath", selectedFile.getAbsolutePath());
                startActivity(intent);
                break;
            case R.id.rename_file:
                UIUtils.showInputDialog(requireContext(), R.string.action_rename, 0, selectedFile.getName(), EditorInfo.TYPE_CLASS_TEXT, new UIUtils.OnShowInputCallback() {
                    @Override
                    public void onConfirm(CharSequence input) {
                        try {
                            int index = adapter.indexOf(selectedFile);
                            File newFile = FileUtil.renameFile(selectedFile, input.toString());
                            // FileUtil.createDirectory(new File(currentDirectory.getAbsolutePath()), input.toString());
                            adapter.updateItemAt(index, newFile);
                        } catch (Exception e) {
                            UIUtils.toast(requireContext(), R.string.toast_error_on_rename);
                            DLog.e(e);
                        }
                    }
                });
                break;
            case R.id.add_new_folder:
                UIUtils.showInputDialog(requireContext(), R.string.action_create_new_folder, 0, null, EditorInfo.TYPE_CLASS_TEXT,
                        new UIUtils.OnShowInputCallback() {
                            @Override
                            public void onConfirm(CharSequence input) {
                                try {
                                    FileUtil.createDirectory(new File(currentDirectory.getAbsolutePath()), input.toString());
                                    setPath(new File(currentDirectory.getAbsolutePath()));
                                } catch (Exception e) {
                                    UIUtils.toast(requireContext(), R.string.toast_error_on_add_folder);
                                    DLog.e(e);
                                }
                            }
                        });
                break;
            case R.id.delete_file:
                try {
                    FileUtil.deleteFile(selectedFile);
                    adapter.removeItemAt(selectedPosition);
                    if (selectedFile.isDirectory()) {
                        UIUtils.toast(requireContext(), String.format(getString(R.string.toast_deleted_dictionary), selectedFile.getName()));
                    } else {
                        UIUtils.toast(requireContext(), String.format(getString(R.string.toast_deleted_item), selectedFile.getName()));
                    }
                } catch (Exception e) {
                    UIUtils.toast(requireContext(), R.string.toast_error_on_delete_file);
                    DLog.e(e);
                }
                break;
        }
    }

    @Override
    public boolean onBackPressed() {
        savePosition(false);

        String currentDirName = currentDirectory.getName();
        File f = new File(projectPatch);
        Fragment manager1 = getChildFragmentManager().findFragmentByTag(SearchFragment.TAG);
        Fragment manager2 = getChildFragmentManager().findFragmentByTag(ColorEditorFragment.TAG);
//        Fragment manager3 = getChildFragmentManager().findFragmentByTag(DimensEditorFragment.TAG);

        if (manager1 != null) {
            getChildFragmentManager().popBackStack();
            return true;
        } else if (manager2 != null) {
            getChildFragmentManager().popBackStack();
            return true;
/*        } else if (manager3 != null) {
            getChildFragmentManager().popBackStack();
            return true;*/
        } else if (currentDirName.equals(f.getName())) {
            requireActivity().finish();
            //getFragmentManager().popBackStack();
            //getActivity().getFragmentManager().beginTransaction().remove(me).commit();
            // getActivity().getFragmentManager().popBackStack();
            //   getActivity().getFragmentManager().beginTransaction().remove(this).commit();
            //  getActivity().getFragmentManager().popBackStack();
            return true;
        } else {
            setPath(new File(downDir(1, currentDirectory.getAbsolutePath())));
            return true;
        }
    }

    private final class OnItemClickListener implements com.mrikso.apkrepacker.recycler.OnItemClickListener {

        private final Context context;

        private OnItemClickListener(Context context) {
            this.context = context;
        }

        @Override
        public void onItemClick(int position) {
            File file = null;
            if (adapter != null && position >= 0) {
                file = adapter.get(position);
                if (adapter.anySelected()) {
                    adapter.toggle(position);
                    return;
                }
            }

            if (file != null) {
                if (file.isDirectory()) {
                    if (file.canRead()) {
                        setPath(file);
                        savePosition(true);
                    } else {
                        UIUtils.toast(context, R.string.cannt_open_directory);
                    }
                } else {
                    switch (FileUtil.FileType.getFileType(file)) {
                        case TXT:
                        case SMALI:
                        case JS:
                        case JSON:
                        case HTM:
                        case HTML:
                        case INI:
                        case XML:
                            if (file.getName().startsWith("colors")) {
                                Fragment colorEditorFragment = ColorEditorFragment.newInstance(file.getAbsolutePath());
                                getChildFragmentManager().beginTransaction().replace(R.id.fragment_container, colorEditorFragment, ColorEditorFragment.TAG).addToBackStack(null).commit();
/*                        } else if (file.getName().equals("dimens.xml")) {
                            Fragment dimensEditorFragment = DimensEditorFragment.newInstance(file.getAbsolutePath());
                            getChildFragmentManager().beginTransaction().replace(R.id.fragment_container, dimensEditorFragment, DimensEditorFragment.TAG).addToBackStack(null).commit();*/
                            } else if ((currentDirectory.getName().startsWith("drawable") || currentDirectory.getName().startsWith("mipmap")) && new VectorMasterDrawable(context, file).isVector()) {
                                ImageViewerActivity.setViewerData(getContext(), adapter, file);
                                startActivity(new Intent(getActivity(), ImageViewerActivity.class));
                            } else {
                                Intent intent = new Intent(getActivity(), CodeEditorActivity.class);
                                intent.putExtra("filePath", file.getAbsolutePath());
                                intent.putExtra("currentDirectory", currentDirectory);
                                startActivity(intent);
                            }
                            break;
                        case IMAGE:
//                    case TTF:
                            ImageViewerActivity.setViewerData(getContext(), adapter, file);
                            startActivity(new Intent(getActivity(), ImageViewerActivity.class));
                            break;
                        default:
                            startActivity(IntentUtils.openFileWithIntent(file));
                            break;
                    }
                }
            }
        }

        @Override
        public boolean onItemLongClick(int position) {
            selectedFile = adapter.get(position);
            selectedPosition = position;
            FileOptionsDialogFragment fragment = FileOptionsDialogFragment.newInstance(new VectorMasterDrawable(context, selectedFile).isVector() != selectedFile.getName().equals("colors.xml"), !selectedFile.isDirectory());
            fragment.show(getChildFragmentManager(), FileOptionsDialogFragment.TAG);
            return true;
        }
    }
}
