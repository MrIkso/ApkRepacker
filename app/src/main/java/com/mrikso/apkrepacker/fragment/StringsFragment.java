package com.mrikso.apkrepacker.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.autotranslator.translator.TranslateItem;
import com.mrikso.apkrepacker.filepicker.FilePickerDialog;
import com.mrikso.apkrepacker.fragment.dialogs.ProgressDialogFragment;
import com.mrikso.apkrepacker.fragment.dialogs.bottomsheet.AddLanguageDialogFragment;
import com.mrikso.apkrepacker.fragment.dialogs.bottomsheet.AddNewStringDialogFragment;
import com.mrikso.apkrepacker.fragment.dialogs.bottomsheet.StringsOptionsDialogFragment;
import com.mrikso.apkrepacker.fragment.dialogs.bottomsheet.TranslateOptionsDialogFragment;
import com.mrikso.apkrepacker.fragment.dialogs.bottomsheet.TranslateStringDialogFragment;
import com.mrikso.apkrepacker.task.TranslateDictionaryTask;
import com.mrikso.apkrepacker.task.TranslateTask;
import com.mrikso.apkrepacker.ui.preferences.PreferenceHelper;
import com.mrikso.apkrepacker.ui.stringlist.StringFile;
import com.mrikso.apkrepacker.ui.stringlist.StringsAdapter;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.mrikso.apkrepacker.utils.IntentUtils;
import com.mrikso.apkrepacker.utils.ScrollingViewOnApplyWindowInsetsListener;
import com.mrikso.apkrepacker.viewmodel.StringFragmentViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import me.zhanghai.android.fastscroll.FastScroller;
import me.zhanghai.android.fastscroll.FastScrollerBuilder;

public class StringsFragment extends Fragment implements AddLanguageDialogFragment.ItemClickListener,
        StringsAdapter.OnItemClickListener, View.OnClickListener, TranslateStringDialogFragment.ItemClickListener,
        StringsOptionsDialogFragment.ItemClickListener, AddNewStringDialogFragment.ItemClickListener, TranslateOptionsDialogFragment.ItemClickListener {

    public static final String TAG = "StringsFragment";

    private StringsAdapter mStringsAdapter;
    private RecyclerView mRecyclerView;

    private FloatingActionButton mFabSelectLanguage;
    private FloatingActionMenu mFabMenu;
    private AppCompatEditText mSearchText;
    private List<String> mLangCodes = new ArrayList<>();
    private List<StringFile> mLangFiles = new ArrayList<>();
    private StringFragmentViewModel mViewModel;
    private boolean isChanget = false;
    private DialogFragment dialog;

    public StringsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(StringFragmentViewModel.class);
        mViewModel.startLoad();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_app_strings, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle bundle) {
        super.onViewCreated(view, bundle);

        mRecyclerView = view.findViewById(R.id.string_list);
        mSearchText = view.findViewById(R.id.et_search);
        mFabMenu = view.findViewById(R.id.fab);
        mFabSelectLanguage = view.findViewById(R.id.fab_select_language);

        FloatingActionButton mFabSaveTranslate = view.findViewById(R.id.fab_save_language);
        FloatingActionButton mFabMore = view.findViewById(R.id.fab_more_options);

        mFabSaveTranslate.setOnClickListener(this);
        mFabMore.setOnClickListener(this);
        mFabSelectLanguage.setOnClickListener(this);
        mFabMenu.setClosedOnTouchOutside(true);

        initList(view);

    }

    private void initList(View view) {
        mViewModel.getStingsFilesData().observe(getViewLifecycleOwner(), stringFiles -> {
            mLangCodes.clear();
            mLangFiles.clear();
            for (StringFile a : stringFiles) {
                Locale locale = a.locale();
                if (locale != null) {
                    mLangCodes.add(String.format("%1s (%2s)", a.locale().getDisplayName(), a.lang()));
                } else {
                    mLangCodes.add(a.lang());
                }
                mLangFiles.add(a);
            }
        });

        mViewModel.getStingsData().observe(getViewLifecycleOwner(), translateItems -> {
            if (translateItems != null)
                mStringsAdapter.setItems(translateItems);
        });

        mStringsAdapter = new StringsAdapter(requireContext());

        mStringsAdapter.setInteractionListener(this);
        mRecyclerView.setAdapter(mStringsAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        FastScroller fastScroller = new FastScrollerBuilder(mRecyclerView).useMd2Style().build();
        mRecyclerView.setOnApplyWindowInsetsListener(new ScrollingViewOnApplyWindowInsetsListener(mRecyclerView, fastScroller));

        mViewModel.getCurrentLanguage().observe(getViewLifecycleOwner(), s ->
                mFabSelectLanguage.setLabelText(getString(R.string.action_select_lang, s)));

        view.findViewById(R.id.button_clear).setOnClickListener(v -> mSearchText.setText(""));
        mSearchText.clearFocus();
        mSearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                view.findViewById(R.id.button_clear).setVisibility(s.toString().isEmpty() ? View.GONE : View.VISIBLE);
                mViewModel.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void showListDialog(Context context, String[] list) {
        UIUtils.showListDialog(context, 0, 0, list, 0, new UIUtils.OnListCallback() {
            @Override
            public void onSelect(MaterialDialog dialog, int which) {
                mViewModel.parseStings(mLangFiles.get(which));
            }
        }, null);
    }

    @Override
    public void onAddLangClick(String item, boolean autotranslate, boolean skipTranslated,
                               boolean skipSupport) {
        if (!autotranslate)
            mViewModel.addNewLang(item);
        else {
            //mViewModel.autoTranslate(item);
            TranslateTask translatingTask = new TranslateTask(mStringsAdapter.getData(), this);
            translatingTask.setTargetLanguageCode(item);
            translatingTask.setSkipSupport(skipSupport);
            translatingTask.setSkipTranslated(skipTranslated);

            translatingTask.execute();
        }
    }

    @Override
    public void onTranslateClicked(TranslateItem item, int position) {
        TranslateStringDialogFragment translateStringDialogFragment = TranslateStringDialogFragment.newInstance();
        translateStringDialogFragment.setData(item, position);
        translateStringDialogFragment.setItemClickListener(this);
        translateStringDialogFragment.show(getChildFragmentManager(), TranslateStringDialogFragment.TAG);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_select_language:
                if (isChanget) {
                    UIUtils.showConfirmDialog(requireContext(), getString(R.string.dialog_warring), getString(R.string.dialog_warring_save_changes), new UIUtils.OnClickCallback() {
                        @Override
                        public void onOkClick() {
                            save();
                        }
                    });
                } else
                    mFabMenu.close(true);
                mViewModel.findStringFiles();
                //convert list to array
                //list dialog dos`nt supported list to view
                String[] arrayOfList = new String[mLangCodes.size()];
                mLangCodes.toArray(arrayOfList);
                showListDialog(requireContext(), arrayOfList);
                break;
            case R.id.fab_save_language:
                save();
                break;
            case R.id.fab_more_options:
                mFabMenu.close(true);
                StringsOptionsDialogFragment stringsOptionsDialog = StringsOptionsDialogFragment.newInstance();
                stringsOptionsDialog.show(getChildFragmentManager(), StringsOptionsDialogFragment.TAG);
                break;
        }
    }

    @Override
    public void onTranslateClicked(String value, int position) {
        isChanget = true;
        mStringsAdapter.setUpdateValue(value, position);
    }

    @Override
    public void onDeleteString(int key) {
        isChanget = true;
        mViewModel.deleteString(key);
    }

    @Override
    public void onItemClick(int item) {
        switch (item) {
            case R.id.open_with:
                startActivity(IntentUtils.openFileWithIntent(mViewModel.getCurrentLangFile()));
                break;
            case R.id.add_new_string:
                AddNewStringDialogFragment dialogFragment = AddNewStringDialogFragment.newInstance();
                dialogFragment.setItemClickListener(this);
                dialogFragment.show(getChildFragmentManager(), AddNewStringDialogFragment.TAG);
                break;
            case R.id.add_new_language:
                if (isChanget) {
                    UIUtils.showConfirmDialog(requireContext(), getString(R.string.dialog_warring), getString(R.string.dialog_warring_save_changes), new UIUtils.OnClickCallback() {
                        @Override
                        public void onOkClick() {
                            save();
                        }
                    });
                } else {
                    AddLanguageDialogFragment fragment = AddLanguageDialogFragment.newInstance(false);
                    fragment.show(getChildFragmentManager(), AddLanguageDialogFragment.TAG);
                }
                break;
            case R.id.auto_translate_language:
                AddLanguageDialogFragment addLanguageDialogFragment = AddLanguageDialogFragment.newInstance(true);
                addLanguageDialogFragment.show(getChildFragmentManager(), AddLanguageDialogFragment.TAG);
                break;
            case R.id.auto_translate_language_with:
                new FilePickerDialog(requireContext())
                        .setTitleText(getString(R.string.select_translate_dictionary))
                        .setSelectMode(FilePickerDialog.MODE_SINGLE)
                        .setSelectType(FilePickerDialog.TYPE_FILE)
                        .setExtensions(new String[]{"mtd"})
                        .setRootDir(FileUtil.getInternalStorage().getAbsolutePath())
                        .setBackCancelable(true)
                        .setOutsideCancelable(true)
                        .setDialogListener(getString(R.string.choose_button_label), getString(R.string.cancel_button_label), new FilePickerDialog.FileDialogListener() {
                            @Override
                            public void onSelectedFilePaths(String[] filePaths) {
                                TranslateOptionsDialogFragment translateOptionsDialogFragment = TranslateOptionsDialogFragment.newInstance(filePaths[0]);
                                translateOptionsDialogFragment.show(getChildFragmentManager(), TranslateOptionsDialogFragment.TAG);
                            }

                            @Override
                            public void onCanceled() {
                            }
                        })
                        .show();
                break;
            case R.id.save_as_dictionary:
                UIUtils.showInputDialog(requireContext(), R.string.dialog_dictionary_name, 0, "Default", EditorInfo.TYPE_CLASS_TEXT, new UIUtils.OnShowInputCallback() {
                    @Override
                    public void onConfirm(CharSequence input) {
                        if (!TextUtils.isEmpty(input))
                            mViewModel.saveTranslationToDictionary(input.toString(), mStringsAdapter.getData());
                        else
                            UIUtils.toast(requireContext(), R.string.cannot_be_empty);
                    }
                });
                break;
        }
    }

    @Override
    public void onAddStringClicked(String key, String value) {
        isChanget = true;
        mViewModel.addNewString(new TranslateItem(key, value));
    }

    private void save() {
        isChanget = false;
        mFabMenu.close(true);
        mViewModel.saveStrings(mStringsAdapter.getData());
    }

    /*public void parseDictionary(String path) {
        DictionaryReader dictionaryReader = new DictionaryReader(new File(path));
        dictionaryReader.readDictionary();
        List<TranslateItem> items = new ArrayList<>(mStringsAdapter.getData());

        for (int i = 0; i < items.size(); i++) {
          //  TranslateItem item = items.get(i);

            for (Map.Entry<String, DictionaryItem> entry : dictionaryReader.getDictionaryMap().entrySet()) {
                String original = entry.getKey();
                String translated = entry.getValue().getTranslated();
                if (items.get(i).originValue.equals(original)) {
                    int finalI = i;
                    getActivity().runOnUiThread(() -> {
                        mStringsAdapter.setUpdateValue(translated, finalI);
                    });
                }
            }
            // DLog.d(entry.getKey() + " => " + entry.getValue().getTranslated());
        }

        //mStringsAdapter.setUpdatedItems();
    }
*/

    @Override
    public void onOkClick(String path) {
        PreferenceHelper helper = PreferenceHelper.getInstance(requireContext());
        TranslateDictionaryTask dictionaryTask = new TranslateDictionaryTask(this);
        dictionaryTask.setDictionaryPath(path);
        dictionaryTask.setTranslateItems(mStringsAdapter.getData());
        dictionaryTask.setReverseDictionary(helper.isReverseDictionary());
        dictionaryTask.setSkipSupport(helper.isSkipSupportLines());
        dictionaryTask.setSkipTranslated(helper.isSkipTranslated());

        dictionaryTask.execute();
    }

    public StringsAdapter getStringsAdapter() {
        return mStringsAdapter;
    }

    public void showProgress() {
        Bundle args = new Bundle();
        args.putString(ProgressDialogFragment.TITLE, getResources().getString(R.string.translating_run_title));
        args.putString(ProgressDialogFragment.MESSAGE, getResources().getString(R.string.dialog_please_wait));
        args.putBoolean(ProgressDialogFragment.CANCELABLE, false);
        //  args.putInt(ProgressDialogFragment.MAX, 100);
        dialog = ProgressDialogFragment.newInstance();
        dialog.setArguments(args);
        dialog.show(getChildFragmentManager(), ProgressDialogFragment.TAG);
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
    }

    private ProgressDialogFragment getProgressDialogFragment() {
        Fragment fragment = getChildFragmentManager().findFragmentByTag(ProgressDialogFragment.TAG);
        return (ProgressDialogFragment) fragment;
    }

    /*@Override
    public boolean onBackPressed() {
        if (isChanget) {
            UIUtils.showConfirmDialog(requireContext(), getString(R.string.dialog_warring), getString(R.string.dialog_warring_save_changes), new UIUtils.OnClickCallback() {
                @Override
                public void onOkClick() {
                    save();
                }
            });
            return true;
        }
        return true;
    }*/
}