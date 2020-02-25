package com.mrikso.apkrepacker.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.fragment.DecompileFragment;
import com.mrikso.apkrepacker.fragment.SimpleEditorFragment;
import com.mrikso.apkrepacker.fragment.dialogs.ApkOptionsDialogFragment;
import com.mrikso.apkrepacker.fragment.dialogs.ProgressDialogFragment;
import com.mrikso.apkrepacker.recycler.Adapter;
import com.mrikso.apkrepacker.task.SignTask;
import com.mrikso.apkrepacker.utils.AppUtils;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.mrikso.apkrepacker.utils.PreferenceUtils;
import com.mrikso.apkrepacker.utils.SignUtil;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import me.zhanghai.android.fastscroll.FastScrollerBuilder;

import static com.mrikso.apkrepacker.utils.FileUtil.getInternalStorage;
import static com.mrikso.apkrepacker.utils.FileUtil.getName;
import static com.mrikso.apkrepacker.utils.FileUtil.getPath;
import static com.mrikso.apkrepacker.utils.StringUtils.EXTRA_NAME;
import static com.mrikso.apkrepacker.utils.StringUtils.EXTRA_TYPE;
import static com.mrikso.apkrepacker.utils.StringUtils.SAVED_DIRECTORY;
import static com.mrikso.apkrepacker.utils.StringUtils.SAVED_SELECTION;

public class FileManagerActivity extends BaseActivity implements ApkOptionsDialogFragment.ItemClickListener {

    private static File selectedApk;
    public File signedApk;
    private CoordinatorLayout coordinatorLayout;
    private Toolbar toolbar;
    private File currentDirectory;
    private Adapter adapter;
    private String name;
    private String type;
    private DialogFragment dialog;
    public static FileManagerActivity Intance;
    //----------------------------------------------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intance = this;
        setContentView(R.layout.activity_file_manager);
        initAppBarLayout();
        initCoordinatorLayout();
        initRecyclerView();
        loadIntoRecyclerView();
        invalidateToolbar();
        invalidateTitle();
    }

    @Override
    public void onBackPressed() {
        if (adapter.anySelected()) {
            adapter.clearSelection();
            return;
        }
        if (!FileUtil.isStorage(currentDirectory)) {
            setPath(Objects.requireNonNull(currentDirectory.getParentFile()));
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(coordinatorLayout, "Permission required", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Settings", v -> AppUtils.gotoApplicationSettings(this, this.getPackageName()))
                        .show();
            } else {
                loadIntoRecyclerView();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onResume() {
        if (adapter != null) adapter.refresh();
        super.onResume();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        adapter.select(Objects.requireNonNull(savedInstanceState.getIntegerArrayList(SAVED_SELECTION)));
        String path = savedInstanceState.getString(SAVED_DIRECTORY, getInternalStorage().getPath());
        if (currentDirectory != null) setPath(new File(path));
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putIntegerArrayList(SAVED_SELECTION, adapter.getSelectedPositions());
        outState.putString(SAVED_DIRECTORY, getPath(currentDirectory));
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.filemanager_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                actionDelete();
                return true;
            case R.id.action_rename:
                actionRename();
                return true;
            case R.id.action_search:
                actionSearch();
                return true;
            case R.id.action_copy:
                actionCopy();
                return true;
            case R.id.action_move:
                actionMove();
                return true;
            case R.id.action_send:
                actionSend();
                return true;
            case R.id.action_sort:
                actionSort();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //----------------------------------------------------------------------------------------------

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (adapter != null) {
            int count = adapter.getSelectedItemCount();
            menu.findItem(R.id.action_delete).setVisible(count >= 1);
            menu.findItem(R.id.action_rename).setVisible(count >= 1);
            menu.findItem(R.id.action_search).setVisible(count == 0);
            menu.findItem(R.id.action_copy).setVisible(count >= 1 && name == null && type == null);
            menu.findItem(R.id.action_move).setVisible(count >= 1 && name == null && type == null);
            menu.findItem(R.id.action_send).setVisible(count >= 1);
            menu.findItem(R.id.action_sort).setVisible(count == 0);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    //----------------------------------------------------------------------------------------------

    private void loadIntoRecyclerView() {
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, permission)) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, 0);
            return;
        }
        final Context context = this;
        if (name != null) {
            adapter.addAll(FileUtil.searchFilesName(context, name));
            return;
        }
        setPath(getInternalStorage());
    }

    private void initAppBarLayout() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.ic_more));
        setSupportActionBar(toolbar);
    }

    private void initCoordinatorLayout() {
        coordinatorLayout = findViewById(R.id.coordinator_layout);
    }

    //----------------------------------------------------------------------------------------------

    private void initRecyclerView() {
        adapter = new Adapter(this);
        adapter.setOnItemClickListener(new OnItemClickListener(this));
        adapter.setOnItemSelectedListener(() -> {
            invalidateOptionsMenu();
            invalidateTitle();
            invalidateToolbar();
        });
        adapter.setItemLayout(R.layout.list_item_file);
        adapter.setSpanCount(getResources().getInteger(R.integer.span_count0));
        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        if (recyclerView != null)
            new FastScrollerBuilder(recyclerView).build();
        if (recyclerView != null) {
            recyclerView.setAdapter(adapter);
        }
    }

    private void invalidateTitle() {

        if (adapter.anySelected()) {
            int selectedItemCount = adapter.getSelectedItemCount();
            toolbar.setTitle(getResources().getString(R.string.selected, selectedItemCount));
        } else if (name != null) {
            toolbar.setTitle(getResources().getString(R.string.search_for, name));
        } else if (currentDirectory != null && !currentDirectory.equals(getInternalStorage())) {
            toolbar.setTitle(getName(currentDirectory));
        } else {
            toolbar.setTitle(getResources().getString(R.string.app_name));
        }
    }

    //----------------------------------------------------------------------------------------------

    private void invalidateToolbar() {

        if (adapter.anySelected()) {
            toolbar.setNavigationIcon(R.drawable.ic_clear);
            toolbar.setNavigationOnClickListener(v -> adapter.clearSelection());
        } else {
            toolbar.setNavigationIcon(R.drawable.ic_back);
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void actionDelete() {
        actionDelete(adapter.getSelectedItems());
        adapter.clearSelection();
    }

    private void actionDelete(final List<File> files) {
        final File sourceDirectory = currentDirectory;
        adapter.removeAll(files);
        String message = getResources().getString(R.string.toast_files_deleted, files.size());
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, v -> {

                    if (currentDirectory == null || currentDirectory.equals(sourceDirectory)) {

                        adapter.addAll(files);
                    }
                })
                .addCallback(new Snackbar.Callback() {

                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {

                        if (event != DISMISS_EVENT_ACTION) {

                            try {
                                for (File file : files) FileUtil.deleteFile(file);
                            } catch (Exception e) {
                                showMessage(e);
                            }
                        }
                        super.onDismissed(snackbar, event);
                    }
                })
                .show();
    }

    private void actionRename() {

        final List<File> selectedItems = adapter.getSelectedItems();
        if (selectedItems.size() == 1) {
            UIUtils.showInputDialog(this, R.string.action_rename, 0, selectedItems.get(0).getName(), EditorInfo.TYPE_CLASS_TEXT,
                    new UIUtils.OnShowInputCallback() {
                        @Override
                        public void onConfirm(CharSequence input) {
                            try {
                                adapter.clearSelection();
                                int index = adapter.indexOf(new File(selectedItems.get(0).getAbsolutePath()));
                                File newFile = FileUtil.renameFile(new File(selectedItems.get(0).getAbsolutePath()), input.toString());
                                adapter.updateItemAt(index, newFile);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
            return;
        }
        UIUtils.showInputDialog(this, R.string.action_rename, 0, selectedItems.get(0).getName(), EditorInfo.TYPE_CLASS_TEXT,
                new UIUtils.OnShowInputCallback() {
                    @Override
                    public void onConfirm(CharSequence input) {
                        adapter.clearSelection();
                        try {
                            if (selectedItems.size() == 1) {
                                File file = selectedItems.get(0);
                                int index = adapter.indexOf(file);
                                adapter.updateItemAt(index, FileUtil.renameFile(file, input.toString()));
                            } else {
                                int size = String.valueOf(selectedItems.size()).length();
                                String format = " (%0" + size + "d)";
                                for (int i = 0; i < selectedItems.size(); i++) {
                                    File file = selectedItems.get(i);
                                    int index = adapter.indexOf(file);
                                    @SuppressLint("DefaultLocale") File newFile = FileUtil.renameFile(file, input.toString() + String.format(format, i + 1));
                                    adapter.updateItemAt(index, newFile);
                                }
                            }
                        } catch (Exception e) {
                            showMessage(e);
                        }
                    }
                });

    }

    private void actionSearch() {
        UIUtils.showInputDialog(this, R.string.action_search, 0, null, EditorInfo.TYPE_CLASS_TEXT,
                new UIUtils.OnShowInputCallback() {
                    @Override
                    public void onConfirm(CharSequence input) {
                        setName(input.toString());
                    }
                });
    }

    private void actionCopy() {
        List<File> selectedItems = adapter.getSelectedItems();
        adapter.clearSelection();
        transferFiles(selectedItems, false);
    }

    private void actionMove() {
        List<File> selectedItems = adapter.getSelectedItems();
        adapter.clearSelection();
        transferFiles(selectedItems, true);
    }

    private void actionSend() {
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("*/*");
        ArrayList<Uri> uris = new ArrayList<>();
        for (File file : adapter.getSelectedItems()) {
            if (file.isFile()) uris.add(Uri.fromFile(file));
        }
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        startActivity(intent);
    }

    //----------------------------------------------------------------------------------------------

    private void actionSort() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        int checkedItem = PreferenceUtils.getInteger(this, "pref_sort", 0);
        String[] sorting = {getResources().getString(R.string.sort_by_name),
                getResources().getString(R.string.sort_by_date), getResources().getString(R.string.sort_by_size)};
        final Context context = this;
        builder.setSingleChoiceItems(sorting, checkedItem, (dialog, which) -> {
            adapter.update(which);
            PreferenceUtils.putInt(context, "pref_sort", which);
            dialog.dismiss();
        });
        builder.setTitle(R.string.sort_by);
        builder.show();
    }
    public void showProgress() {
        Bundle args = new Bundle();
        args.putString(ProgressDialogFragment.TITLE, getResources().getString(R.string.dialog_sign));
        args.putString(ProgressDialogFragment.MESSAGE, getResources().getString(R.string.dialog_please_wait));
        args.putBoolean(ProgressDialogFragment.CANCELABLE, false);
        //  args.putInt(ProgressDialogFragment.MAX, 100);
        dialog = ProgressDialogFragment.newInstance();
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), ProgressDialogFragment.TAG);
    }

    public void updateProgress(Integer... values) {
        ProgressDialogFragment progress = getProgressDialogFragment();
        if (progress == null) {
            return;
        }
        progress.updateProgress(values[0]);
    }

    public void hideProgress() {
        dialog.dismiss();
        //setPath(new File(selectedApk.getParent()));
        //int index = adapter.indexOf(selectedApk);
        setPath(new File(selectedApk.getParent()));
        showMessage(this.getResources().getString(R.string.toast_sign_done));
    }

    private ProgressDialogFragment getProgressDialogFragment() {
        assert getFragmentManager() != null;
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(ProgressDialogFragment.TAG);
        return (ProgressDialogFragment) fragment;
    }

    private void transferFiles(final List<File> files, final Boolean delete) {
        String paste = delete ? "moved" : "copied";
        String message = String.format(Locale.getDefault(), "%d items waiting to be %s", files.size(), paste);
        View.OnClickListener onClickListener = v -> {
            try {
                for (File file : files) {
                    adapter.addAll(FileUtil.copyFile(file, currentDirectory));
                    if (delete) FileUtil.deleteFile(file);
                }
            } catch (Exception e) {
                showMessage(e);
            }
        };
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.paste, onClickListener)
                .show();
    }

    private void showMessage(Exception e) {
        showMessage(e.getMessage());
    }

    //----------------------------------------------------------------------------------------------

    private void showMessage(String message) {
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    public static FileManagerActivity getInstance() {
        return Intance;
    }

    private void setPath(File directory) {

        if (!directory.exists()) {
            Toast.makeText(this, R.string.toast_error_directory_not_exits, Toast.LENGTH_SHORT).show();
            return;
        }
        currentDirectory = directory;
        adapter.clear();
        adapter.clearSelection();
        adapter.addAll(FileUtil.getChildren(directory));
        invalidateTitle();
    }

    private void setName(String name) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(EXTRA_NAME, name);
        startActivity(intent);
    }

    private void setType(String type) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(EXTRA_TYPE, type);
        if (Build.VERSION.SDK_INT >= 21) {
            intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        }
        startActivity(intent);
    }

    @Override
    public void onApkItemClick(Integer item) {
        switch (item) {
            case R.id.decompile_app:
                DecompileFragment decompileFragment = new DecompileFragment(selectedApk);
                getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(android.R.id.content, decompileFragment).commit();
                break;
            case R.id.simple_edit_apk:
                SimpleEditorFragment simpleEditorFragment = new SimpleEditorFragment(selectedApk);
                getSupportFragmentManager().beginTransaction().addToBackStack(null).add(android.R.id.content, simpleEditorFragment).commit();
                break;
            case R.id.install_app:
                showMessage(this.getResources().getString(R.string.install_app));
                FileUtil.installApk(FileManagerActivity.this, selectedApk);
                break;
            case R.id.sign_app:
                //showMessage("sign apk");
                Runnable build = () -> SignUtil.loadKey(this, signTool -> new SignTask(this, signTool).execute(selectedApk));
                build.run();
                break;
            case R.id.delete_item:
                try {
                    FileUtil.deleteFile(selectedApk);
                    showMessage(this.getResources().getString(R.string.toast_deleted_item, selectedApk.getName()));
                } catch (Exception e) {
                    showMessage(this.getResources().getString(R.string.error));
                    e.printStackTrace();
                }
                break;
        }
    }
    //----------------------------------------------------------------------------------------------

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
                    showMessage("Cannot open directory");
                }
            } else {
                Thread thread;
                switch (FileUtil.FileType.getFileType(file)) {
                    /*
                    case ZIP:
                        final ProgressDialog dialog1 = ProgressDialog.show(context, "", "Unzipping", true);
                        thread = new Thread(() -> {
                            try {
                                setPath(unzip(file));
                                runOnUiThread(dialog1::dismiss);
                            } catch (Exception e) {
                                showMessage(e);
                            }
                        });
                        thread.run();
                        break;
                    case TXT:
                      //  showMessage(String.format("Clicked txt file %s", getName(file)));
                        break;
                    case BAK:
                      //  showMessage(String.format("Clicked bak file %s", getName(file)));
                        break;

                     */
                    case APK:
                        selectedApk = file;
                        ApkOptionsDialogFragment fragment = ApkOptionsDialogFragment.newInstance();
                        fragment.show(getSupportFragmentManager(), ApkOptionsDialogFragment.TAG);
                        break;
                        /*
                    case DEX:
                        // openDexFile(file);
                        showMessage(String.format("Clicked dex file %s", getName(file)));
                        break;
                    case APKS:
                        showMessage(String.format("Clicked apks file %s", getName(file)));

                         */
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
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.fromFile(file));//, getMimeType(file));
                            intent.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                            // Log.i("OPEN", "open file " + getMimeType(file));
                            startActivity(intent);
                        } catch (Exception e) {
                            Log.d("OPENERR", e.toString());
                            showMessage(String.format("Cannot open %s", getName(file)));
                        }
                }
            }
        }

        @Override
        public boolean onItemLongClick(int position) {
            adapter.toggle(position);
            return true;
        }
    }
}
