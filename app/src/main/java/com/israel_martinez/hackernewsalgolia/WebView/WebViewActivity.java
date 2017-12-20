package com.israel_martinez.hackernewsalgolia.WebView;

import android.graphics.drawable.Animatable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.israel_martinez.hackernewsalgolia.R;

public class WebViewActivity extends AppCompatActivity {
    private WebView webView;
    private TextView textView;
    private Animation animFadeIn;
    private Animation animFadeOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);

        webView = findViewById(R.id.webView);
        textView = findViewById(R.id.webView_state);
        textView.setVisibility(View.INVISIBLE);

        String url = getIntent().getStringExtra("url");
        String comment = getIntent().getStringExtra("comment");

        if(url != null){
            webView.getSettings().setJavaScriptEnabled(true);
            webView.loadUrl(url);
        }else{
            if(comment != null){
                textView.setVisibility(View.VISIBLE);
                textView.setText(comment);
                webView.setVisibility(View.INVISIBLE);
            }else{
                webView.setVisibility(View.INVISIBLE);
                textView.setVisibility(View.VISIBLE);
                textView.startAnimation(animFadeIn);
                textView.setText("Post without Url and comment ...");
            }
        }

    }
}
