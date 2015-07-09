package com.jinsu.smarthome;

import android.content.Intent;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements View.OnClickListener, RecognitionListener {
    private TextView mText;
    private SpeechRecognizer sr = null;
    private static final String TAG = "SmartHomeActivity";
    private Command cmd = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageButton speakButton = (ImageButton) findViewById(R.id.btn_speak);
        this.mText = (TextView) findViewById(R.id.textView);
        speakButton.setOnClickListener(this);
        this.sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(this);
        this.cmd = new Command(mText);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mText.setText("Try to connect...");
        try {
            cmd.connect("http://52.25.241.254");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        cmd.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_speak) {
            Log.d(TAG, "onClick btn_speak");
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "com.jinsu.smarthome");
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);
            sr.startListening(intent);
        }
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        Log.d(TAG, "onReadyForSpeech");
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d(TAG, "onBeggingingOfSpeech");
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        Log.d(TAG, "onRmsChanged");
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.d(TAG, "onBufferReceived");
    }

    @Override
    public void onEndOfSpeech() {
        Log.d(TAG, "onEndOfSpeech");
    }

    @Override
    public void onError(int error) {
        Log.d(TAG, "onError");
    }

    @Override
    public void onResults(Bundle results) {
        Log.d(TAG, "onResults");
        //String str = new String();
        ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        /*
        for (int i = 0; i < data.size(); i++) {
            Log.d(TAG, "result " + data.get(i));
            str += "[" + data.get(i) + "]";
        }
        mText.setText("results(" + data.size() + "): " + str);
        */
        if(data.size() > 0) {
            Log.d(TAG, "result: " + data.get(0));
            mText.setText((String)data.get(0));

            if(cmd.isConnected()) {
                try {
                    JSONObject action = cmd.getAction((String)data.get(0));
                    if(action != null) {
                        mText.setText("[" + action.getString("name") + "] 수행중...");
                        cmd.send(action);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            Log.d(TAG, "result: not found");
            mText.setText("뭐라구요?");
        }
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        Log.d(TAG, "onPartialResults");
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        Log.d(TAG, "onEvent");
    }

}
