package com.example.ssd.shimaneuniversitybrowser;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyllabusSearchResultActivity extends AppCompatActivity {

    private ListView listview;
    private View footer;
    private String[] searchData;
    private int search_cnt;  //検索結果の全件数
    private int s_cnt;  //現在読み込んだ件数
    private boolean road_limiter; //true:読み込みを行わない false:読み込みを行う
    private List<Map<String, String>> List;
    private SyllabusSearchListReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_syllabus_search_result);

        setTitle(R.string.title_syllabus_search_result);

        //初期化
        List = new ArrayList<Map<String, String>>();
        road_limiter = false;
        s_cnt = 0;

        listview = (ListView)findViewById(R.id.listView);
        footer = getLayoutInflater().inflate(R.layout.list_footer_roading, null);

        Intent intent = getIntent();
        searchData = new String[10];
        for(int i = 0; i< searchData.length; i++){
            searchData[i] = intent.getStringExtra("data"+i);
        }

        String[] from = {"class_name", "teacher"};
        int[] to = {android.R.id.text1, android.R.id.text2};
        SimpleAdapter adapter = new SimpleAdapter(SyllabusSearchResultActivity.this, List, android.R.layout.simple_list_item_2, from, to);

        listview.setAdapter(adapter);
        listview.addFooterView(footer);
        listview.setOnItemClickListener(new ListItemClickListener());
        listview.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // 最後までスクロールされたかどうかの判定
                if (totalItemCount == firstVisibleItem + visibleItemCount) {
                    // ここに次の数件を取得して表示する処理を書けばいい
                    if((receiver!=null && receiver.getStatus()==AsyncTask.Status.RUNNING) || road_limiter)
                        return; //スキップ処理
                    //新しいタスクを生成
                    receiver = new SyllabusSearchListReceiver();
                    //非同期処理を行う 引数はURLとセレクトタグ
                    receiver.execute("http://gakumuweb1.shimane-u.ac.jp/shinwa/SYOutsideReferSearchList", "body tr");
                }
            }
        });
    }

    private class ListItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Map<String, String> item = (Map<String, String>) parent.getItemAtPosition(position);
            String url = item.get("url");

            Intent intent = new Intent(SyllabusSearchResultActivity.this, SyllabusViewActivity.class);
            intent.putExtra("url", url);
            startActivity(intent);
        }
    }

    private class SyllabusSearchListReceiver extends AsyncTask<String, String, Elements> {

        public SyllabusSearchListReceiver() {
        }

        @Override
        public Elements doInBackground(String... params) {

            String acceptURL ;
            final String charset = "Shift_JIS";

            try{
                acceptURL = params[0]
                        + "?nendo=" + searchData[0]
                        + "&j_s_cd=" + searchData[1]
                        + "&kamokud_cd=" + searchData[2]
                        + "&j_name=" + URLEncoder.encode(searchData[3], charset)
                        + "&j_name_eng=" + URLEncoder.encode(searchData[4], charset)
                        + "&keyword=" + URLEncoder.encode(searchData[5], charset)
                        + "&tantonm=" + URLEncoder.encode(searchData[6], charset)
                        + "&yobi=" + searchData[7]
                        + "&jigen=" + searchData[8]
                        + "&disp_cnt=" + searchData[9]
                        + "&s_cnt=" + String.valueOf(s_cnt);

                StringBuilder sb = new StringBuilder();

                URL url = new URL(acceptURL);
                // 接続
                URLConnection uc = url.openConnection();
                // HTMLを読み込む
                BufferedInputStream bis = new BufferedInputStream(uc.getInputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(bis, charset));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();

                Document document = Jsoup.parse(sb.toString(),params[0]);
                Elements elements = document.select(params[1]);
                if(elements.size() >= 1) {
                    String cnt_text = document.select("p").get(0).text();
                    search_cnt = Integer.parseInt(cnt_text.substring(cnt_text.indexOf("｜　全") + 3, cnt_text.indexOf("件　｜")));
                }
                return elements;

            }catch (Exception e){

                return null;
            }

        }

        public void onPostExecute(Elements result) {

            if(result==null){
                listview.removeFooterView(footer); //フッターの削除
                new AlertDialog.Builder(SyllabusSearchResultActivity.this)
                        .setTitle(R.string.error_title_data_response)
                        .setMessage(R.string.error_message_response_refuse)
                        .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int which) {
                                //okボタンが押された時の処理
                                SyllabusSearchResultActivity.this.finish();
                            }
                        })
                        .show();
            }
            else if(result.size()<1) {
                listview.removeFooterView(footer); //フッターの削除
                new AlertDialog.Builder(SyllabusSearchResultActivity.this)
                        .setTitle(R.string.dialog_title_search_result)
                        .setMessage(R.string.dialog_message_search_result_syllabus)
                        .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int which) {
                                //okボタンが押された時の処理
                                SyllabusSearchResultActivity.this.finish();
                            }
                        })
                        .show();
            }else {

                SyllabusSearchResultActivity.this.setTitle(
                        getResources().getString(R.string.title_syllabus_search_result) + " "
                                +search_cnt //検索結果件数の数字
                                +getResources().getString(R.string.results) //単位（件)
                );

                try {

                    for (int i = 1; i < result.size(); i++) {
                        Map<String, String> menu = new HashMap<String, String>();
                        Elements elements = result.get(i).select("td");
                        int position = elements.get(2).text().indexOf("／") - 1; //英語名はカット
                        if (position < 0) position = elements.get(2).text().length();
                        menu.put("class_name", elements.get(2).text().substring(0, position));
                        menu.put("teacher", elements.get(3).text().replaceAll("　　", "　").replaceAll("，　", "，"));
                        menu.put("url", elements.select("a").attr("abs:href"));
                        List.add(menu);
                        s_cnt++;
                    }

                    listview.invalidateViews(); //再描画

                    if(s_cnt >= search_cnt){

                        road_limiter = true; //次回からはこれ以上読み込まないように設定
                        listview.removeFooterView(footer); //フッターの削除
                    }


                }catch (Exception e){
                    new AlertDialog.Builder(SyllabusSearchResultActivity.this)
                            .setTitle(R.string.error_title_data_response)
                            .setMessage(R.string.error_message_data_invalid)
                            .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener(){
                                public void onClick(DialogInterface dialog, int which) {
                                    //okボタンが押された時の処理
                                    SyllabusSearchResultActivity.this.finish();
                                }
                            })
                            .show();
                }

            }
        }

    }
}
