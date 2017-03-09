package com.example.ssd.shimaneuniversitybrowser;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.HashMap;

public class SyllabusFormActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_syllabus_form);

        //コンストラクタの引数に表示させるパーツを指定
        SyllabusFormActivity.SearchFormReceiver receiver = new SyllabusFormActivity.SearchFormReceiver(this);
        //非同期処理を行う
        receiver.execute("http://gakumuweb1.shimane-u.ac.jp/shinwa/SYOutsideReferSearchInput");
    }

    private class SearchFormReceiver extends AsyncTask<String, String, Elements> {
        private Activity parent_activity;

        public SearchFormReceiver(Activity activity){
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
                Toast.makeText(SyllabusFormActivity.this, "取得失敗", Toast.LENGTH_LONG).show();
            }
            return null;
        }

        public void onPostExecute(Elements result) {

            /*
                学部の項目設定
             */
            Elements options1 = result.select("[name=j_s_cd] option");

            final String[] values1 = new String[options1.size()]; //学部のvalueの値

            String[] departments = new String[options1.size()];
            for(int i = 0; i < options1.size(); i++){
                departments[i] = options1.get(i).text();
                values1[i] = options1.get(i).attr("value");
            }

            Spinner form1 = (Spinner)parent_activity.findViewById(R.id.form1);
            ArrayAdapter adapter1 = new ArrayAdapter( parent_activity, android.R.layout.simple_spinner_item, departments);
            form1.setAdapter(adapter1);
            adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            form1.setPromptId(R.string.syllabus_department);

            /*
                科目分類の項目設定
             */
            Elements options2 = result.select("[name=kamokud_cd] option");

            final String[] values2 = new String[options2.size()]; //科目分類のvalueの値

            String[] course = new String[options2.size()];
            for(int i = 0; i < options2.size(); i++){
                course[i] = options2.get(i).text();
                values2[i] = options2.get(i).attr("value");
            }

            Spinner form2 = (Spinner)parent_activity.findViewById(R.id.form2);
            ArrayAdapter adapter2 = new ArrayAdapter( parent_activity, android.R.layout.simple_spinner_item, course);
            form2.setAdapter(adapter2);
            adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            form2.setPromptId(R.string.syllabus_course);

            /*
                曜日の項目設定
             */
            Elements options3 = result.select("[name=yobi] option");

            final String[] values3 = new String[options3.size()]; //曜日のvalueの値

            String[] weekday = new String[options3.size()];
            for(int i = 0; i < options3.size(); i++){
                weekday[i] = options3.get(i).text();
                values3[i] = options3.get(i).attr("value");
            }

            Spinner form5 = (Spinner)parent_activity.findViewById(R.id.form5);
            ArrayAdapter adapter3 = new ArrayAdapter( parent_activity, android.R.layout.simple_spinner_item, weekday);
            form5.setAdapter(adapter3);
            adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            form5.setPromptId(R.string.syllabus_weekday);

            /*
                時限の項目設定
             */
            Elements options4 = result.select("[name=jigen] option");

            final String[] values4 = new String[options4.size()]; //時限のvalueの値

            String[] times = new String[options4.size()];
            for(int i = 0; i < options4.size(); i++){
                times[i] = options4.get(i).text();
                values4[i] = options4.get(i).attr("value");
            }

            Spinner form6 = (Spinner)parent_activity.findViewById(R.id.form6);
            ArrayAdapter adapter4 = new ArrayAdapter( parent_activity, android.R.layout.simple_spinner_item, times);
            form6.setAdapter(adapter4);
            adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            form6.setPromptId(R.string.syllabus_time);

            Button Button1 = (Button) parent_activity.findViewById(R.id.button_search);
            Button Button2 = (Button) parent_activity.findViewById(R.id.button_clear);
            Button1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Spinner spinner1 = (Spinner) parent_activity.findViewById(R.id.form1);
                    Spinner spinner2 = (Spinner) parent_activity.findViewById(R.id.form2);
                    EditText edittext3 = (EditText) parent_activity.findViewById(R.id.form3);
                    EditText edittext4 = (EditText) parent_activity.findViewById(R.id.form4);
                    Spinner spinner7 = (Spinner) parent_activity.findViewById(R.id.form5);
                    Spinner spinner8 = (Spinner) parent_activity.findViewById(R.id.form6);

                    Intent intent = new Intent(parent_activity, SyllabusSearchResultActivity.class);
                    intent.putExtra("data0", "2017");
                    intent.putExtra("data1", values1[spinner1.getSelectedItemPosition()]);
                    intent.putExtra("data2", values2[spinner2.getSelectedItemPosition()]);
                    intent.putExtra("data3", edittext3.getText().toString());
                    intent.putExtra("data4", "");
                    intent.putExtra("data5", "");
                    intent.putExtra("data6", edittext4.getText().toString());
                    intent.putExtra("data7", values3[spinner7.getSelectedItemPosition()]);
                    intent.putExtra("data8", values4[spinner8.getSelectedItemPosition()]);
                    intent.putExtra("data9", "100");
                    startActivity(intent);
                }
            });
            Button2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((Spinner) parent_activity.findViewById(R.id.form1)).setSelection(0);
                    ((Spinner) parent_activity.findViewById(R.id.form2)).setSelection(0);
                    ((EditText) parent_activity.findViewById(R.id.form3)).getEditableText().clear();
                    ((EditText) parent_activity.findViewById(R.id.form4)).getEditableText().clear();
                    ((Spinner) parent_activity.findViewById(R.id.form5)).setSelection(0);
                    ((Spinner) parent_activity.findViewById(R.id.form6)).setSelection(0);
                }
            });
        }
    }
}
