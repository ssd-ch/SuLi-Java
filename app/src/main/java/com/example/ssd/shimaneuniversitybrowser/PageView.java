package com.example.ssd.shimaneuniversitybrowser;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
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
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
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
            private String[] pagetitle = {"月","火","水","木","金"};

            @Override
            public Fragment getItem(int position) {
                return PageFragment.newInstance(position);
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

                //((TextView) _view.findViewById(R.id.page_text)).setText("Page " + page);
                //((TextView) _view.findViewById(R.id.tvMenuUrl)).setText(url);
                ListView listview = (ListView) _view.findViewById(R.id.listView);

                List<SectionHeaderData> sectionList = new ArrayList<SectionHeaderData>();
                List<List<SectionRowData>> rowList = new ArrayList<List<SectionRowData>>();

                String[] places = new String[result.get(2).select("td").size()-2];
                String[] times = {"8:30〜10:00","10:15〜11:45","12:45〜14:15","14:30〜16:00","16:15〜17:45"};
                int k = 0,l = 0;
                for(int i=1;i<result.get(0).select("td").size();i++){
                    Element td = result.get(0).select("td").get(i);
                    for(int j=0;j< (td.attr("colspan").equals("") ? 1 : Integer.parseInt(td.attr("colspan")));j++){
                        if(td.attr("rowspan").equals(""))
                            places[l++] = td.text() +"_"+ result.get(1).select("td").get(k++).text();
                        else places[l++] = td.text();
                    }
                }
                for(int i = 0; i < places.length; i++){
                    places[i] = places[i].replaceAll("_"," "); //_を空白にする
                    places[i] = places[i].replaceAll("　"," "); //全角空白を半角空白にする
                    places[i] = StringUtil.fullWidthNumberToHalfWidthNumber(places[i]); //全角数字を半角数字に変換する
                }

                int point = page * 5 + 2 ; //trタグの位置
                for(int i = 0; i < 5 ; i++){ //1から5コマのループ
                    sectionList.add(new SectionHeaderData( (i*2+1) + "." + (i*2+2) + "限 (" + (i+1) +"コマ)", times[i]));
                    List<SectionRowData> sectionDatalist = new ArrayList<SectionRowData>();
                    Elements tdData = result.get(point + i).select("td");
                    for(int pn=0;pn<places.length;pn++) { //場所のループ
                        String text = tdData.get(i==0?pn+2:pn+1).text(); //一番最初は曜日名が入るので一つずらす

                        String color = "#000000";
                        if(text.equals(String.valueOf('\u00A0'))||text.equals("")||text.equals("¥n")) { //"",&nbsp;改行のみ
                            text = "";
                        }
                        else{
                            String style = tdData.get(i==0?pn+2:pn+1).select("span").attr("style");
                            int m = style.indexOf("#");
                            if(m >= 0) color = style.substring(m, m+7);
                        }
                        sectionDatalist.add(new SectionRowData( text, places[pn], color));
                    }
                    rowList.add(sectionDatalist);
                }

                CustomSectionListAdapter adapter = new CustomSectionListAdapter(parentActivity, sectionList, rowList);
                listview.setAdapter(adapter);

                //listview.setOnItemClickListener(new MainActivity.ListItemClickListener());

            }
        }

        public class CustomSectionListAdapter extends BaseSectionAdapter<SectionHeaderData, SectionRowData> {

            public CustomSectionListAdapter(Context context, List<SectionHeaderData> sectionList, List<List<SectionRowData>> rowList) {
                super(context, sectionList, rowList);
            }

            @Override
            public View viewForHeaderInSection(View convertView, int section) {
                ListHeaderViewHolder holder = null;
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.pageview_list_header, null);
                    holder = new ListHeaderViewHolder();
                    holder.titleTxt = (TextView) convertView.findViewById(R.id.titleTxt);
                    holder.subtitleTxt = (TextView) convertView.findViewById(R.id.subtitleTxt);
                    convertView.setTag(holder);
                } else {
                    holder = (ListHeaderViewHolder) convertView.getTag();
                }
                SectionHeaderData headerData = sectionList.get(section);
                holder.titleTxt.setText(headerData.title);
                holder.subtitleTxt.setText(headerData.subTitle);
                return convertView;
            }

            @Override
            public View cellForRowAtIndexPath(View convertView, IndexPath indexPath) {
                ListRowViewHolder holder = null;
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.pageview_list_row, null);
                    holder = new ListRowViewHolder();
                    holder.labelTxt = (TextView) convertView.findViewById(R.id.labelTxt);
                    holder.valueTxt = (TextView) convertView.findViewById(R.id.valueTxt);
                    convertView.setTag(holder);
                } else {
                    holder = (ListRowViewHolder) convertView.getTag();
                }
                SectionRowData rowData = rowList.get(indexPath.section).get(indexPath.row);
                holder.labelTxt.setText(rowData.label);
                holder.labelTxt.setTextColor(Color.parseColor(rowData.color));
                holder.valueTxt.setText(rowData.value);
                return convertView;
            }

            class ListHeaderViewHolder {
                TextView titleTxt;
                TextView subtitleTxt;
            }

            class ListRowViewHolder {
                TextView labelTxt;
                TextView valueTxt;
            }
        }
    }

}
