package edu.northwestern.langlearn;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Locale;

public class encoding extends AppCompatActivity implements OnInitListener, View.OnClickListener {
    private int MY_DATA_CHECK_CODE = 0;
    private TextToSpeech myTTS;
    String currentWord = ""; //current word is used to allow other functions to accsess the current German word in updateTrans function;
    WordList words;
    int tPointer = 0;
    boolean firstRun = false; //firt run of activity?
    int seen = 1; //number of words see// n
    boolean ttsWarmedUp = false; //during encoding, don't let the first word be shown until the device starts speaking

    public void onInit(int initStatus) {
        if (initStatus == TextToSpeech.SUCCESS) {
            myTTS.setLanguage(Locale.GERMAN);
            updateTrans();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                myTTS = new TextToSpeech(this, this);
            } else {
                Log.e("TTS", "Not set up");
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);

            }
        }
    }

    //update the translation displayed on the screen
    public void updateTrans() {
        String german = words.germanWords[tPointer];
        String english = words.englishWords[tPointer];

        myTTS.speak(words.germanWords[tPointer], TextToSpeech.QUEUE_FLUSH, null);
        while (!ttsWarmedUp && myTTS.isSpeaking() == false) { //wait until the TTS is ready

        }
        ttsWarmedUp = true;

        TextView germanword = (TextView) findViewById(R.id.germanword);
        germanword.setText(german);

        TextView englishword = (TextView) findViewById(R.id.englishword);
        englishword.setText(english);

        //update the current word to be accessed from other functions
        currentWord = german;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encoding);
        Intent intent = getIntent();
        tPointer = intent.getIntExtra("tPointer", 0);

        //when we do the initial encoding we need to get just the first <x> words from the translation data
        words = new WordList(this, 0, false);

        // if tPointer is >= translations.length, this means we just came back from the last mini
        // test and we should go directly to the regular test.
        if (tPointer < words.length) {
            if (tPointer == 0) {
                // if we're not coming back from a mini test, this is the first run. Important
                // because we don't need to display the test instructions before every test,
                // just the first one.
                firstRun = true;
            }
            if (tPointer > 4 && tPointer < 7) { //we're coming back from the first test
                Toast.makeText(encoding.this,
                        "Great! Now you'll go through the rest of the words.", Toast.LENGTH_LONG).show();
            }

            Intent checkTTSIntent = new Intent();
            checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
            Button nextButton = (Button) findViewById(R.id.nextbutton); //button to go to the next pair
            nextButton.setOnClickListener(this);

            Button speakButton = (Button) findViewById(R.id.speakbutton); //button to speak again
            speakButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myTTS.speak(currentWord, TextToSpeech.QUEUE_FLUSH, null);
                }
            });
        } else { //we're done with all the learning, move to the regular vocab test
            Intent myIntent = new Intent(encoding.this, testIntructions.class);
            encoding.this.startActivity(myIntent);
        }
    }

    @Override
    public void onClick(View view) {
        if (tPointer < words.length - 1) {
            seen = seen + 1;
            if (seen > 5) {
                if (firstRun) {
                    Intent myIntent = new Intent(encoding.this, encodingTestInstructions.class);
                    myIntent.putExtra("tPointer", (int) tPointer); //Optional parameters
                    encoding.this.startActivity(myIntent);
                } else {
                    Intent myIntent = new Intent(encoding.this, wordTest.class);
                    myIntent.putExtra("tPointer", tPointer); //Optional parameters
                    encoding.this.startActivity(myIntent);
                }
            } else {
                tPointer = tPointer + 1; //move to the next word pair and update the screen
                updateTrans();
            }
        } else { //we've gone through all the words
            if (seen > 2) { //do a mini test for any words we haven't done this for yet
                Intent myIntent = new Intent(encoding.this, wordTest.class);
                myIntent.putExtra("tPointer", tPointer);
                encoding.this.startActivity(myIntent);
            } else { //if we've done mini test for all words, move to the regular vocab test
                Intent myIntent = new Intent(encoding.this, testIntructions.class);
                encoding.this.startActivity(myIntent);
            }
        }
    }
}
