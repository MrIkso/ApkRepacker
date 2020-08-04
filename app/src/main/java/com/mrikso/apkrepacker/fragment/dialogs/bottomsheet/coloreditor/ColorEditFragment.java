package com.mrikso.apkrepacker.fragment.dialogs.bottomsheet.coloreditor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.fragment.dialogs.bottomsheet.ColorOptionsDialogFragment;

import java.util.Objects;

public class ColorEditFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "ColorEditFragment";
    private TextInputEditText mColorNameEditText;

    private MaterialButton mDoneBtn;
    private AppCompatImageButton mSelectColorBtn, mDeleteColorBtn;

    private ColorOptionsDialogFragment.ItemClickListener mListener;
    private static String colorName;
    private static int colorValue;
    private static boolean isChange;

    public static ColorEditFragment newInstance(String name, int value, boolean change) {
        colorName = name;
        colorValue = value;
        isChange = change;
        return new ColorEditFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_color_editor_main, container, false);
        return view;
    }

    private void initView() {
     //   mTitleTextView.setText(isChange ? R.string.action_change_color : R.string.action_add_new_color);
        mColorNameEditText.setHint(!isChange ? getString(R.string.color_name_exapmle) : "");
        mColorNameEditText.setText(colorName);
        mDoneBtn.setOnClickListener(this);
        mDeleteColorBtn.setVisibility(isChange ? View.VISIBLE : View.GONE);
        mDeleteColorBtn.setOnClickListener(this);
    /*    mSelectColorBtn.setOnClickListener(v -> {
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
        });*/
    }

    @Override
    public void onClick(View view) {
        if (!Objects.requireNonNull(mColorNameEditText.getText()).toString().isEmpty()) {
            mListener.onColorClick(view, mColorNameEditText.getText().toString(), colorValue, isChange);
           // dismiss();
        } else {
            Toast.makeText(getContext(), getString(R.string.enter_color_name), Toast.LENGTH_SHORT).show();
        }
    }

}
