package com.example.kakaoeventttsapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SmsActivity extends AppCompatActivity {
   EditText editText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        editText = (EditText)findViewById(R.id.contentsText);

        Intent passedIntent = getIntent();
        processIntent(passedIntent);
    }
    private void processIntent(Intent intent){
        if(intent != null){
            String string = intent.getStringExtra("string");
            editText.setText(string);
        }
    }
    @Override
    protected void onNewIntent(Intent intent) {
        processIntent(intent);
        super.onNewIntent(intent);
    }
}


