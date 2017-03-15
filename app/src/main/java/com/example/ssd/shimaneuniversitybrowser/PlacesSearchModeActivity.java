package com.example.ssd.shimaneuniversitybrowser;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class PlacesSearchModeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_search_mode);

        setTitle(R.string.title_places_search_mode);

        Button NextButton1 = (Button) this.findViewById(R.id.button1);
        Button NextButton2 = (Button) this.findViewById(R.id.button2);
        Button NextButton3 = (Button) this.findViewById(R.id.button3);

        NextButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlacesSearchModeActivity.this, AllPlacesListActivity.class);
                startActivity(intent);
            }
        });

        NextButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlacesSearchModeActivity.this, BuildingListActivity.class);
                startActivity(intent);
            }
        });

        NextButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlacesSearchModeActivity.this, PlacesSearchClassnameActivity.class);
                startActivity(intent);
            }
        });
    }
}
