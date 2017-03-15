package com.example.ssd.shimaneuniversitybrowser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class AllPlacesListActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_places_list);

        setTitle(R.string.title_all_places_list);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);

        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            //pageTitleに指定した文字をページ名として追加する
            private String[] pageTitle = getResources().getStringArray(R.array.tab_weekday);

            @Override
            public Fragment getItem(int position) {
                return PageFragment.newInstance(position);
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return pageTitle[position];
            }

            @Override
            public int getCount() {
                return pageTitle.length;
            }
        };

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(this);

        //オートマチック方式: これだけで両方syncする
        tabLayout.setupWithViewPager(viewPager);
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
        private static Elements[] tableData; //取得したデータ(static)

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

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            //データを書き込むビューを取得(ローディング画面を設定)
            View view = inflater.inflate(R.layout.classroom_lists_content, container, false);

            //コンストラクタの引数に表示させるパーツなどを指定
            HTMLPageReceiver receiver = new HTMLPageReceiver(view);

            //非同期処理を行う
            receiver.execute("");

            return view;
        }

        private class HTMLPageReceiver extends AsyncTask<String, String, Void> {

            private View _view;
            private ProgressDialog m_ProgressDialog;

            public HTMLPageReceiver(View view){
                _view = view;
            }

            @Override
            protected void onPreExecute() {

                if(tableData==null) {
                    // プログレスダイアログの生成
                    this.m_ProgressDialog = new ProgressDialog(parentActivity);
                    // プログレスダイアログの設定
                    this.m_ProgressDialog.setMessage(getResources().getString(R.string.message_now_loading));  // メッセージをセット
                    // プログレスダイアログの表示
                    this.m_ProgressDialog.show();
                }

                return;
            }

            @Override
            public Void doInBackground(String... params) {

                if(tableData == null) { //データが取得されていない

                    //読み込みページ一覧を取得
                    String url = "http://www.shimane-u.ac.jp/education/school_info/class_data/class_data01.html";
                    String select = ".body li a"; //select tag

                    //一覧のそれぞれの情報を取得
                    try {
                        Document doc = Jsoup.connect(url).get();
                        Elements urlList = doc.select(select);
                        tableData = new Elements[urlList.size()];
                        int i = 0;
                        for (Element element : urlList) {
                            try {
                                tableData[i] = Jsoup.connect(element.attr("abs:href")).get().select(".body tr");
                            } catch (Exception e) {
                                tableData[i] = null;
                            } finally {
                                i++;
                            }
                        }

                    } catch (Exception e) {
                        tableData = null;
                    }
                }

                return null;
            }

            public void onPostExecute(Void none) {

                // プログレスダイアログを閉じる
                if (m_ProgressDialog != null && m_ProgressDialog.isShowing()) {
                    m_ProgressDialog.dismiss();
                }

                if (tableData == null) {
                    new AlertDialog.Builder(parentActivity)
                            .setTitle(R.string.error_title_data_response)
                            .setMessage(R.string.error_message_response_refuse)
                            .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //okボタンが押された時の処理
                                    parentActivity.finish();
                                }
                            })
                            .show();
                } else {

                    int page = getArguments().getInt("page", 0);

                    ListView listview = (ListView) _view.findViewById(R.id.listView);

                    List<SectionHeaderData> sectionList = new ArrayList<SectionHeaderData>();
                    List<List<SectionRowData>> rowList = new ArrayList<List<SectionRowData>>();

                    try {

                        String[] times = getResources().getStringArray(R.array.time_separate);

                        int point = page * 5 + 2; //trタグの位置
                        for (int i = 0; i < times.length; i++) { //1から5コマのループ

                            sectionList.add(new SectionHeaderData((i * 2 + 1) + "." + (i * 2 + 2) + "限 (" + (i + 1) + "コマ)", times[i]));

                            List<SectionRowData> sectionDataList = new ArrayList<SectionRowData>();

                            for(Elements elements : tableData) { //建物のループ

                                if (elements != null) {
                                    String[] places = new String[elements.get(2).select("td").size() - 2];
                                    int k = 0, l = 0;
                                    for (int h = 1; h < elements.get(0).select("td").size(); h++) {
                                        Element td = elements.get(0).select("td").get(h);
                                        for (int j = 0; j < (td.attr("colspan").equals("") ? 1 : Integer.parseInt(td.attr("colspan"))); j++) {
                                            if (td.attr("rowspan").equals(""))
                                                places[l++] = td.text() + "_" + elements.get(1).select("td").get(k++).text();
                                            else places[l++] = td.text();
                                        }
                                    }
                                    for (int j = 0; j < places.length; j++) {
                                        places[j] = places[j].replaceAll("_", " "); //_を空白にする
                                        places[j] = places[j].replaceAll("　", " "); //全角空白を半角空白にする
                                        places[j] = StringUtil.fullWidthNumberToHalfWidthNumber(places[j]); //全角数字を半角数字に変換する
                                    }

                                    Elements tdData = elements.get(point + i).select("td");
                                    for (int pn = 0; pn < places.length; pn++) { //場所のループ
                                        String text = tdData.get(i == 0 ? pn + 2 : pn + 1).text(); //一番最初は曜日名が入るので一つずらす

                                        String color = "#000000";
                                        if (text.equals(String.valueOf('\u00A0')) || text.equals("") || text.equals("¥n")) { //"",&nbsp;改行のみ
                                            text = "";
                                        } else {
                                            String style = tdData.get(i == 0 ? pn + 2 : pn + 1).select("span").attr("style");
                                            int m = style.indexOf("#");
                                            if (m >= 0) color = style.substring(m, m + 7);
                                        }
                                        sectionDataList.add(new SectionRowData(text, places[pn], color));
                                    }
                                }
                            }

                            rowList.add(sectionDataList);

                        }

                        CustomSectionListAdapter adapter = new CustomSectionListAdapter(parentActivity, sectionList, rowList);
                        listview.setAdapter(adapter);

                        //listview.setOnItemClickListener(new MainActivity.ListItemClickListener());

                    } catch (Exception e) {
                        new AlertDialog.Builder(parentActivity)
                                .setTitle(R.string.error_title_data_response)
                                .setMessage(R.string.error_message_data_invalid)
                                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        //okボタンが押された時の処理
                                        parentActivity.finish();
                                    }
                                })
                                .show();
                    }
                }

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
                    convertView = inflater.inflate(R.layout.list_header, null);
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
                    convertView = inflater.inflate(R.layout.list_row, null);
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

