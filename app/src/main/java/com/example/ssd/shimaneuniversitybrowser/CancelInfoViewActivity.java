package com.example.ssd.shimaneuniversitybrowser;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CancelInfoViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancel_info_view);
        setTitle(R.string.title_cancellation_info);

        Intent intent = getIntent();
        String id = intent.getStringExtra("id");

        DBAdapter dbAdapter = new DBAdapter(this);
        dbAdapter.openDB();
        Cursor cursor = dbAdapter.searchDB("CancelInfo", null, "_id = ?", new String[]{id});

        if (cursor.moveToFirst()) {

            ListView listView = (ListView) findViewById(R.id.listView1);

            List<SectionHeaderData> sectionList = new ArrayList<SectionHeaderData>();
            List<List<SectionRowData>> rowList = new ArrayList<List<SectionRowData>>();

            String[] section = getResources().getStringArray(R.array.cancel_info_section);

            String[] col_text = new String[7];
            col_text[0] = cursor.getString(cursor.getColumnIndex("classname"));
            col_text[1] = cursor.getString(cursor.getColumnIndex("date")) + " " + cursor.getString(cursor.getColumnIndex("time")) + "限";
            col_text[2] = cursor.getString(cursor.getColumnIndex("classification"));
            col_text[3] = cursor.getString(cursor.getColumnIndex("department"));
            col_text[4] = cursor.getString(cursor.getColumnIndex("person"));
            col_text[5] = cursor.getString(cursor.getColumnIndex("place"));
            col_text[6] = cursor.getString(cursor.getColumnIndex("note"));

            for (int i = 0; i < section.length; i++) {
                sectionList.add(new SectionHeaderData(section[i], ""));
                List<SectionRowData> sectionDataList = new ArrayList<SectionRowData>();
                sectionDataList.add(new SectionRowData(col_text[i], "", ""));
                rowList.add(sectionDataList);
            }

            CustomSectionListAdapter adapter = new CustomSectionListAdapter(this, sectionList, rowList);
            listView.setAdapter(adapter);

        } else {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.error_title_data_response)
                    .setMessage(R.string.error_message_response_refuse)
                    .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //okボタンが押された時の処理
                            finish();
                        }
                    })
                    .show();
        }

        cursor.close();
        dbAdapter.closeDB();
    }

    private class CustomSectionListAdapter extends BaseSectionAdapter<SectionHeaderData, SectionRowData> {

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
