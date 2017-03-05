package com.example.ssd.shimaneuniversitybrowser;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView tvView = (ListView)findViewById(R.id.listView1);

        //コンストラクタの引数に表示させるパーツを指定
        PageListReceiver receiver = new PageListReceiver(tvView);
        //非同期処理を行う 引数はURLとセレクトタグ
        receiver.execute("http://www.shimane-u.ac.jp/education/school_info/class_data/class_data01.html",".body li a");

    }

    private class ListItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Map<String, String> item = (Map<String, String>) parent.getItemAtPosition(position);
            String menuName = item.get("name");
            String menuUrl = item.get("url");

            Intent intent = new Intent(MainActivity.this,PageView.class);
            intent.putExtra("menuName", menuName);
            intent.putExtra("menuUrl", menuUrl);
            startActivity(intent);
        }
    }

    private class PageListReceiver extends AsyncTask<String, String, Elements> {
        private ListView _tvView;

        public PageListReceiver(ListView tvView){
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
                Toast.makeText(MainActivity.this, "取得失敗", Toast.LENGTH_LONG).show();
            }
            return null;
        }

        public void onPostExecute(Elements result) {

            List<Map<String, String>> menuList = new ArrayList<Map<String, String>>();
            for(Element element : result){
                Map<String, String> menu = new HashMap<String, String>();
                menu.put("name", element.text().replace("教室配当表_","").replaceAll("_"," "));
                menu.put("url", element.attr("abs:href"));
                menuList.add(menu);
            }

            //String[] from = {"name", "url"};
            //int[] to = {android.R.id.text1, android.R.id.text2};
            String[] from = {"name"};
            int[] to = {android.R.id.text1};
            SimpleAdapter adapter = new SimpleAdapter(MainActivity.this, menuList, android.R.layout.simple_list_item_1, from, to);
            _tvView.setAdapter(adapter);

            _tvView.setOnItemClickListener(new ListItemClickListener());
        }
    }
}
