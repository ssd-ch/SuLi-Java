package com.example.ssd.shimaneuniversitybrowser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button NextButton1 = (Button) this.findViewById(R.id.button1);
        Button NextButton2 = (Button) this.findViewById(R.id.button2);
        Button NextButton3 = (Button) this.findViewById(R.id.button3);

        NextButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SyllabusFormActivity.class);
                startActivity(intent);
            }
        });

        NextButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PlacesSearchModeActivity.class);
                startActivity(intent);
            }
        });

        NextButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CancellationInfoActivity.class);
                startActivity(intent);
            }
        });

        //データを取得し、データベースに登録を行う
        HTMLPageReceiver receiver = new HTMLPageReceiver(this);
        //非同期処理を行う
        receiver.execute("");

    }

    private class HTMLPageReceiver extends AsyncTask<String, String, Void> {

        private Activity parentActivity;
        private ProgressDialog m_ProgressDialog;
        private Elements[] tableData = null;
        private DBAdapter dbAdapter;

        public HTMLPageReceiver(Activity activity){
            parentActivity = activity;
            dbAdapter = new DBAdapter(activity);
        }

        @Override
        protected void onPreExecute() {

            // プログレスダイアログの生成
            this.m_ProgressDialog = new ProgressDialog(parentActivity);
            // プログレスダイアログの設定
            this.m_ProgressDialog.setMessage(getResources().getString(R.string.message_now_loading));  // メッセージをセット
            // プログレスダイアログの表示
            this.m_ProgressDialog.show();

        }

        @Override
        public Void doInBackground(String... params) {

            if(tableData == null) { //データが取得されていない

                //読み込みページ一覧を取得
                String url = "http://www.shimane-u.ac.jp/education/school_info/class_data/class_data01.html";
                String select = ".body li a"; //select tag

                //DBを開く
                dbAdapter.openDB();
                dbAdapter.allDelete("Building");

                //一覧のそれぞれの情報を取得
                try {

                    Document doc = Jsoup.connect(url).get();
                    Elements urlList = doc.select(select);
                    tableData = new Elements[urlList.size()];
                    int i = 0;
                    for (Element element : urlList) {
                        try {
                            String name = element.text().replace("教室配当表_", "").replaceAll("_", " ");
                            String URL = element.attr("abs:href");

                            //DBへの登録
                            String[] column = {"_id", "building_name"};
                            String[] value = { String.valueOf(i), name};
                            dbAdapter.saveDB("Building", column, value);

                            tableData[i] = Jsoup.connect(URL).get().select(".body tr");
                        } catch (Exception e) {
                            tableData[i] = null;
                        } finally {
                            i++;
                        }
                    }

                } catch (Exception e) {
                    tableData = null;
                }finally {
                    dbAdapter.closeDB();
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

                try {

                    //DBを開く
                    dbAdapter.openDB();
                    dbAdapter.allDelete("ClassroomDivide");

                    for(int d = 0; d < 5; d++) { //月から金のループ
                        int point = d * 5 + 2; //trタグの位置
                        for (int i = 0; i < 5; i++) { //1から5コマのループ

                            int building_id = 0;
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
                                        //DBへの登録
                                        String[] column = {"_id","building_id","place","weekday","time","cell_text","cell_color"};
                                        String[] value = {"", String.valueOf(building_id++),places[pn],String.valueOf(d),String.valueOf(i),text,color};
                                        dbAdapter.saveDB("ClassroomDivide", column, value);
                                    }
                                }
                            }

                        }
                    }


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
                }finally {
                    //DBを閉じる
                    dbAdapter.closeDB();
                }
            }

        }
    }
}
