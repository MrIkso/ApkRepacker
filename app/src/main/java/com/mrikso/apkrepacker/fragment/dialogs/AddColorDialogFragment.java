package com.mrikso.apkrepacker.fragment.dialogs;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;
import com.jaredrummler.android.colorpicker.ColorShape;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.utils.Theme;

import java.util.Objects;

public class AddColorDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener, ColorPickerDialogListener {

    public static final String TAG = "AddColorDialogFragment";
    private TextInputEditText colorNameEditText;
    private MaterialButton addColorBtn;
    private AppCompatImageButton selectColorBtn;

    private ItemClickListener mListener;
    private int colorValue;
    private Context mContext;

    public static AddColorDialogFragment newInstance() {
        return new AddColorDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_add_new_color, container, false);
        mContext = view.getContext();
        colorNameEditText = view.findViewById(R.id.color_name);
        selectColorBtn = view.findViewById(R.id.select_color);
        addColorBtn = view.findViewById(R.id.add_color);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        addColorBtn.setOnClickListener(this);
        initView();
    }

    private void initView() {
        selectColorBtn.setOnClickListener(v -> {
            ColorPickerDialog dialog = ColorPickerDialog.newBuilder()
//                    .setDialogTitle("colorName")
                    .setDialogType(ColorPickerDialog.TYPE_PRESETS)
                    .setColorShape(ColorShape.CIRCLE)
                    .setPresets(ColorPickerDialog.MATERIAL_COLORS)
                    .setAllowPresets(true)
                    .setAllowCustom(true)
                    .setShowAlphaSlider(true)
                    .setShowColorShades(true)
                    .setColor(Color.BLACK)
                    .setDialogWidth(0.95f)
                    .setDialogTheme(!Theme.getInstance(mContext).getCurrentTheme().isDark() ? R.style.ColorPickerDialog : R.style.ColorPickerDialog_Dark)
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
        if (!Objects.requireNonNull(colorNameEditText.getText()).toString().isEmpty() && colorValue != 0) {
            mListener.onAddColorClick(colorNameEditText.getText().toString(), colorValue);
            dismiss();
        } else if (colorNameEditText.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "set color name!!", Toast.LENGTH_SHORT).show();
        } else if (colorValue == 0) {
            Toast.makeText(getContext(), "set color value!!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        this.colorValue = color;
    }

    @Override
    public void onDialogDismissed(int dialogId) {
    }

    public interface ItemClickListener {
        void onAddColorClick(String colorName, int colorValue);
    }
}

