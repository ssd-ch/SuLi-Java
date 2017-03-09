package com.example.ssd.shimaneuniversitybrowser;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class SyllabusViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_syllabus_view);

        Intent intent = getIntent();
        String url = intent.getStringExtra("url");

        //コンストラクタの引数に表示させるパーツを指定
        SyllabusInfoReceiver receiver = new SyllabusInfoReceiver(this);
        //非同期処理を行う
        receiver.execute(url);
    }

    private class SyllabusInfoReceiver extends AsyncTask<String, String, Elements> {
        private Activity parent_activity;

        public SyllabusInfoReceiver(Activity activity){
            parent_activity = activity ;
        }

        @Override
        public Elements doInBackground(String... params) {
            String url = params[0]; //URL
            try {
                Document doc = Jsoup.connect(url).get();
                Elements data = doc.select("table");
                return data;
            } catch (Exception e) {
                Toast.makeText(SyllabusViewActivity.this, "取得失敗", Toast.LENGTH_LONG).show();
            }
            return null;
        }

        public void onPostExecute(Elements result) {
            ((TextView) parent_activity.findViewById(R.id.textView)).setText(result.text());
        }
    }
}
