package cf.ssdb.suli;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CancelInfoActivity extends AppCompatActivity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancel_info);

        setTitle(R.string.title_cancellation_info);

        ListView listView = (ListView)findViewById(R.id.listView1);

        DBAdapter dbAdapter = new DBAdapter(this);
        dbAdapter.openDB();
        Cursor cursor = dbAdapter.getDB("CancelInfo", null);

        if(cursor.moveToFirst()){

            List<Map<String, String>> menuList = new ArrayList<Map<String, String>>();

            int id_index = cursor.getColumnIndex("_id");
            int name_index = cursor.getColumnIndex("classname");
            do {
                Map<String, String> menu = new HashMap<String, String>();
                menu.put("id", cursor.getString(id_index));
                menu.put("name", cursor.getString(name_index));
                menuList.add(menu);
            } while (cursor.moveToNext());

            String[] from = {"name"};
            int[] to = {android.R.id.text1};
            SimpleAdapter adapter = new SimpleAdapter(this, menuList, android.R.layout.simple_list_item_1, from, to);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new ListItemClickListener());

        }
        else{
            new AlertDialog.Builder(this)
                    .setTitle(R.string.error_title_data_response)
                    .setMessage(R.string.error_message_response_refuse)
                    .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener(){
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

    private class ListItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Map<String, String> item = (Map<String, String>) parent.getItemAtPosition(position);
            String menuUrl = item.get("id");

            Intent intent = new Intent(CancelInfoActivity.this,CancelInfoViewActivity.class);
            intent.putExtra("id", menuUrl);
            startActivity(intent);
        }
    }
}
