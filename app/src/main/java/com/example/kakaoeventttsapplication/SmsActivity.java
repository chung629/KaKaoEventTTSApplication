package com.example.kakaoeventttsapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SmsActivity extends AppCompatActivity {
   EditText editTextSend;
   EditText editTextContent;
   Button button;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        editTextContent = (EditText)findViewById(R.id.contentsText);
        editTextSend = findViewById(R.id.senderText);
        Intent passedIntent = getIntent();
        processIntent(passedIntent);

        button = findViewById(R.id.close);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    private void processIntent(Intent intent){
        if(intent != null){
            String string = intent.getStringExtra("contents");
            editTextContent.setText(string);
            string = intent.getStringExtra("sender");
            editTextSend.setText(string);
        }
    }
    @Override
    protected void onNewIntent(Intent intent) {
        processIntent(intent);
        super.onNewIntent(intent);
    }
}


