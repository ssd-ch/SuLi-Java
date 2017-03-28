package com.example.ssd.shimaneuniversitybrowser;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class PlacesSearchFormActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_search_form);

        setTitle(R.string.title_places_search_form);

        Button Button1 = (Button) findViewById(R.id.button_search);
        Button Button2 = (Button) findViewById(R.id.button_clear);

        Button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText edittext1 = (EditText) findViewById(R.id.form1);
                EditText edittext2 = (EditText) findViewById(R.id.form2);
                Spinner spinner3 = (Spinner) findViewById(R.id.form3);
                Spinner spinner4 = (Spinner) findViewById(R.id.form4);

                Intent intent = new Intent(PlacesSearchFormActivity.this, PlacesSearchResultActivity.class);
                intent.putExtra("data0", edittext1.getText().toString());
                intent.putExtra("data1", edittext2.getText().toString());
                intent.putExtra("data2", String.valueOf(spinner3.getSelectedItemPosition()-1));
                intent.putExtra("data3", String.valueOf(spinner4.getSelectedItemPosition()-1));
                startActivity(intent);
            }
        });
        Button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((EditText) findViewById(R.id.form1)).getEditableText().clear();
                ((EditText) findViewById(R.id.form2)).getEditableText().clear();
                ((Spinner) findViewById(R.id.form3)).setSelection(0);
                ((Spinner) findViewById(R.id.form4)).setSelection(0);
            }
        });

    }
}
