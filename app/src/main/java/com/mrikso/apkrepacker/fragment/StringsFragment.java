package com.mrikso.apkrepacker.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.autotranslator.dictionary.DictionaryItem;
import com.mrikso.apkrepacker.autotranslator.dictionary.DictionaryReader;
import com.mrikso.apkrepacker.autotranslator.translator.TranslateItem;
import com.mrikso.apkrepacker.filepicker.FilePickerDialog;
import com.mrikso.apkrepacker.fragment.dialogs.bottomsheet.AddLanguageDialogFragment;
import com.mrikso.apkrepacker.fragment.dialogs.bottomsheet.AddNewStringDialogFragment;
import com.mrikso.apkrepacker.fragment.dialogs.bottomsheet.StringsOptionsDialogFragment;
import com.mrikso.apkrepacker.fragment.dialogs.bottomsheet.TranslateStringDialogFragment;
import com.mrikso.apkrepacker.task.TranslateTask;
import com.mrikso.apkrepacker.ui.stringlist.StringFile;
import com.mrikso.apkrepacker.ui.stringlist.StringsAdapter;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.mrikso.apkrepacker.utils.IntentUtils;
import com.mrikso.apkrepacker.utils.ScrollingViewOnApplyWindowInsetsListener;
import com.mrikso.apkrepacker.utils.StringUtils;
import com.mrikso.apkrepacker.viewmodel.StringFragmentViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import me.zhanghai.android.fastscroll.FastScroller;
import me.zhanghai.android.fastscroll.FastScrollerBuilder;

public class StringsFragment extends Fragment implements AddLanguageDialogFragment.ItemClickListener,
        StringsAdapter.OnItemClickListener, View.OnClickListener, TranslateStringDialogFragment.ItemClickListener,
        StringsOptionsDialogFragment.ItemClickListener, AddNewStringDialogFragment.ItemClickListener {

    public static final String TAG = "StringsFragment";

    public StringsAdapter mStringsAdapter;
    private RecyclerView mRecyclerView;

    private FloatingActionButton mFabSelectLanguage;
    private FloatingActionMenu mFabMenu;
    private AppCompatEditText mSearchText;
    private List<String> mLangCodes = new ArrayList<>();
    private List<StringFile> mFangFiles = new ArrayList<>();
    private boolean mIsAddLanguage;
    private String targetLanguageCode;
    private StringFragmentViewModel mViewModel;
    private boolean isChanget = false;

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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app_strings, container, false);
        return view;
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
            mFangFiles.clear();
            for (StringFile a : stringFiles) {
                mLangCodes.add(a.lang());
                mFangFiles.add(a);
            }
        });

        mViewModel.getStingsData().observe(getViewLifecycleOwner(), translateItems -> {
            if(translateItems != null)
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
                mViewModel.parseStings(mFangFiles.get(which));
            }
        }, null);
    }


    @Override
    public void onAddLangClick(String item) {
        if (mIsAddLanguage)
            mViewModel.addNewLang(item);
        else {
            targetLanguageCode = item;
            //mViewModel.autoTranslate(item);
            TranslateTask translatingTask = new TranslateTask(mStringsAdapter.getData(), this);
            translatingTask.execute();
        }
    }

    @Override
    public int setTitle() {
        if (mIsAddLanguage)
            return R.string.action_add_new_lang;
        else
            return R.string.action_auto_translate_lang;
    }

    @Override
    public void onTranslateClicked(TranslateItem item, int position) {
        TranslateStringDialogFragment translateStringDialogFragment = TranslateStringDialogFragment.newInstance();
        translateStringDialogFragment.setData(item, position);
        translateStringDialogFragment.setItemClickListener(this);
        translateStringDialogFragment.show(getChildFragmentManager(), TranslateStringDialogFragment.TAG);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 /*&& !TranslateStringsHelper.getTranslatedStrings().isEmpty()*/) {
            // mViewModel.saveStrings(TranslateStringsHelper.getTranslatedStrings(), mTargetLang.replace("-auto", ""));
        }
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
                mIsAddLanguage = false;
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
                } else
                    mIsAddLanguage = true;
                AddLanguageDialogFragment fragment = AddLanguageDialogFragment.newInstance();
                fragment.show(getChildFragmentManager(), AddLanguageDialogFragment.TAG);
                break;
            case R.id.auto_translate_language:
                mIsAddLanguage = false;
                AddLanguageDialogFragment addLanguageDialogFragment = AddLanguageDialogFragment.newInstance();
                addLanguageDialogFragment.show(getChildFragmentManager(), AddLanguageDialogFragment.TAG);
                break;
            case R.id.auto_translate_language_with:
                new FilePickerDialog(requireContext())
                        .setTitleText(getString(R.string.select_directory))
                        .setSelectMode(FilePickerDialog.MODE_SINGLE)
                        .setSelectType(FilePickerDialog.TYPE_FILE)
                        .setExtensions(new String[]{"mtd"})
                        .setRootDir(FileUtil.getInternalStorage().getAbsolutePath())
                        .setBackCancelable(true)
                        .setOutsideCancelable(true)
                        .setDialogListener(getString(R.string.choose_button_label), getString(R.string.cancel_button_label), new FilePickerDialog.FileDialogListener() {
                            @Override
                            public void onSelectedFilePaths(String[] filePaths) {
                                new Thread(() -> {
                                    parseDictionary(filePaths[0]);
                                }).start();
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

    public void parseDictionary(String path) {
        DictionaryReader dictionaryReader = new DictionaryReader(new File(path));
        dictionaryReader.readDictionary();
        List<TranslateItem> items = new ArrayList<>(mStringsAdapter.getData());
        List<TranslateItem> iterator = new ArrayList<TranslateItem>();

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

    // Get the google language code
    // Convert -zh-rCN to zh-CN
    public String getGoogleLangCode() {
        String code = targetLanguageCode.substring(1);
        int pos = code.indexOf("-");
        if (pos != -1) {
            code = code.substring(0, pos + 1) + code.substring(pos + 2);
        }
        return code;
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