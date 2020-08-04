package com.mrikso.apkrepacker.fragment.dialogs.bottomsheet;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import com.mrikso.apkrepacker.ide.editor.text.InputMethodManagerCompat;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;
import com.jaredrummler.android.colorpicker.ColorShape;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.utils.Theme;

import java.util.Objects;

public class ColorOptionsDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener, ColorPickerDialogListener {

    public static final String TAG = "ColorOptionsDialogFragment";
    private TextInputEditText mColorNameEditText;
    private AppCompatTextView mTitleTextView;
    private MaterialButton mDoneBtn;
    private AppCompatImageButton mSelectColorBtn, mDeleteColorBtn;

    private ItemClickListener mListener;
    private static String colorName;
    private static int colorValue;
    private static boolean isChange;

    public static ColorOptionsDialogFragment newInstance(String name, int value, boolean change) {
        colorName = name;
        colorValue = value;
        isChange = change;
        return new ColorOptionsDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_color_options, container, false);
        mTitleTextView = view.findViewById(R.id.tv_dialog_title);
        mColorNameEditText = view.findViewById(R.id.color_name);
        mSelectColorBtn = view.findViewById(R.id.select_color);
        mDeleteColorBtn = view.findViewById(R.id.delete_color);
        mDoneBtn = view.findViewById(R.id.done);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    private void initView() {
        mTitleTextView.setText(isChange ? R.string.action_change_color : R.string.action_add_new_color);
        mColorNameEditText.setHint(!isChange ? getString(R.string.color_name_exapmle) : "");
        mColorNameEditText.setText(colorName);
        mDoneBtn.setOnClickListener(this);
        mDeleteColorBtn.setVisibility(isChange ? View.VISIBLE : View.GONE);
        mDeleteColorBtn.setOnClickListener(this);
        mSelectColorBtn.setOnClickListener(v -> {
            InputMethodManagerCompat.hideSoftInput(mSelectColorBtn);
            ColorPickerDialog dialog = ColorPickerDialog.newBuilder()
                    .setDialogTitle(colorName)
                    .setDialogType(ColorPickerDialog.TYPE_PRESETS)
                    .setColorShape(ColorShape.CIRCLE)
                    .setPresets(ColorPickerDialog.MATERIAL_COLORS)
                    .setAllowPresets(true)
                    .setAllowCustom(true)
                    .setShowAlphaSlider(true)
                    .setShowColorShades(true)
                    .setColor(colorValue)
                    .setDialogWidth(0.95f)
                    .setDialogTheme(!Theme.getInstance(requireContext()).getCurrentTheme().isDark() ? R.style.ColorPickerDialog : R.style.ColorPickerDialog_Dark)
                    .create();
            dialog.setColorPickerDialogListener(this);
            ((AppCompatActivity) requireContext()).getSupportFragmentManager().beginTransaction().add(dialog, "color_picker_dialog").commitAllowingStateLoss();
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        final Fragment parent = getParentFragment();
        if (parent != null) {
            mListener = (ItemClickListener) parent;
        } else {
            mListener = (ItemClickListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View view) {
        if (!Objects.requireNonNull(mColorNameEditText.getText()).toString().isEmpty()) {
            mListener.onColorClick(view, mColorNameEditText.getText().toString(), colorValue, isChange);
            dismiss();
        } else {
            Toast.makeText(getContext(), getString(R.string.enter_color_name), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        colorValue = color;
    }

    @Override
    public void onDialogDismissed(int dialogId) {
    }

    public interface ItemClickListener {
        void onColorClick(View view, String colorName, int colorValue, boolean isChange);
    }
}

