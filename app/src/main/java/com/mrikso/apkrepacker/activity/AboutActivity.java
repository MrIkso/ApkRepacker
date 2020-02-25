package com.mrikso.apkrepacker.activity;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.fragment.LibraresFragment;
import com.mrikso.apkrepacker.utils.AppUtils;
import com.mrikso.apkrepacker.utils.Constant;

public class AboutActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = findViewById(R.id.toolbar_about);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getWindow().setNavigationBarColor(getResources().getColor(R.color.light_primary));

        initView();
    }

    public void initView() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_about_card_show);
        ScrollView scroll_about = findViewById(R.id.scroll_about);
        scroll_about.startAnimation(animation);

        LinearLayout ll_card_about_2_email = findViewById(R.id.ll_card_about_2_email);
        LinearLayout ll_card_about_2_git_hub = findViewById(R.id.ll_card_about_2_git_hub);
        LinearLayout ll_card_about_2_website = findViewById(R.id.ll_card_about_2_website);
        LinearLayout ll_card_about_source_licenses = findViewById(R.id.ll_card_about_source_licenses);
        LinearLayout telegram = findViewById(R.id.ll_card_about_2_telegram);
        LinearLayout telegram_easy_apk = findViewById(R.id.ll_card_about_2_easy_apk);
        LinearLayout telegram_alex_stannik = findViewById(R.id.ll_card_about_2_alex_strannik);
        telegram_alex_stannik.setOnClickListener(this);
        telegram.setOnClickListener(this);
        ll_card_about_2_email.setOnClickListener(this);
        ll_card_about_2_git_hub.setOnClickListener(this);
        ll_card_about_2_website.setOnClickListener(this);
        ll_card_about_source_licenses.setOnClickListener(this);
        telegram_easy_apk.setOnClickListener(this);

        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        alphaAnimation.setDuration(300);
        alphaAnimation.setStartOffset(600);

        TextView tv_about_version = findViewById(R.id.tv_about_version);
        tv_about_version.setText(AppUtils.getVersionName(this));
        tv_about_version.startAnimation(alphaAnimation);
    }

    @Override
    public void onClick(View view) {

        Intent intent = new Intent();
        switch (view.getId()) {

            case R.id.ll_card_about_2_email:
                intent.setAction(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse(Constant.EMAIL + "?subject=" + Uri.encode(getString(R.string.about_email_intent))));
                //intent.putExtra(Intent.EXTRA_EMAIL, Constant.EMAIL);
              //  intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.about_email_intent));
                //intent.putExtra(Intent.EXTRA_TEXT, "Hi,");
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
            case R.id.ll_card_about_2_telegram:
                openSite(Constant.TELEGRAM_CHANNEL);
                break;
            case R.id.ll_card_about_2_easy_apk:
                openSite(Constant.EASY_APK);
                break;
            case R.id.ll_card_about_2_alex_strannik:
                openSite(Constant.ALEX_STRANNIK);
                break;
        }
    }
    private void openSite(String url){
        Intent intent = new Intent();
        intent.setData(Uri.parse(url));
        intent.setAction(Intent.ACTION_VIEW);
        startActivity(intent);
    }

}
