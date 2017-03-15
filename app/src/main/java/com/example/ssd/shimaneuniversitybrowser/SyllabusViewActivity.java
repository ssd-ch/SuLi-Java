package com.example.ssd.shimaneuniversitybrowser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class SyllabusViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_syllabus_view);

        setTitle(R.string.title_syllabus_view);

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

                return null;
            }
        }

        public void onPostExecute(Elements result) {

            if(result==null){
                new AlertDialog.Builder(SyllabusViewActivity.this)
                        .setTitle(R.string.error_title_data_response)
                        .setMessage(R.string.error_message_response_refuse)
                        .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int which) {
                                //okボタンが押された時の処理
                                SyllabusViewActivity.this.finish();
                            }
                        })
                        .show();

            }else {

                ListView listview = (ListView) parent_activity.findViewById(R.id.listView1);

                List<SectionHeaderData> sectionList = new ArrayList<SectionHeaderData>();
                List<List<SectionRowData>> rowList = new ArrayList<List<SectionRowData>>();

                //基本データ
                sectionList.add(new SectionHeaderData(getResources().getString(R.string.syllabus_basic), ""));
                List<SectionRowData> sectionDatalist = new ArrayList<SectionRowData>();
                Elements tdData = result.select("table.normal").get(0).select("td");
                Elements thData = result.select("table.normal").get(0).select("th");
                for (int i = 0; i < tdData.size(); i++) {
                    sectionDatalist.add(new SectionRowData(thData.get(i).text(), tdData.get(i).text(), ""));
                }
                rowList.add(sectionDatalist);

                //講義内容
                sectionList.add(new SectionHeaderData(getResources().getString(R.string.syllabus_contents), ""));
                List<SectionRowData> sectionDatalist2 = new ArrayList<SectionRowData>();
                Elements tdData2 = result.select("table.normal").get(1).select("td");
                Elements thData2 = result.select("table.normal").get(1).select("th");
                final String BR = System.getProperty("line.separator");
                for (int i = 0; i < tdData2.size(); i++) {
                    String str = tdData2.get(i).html().replaceAll("<br>", BR);
                    sectionDatalist2.add(new SectionRowData(thData2.get(i).text(), str.replaceAll("<.+?>",""),""));
                }
                rowList.add(sectionDatalist2);

                //担当教員
                sectionList.add(new SectionHeaderData(getResources().getString(R.string.syllabus_teacher_list), ""));
                Elements tdData3 = result.select("table.normal").get(2).select("td");
                List<SectionRowData> sectionDatalist3 = new ArrayList<SectionRowData>();
                for (int i = 0; i < tdData3.size(); i += 2) {
                    sectionDatalist3.add(new SectionRowData(tdData3.get(i).text(), tdData3.get(i + 1).text(), ""));
                }
                rowList.add(sectionDatalist3);


                CustomSectionListAdapter adapter = new CustomSectionListAdapter(parent_activity, sectionList, rowList);
                listview.setAdapter(adapter);
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
                //holder.labelTxt.setTextColor(Color.parseColor(rowData.color));
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
