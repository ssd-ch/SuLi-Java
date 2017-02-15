package com.example.ssd.shimaneuniversitybrowser;

import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.PagerAdapter;
import android.support.design.widget.TabLayout;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.annotation.SuppressLint;

public class PageView extends AppCompatActivity{

    @SuppressLint("SetJavaScriptEnabled")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pageview);

        Intent intent = getIntent();
        String menuName = intent.getStringExtra("menuName");
        String menuUrl = intent.getStringExtra("menuUrl");

        TextView tvMenuName = (TextView)findViewById(R.id.tvMenuName);
        TextView tvMenuUrl = (TextView)findViewById(R.id.tvMenuUrl);

        tvMenuName.setText(menuName);
        tvMenuUrl.setText(menuUrl);

        //レイアウトで指定したWebViewのIDを指定する。
        WebView myWebView = (WebView)findViewById(R.id.webView1);

        //リンクをタップしたときに標準ブラウザを起動させない
        myWebView.setWebViewClient(new WebViewClient());

        //ページの表示
        myWebView.loadUrl(menuUrl);

        //javascriptを許可する
        myWebView.getSettings().setJavaScriptEnabled(true);
    }

    public void onBackButtonClick(View view) {
        finish();
    }

}
