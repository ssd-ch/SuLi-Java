package com.example.ssd.shimaneuniversitybrowser;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.design.widget.TabLayout;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.util.Log;
import android.annotation.SuppressLint;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PageView extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    @SuppressLint("SetJavaScriptEnabled")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pageview);

        Intent intent = getIntent();
        String menuName = intent.getStringExtra("menuName");

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);

        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            //pagetitleに指定した文字をページ名として追加する
            private String[] pagetitle = {"MON","TUE","WED","THU","FRI"};

            @Override
            public Fragment getItem(int position) {
                return PageFragment.newInstance(position + 1);
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return pagetitle[position];
            }

            @Override
            public int getCount() {
                return pagetitle.length;
            }
        };

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(this);

        //オートマチック方式: これだけで両方syncする
        tabLayout.setupWithViewPager(viewPager);

        TextView tvMenuName = (TextView)findViewById(R.id.tvMenuName);
        tvMenuName.setText(menuName);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        //Log.d("MainActivity", "onPageSelected() position="+position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public static class PageFragment extends Fragment {

        private Activity parentActivity;

        public PageFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            parentActivity = getActivity();
        }

        public static PageFragment newInstance(int page) {
            Bundle args = new Bundle();
            args.putInt("page", page);
            PageFragment fragment = new PageFragment();
            fragment.setArguments(args);
            return fragment;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            //データを書き込むビューを取得
            View view = inflater.inflate(R.layout.pageview_content, container, false);

            //コンストラクタの引数に表示させるパーツなどを指定
            HTMLPageReceiver receiver = new HTMLPageReceiver(view);

            //非同期処理を行う
            receiver.execute("");

        /*
            //レイアウトで指定したWebViewのIDを指定する。
            WebView myWebView = (WebView)findViewById(R.id.webView1);

            //リンクをタップしたときに標準ブラウザを起動させない
            myWebView.setWebViewClient(new WebViewClient());

            //ページの表示
            myWebView.loadUrl(menuUrl);

            //javascriptを許可する
            myWebView.getSettings().setJavaScriptEnabled(true);
        */

            //Button btBackButton = (Button) view.findViewById(R.id.btBackButton);
            //btBackButton.setOnClickListener(new ButtonClickListener());

            return view;
        }

        //フラグメントに戻るボタンを実装する
        private class ButtonClickListener implements View.OnClickListener {
            @Override
            public void onClick(View view) {
                parentActivity.finish();
            }
        }

        private class HTMLPageReceiver extends AsyncTask<String, String, Elements> {

            private View _view;

            public HTMLPageReceiver(View view){
                _view = view;
            }

            @Override
            public Elements doInBackground(String... params) {

                //コンテキストからURL引き継ぎ
                Intent intent = parentActivity.getIntent();
                String url = intent.getStringExtra("menuUrl"); //URL
                String select = ".body tr"; //select tag
                try {
                    Document doc = Jsoup.connect(url).get();
                    Elements data = doc.select(select);
                    return data;
                } catch (Exception e) {
                    Toast.makeText(parentActivity, "取得失敗", Toast.LENGTH_LONG).show();
                    return null;
                }
            }

            public void onPostExecute(Elements result) {

                int page = getArguments().getInt("page", 0);

                //コンテキストからURL引き継ぎ
                Intent intent = parentActivity.getIntent();
                String url = intent.getStringExtra("menuUrl"); //URL

                ((TextView) _view.findViewById(R.id.page_text)).setText("Page " + page);
                ((TextView) _view.findViewById(R.id.tvMenuUrl)).setText(url);

                TableLayout tablelayout = (TableLayout)_view.findViewById(R.id.TableLayout);

                List<Map<String, String>> menuList = new ArrayList<Map<String, String>>();
                int i = 0;
                for(Element element : result){
                    Map<String, String> menu = new HashMap<String, String>();
                    menu.put("name", element.text().replace("教室配当表_",""));
                    menu.put("url", element.attr("abs:href"));
                    menuList.add(menu);

                    // 行を追加
                    parentActivity.getLayoutInflater().inflate(R.layout.pageview_tablelow, tablelayout);
                    // 文字設定
                    TableRow tr = (TableRow)tablelayout.getChildAt(i++);
                    ((TextView)(tr.getChildAt(0))).setText(element.text());
                }

            }
        }
    }

}
