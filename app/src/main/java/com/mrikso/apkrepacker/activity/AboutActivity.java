package com.mrikso.apkrepacker.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.fragment.LibraresFragment;
import com.mrikso.apkrepacker.utils.AppUtils;
import com.mrikso.apkrepacker.utils.Constant;

public class AboutActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
//        getWindow().setNavigationBarColor(getResources().getColor(R.color.light_primary));
        initView();
    }

    public void initView() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_about_card_show);
        NestedScrollView scroll_about = findViewById(R.id.scroll_about);
        scroll_about.startAnimation(animation);

        findViewById(R.id.ll_card_about_2_email).setOnClickListener(this);
        findViewById(R.id.ll_card_about_2_git_hub).setOnClickListener(this);
        findViewById(R.id.ll_card_about_2_website).setOnClickListener(this);
        findViewById(R.id.ll_card_about_source_licenses).setOnClickListener(this);
        findViewById(R.id.ll_card_about_2_telegram_channel).setOnClickListener(this);
        findViewById(R.id.ll_card_about_2_mrikso).setOnClickListener(this);
        findViewById(R.id.ll_card_about_2_phoenix).setOnClickListener(this);
        findViewById(R.id.ll_card_about_2_easy_apk).setOnClickListener(this);
        findViewById(R.id.ll_card_about_2_alex_strannik).setOnClickListener(this);

        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        alphaAnimation.setDuration(200);
        alphaAnimation.setStartOffset(400);

        try {
            ((ImageView) findViewById(R.id.tv_about_app_icon)).setImageDrawable(getPackageManager().getApplicationIcon(this.getPackageName()));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        findViewById(R.id.tv_about_app_name).startAnimation(animation);
        TextView tv_about_version = findViewById(R.id.tv_about_version);
        tv_about_version.setText(AppUtils.getVersionName(this));
        tv_about_version.startAnimation(alphaAnimation);
        scroll_about.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> findViewById(R.id.app_bar).setSelected(scroll_about.canScrollVertically(-1)));
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
                    Toast.makeText(AboutActivity.this, getString(R.string.about_not_found_email), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.ll_card_about_source_licenses:
                LibraresFragment libraresFragment = new LibraresFragment();
                getSupportFragmentManager().beginTransaction().
                        addToBackStack(null).replace(android.R.id.content, libraresFragment)
                        .commit();
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