package com.example.ssd.shimaneuniversitybrowser;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class CancellationInfoActivity extends AppCompatActivity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancellation_info);

        setTitle(R.string.title_cancellation_info);

        //レイアウトで指定したWebViewのIDを指定する。
        WebView myWebView = (WebView) findViewById(R.id.webView1);

        //リンクをタップしたときに標準ブラウザを起動させない
        myWebView.setWebViewClient(new WebViewClient());

        //ページを表示する。
        myWebView.loadUrl("http://www.kougi.shimane-u.ac.jp/selectweb/");

        //javascriptを許可する
        myWebView.getSettings().setJavaScriptEnabled(true);
    }

}