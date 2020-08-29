package com.example.kakaoeventttsapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Switch;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

// SMS 메시지가 오면 화면에 표시 해주는 Activity
public class SmsActivity extends AppCompatActivity {
    
  String string2="";
   EditText editTextSend;
   EditText editTextContent;
   EditText editTextSendTime;
   Button button;

  @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        editTextContent = (EditText)findViewById(R.id.contentsText);
        editTextSend = findViewById(R.id.senderText);
        editTextSendTime = findViewById(R.id.sendTimeText);

        Intent passedIntent = getIntent();
        processIntent(passedIntent);

        button = findViewById(R.id.close);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        finish();
    }
    private void processIntent(Intent intent){
        if(intent != null){
            String string = intent.getStringExtra("sender");
            editTextSend.setText(string);
            string2 += string;
            string = intent.getStringExtra("contents");
            editTextContent.setText(string);
            string2 += string;
            string = intent.getStringExtra("receivedDate");
            editTextSendTime.setText(string);
        }
    }
    @Override
    protected void onNewIntent(Intent intent) {
        processIntent(intent);
        super.onNewIntent(intent);
    }
}
// SMS 메시지 끝

