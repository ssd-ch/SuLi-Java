package com.example.ssd.shimaneuniversitybrowser;

import android.app.AlertDialog;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_syllabus_search_result);

        setTitle(R.string.title_syllabus_search_result);

        ListView listview = (ListView)findViewById(R.id.listView1);

        Intent intent = getIntent();
        String[] searchData = new String[10];
        for(int i = 0; i< searchData.length; i++){
            searchData[i] = intent.getStringExtra("data"+i);
        }

        //コンストラクタの引数に表示させるパーツを指定
        SyllabusSearchResultActivity.SyllabusSearchListReceiver receiver = new SyllabusSearchResultActivity.SyllabusSearchListReceiver(listview,searchData);
        //非同期処理を行う 引数はURLとセレクトタグ
        receiver.execute("http://gakumuweb1.shimane-u.ac.jp/shinwa/SYOutsideReferSearchList", "body tr");

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
        private ListView _view;
        private String[] _data;
        private String search_cnt;

        public SyllabusSearchListReceiver(ListView view, String[] data) {
            _view = view;
            _data = data;
        }

        @Override
        public Elements doInBackground(String... params) {

            String acceptURL ;
            final String charset = "Shift_JIS";

            try{
                acceptURL = params[0]
                        + "?nendo=" + _data[0]
                        + "&j_s_cd=" + _data[1]
                        + "&kamokud_cd=" + _data[2]
                        + "&j_name=" + URLEncoder.encode(_data[3], charset)
                        + "&j_name_eng=" + URLEncoder.encode(_data[4], charset)
                        + "&keyword=" + URLEncoder.encode(_data[5], charset)
                        + "&tantonm=" + URLEncoder.encode(_data[6], charset)
                        + "&yobi=" + _data[7]
                        + "&jigen=" + _data[8]
                        + "&disp_cnt=" + _data[9];

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
                String cnt_text = document.select("p").get(0).text();
                search_cnt = cnt_text.substring( cnt_text.indexOf("｜　全")+3, cnt_text.indexOf("件　｜"));
                return document.select(params[1]);

            }catch (Exception e){

                return null;
            }

        }

        public void onPostExecute(Elements result) {

            if(result==null){
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
                new AlertDialog.Builder(SyllabusSearchResultActivity.this)
                        .setTitle(R.string.dialog_title_search_result)
                        .setMessage(R.string.dialog_message_search_result)
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

                    List<Map<String, String>> List = new ArrayList<Map<String, String>>();
                    for (int i = 1; i < result.size(); i++) {
                        Map<String, String> menu = new HashMap<String, String>();
                        Elements elements = result.get(i).select("td");
                        int position = elements.get(2).text().indexOf("／") - 1; //英語名はカット
                        if (position < 0) position = elements.get(2).text().length();
                        menu.put("class_name", elements.get(2).text().substring(0, position));
                        menu.put("teacher", elements.get(3).text().replaceAll("　　", "　").replaceAll("，　", "，"));
                        menu.put("url", elements.select("a").attr("abs:href"));
                        List.add(menu);
                    }


                    String[] from = {"class_name", "teacher"};
                    int[] to = {android.R.id.text1, android.R.id.text2};
                    SimpleAdapter adapter = new SimpleAdapter(SyllabusSearchResultActivity.this, List, android.R.layout.simple_list_item_2, from, to);
                    _view.setAdapter(adapter);
                    _view.addFooterView(getLayoutInflater().inflate(R.layout.list_footer_roading, null));
                    _view.setOnItemClickListener(new SyllabusSearchResultActivity.ListItemClickListener());

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
