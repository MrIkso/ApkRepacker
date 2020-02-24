package com.mrikso.apkrepacker.fragment;

import android.content.Context;
import android.content.Intent;
import android.core.text.TextUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.jecelyin.common.utils.DLog;
import com.jecelyin.common.utils.StringUtils;
import com.jecelyin.common.utils.UIUtils;
import com.jecelyin.editor.v2.ui.activities.MainActivity;
import com.mrikso.apkrepacker.App;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.filepicker.FilePickerDialog;
import com.mrikso.apkrepacker.fragment.dialogs.FileOptionsDialogFragment;
import com.mrikso.apkrepacker.ui.filelist.FileAdapter;
import com.mrikso.apkrepacker.ui.filelist.PathButtonAdapter;
import com.mrikso.apkrepacker.utils.FileUtil;

import java.io.File;
import java.lang.reflect.Method;
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
    private FloatingActionButton addFolder, addFile, searchFab;
    private FloatingActionMenu actionMenu;
    private File selecedFile;

    public FilesFragment() {
        // Required empty public constructor
    }

    private static String downDir(int levels, String oldPath) {
        // String oldPath = System.getProperty("user.dir");
        String[] splitedPathArray = oldPath.split("/");
        levels = splitedPathArray.length - levels;
        List<String> splitedPathList = Arrays.asList(splitedPathArray);
        splitedPathList = splitedPathList.subList(0, levels);
        String newPath = null;
       // if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
       //     newPath = String.join("/", splitedPathList);
       // }
       // else {
            newPath = TextUtils.join("/", splitedPathList);
        //}
        return newPath;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // DataBindingUtil binding = DataBindingUtil.inflate(inflater, R.layout.file_explorer_fragment, container, false);
        View result = inflater.inflate(R.layout.fragment_files, container, false);
        mContext = result.getContext();
        initViews(result);
        adapter = new FileAdapter(App.getContext());
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
            Toast.makeText(App.getContext(), "Directory doesn't exist", Toast.LENGTH_SHORT).show();
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
        });
        patchRecyclerView.setAdapter(pathAdapter);
        adapter.setOnItemClickListener(new OnItemClickListener(App.getContext()));
        adapter.setItemLayout(R.layout.item_project_file);
        adapter.setSpanCount(getResources().getInteger(R.integer.span_count0));

        if (recyclerView != null) {
            //  fastScroller.attachRecyclerView(recyclerView);
            new FastScrollerBuilder(recyclerView).build();
            recyclerView.setAdapter(adapter);
        }
    }

    private void initViews(View view) {
        actionMenu = view.findViewById(R.id.fab_menu);
        addFile = view.findViewById(R.id.fab_add_file);
        addFolder = view.findViewById(R.id.fab_add_folder);
        searchFab = view.findViewById(R.id.fab_search);
        RelativeLayout layout = view.findViewById(R.id.home_folder_app);
        layout.setOnClickListener(v -> setPath(new File(projectPatch)));
        searchFab.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("curDirect", currentDirectory.getAbsolutePath());
            actionMenu.close(false);
            SearchFragment searchFragment = new SearchFragment();
            searchFragment.setArguments(bundle);
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, searchFragment, SearchFragment.TAG)
                    .addToBackStack(null)
                    .commit();
        });
        addFolder.setOnClickListener(v -> {
            actionMenu.close(true);
            new FilePickerDialog(view.getContext())
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
            new FilePickerDialog(view.getContext())
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
            case R.id.rename_file:
                UIUtils.showInputDialog(mContext, R.string.action_rename, 0, selecedFile.getName(), EditorInfo.TYPE_CLASS_TEXT, new UIUtils.OnShowInputCallback() {
                    @Override
                    public void onConfirm(CharSequence input) {
                        try {
                            int index = adapter.indexOf(selecedFile);
                            File newFile = FileUtil.renameFile(selecedFile, input.toString());
                            // FileUtil.createDirectory(new File(currentDirectory.getAbsolutePath()), input.toString());
                            adapter.updateItemAt(index, newFile);
                        } catch (Exception e) {
                            UIUtils.toast(App.getContext(), R.string.toast_error_on_rename);
                            DLog.e(e);
                        }
                    }
                });
                break;
            case R.id.add_new_folder:
                UIUtils.showInputDialog(mContext, R.string.action_create_new_folder, 0, null, EditorInfo.TYPE_CLASS_TEXT,
                        new UIUtils.OnShowInputCallback() {
                    @Override
                    public void onConfirm(CharSequence input) {
                        try {
                            FileUtil.createDirectory(new File(currentDirectory.getAbsolutePath()), input.toString());
                            setPath(new File(currentDirectory.getAbsolutePath()));
                        } catch (Exception e) {
                            UIUtils.toast(App.getContext(), R.string.toast_error_on_add_folder);
                            DLog.e(e);
                        }
                    }
                });
                break;
            case R.id.delete_file:
                try {
                    FileUtil.deleteFile(selecedFile);
                    setPath(new File(currentDirectory.getAbsolutePath()));
                    if(selecedFile.isDirectory()){
                        Toast.makeText(App.getContext(), String.format(getString(R.string.toast_deleted_dictionary),
                                selecedFile.getName()), Toast.LENGTH_LONG).show();
                    }else {
                        Toast.makeText(App.getContext(), String.format(getString(R.string.toast_deleted_item ),
                                selecedFile.getName()), Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    UIUtils.toast(App.getContext(), R.string.toast_error_on_delete_file);
                    DLog.e(e);
                }
                break;
        }
    }

    @Override
    public boolean onBackPressed() {
        String currentDirName = currentDirectory.getName();
        File f = new File(projectPatch);
        Fragment manager1 = getChildFragmentManager().findFragmentByTag(SearchFragment.TAG);
        Fragment manager2 = getChildFragmentManager().findFragmentByTag(ColorEditorFragment.TAG);

        if (manager1 != null) {
            getChildFragmentManager().popBackStack();
            return true;
        } else if (manager2 != null) {
            getChildFragmentManager().popBackStack();
            return true;
        } else if (currentDirName.equals(f.getName())) {
             Objects.requireNonNull(getActivity()).finish();
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
            final File file = adapter.get(position);
            if (adapter.anySelected()) {
                adapter.toggle(position);
                return;
            }
            if (file.isDirectory()) {
                if (file.canRead()) {
                    setPath(file);
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
                        if (file.getName().equals("colors.xml")) {
                            ColorEditorFragment colorEditorFragment = new ColorEditorFragment(file);
                            getChildFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, colorEditorFragment, ColorEditorFragment.TAG)
                                    .addToBackStack(null)
                                    .commit();
                        } else {
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            intent.putExtra("filePath", file.getAbsolutePath());
                            startActivity(intent);
                        }
                        break;
                    default:
                        try {
                            if (Build.VERSION.SDK_INT >= 24) {
                                try {
                                    Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                                    m.invoke(null);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            Intent adctionView = new Intent(Intent.ACTION_VIEW);
                            adctionView.setData(Uri.fromFile(file));
                            adctionView.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                            startActivity(adctionView);
                        } catch (Exception e) {
                            UIUtils.toast(App.getContext(), R.string.cannt_open_file);
                            Log.d("OPENERROR", e.toString());
                            // showMessage(String.format("Cannot open %s", getName(file)));
                        }
                }
                /*
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.putExtra("filePath", file.getAbsolutePath());
                startActivity(intent);
                */
            }
        }

        @Override
        public boolean onItemLongClick(int position) {
            selecedFile = adapter.get(position);
            FileOptionsDialogFragment fragment = FileOptionsDialogFragment.newInstance();
            /// FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            // ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            // ft.add(android.R.id.content,fragment ).addToBackStack(null).commitAllowingStateLoss();
            //   ft.commitAllowingStateLoss();
            fragment.show(getChildFragmentManager(), FileOptionsDialogFragment.TAG);
            // fragment.show(ft, FileOptionsDialogFragment.TAG );
            //FileOptionsDialogFragment fragment = FileOptionsDialogFragment.newInstance();
            //fragment.commit(((FragmentActivity)context).getSupportFragmentManager(), FileOptionsDialogFragment.TAG);
            return true;
        }
    }
}
