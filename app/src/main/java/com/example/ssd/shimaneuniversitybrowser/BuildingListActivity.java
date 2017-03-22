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
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildingListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_building_list);

        setTitle(R.string.title_building_list);

        ListView tvView = (ListView)findViewById(R.id.listView1);

        //コンストラクタの引数に表示させるパーツを指定
        BuildingListReceiver receiver = new BuildingListReceiver(tvView);
        //非同期処理を行う 引数はURLとセレクトタグ
        receiver.execute("http://www.shimane-u.ac.jp/education/school_info/class_data/class_data01.html",".body li a");

    }

    private class ListItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Map<String, String> item = (Map<String, String>) parent.getItemAtPosition(position);
            String menuName = item.get("name");
            String menuUrl = item.get("url");

            Intent intent = new Intent(BuildingListActivity.this,ClassroomListActivity.class);
            intent.putExtra("menuName", menuName);
            intent.putExtra("menuUrl", menuUrl);
            startActivity(intent);
        }
    }

    private class BuildingListReceiver extends AsyncTask<String, String, Elements> {
        private ListView _tvView;

        public BuildingListReceiver(ListView tvView){
            _tvView = tvView;
        }

        @Override
        public Elements doInBackground(String... params) {
            String url = params[0]; //URL
            String select = params[1]; //select tag
            try {
                Document doc = Jsoup.connect(url).get();
                Elements data = doc.select(select);
                return data;
            } catch (Exception e) {
                return null;
            }
        }

        public void onPostExecute(Elements result) {

            if(result==null){
                new AlertDialog.Builder(BuildingListActivity.this)
                        .setTitle(R.string.error_title_data_response)
                        .setMessage(R.string.error_message_response_refuse)
                        .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int which) {
                                //okボタンが押された時の処理
                                BuildingListActivity.this.finish();
                            }
                        })
                        .show();
            }
            else {

                List<Map<String, String>> menuList = new ArrayList<Map<String, String>>();

                try{

                    for (Element element : result) {
                        Map<String, String> menu = new HashMap<String, String>();
                        menu.put("name", element.text().replace("教室配当表_", "").replaceAll("_", " "));
                        menu.put("url", element.attr("abs:href"));
                        menuList.add(menu);
                    }

                    //String[] from = {"name", "url"};
                    //int[] to = {android.R.id.text1, android.R.id.text2};
                    String[] from = {"name"};
                    int[] to = {android.R.id.text1};
                    SimpleAdapter adapter = new SimpleAdapter(BuildingListActivity.this, menuList, android.R.layout.simple_list_item_1, from, to);
                    _tvView.setAdapter(adapter);

                    _tvView.setOnItemClickListener(new BuildingListActivity.ListItemClickListener());

                }catch(Exception e){
                    new AlertDialog.Builder(BuildingListActivity.this)
                            .setTitle(R.string.error_title_data_response)
                            .setMessage(R.string.error_message_data_invalid)
                            .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener(){
                                public void onClick(DialogInterface dialog, int which) {
                                    //okボタンが押された時の処理
                                    BuildingListActivity.this.finish();
                                }
                            })
                            .show();
                }
            }
        }


    }
}
