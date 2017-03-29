package com.example.ssd.shimaneuniversitybrowser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.design.widget.TabLayout;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ClassroomListActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classroom_lists);

        Intent intent = getIntent();
        String building_name = intent.getStringExtra("building_name");

        setTitle(building_name);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);

        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            //pageTitleに指定した文字をページ名として追加する
            private String[] pageTitle = getResources().getStringArray(R.array.tab_weekday);

            @Override
            public Fragment getItem(int position) {
                return PageFragment.newInstance(position);
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return pageTitle[position];
            }

            @Override
            public int getCount() {
                return pageTitle.length;
            }
        };

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(this);

        //オートマチック方式: これだけで両方syncする
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        //Log.d("MainActivity", "onPageSelected() position="+position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public static class PageFragment extends Fragment {

        private Activity parentActivity;

        public PageFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            parentActivity = getActivity();
        }

        public static PageFragment newInstance(int page) {
            Bundle args = new Bundle();
            args.putInt("page", page);
            PageFragment fragment = new PageFragment();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            //データを書き込むビューを取得
            View view = inflater.inflate(R.layout.classroom_lists_content, container, false);

            Intent intent = parentActivity.getIntent();
            String id = intent.getStringExtra("id");

            ListView listview = (ListView) view.findViewById(R.id.listView);

            int page = getArguments().getInt("page", 0);

            DBAdapter dbAdapter = new DBAdapter(parentActivity);
            dbAdapter.openDB();

            List<SectionHeaderData> sectionList = new ArrayList<SectionHeaderData>();
            List<List<SectionRowData>> rowList = new ArrayList<List<SectionRowData>>();

            try {

                String[] times = getResources().getStringArray(R.array.time_period); //コマ
                String[] times_m = getResources().getStringArray(R.array.time_period_m); //時間（時、分）

                Boolean DataExists = false; //データが存在するかどうかのフラグ　ない場合はアクティビティ終了

                for (int i = 0; i < times.length; i++) { //1から7コマのループ
                    Cursor cursor = dbAdapter.searchDB("ClassroomDivide", null,
                            "building_id = ? and weekday = ? and time = ?", new String[]{id,String.valueOf(page),String.valueOf(i+1)});
                    if(cursor.moveToFirst()) {
                        DataExists = true;

                        int col_place = cursor.getColumnIndex("place");
                        int col_text = cursor.getColumnIndex("cell_text");
                        int col_color = cursor.getColumnIndex("cell_color");

                        sectionList.add(new SectionHeaderData(times[i], times_m[i]));
                        List<SectionRowData> sectionDataList = new ArrayList<SectionRowData>();
                        do { //部屋のループ
                            sectionDataList.add(new SectionRowData(cursor.getString(col_text), cursor.getString(col_place), cursor.getString(col_color)));
                        } while (cursor.moveToNext());
                        rowList.add(sectionDataList);
                    }
                    cursor.close();
                }

                if(!DataExists){ //データが存在しなかった場合
                    new AlertDialog.Builder(parentActivity)
                            .setTitle(intent.getStringExtra("building_name"))
                            .setMessage(R.string.dialog_message_search_result_notExists)
                            .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //okボタンが押された時の処理
                                    parentActivity.finish();
                                }
                            })
                            .show();
                }

                CustomSectionListAdapter adapter = new CustomSectionListAdapter( parentActivity, sectionList, rowList);
                listview.setAdapter(adapter);
                //listview.setOnItemClickListener(new MainActivity.ListItemClickListener());

            }catch (Exception e){
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
            }finally {
                dbAdapter.closeDB();
            }

            return view;
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
                holder.labelTxt.setTextColor(Color.parseColor(rowData.color));
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
