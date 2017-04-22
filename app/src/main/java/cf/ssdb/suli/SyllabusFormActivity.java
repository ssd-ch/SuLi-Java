package cf.ssdb.suli;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class SyllabusFormActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_syllabus_form);

        setTitle(R.string.title_syllabus_form);

        try {

            DBAdapter dbAdapter = new DBAdapter(this);
            dbAdapter.openDB();

            int display_index; //使い回す
            int value_index; //使い回す

            Cursor cursor0 = dbAdapter.searchDB("SyllabusForm", null, "form = ?", new String[]{"nendo"});
            cursor0.moveToFirst();
            final String year = cursor0.getString(cursor0.getColumnIndex("value"));
            cursor0.close();

            /*
                学部の項目設定
            */
            Cursor cursor1 = dbAdapter.searchDB("SyllabusForm", null, "form = ?", new String[]{"j_s_cd"});
            cursor1.moveToFirst();

            display_index = cursor1.getColumnIndex("display");
            value_index = cursor1.getColumnIndex("value");

            final String[] departments = new String[cursor1.getCount()]; //表示するテキスト
            final String[] values1 = new String[cursor1.getCount()]; //送信するvalueの値
            for (int i = 0; i < cursor1.getCount(); i++) {
                departments[i] = cursor1.getString(display_index);
                values1[i] = cursor1.getString(value_index);
                cursor1.moveToNext();
            }

            Spinner form1 = (Spinner) this.findViewById(R.id.form1);
            ArrayAdapter adapter1 = new ArrayAdapter(this, android.R.layout.simple_spinner_item, departments);
            form1.setAdapter(adapter1);
            adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            form1.setPromptId(R.string.syllabus_department);

            /*
                科目分類の項目設定
            */
            Cursor cursor2 = dbAdapter.searchDB("SyllabusForm", null, "form = ?", new String[]{"kamokud_cd"});
            cursor2.moveToFirst();

            display_index = cursor2.getColumnIndex("display");
            value_index = cursor2.getColumnIndex("value");

            final String[] course = new String[cursor2.getCount()]; //表示するテキスト
            final String[] values2 = new String[cursor2.getCount()]; //送信するvalueの値
            for (int i = 0; i < cursor2.getCount(); i++) {
                course[i] = cursor2.getString(display_index);
                values2[i] = cursor2.getString(value_index);
                cursor2.moveToNext();
            }

            Spinner form2 = (Spinner) this.findViewById(R.id.form2);
            ArrayAdapter adapter2 = new ArrayAdapter(this, android.R.layout.simple_spinner_item, course);
            form2.setAdapter(adapter2);
            adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            form2.setPromptId(R.string.syllabus_course);

            /*
                曜日の項目設定
            */
            Cursor cursor3 = dbAdapter.searchDB("SyllabusForm", null, "form = ?", new String[]{"yobi"});
            cursor3.moveToFirst();

            display_index = cursor3.getColumnIndex("display");
            value_index = cursor3.getColumnIndex("value");

            final String[] weekday = new String[cursor3.getCount()]; //表示するテキスト
            final String[] values3 = new String[cursor3.getCount()]; //送信するvalueの値
            for (int i = 0; i < cursor3.getCount(); i++) {
                weekday[i] = cursor3.getString(display_index);
                values3[i] = cursor3.getString(value_index);
                cursor3.moveToNext();
            }

            Spinner form5 = (Spinner) this.findViewById(R.id.form5);
            ArrayAdapter adapter3 = new ArrayAdapter(this, android.R.layout.simple_spinner_item, weekday);
            form5.setAdapter(adapter3);
            adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            form5.setPromptId(R.string.syllabus_weekday);

            /*
                時限の項目設定
            */
            Cursor cursor4 = dbAdapter.searchDB("SyllabusForm", null, "form = ?", new String[]{"jigen"});
            cursor4.moveToFirst();

            display_index = cursor4.getColumnIndex("display");
            value_index = cursor4.getColumnIndex("value");

            final String[] times = new String[cursor4.getCount()]; //表示するテキスト
            final String[] values4 = new String[cursor4.getCount()]; //送信するvalueの値
            for (int i = 0; i < cursor4.getCount(); i++) {
                times[i] = cursor4.getString(display_index);
                values4[i] = cursor4.getString(value_index);
                cursor4.moveToNext();
            }

            Spinner form6 = (Spinner) this.findViewById(R.id.form6);
            ArrayAdapter adapter4 = new ArrayAdapter(this, android.R.layout.simple_spinner_item, times);
            form6.setAdapter(adapter4);
            adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            form6.setPromptId(R.string.syllabus_time);

            dbAdapter.closeDB();

            Button Button1 = (Button) this.findViewById(R.id.button_search);
            Button Button2 = (Button) this.findViewById(R.id.button_clear);
            Button1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Spinner spinner1 = (Spinner) SyllabusFormActivity.this.findViewById(R.id.form1);
                    Spinner spinner2 = (Spinner) SyllabusFormActivity.this.findViewById(R.id.form2);
                    EditText edittext3 = (EditText) SyllabusFormActivity.this.findViewById(R.id.form3);
                    EditText edittext4 = (EditText) SyllabusFormActivity.this.findViewById(R.id.form4);
                    Spinner spinner7 = (Spinner) SyllabusFormActivity.this.findViewById(R.id.form5);
                    Spinner spinner8 = (Spinner) SyllabusFormActivity.this.findViewById(R.id.form6);

                    Intent intent = new Intent(SyllabusFormActivity.this, SyllabusResultActivity.class);
                    intent.putExtra("data0", year);
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
                    ((Spinner) SyllabusFormActivity.this.findViewById(R.id.form1)).setSelection(0);
                    ((Spinner) SyllabusFormActivity.this.findViewById(R.id.form2)).setSelection(0);
                    ((EditText) SyllabusFormActivity.this.findViewById(R.id.form3)).getEditableText().clear();
                    ((EditText) SyllabusFormActivity.this.findViewById(R.id.form4)).getEditableText().clear();
                    ((Spinner) SyllabusFormActivity.this.findViewById(R.id.form5)).setSelection(0);
                    ((Spinner) SyllabusFormActivity.this.findViewById(R.id.form6)).setSelection(0);
                }
            });

        }catch(Exception e){
            new AlertDialog.Builder(SyllabusFormActivity.this)
                    .setTitle(R.string.error_title_data_response)
                    .setMessage(R.string.error_message_data_invalid)
                    .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int which) {
                            //okボタンが押された時の処理
                            SyllabusFormActivity.this.finish();
                        }
                    })
                    .show();
        }
    }
    
}
