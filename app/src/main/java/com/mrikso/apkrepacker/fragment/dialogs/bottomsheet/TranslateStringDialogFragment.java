package com.mrikso.apkrepacker.fragment.dialogs.bottomsheet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.autotranslator.translator.TranslateItem;
import com.mrikso.apkrepacker.autotranslator.translator.Translator;
import com.mrikso.apkrepacker.ui.publicxml.PublicXmlParser;
import com.mrikso.apkrepacker.utils.ProjectUtils;
import com.mrikso.apkrepacker.utils.StringUtils;

import java.io.File;
import java.util.concurrent.Executors;


public class TranslateStringDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener, MenuItem.OnMenuItemClickListener, AddLanguageDialogFragment.ItemClickListener {

    public static final String TAG = "TranslateStringDialogFragment";

    private ItemClickListener mListener;
    private String mKey;
    private String mNewValue;
    private String mOldValue;
    private TextInputEditText mOldEdit;
    private TextInputEditText mNewEdit;
    private TextInputLayout mNewEditLayout;
    private int mItemPosition;

    public static TranslateStringDialogFragment newInstance() {
        return new TranslateStringDialogFragment();
    }

    public void setItemClickListener(ItemClickListener listener) {
        mListener = listener;
    }

    public void setData(TranslateItem item, int position) {
        mKey = item.name;
        mOldValue = item.originValue;
        mNewValue = item.translatedValue;
        mItemPosition = position;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_translate_string, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MaterialButton ok = view.findViewById(R.id.btn_add_lang_ok);
        ok.setOnClickListener(this);
        AppCompatImageButton copyOldValue = view.findViewById(R.id.popup_menu);
        copyOldValue.setOnClickListener(this);
        TextInputLayout textInputLayout = view.findViewById(R.id.text_input_layout_old);
        textInputLayout.setHint(mKey);

        mNewEditLayout = view.findViewById(R.id.new_value_text_layout);
        mOldEdit = view.findViewById(R.id.old_value);
        mOldEdit.setText(mOldValue);
        mNewEdit = view.findViewById(R.id.new_value);
        mNewEdit.setText(mNewValue);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_add_lang_ok:
                mListener.onTranslateClicked(mNewEdit.getText().toString(), mItemPosition);
                dismiss();
                break;
            case R.id.popup_menu:
                PopupMenu popupMenu = new PopupMenu(requireContext(), view);
                popupMenu.inflate(R.menu.string_item_menu);
                popupMenu.setOnMenuItemClickListener(this::onMenuItemClick);
                MenuPopupHelper menuHelper = new MenuPopupHelper(requireContext(), (MenuBuilder) popupMenu.getMenu(), view);
                menuHelper.setForceShowIcon(true);
                menuHelper.show();
                break;
        }

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_paste:
                mNewEdit.setText(StringUtils.getClipboard(requireContext()));
                return true;
            case R.id.action_copy_original_value:
                StringUtils.setClipboard(requireContext(), mOldValue, true);
                return true;
            case R.id.action_copy_id:
                Executors.newSingleThreadExecutor().execute(() -> {
                    PublicXmlParser xmlParser = new PublicXmlParser(new File(ProjectUtils.getProjectPath() + "/res/values/public.xml"));
                    getActivity().runOnUiThread(() -> {
                        String idText = xmlParser.getIdByName(mKey);
                        StringUtils.setClipboard(requireContext(), idText, false);
                        UIUtils.toast(requireContext(), getString(R.string.string_id_copied, idText));
                    });
                });
                return true;
            case R.id.action_clear:
                mNewEdit.setText("");
                return true;
            case R.id.action_auto_translate:
                AddLanguageDialogFragment addLanguageDialogFragment = AddLanguageDialogFragment.newInstance(true, true);
                addLanguageDialogFragment.show(getChildFragmentManager(), AddLanguageDialogFragment.TAG);
                return true;
            case R.id.action_delete:
                mListener.onDeleteString(mItemPosition);
                dismiss();
                return true;

        }
        return false;
    }

    @Override
    public void onAddLangClick(String code, boolean autotranslate, boolean skipTranslated, boolean skipSupport) {

    }

    @Override
    public void onTranslateSting(String code) {
        Executors.newSingleThreadExecutor().execute(()->{
            String googleLangCode = StringUtils.getGoogleLangCode(code);
            mNewEdit.setText(new Translator(googleLangCode).translate(mOldValue));
            mNewEditLayout.clearFocus();
            mNewEditLayout.requestFocus();
        });
    }

    public interface ItemClickListener {
        void onTranslateClicked(String value, int key);

        void onDeleteString(int key);
    }
}
