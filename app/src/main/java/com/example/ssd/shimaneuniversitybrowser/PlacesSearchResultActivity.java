package com.example.ssd.shimaneuniversitybrowser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlacesSearchResultActivity extends AppCompatActivity {

    static Elements[] tableData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_search_result);

        setTitle(R.string.title_places_search_result);

        String[] searchData = new String[4];

        Intent intent = getIntent();
        for(int i = 0; i < searchData.length; i++) {
            searchData[i] = intent.getStringExtra("data" + i);
        }

        //コンストラクタの引数に表示させるパーツを指定
        PlacesSearchReceiver receiver = new PlacesSearchReceiver(this,searchData);
        //非同期処理を行う 引数はURLとセレクトタグ
        receiver.execute("");

    }

    private class ListItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Map<String, String> item = (Map<String, String>) parent.getItemAtPosition(position);
            String menuName = item.get("name");
            String menuUrl = item.get("url");

            Intent intent = new Intent(PlacesSearchResultActivity.this,ClassroomListActivity.class);
            intent.putExtra("menuName", menuName);
            intent.putExtra("menuUrl", menuUrl);
            startActivity(intent);
        }
    }

    private class PlacesSearchReceiver extends AsyncTask<String, String, Void> {

        private Activity parentActivity;
        private ProgressDialog m_ProgressDialog;
        private String[] searchData;

        public PlacesSearchReceiver(Activity activity, String[] data){
            parentActivity = activity;
            searchData = data;
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

                ListView listview = (ListView) parentActivity.findViewById(R.id.listView1);

                List<Map<String, String>> menuList = new ArrayList<Map<String, String>>();

                try{

                    String[] times = getResources().getStringArray(R.array.time_separate);

                    for(int p = 0; p < 5; p++) { //月曜から金曜のループ

                        int point = p * 5 + 2; //trタグの位置
                        for (int i = 0; i < times.length; i++) { //1から5コマのループ

                            for (Elements elements : tableData) { //建物のループ

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

                                    for (int pn = 0; pn < places.length; pn++) { //部屋のループ

                                        String text = tdData.get(i == 0 ? pn + 2 : pn + 1).text(); //一番最初は曜日名が入るので一つずらす
                                        if(text.equals("")) {
                                            Map<String, String> menu = new HashMap<String, String>();
                                            menu.put("name", "");
                                            menu.put("text", "");
                                            menuList.add(menu);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    String[] from = {"name", "text"};
                    int[] to = {android.R.id.text1, android.R.id.text2};
                    SimpleAdapter adapter = new SimpleAdapter(parentActivity, menuList, android.R.layout.simple_list_item_2, from, to);
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
}

