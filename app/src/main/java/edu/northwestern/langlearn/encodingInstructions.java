package edu.northwestern.langlearn;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class encodingInstructions extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encoding_instructions);
        Button contButton = (Button) findViewById(R.id.button);
        contButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(encodingInstructions.this, encoding.class);
                encodingInstructions.this.startActivity(myIntent);
            }
        });
        SharedPreferences prefs = this.getSharedPreferences(
                "edu.northwestern.langlearn", Context.MODE_PRIVATE);
        //prefs.edit().putInt("learningstage", 0).apply(); //reset the learning stage when we start encoding
    }
}
