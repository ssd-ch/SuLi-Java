package cf.ssdb.suli;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class AllPlacesListActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_places_list);

        setTitle(R.string.title_all_places_list);

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

            ListView listview = (ListView) view.findViewById(R.id.listView);

            int page = getArguments().getInt("page", 0);

            DBAdapter dbAdapter = new DBAdapter(parentActivity);
            dbAdapter.openDB();

            List<SectionHeaderData> sectionList = new ArrayList<SectionHeaderData>();
            List<List<SectionRowData>> rowList = new ArrayList<List<SectionRowData>>();

            try {

                String[] times = getResources().getStringArray(R.array.time_period); //コマ
                String[] times_m = getResources().getStringArray(R.array.time_period_m); //時間（時、分）

                for (int i = 0; i < times.length; i++) { //1から5コマのループ
                    Cursor cursor = dbAdapter.searchDB("ClassroomDivide", null,
                            "weekday = ? and time = ?", new String[]{String.valueOf(page),String.valueOf(i+1)});
                    if(cursor.moveToFirst()) {
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
    }

}

