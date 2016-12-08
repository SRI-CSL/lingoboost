package edu.northwestern.langlearn;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class testIntructions extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_intructions);
        Button testButton = (Button) findViewById(R.id.testbutton); //button to speak again
        testButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(testIntructions.this, wordTest.class);
                testIntructions.this.startActivity(myIntent);
            }
        });
    }
}
