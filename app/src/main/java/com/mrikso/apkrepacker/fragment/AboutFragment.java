package com.mrikso.apkrepacker.fragment;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.fragment.dialogs.LicensesDialogFragment;
import com.mrikso.apkrepacker.utils.AppUtils;
import com.mrikso.apkrepacker.utils.Constant;
import com.mrikso.apkrepacker.utils.FragmentUtils;
import com.mrikso.apkrepacker.viewmodel.ProjectsFragmentViewModel;

import org.jetbrains.annotations.NotNull;

public class AboutFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "AboutFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ;
        initView(view);
    }

    public void initView(View view) {
        Animation animation = AnimationUtils.loadAnimation(requireContext(), R.anim.about_card_show);
        NestedScrollView scroll_about = view.findViewById(R.id.scroll_about);
        scroll_about.startAnimation(animation);

        view.findViewById(R.id.ll_card_about_2_email).setOnClickListener(this);
        view.findViewById(R.id.ll_card_about_2_git_hub).setOnClickListener(this);
        view.findViewById(R.id.ll_card_about_2_website).setOnClickListener(this);
        view.findViewById(R.id.ll_card_about_source_licenses).setOnClickListener(this);
        view.findViewById(R.id.ll_card_about_2_telegram_channel).setOnClickListener(this);
        view.findViewById(R.id.ll_card_about_2_mrikso).setOnClickListener(this);
        view.findViewById(R.id.ll_card_about_2_phoenix).setOnClickListener(this);
        view.findViewById(R.id.ll_card_about_2_easy_apk).setOnClickListener(this);
        view.findViewById(R.id.ll_card_about_2_alex_strannik).setOnClickListener(this);

        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        alphaAnimation.setDuration(200);
        alphaAnimation.setStartOffset(400);

        try {
            ((ImageView) view.findViewById(R.id.tv_about_app_icon)).setImageDrawable(requireContext().getPackageManager().getApplicationIcon(requireContext().getPackageName()));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        view.findViewById(R.id.tv_about_app_name).startAnimation(animation);
        TextView tv_about_version = view.findViewById(R.id.tv_about_version);
        tv_about_version.setText(AppUtils.getVersionName(requireContext()));
        tv_about_version.startAnimation(alphaAnimation);
       // scroll_about.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> view.findViewById(R.id.app_bar).setSelected(scroll_about.canScrollVertically(-1)));
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.ll_card_about_2_email:
                intent.setAction(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse(Constant.EMAIL + "?subject=" + Uri.encode(getString(R.string.about_email_intent))));
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(requireContext(), getString(R.string.about_not_found_email), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.ll_card_about_source_licenses:
                LicensesDialogFragment.show(this);
                break;
            case R.id.ll_card_about_2_git_hub:
                openSite(Constant.GIT_HUB);
                break;
            case R.id.ll_card_about_2_website:
                openSite(Constant.MY_WEBSITE);
                break;
            case R.id.ll_card_about_2_telegram_channel:
                openSite(Constant.TELEGRAM_CHANNEL);
                break;
            case R.id.ll_card_about_2_mrikso:
                openSite(Constant.MRIKSO);
                break;
            case R.id.ll_card_about_2_phoenix:
                openSite(Constant.PHOENIX);
                break;
            case R.id.ll_card_about_2_easy_apk:
                openSite(Constant.EASY_APK);
                break;
            case R.id.ll_card_about_2_alex_strannik:
                openSite(Constant.ALEX_STRANNIK);
                break;
        }
    }

    private void openSite(String url) {
        Intent intent = new Intent();
        intent.setData(Uri.parse(url));
        intent.setAction(Intent.ACTION_VIEW);
        startActivity(intent);
    }
}