package edu.northwestern.langlearn;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button sleepButton = (Button) findViewById(R.id.sleep); //button to start sleep mode
        sleepButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, sleepMode.class);
                MainActivity.this.startActivity(myIntent);
            }
        });

        Button trainButton = (Button) findViewById(R.id.startinitial); //button to start training
        trainButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, encodingInstructions.class);
                MainActivity.this.startActivity(myIntent);
            }
        });
    }
}
