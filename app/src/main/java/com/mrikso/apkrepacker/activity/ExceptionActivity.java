package com.mrikso.apkrepacker.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import com.mrikso.apkrepacker.BuildConfig;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.utils.StringUtils;

public class ExceptionActivity extends BaseActivity {

    private static String DEFAULT_EMAIL_SUBJECT = "Apk Repacker: Crash Report";
    private String mError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mError = getIntent().getStringExtra("mError");
        setContentView(R.layout.activity_exception);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        AppCompatTextView error = findViewById(R.id.error_view);
        error.setText(mError);
        Button copy = findViewById(R.id.btn_copy_log);
        copy.setOnClickListener(v -> {
            StringUtils.setClipboard(this, mError, true);
        });
        if(!BuildConfig.DEBUG) {
            sendErrorMail(this, mError);
        }
        final NestedScrollView scrollView = findViewById(R.id.exception_scrollview);
        scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener)
                (v, scrollX, scrollY, oldScrollX, oldScrollY) ->
                        findViewById(R.id.app_bar).setSelected(scrollView.canScrollVertically(-1)));
    }

    private void sendErrorMail(Context context, String errorContent) {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        String subject = DEFAULT_EMAIL_SUBJECT;
        String body = "\n\n" + errorContent + "\n\n";
        sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"mrikso821@gmail.com"});
        sendIntent.putExtra(Intent.EXTRA_TEXT, body);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        sendIntent.setType("message/rfc822");
        //sendIntent.setType("text/html");
        sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(Intent.createChooser(sendIntent, getString(R.string.title_select_mail_app)));
    }
    /*
    private void sendErrorMail(Context _context, String errorContent) {

        Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
        String subject = DEFAULT_EMAIL_SUBJECT;
      //  String body = "\n\n" + errorContent + "\n\n";
        String mailto = Constant.EMAIL + "?subject=" + Uri.encode(subject) + "&body=" + Uri.encode(errorContent);
        sendIntent.setData(Uri.parse(mailto));
        //sendIntent.putExtra(Intent.EXTRA_EMAIL, Constant.EMAIL);
       // sendIntent.setType("plain/text");
       // sendIntent.putExtra(Intent.EXTRA_TEXT, body);
       // sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
       // sendIntent.putExtra(Intent.EXTRA_EMAIL, Constant.EMAIL);

        //sendIntent.setDataAndType(Uri.parse(Constant.EMAIL), "message/rfc822");
           sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            startActivity(sendIntent);
        } catch (Exception e) {
            Toast.makeText(_context, getString(R.string.about_not_found_email), Toast.LENGTH_SHORT).show();
        }
    }

     */
}
