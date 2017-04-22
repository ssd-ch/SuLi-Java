package cf.ssdb.suli;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v4.app.AppLaunchChecker;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button NextButton1 = (Button) this.findViewById(R.id.button1);
        Button NextButton2 = (Button) this.findViewById(R.id.button2);
        Button NextButton3 = (Button) this.findViewById(R.id.button3);
        Button NextButton4 = (Button) this.findViewById(R.id.button4);

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
                Intent intent = new Intent(MainActivity.this, CancelInfoActivity.class);
                startActivity(intent);
            }
        });

        NextButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //データを取得し、データベースに登録を行う
                HTMLPageReceiver receiver = new HTMLPageReceiver(MainActivity.this);
                //非同期処理を行う
                receiver.execute("");
            }
        });

        if (!AppLaunchChecker.hasStartedFromLauncher(this)) {

            //データを取得し、データベースに登録を行う
            HTMLPageReceiver receiver = new HTMLPageReceiver(MainActivity.this);
            //非同期処理を行う
            receiver.execute("");

            //初回の起動をしたことを保存
            AppLaunchChecker.onActivityCreate(this);
        }

    }

    private class HTMLPageReceiver extends AsyncTask<String, String, Void> {

        private Activity parentActivity;
        private ProgressDialog m_ProgressDialog;
        private Elements[] tableData = null;
        private String UpdateDateText;
        private DBAdapter dbAdapter;

        private HTMLPageReceiver(Activity activity) {
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

            if (tableData == null) { //データが取得されていない

                //読み込みページ一覧を取得
                String BuildingUrl = "http://www.shimane-u.ac.jp/education/school_info/class_data/class_data01.html";
                String SyllabusFormUrl = "http://gakumuweb1.shimane-u.ac.jp/shinwa/SYOutsideReferSearchInput";
                String CancelInfoUrl = "http://www.kougi.shimane-u.ac.jp/selectweb/conduct_list.asp";

                //DBを開く
                dbAdapter.openDB();
                dbAdapter.allDelete("Building");
                dbAdapter.allDelete("SyllabusForm");
                dbAdapter.allDelete("CancelInfo");

                try {

                    /*
                        教室配当表のダウンロードと建物の登録
                     */
                    String[] column = {"_id", "building_name"};
                    dbAdapter.saveDB("Building", column, CreateBuildingData(BuildingUrl));

                    /*
                        シラバスの検索フォームのダウンロード、登録
                     */
                    String[] column2 = {"_id", "form", "display", "value"};
                    dbAdapter.saveDB("SyllabusForm", column2, CreateSyllabusFormData(SyllabusFormUrl));

                    /*
                        休講情報のダウンロード、登録
                     */
                    String[] column3 = {"date", "classification", "time", "department", "classname", "person", "place", "note"};
                    dbAdapter.saveDB("CancelInfo", column3, CreateCancelInfo(CancelInfoUrl));

                } catch (Exception e) {
                    tableData = null;
                } finally {
                    dbAdapter.closeDB();
                }
            }

            return null;
        }

        @Override
        public void onPostExecute(Void none) {

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

                    String[] column = {"_id", "building_id", "place", "weekday", "time", "cell_text", "cell_color", "classname", "person", "department", "class_code"};

                    dbAdapter.saveDB("ClassroomDivide", column, CreatePlaceData(tableData));

                    ((TextView) parentActivity.findViewById(R.id.updateDateText)).setText(UpdateDateText);

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
                } finally {
                    //DBを閉じる
                    dbAdapter.closeDB();
                }
            }

            // プログレスダイアログを閉じる
            if (m_ProgressDialog != null && m_ProgressDialog.isShowing()) {
                m_ProgressDialog.dismiss();
            }

        }

        private String[][] CreatePlaceData(Elements[] tableData) throws Exception {

            int placeCount = 0;
            int otherCount = 0;
            for (Elements elements : tableData) {
                if (elements != null) {
                    //本データ
                    placeCount += elements.get(2).select("td").size() - 2;
                    //その他のデータ(下にある小さい表)
                    if (elements.size() > 27) {
                        for (int i = 27; i < elements.size(); i++) {
                            String[] tdText = {elements.get(i).select("td").get(0).text(), elements.get(i).select("td").get(1).text()};
                            if (!(tdText[0].equals(String.valueOf('\u00A0')) && tdText[1].equals(String.valueOf('\u00A0')))) {
                                otherCount++;
                            }
                        }
                    }
                }
            }

            String[][] value = new String[placeCount * 25 + otherCount][];
            int value_count = 0;

            for (int d = 0; d < 5; d++) { //月から金のループ
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

                                String[] L_Info = new String[4];

                                String color = "#000000";
                                if (text.equals(String.valueOf('\u00A0')) || text.equals("") || text.equals("¥n")) { //&nbsp;,"",改行のみ
                                    text = "";
                                } else {
                                    String style = tdData.get(i == 0 ? pn + 2 : pn + 1).select("span").attr("style");
                                    int m = style.indexOf("#");
                                    if (m >= 0) {
                                        color = style.substring(m, m + 7);
                                        L_Info = new String[]{"", "", "", ""};
                                    } else {
                                        L_Info = ExtractionLecture(text);
                                    }
                                }
                                String id = "" + (building_id < 10 ? "0" + building_id : building_id) + (pn < 10 ? "0" + pn : pn) + d + i;

                                value[value_count++] = new String[]{id, String.valueOf(building_id), places[pn], String.valueOf(d), String.valueOf(i + 1), text, color, L_Info[0], L_Info[1], L_Info[2], L_Info[3]};
                            }
                        }
                        building_id++;
                    }
                }
            }

            for (int building_id = 0; building_id < tableData.length; building_id++) { //建物のループ
                if (tableData[building_id] != null) {
                    if (tableData[building_id].size() > 27) {
                        String place_text = "";
                        String day_cache = "";
                        int pn = tableData[building_id].get(2).select("td").size() - 2;

                        for (int i = 27; i < tableData[building_id].size(); i++) {
                            String[] tdText = {
                                    tableData[building_id].get(i).select("td").get(0).text(),
                                    tableData[building_id].get(i).select("td").get(1).text(),
                                    tableData[building_id].get(i).select("td").get(2).text()};
                            if (tdText[0].equals(String.valueOf('\u00A0')) && tdText[1].equals(String.valueOf('\u00A0'))) {
                                place_text = StringUtil.fullWidthNumberToHalfWidthNumber(tdText[2].replaceAll("　", ""));
                            } else {
                                tdText[0] = tdText[0].replaceAll("[ 　" + String.valueOf('\u00A0') + "]", "");//全角半角&nbspスペースの排除
                                tdText[1] = tdText[1].replaceAll("[ 　" + String.valueOf('\u00A0') + "]", "");//全角半角&nbspスペースの排除
                                if (tdText[0].equals("")) tdText[0] = day_cache;
                                day_cache = tdText[0];
                                int day;
                                switch (tdText[0]) {
                                    case "月":
                                        day = 0;
                                        break;
                                    case "火":
                                        day = 1;
                                        break;
                                    case "水":
                                        day = 2;
                                        break;
                                    case "木":
                                        day = 3;
                                        break;
                                    case "金":
                                        day = 4;
                                        break;
                                    case "土":
                                        day = 5;
                                        break;
                                    case "日":
                                        day = 6;
                                        break;
                                    default:
                                        day = 99; //unknown
                                }
                                int time = Integer.valueOf(tdText[1].substring(tdText[1].indexOf(".") + 1)) / 2;//コマを格納
                                String id = "" + (building_id < 10 ? "0" + building_id : building_id) + (pn < 10 ? "0" + pn : pn) + day + time;
                                pn++;

                                String[] L_Info = ExtractionLecture(tdText[2]);
                                value[value_count++] = new String[]{id, String.valueOf(building_id), place_text, String.valueOf(day), String.valueOf(time), tdText[2], "#000000", L_Info[0], L_Info[1], L_Info[2], L_Info[3]};
                            }
                        }
                    }
                }
            }

            return value;
        }

        /**
         * 教室配当表を建物別に取得し、建物一覧を作成する
         *
         * @param url アクセスURL
         * @return result String[][]
         */
        private String[][] CreateBuildingData(String url) throws IOException {

            Document doc = Jsoup.connect(url).get();
            Elements urlList = doc.select(".body li a"); //そのまま:tag #:id .:class
            tableData = new Elements[urlList.size()];
            //String[] column = {"_id", "building_name"};
            String[][] result = new String[urlList.size()][2];
            for (int i = 0; i < urlList.size(); i++) {
                try {
                    result[i][0] = String.valueOf(i);
                    result[i][1] = urlList.get(i).text().replace("教室配当表_", "").replaceAll("_", " ");
                    Document doc_p = Jsoup.connect(urlList.get(i).attr("abs:href")).get();
                    tableData[i] = doc_p.select(".body tr");
                    if (i == 0)
                        UpdateDateText = doc_p.select(".body h2").get(0).text();
                } catch (Exception e) {
                    tableData[i] = null;
                }
            }

            return result;
        }

        /**
         * シラバスのフォームを取得する
         *
         * @param url アクセスURL
         * @return result String[][]
         */
        private String[][] CreateSyllabusFormData(String url) throws IOException {

            Elements FormData = Jsoup.connect(url).get().select("table");

            String year = FormData.select("[name=nendo]").attr("value"); //年度
            Elements options1 = FormData.select("[name=j_s_cd] option"); //学部
            Elements options2 = FormData.select("[name=kamokud_cd] option"); //科目分類
            Elements options3 = FormData.select("[name=yobi] option"); //曜日
            Elements options4 = FormData.select("[name=jigen] option"); //時限

            //String[] column = {"_id", "form", "display", "value"};

            String[][] result = new String[1 + options1.size() + options2.size() + options3.size() + options4.size()][4];
            int data_cnt = 0;

            result[data_cnt++] = new String[]{"0", "nendo", year, year};

            for (int k = 0; k < options1.size(); k++)
                result[data_cnt++] = new String[]{String.valueOf(k), "j_s_cd", options1.get(k).text(), options1.get(k).attr("value")};

            for (int k = 0; k < options2.size(); k++)
                result[data_cnt++] = new String[]{String.valueOf(k), "kamokud_cd", options2.get(k).text(), options2.get(k).attr("value")};

            for (int k = 0; k < options3.size(); k++)
                result[data_cnt++] = new String[]{String.valueOf(k), "yobi", options3.get(k).text(), options3.get(k).attr("value")};

            for (int k = 0; k < options4.size(); k++)
                result[data_cnt++] = new String[]{String.valueOf(k), "jigen", options4.get(k).text(), options4.get(k).attr("value")};

            return result;

        }

        /**
         * 休講情報を取得する
         *
         * @param url アクセスURL
         * @return result データが存在する場合はString[][]のデータ,0件の場合はnullを返す
         */
        private String[][] CreateCancelInfo(String url) throws IOException {

            ArrayList<Element> data = new ArrayList<Element>();

            int page_num = 1;
            boolean flg = true;
            do {
                Document document = Jsoup.connect(url).data("abspage", String.valueOf(page_num)).method(Connection.Method.POST).get();
                Elements ele = document.select(".table_data tr");
                for (int j = 1; j < ele.size(); j++) {
                    data.add(ele.get(j));
                }
                //そのまま:tag #:id .:class
                if (document.select(".prevnextpage").size() < (page_num++ == 1 ? 1 : 2))
                    flg = false;
            } while (flg);

            if (data.size() > 0) {
                String[][] result = new String[data.size()][];
                for (int i = 0; i < data.size(); i++) {
                    Elements tdData = data.get(i).select("td");
                    String[] str = new String[tdData.size()];
                    for (int j = 0; j < tdData.size(); j++) {
                        str[j] = tdData.get(j).text();
                    }
                    result[i] = str;
                }
                return result;
            } else {
                return null;
            }
        }

        /**
         * 授業情報のテーブルのセルのテキストから授業情報を抽出する
         *
         * @param text セルのテキスト
         * @return result String[4] (授業名、担当者名、担当者の所属、時間割コード)
         */
        private String[] ExtractionLecture(String text) {

            String[] result = {"", "", "", ""};

            final String d_code = "ＬＳＥＨＭＳＡＦＣ○*";

            text = text.replaceAll("、", " ");

            //授業名
            if (text.matches(".*『.*』.*")) {
                result[0] = StringUtil.matcherSubString(text, "『.*』").replaceAll("[『』]", "");
                text = text.replaceAll("『.*』", "");
            }
            //時間割コード
            if (text.matches(".*[A-Z0-9]{6}.*")) {
                result[3] = StringUtil.matcherSubString(text, "[A-Z0-9]{6,}");
                text = text.replaceAll("[A-Z0-9/]{6,}", "");
            }
            //担当者情報
            if (text.matches(".*[" + d_code + "].*")) {
                String t = StringUtil.matcherSubString(text, "[" + d_code + "]{1,2}[^" + d_code + "]*");
                result[2] = StringUtil.matcherSubString(t, "[" + d_code + "]{1,2}");
                result[1] = t.replaceAll("[" + d_code + "]", "");
            }

            for (int i = 0; i < result.length; i++) {
                result[i] = result[i].replaceAll("[　 ]", "");
            }

            //Log.d("DEBUG","授業名:"+result[0]+" 時間割コード:"+result[3]+" 担当者名:"+result[1]+" 担当者所属:"+result[2]);

            return result;
        }

    }
}
