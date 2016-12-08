package edu.northwestern.langlearn;

import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class encodingTestInstructions extends AppCompatActivity {
    int tPointer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encoding_test_instructions);

        Intent intent = getIntent();
        tPointer = intent.getIntExtra("tPointer",0);

        Button testButton = (Button) findViewById(R.id.testbutton); //button to speak again
        testButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(encodingTestInstructions.this, wordTest.class);
                myIntent.putExtra("tPointer", (int) tPointer); //Optional parameters
                encodingTestInstructions.this.startActivity(myIntent);
            }
        });
    }
}
