
package com.mrikso.apkrepacker.filepicker;

import android.app.Dialog;
import android.content.Context;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.mrikso.apkrepacker.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;



public class FilePickerDialog extends Dialog implements AdapterView.OnItemClickListener, AbsListView.OnScrollListener {

    public static final String DEFAULT_DIR = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final int EXTERNAL_READ_PERMISSION_GRANT = 0x1a;
    public static final int MODE_SINGLE = 0;
    public static final int MODE_MULTI = 1;
    public static final int TYPE_ALL = 0;
    public static final int TYPE_FILE = 1;
    public static final int TYPE_DIR = 2;

    private Context context;
    private ListView listView;
    private TextView tvDirPath, tvTitle;

    private ImageView imTitle;
    private Button btnCancel;
    private Button btnSelect;

    private ArrayList<FileListItem> internalList;
    private FileListAdapter mFileListAdapter;
    private ExtensionFilter filter;
    private FileDialogListener listener;

    private String titleStr, selectBtnText, cancelBtnText;

    private int selectMode;
    private int selectType;
    private File rootDir;
    private File primaryDir;
    private String[] extensions;

    private String curDirPath;
    boolean flag = false;
    private HashMap<String, Integer> positionMap;

    public FilePickerDialog(Context context) {
        super(context);
        this.context = context;
        internalList = new ArrayList<>();
        selectMode = MODE_SINGLE;
        selectType = TYPE_FILE;
        rootDir = new File(DEFAULT_DIR);
        primaryDir = new File(DEFAULT_DIR);
        extensions = new String[]{""};
        positionMap = new HashMap<>();
        this.setCancelable(true);
        this.setCanceledOnTouchOutside(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_main);

        listView = findViewById(R.id.lv_files);
        btnSelect = findViewById(R.id.btn_select);
        btnCancel = findViewById(R.id.btn_cancel);
        tvTitle = findViewById(R.id.tv_dialog_title);
        tvDirPath = findViewById(R.id.tv_dir_path);
        imTitle = findViewById(R.id.iv_title);
        filter = new ExtensionFilter(selectType, extensions);

        int size = MarkedItemList.getFileCount();
        if (size == 0) {
            btnSelect.setEnabled(false);
            /*int color = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                color = context.getResources().getColor(R.color.white, context.getTheme());
            }
            btnSelect.setTextColor(color);*/
        }

        if (cancelBtnText != null) {
            btnCancel.setText(cancelBtnText);
        }

        btnSelect.setOnClickListener(v -> {
            String[] paths = MarkedItemList.getSelectedPaths();
            if (listener != null) {
                listener.onSelectedFilePaths(paths);
            }
            dismiss();
        });
        btnCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCanceled();
            }
            dismiss();
        });
        this.setOnCancelListener(dialog -> {
            if (listener != null) {
                listener.onCanceled();
            }
        });

        imTitle.setOnClickListener(v -> {

            if(flag){
                imTitle.setImageResource(R.drawable.ic_phone_android);
                rootDir = new File(DEFAULT_DIR);
                setUp();
                flag = false;
            }
            else {
                imTitle.setImageResource(R.drawable.ic_sd_card);
                String sd = Utility.getExternalStoragePath(getContext(), true);
                if(sd !=null) {
                    rootDir = new File(sd);
                    setUp();
                    flag = true;
                }
                else {
                    Toast.makeText(context, R.string.toast_sd_card_not_found, Toast.LENGTH_LONG).show();
                }

            }
        });
        mFileListAdapter = new FileListAdapter(context, internalList, selectType, selectMode);
        mFileListAdapter.setFileItemSelectedListener(() -> {
            selectBtnText = selectBtnText == null ? context.getResources().getString(R.string.choose_button_label) : selectBtnText;
            int size1 = MarkedItemList.getFileCount();
           /* int color = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
               color = context.getResources().getColor(R.color.white, context.getTheme());
            }*/
            if (size1 == 0) {
                btnSelect.setEnabled(false);
               // btnSelect.setTextColor(color);
                btnSelect.setText(selectBtnText);
            } else {
                btnSelect.setEnabled(true);
                //btnSelect.setTextColor(color);
                btnSelect.setText(String.format(Locale.getDefault(), "%s (%d) ", selectBtnText, size1));
            }
        });
        listView.setAdapter(mFileListAdapter);
        setTitle();
    }

    private void setTitle() {
        if (!TextUtils.isEmpty(titleStr)) {
            tvTitle.setText(titleStr);
        } else {
            tvTitle.setText(R.string.label_path);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        selectBtnText = (selectBtnText == null ? context.getResources().getString(R.string.choose_button_label) : selectBtnText);
        btnSelect.setText(selectBtnText);
        if (Utility.checkStorageAccessPermissions(context)) {
            setUp();
        }
    }

    private void setUp(){
        File currLoc;
        internalList.clear();
        if (primaryDir.isDirectory() && validateOffsetPath()) {
            currLoc = new File(primaryDir.getAbsolutePath());
            internalList.add(new FileListItem(
                    context.getString(R.string.label_parent_dir),
                    currLoc.getParentFile().getAbsolutePath(),
                    currLoc.lastModified(),
                    true));
        } else if (rootDir.exists() && rootDir.isDirectory()) {
            currLoc = new File(rootDir.getAbsolutePath());
        } else {
            currLoc = new File(DEFAULT_DIR);
        }
        setCurDirPath(currLoc.getAbsolutePath());
        setTitle();
        internalList = Utility.prepareFileListEntries(internalList, currLoc, filter);
        mFileListAdapter.notifyDataSetChanged();
        listView.setSelection(0);
        listView.setOnItemClickListener(this);
        listView.setOnScrollListener(this);
    }
    private boolean validateOffsetPath() {
        String offset_path = primaryDir.getAbsolutePath();
        String root_path = rootDir.getAbsolutePath();
        return !offset_path.equals(root_path) && offset_path.contains(root_path);
    }

    private void setCurDirPath(String dirPath) {
        this.curDirPath = dirPath;
        tvDirPath.setText(curDirPath);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (internalList.size() > i) {
            FileListItem fItem = internalList.get(i);
            if (fItem.isDirectory()) {
                if (new File(fItem.getPath()).canRead()) {
                    File currLoc = new File(fItem.getPath());
                    Integer position = 0;

                    if (i == 0) {
                        positionMap.remove(curDirPath);
                        position = positionMap.get(currLoc.getAbsolutePath());
                        if (position == null || position < 0) {
                            position = 0;
                        }
                    }
                    String tempCurDirPath = curDirPath;

                    setCurDirPath(currLoc.getAbsolutePath());
                    internalList.clear();
                    if (!currLoc.getName().equals(rootDir.getName())) {
                        internalList.add(new FileListItem(
                                context.getString(R.string.label_parent_dir),
                                currLoc.getParentFile().getAbsolutePath(),
                                currLoc.lastModified(),
                                true));
                    }
                    internalList = Utility.prepareFileListEntries(internalList, currLoc, filter);
                    mFileListAdapter.notifyDataSetChanged();

                    if (position == 0) {
                        for (int j = 0; j < internalList.size(); j++) {
                            FileListItem item = internalList.get(j);
                            if (item.getPath().equals(tempCurDirPath)) {
                                position = j;
                            }
                        }
                    }

                    listView.setSelection(position);
                } else {
                    Toast.makeText(context, R.string.error_dir_access, Toast.LENGTH_SHORT).show();
                }
            } else {
                if (selectType == TYPE_DIR) {
                    Toast.makeText(context, R.string.toast_error_not_selectable, Toast.LENGTH_SHORT).show();
                } else {
                    view.findViewById(R.id.cb_file_mark).performClick();
                }
            }
        }
    }


    public FilePickerDialog setDialogListener(CharSequence selectBtnText, CharSequence cancelBtnText, FileDialogListener listener) {
        if (selectBtnText != null) {
            this.selectBtnText = selectBtnText.toString();
        } else {
            this.selectBtnText = null;
        }
        if (cancelBtnText != null) {
            this.cancelBtnText = cancelBtnText.toString();
        } else {
            this.cancelBtnText = null;
        }
        this.listener = listener;
        return this;
    }

    public FilePickerDialog setTitleText(CharSequence titleStr) {
        if (titleStr != null) {
            this.titleStr = titleStr.toString();
        } else {
            this.titleStr = null;
        }
        if (tvTitle != null) {
            setTitle();
        }
        return this;
    }

    public FilePickerDialog setSelectMode(int selectMode) {
        this.selectMode = selectMode;
        return this;
    }

    public FilePickerDialog setSelectType(int selectType) {
        this.selectType = selectType;
        return this;
    }

    public FilePickerDialog setRootDir(String rootDir) {
        this.rootDir = new File(rootDir);
        return this;
    }

    public FilePickerDialog setPrimaryDir(String primaryDir) {
        this.primaryDir = new File(primaryDir);
        return this;
    }

    public FilePickerDialog setExtensions(String[] extensions) {
        this.extensions = extensions;
        return this;
    }

    public FilePickerDialog setBackCancelable(boolean cancelable) {
        this.setCancelable(cancelable);
        return this;
    }

    public FilePickerDialog setOutsideCancelable(boolean cancelable) {
        this.setCanceledOnTouchOutside(cancelable);
        return this;
    }

    @Override
    public void show() {
        if (Utility.checkStorageAccessPermissions(context)) {
            super.show();
            selectBtnText = selectBtnText == null ? context.getResources().getString(R.string.choose_button_label) : selectBtnText;
            btnSelect.setText(selectBtnText);
            int size = MarkedItemList.getFileCount();
            if (size == 0) {
                btnSelect.setText(selectBtnText);
            } else {
                String button_label = selectBtnText + " (" + size + ") ";
                btnSelect.setText(button_label);
            }
        }
    }

    @Override
    public void onBackPressed() {
        //currentDirName is dependent on dirName
        String currentDirName = new File(curDirPath).getName();
        if (internalList.size() > 0) {
            FileListItem fItem = internalList.get(0);
            File currLoc = new File(fItem.getPath());
            if (currentDirName.equals(rootDir.getName()) || !currLoc.canRead()) {
                super.onBackPressed();
            } else {
                positionMap.remove(curDirPath);
                Integer position = positionMap.get(currLoc.getAbsolutePath());
                if (position == null || position < 0) {
                    position = 0;
                }

                setCurDirPath(currLoc.getAbsolutePath());

                internalList.clear();
                if (!currLoc.getName().equals(rootDir.getName())) {
                    internalList.add(new FileListItem(
                            context.getString(R.string.label_parent_dir),
                            currLoc.getParentFile().getAbsolutePath(),
                            currLoc.lastModified(),
                            true));
                }
                internalList = Utility.prepareFileListEntries(internalList, currLoc, filter);
                mFileListAdapter.notifyDataSetChanged();

                if (position == 0) {
                    for (int i = 0; i < internalList.size(); i++) {
                        FileListItem item = internalList.get(i);
                        if (item.getName().equals(currentDirName)) {
                            position = i;
                        }
                    }
                }
                listView.setSelection(position);
            }
            setTitle();
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public void dismiss() {
        MarkedItemList.clearSelectionList();
        internalList.clear();
        super.dismiss();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        Integer position = positionMap.get(curDirPath);
        if (position == null || position != firstVisibleItem) {
            positionMap.put(curDirPath, firstVisibleItem);
        }
    }

    public interface FileDialogListener {

        void onSelectedFilePaths(String[] filePaths);

        void onCanceled();
    }
}
