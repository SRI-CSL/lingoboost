package edu.northwestern.langlearn;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.widget.Toast;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.os.Environment.getExternalStorageDirectory;

public class WordTest extends AppCompatActivity implements OnInitListener {
    // private int MY_DATA_CHECK_CODE = 0;
    // private TextToSpeech myTTS;
    //
    // String currentWord = ""; //current word is used to allow other functions to accsess the current German word in updateTrans function;
    // String currentGerman = "";
    // WordList words;
    // int tPointer = 0;
    // int myPointer = 0; //scratch space to iterate through the test
    // boolean minitest = true; //is this a mini test embedded in learning? If so this will be true
    // boolean feedbackState = false; //what to do when button clicked, if this is false it will give feedback if true will go onto next word.
    // int[] correct; //stores whether each word has been translated succsesfully
    // int NUM_REQUIRED = 1; //number of correct translations required for each word
    // boolean ttsWarmedUp = false; //goes true when the TTS is fully initialized
    // MediaPlayer correctsound;
    // MediaPlayer incorrectsound;
    // SharedPreferences prefs;
    // FileWriter logWriter;
    //
    // public boolean isExternalStorageWritable() { //check to make sure we can store data on the SD card.
    //     String state = Environment.getExternalStorageState();
    //     if (Environment.MEDIA_MOUNTED.equals(state)) {
    //         return true;
    //     }
    //     return false;
    // }
    //
    // void fileError() {
    //     Toast.makeText(WordTest.this,
    //             "Logfile error. Unplug the phone and try again. If problem persists, contact nathanww@u.northwestern.edu", Toast.LENGTH_LONG).show();
    //     Intent myIntent = new Intent(WordTest.this, ParticipantMode.class);
    //     WordTest.this.startActivity(myIntent);
    // }
    //
    // void logTimestamp(String message) { //write something to the log file with a timestamp.
    //     SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
    //     String current = sdf.format(new Date());
    //     try {
    //         logWriter.write(current + ":" + message + "\n");
    //         logWriter.flush();
    //     } catch (Exception e) { //fail silently if we can't write
    //
    //     }
    //
    // }
    //
    // public int getNextWord(int index) { //gets the index for the next word that has not been succsessfully translated at the criterion. If none is present, returns -1;
    //     for (int i = index + 1; i < words.length; i++) {
    //         if (correct[i] < NUM_REQUIRED) {
    //             return i;
    //         }
    //     }
    //     //we didn't find any going forward, so loop around
    //     for (int i = 0; i < index + 1; i++) {
    //         if (correct[i] < NUM_REQUIRED) {
    //             return i;
    //         }
    //     }
    //     return -1;
    // }


    public void onInit(int initStatus) {
        if (initStatus == TextToSpeech.SUCCESS) {
            // myTTS.setLanguage(Locale.GERMAN);
            // updateTrans();
        }
    }

    // protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    //     if (requestCode == MY_DATA_CHECK_CODE) {
    //         if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
    //             myTTS = new TextToSpeech(this, this);
    //         } else {
    //             Log.e("TTS", "Not set up");
    //             Intent installTTSIntent = new Intent();
    //             installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
    //             startActivity(installTTSIntent);
    //
    //         }
    //     }
    // }
    //
    // public void updateTrans() { //update the translation displayed on the screen
    //     View subject = (View) findViewById(R.id.response);
    //     subject.setVisibility(View.VISIBLE);
    //     TextView englishView = (TextView) findViewById(R.id.englishword);
    //     englishView.setVisibility(View.GONE);
    //     EditText rField = (EditText) findViewById(R.id.response);
    //     rField.setText("", TextView.BufferType.EDITABLE);
    //     //String[] splitup=translations[myPointer].split(" ");
    //
    //     String german = words.germanWords[myPointer];
    //     String english = words.englishWords[myPointer];
    //
    //
    //     TextView germanword = (TextView) findViewById(R.id.germanword);
    //     myTTS.speak(german, TextToSpeech.QUEUE_FLUSH, null);
    //     while (!ttsWarmedUp && myTTS.isSpeaking() == false) { //wait until the TTS is ready
    //
    //     }
    //     ttsWarmedUp = true;
    //     germanword.setText(words.germanWords[myPointer]);
    //     TextView englishword = (TextView) findViewById(R.id.englishword);
    //     englishword.setText(english);
    //     currentWord = english.toLowerCase(); //update the current word to be accessed from other functions
    //     currentGerman = german.toLowerCase();
    //     AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
    //     int volumeLevel = am.getStreamVolume(AudioManager.STREAM_MUSIC); //get the system volume for logging
    //     int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    //     float volume = (float) volumeLevel / maxVolume;
    //     logTimestamp("Presented word=" + currentWord + ",system volume=" + volume);
    // }
    //
    // public boolean equalsLenient(String response) { // checks a response to see if it matches including or excluding spaces
    //     return (response.equals(currentWord) || response.equals(currentWord.replace(" ", "")) || response.replace(" ", "").equals(currentWord));
    // }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_word_test);
        // if (!isExternalStorageWritable()) { //if we can't write to the log file, abort
        //     fileError();
        // }
        // try { //set up the logging, if it doesn't work then go back to the participant screen
        //     logWriter = new FileWriter(getExternalStorageDirectory() + "/experimentlog.txt", true);
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }
        // logTimestamp("Recall test started");
        //
        // correctsound = new MediaPlayer();
        // correctsound = MediaPlayer.create(getApplicationContext(), R.raw.correct);
        // incorrectsound = new MediaPlayer();
        // incorrectsound = MediaPlayer.create(getApplicationContext(), R.raw.incorrect);
        // Intent intent = getIntent();
        // tPointer = intent.getIntExtra("tPointer", -1);
        //
        // if (tPointer == -1) {
        //     minitest = false;
        //     myPointer = 0;
        //     logTimestamp("test type=2");
        // } else {
        //     myPointer = tPointer - 4;
        //     logTimestamp("test type=1");
        // }
        // prefs = this.getSharedPreferences(
        //         "edu.northwestern.langlearn", Context.MODE_PRIVATE);
        // int stage = prefs.getInt("learningstage", 0);
        // int estage = prefs.getInt("experimentstage", 0);
        // int today = (int) ((((System.currentTimeMillis() / 1000) / 60) / 60) / 24);
        // prefs.edit().putInt("lastTestTime", today).apply(); // update the "latest test" to show it was today.
        //
        //
        // words = new WordList(this, stage, !minitest);
        //
        // if (!minitest) { //if this is a real test, let the person know that there will be new words and also increment the number of tests that have been done
        //     if (estage > 0) { //we're not introducing new words in initial training
        //         TextView newwords = (TextView) findViewById(R.id.newwords);
        //         newwords.setVisibility(View.VISIBLE);
        //     }
        //     prefs.edit().putInt("learningstage", stage + 1).apply();
        // }
        //
        // correct = new int[words.length]; //set up our array to say if this owrd has been gotten correctly
        // for (int i = 0; i < correct.length; i++) {
        //     correct[i] = 0;
        // }
        // Intent checkTTSIntent = new Intent();
        // checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        // startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
        // Button submitButton = (Button) findViewById(R.id.nextbutton); //button to speak again

        //        submitButton.setOnClickListener(new View.OnClickListener() {
        //            @Override
        //            public void onClick(View v) {
        //                EditText rField = (EditText) findViewById(R.id.response);
        //                String response = rField.getText().toString().toLowerCase();
        //                //check to see if they've entered anything
        //                if (response.length() >= 3) {
        //                    if (!feedbackState) { //this is the first feedback
        //
        //                        if (equalsLenient(response)) { //correct
        //                            correctsound.start();
        //                            correct[myPointer]++;
        //                        } else {
        //                            incorrectsound.start();
        //                        }
        //
        //                        logTimestamp("Entered word=" + currentWord + ",response=" + response + ", judgement=" + response.equals(currentWord));
        //                        TextView english = (TextView) findViewById(R.id.englishword);
        //                        english.setVisibility(View.VISIBLE);
        //                        View subject = (View) findViewById(R.id.response);
        //                        subject.setVisibility(View.GONE);
        //
        //                    } else {
        //
        //                        if (minitest) {
        //                            myPointer = myPointer + 1;
        //                            if (myPointer <= tPointer && myPointer < words.length) {
        //                                updateTrans();
        //                            } else {
        //                                Intent myIntent = new Intent(WordTest.this, encoding.class);
        //                                myIntent.putExtra("tPointer", tPointer + 1);
        //                                WordTest.this.startActivity(myIntent);
        //                            }
        //                        } else { //regular standalone test
        //                            myPointer = getNextWord(myPointer);
        //                            if (myPointer == -1) {
        //
        //                                prefs.edit().putInt("experimentstage", 2).apply(); //we've completed the test, tell the system the next step is the sleep.
        //
        //                                Intent myIntent = new Intent(WordTest.this, ParticipantMode.class);
        //                                myIntent.putExtra("status", "Learning Complete");
        //                                WordTest.this.startActivity(myIntent);
        //                            } else {
        //                                updateTrans();
        //                            }
        //
        //                        }
        //
        //                    }
        //                    feedbackState = !feedbackState;
        //
        //                } else {//they haven't entered anything, just clear the textbox
        //                    // EditText rField   = (EditText)findViewById(R.id.response);
        //                    updateTrans();
        //                    Toast.makeText(WordTest.this,
        //                            "If you don't know this word, take a guess!", Toast.LENGTH_LONG).show();
        //                }
        //            }
        //        });
    }

    @Override
    protected void onStop() { //close the file when the activity stops.
        super.onStop();
        // try {
        //     logWriter.close();
        // } catch (Exception e) {
        //     fileError();
        // }
    }
}
