package com.example.kakaoeventttsapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Telephony;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;
import android.speech.tts.TextToSpeech;

import static android.speech.tts.TextToSpeech.ERROR;
import java.util.Locale;

import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final int SMS_RECEIVE_PERMISSON = 0;

    // 기능사용하기, 알림 버튼
    Switch FuncSwitch;
    Switch NoticeSoundSwitch;

    //TTS 관련
    SeekBar seekVolumn;
    static String Sms_Text;
    TextToSpeech tts;
    float TTS_speed;

    // 알림 안내 권한 설정
    Switch Alarm_Permission_Switch;
    Button Alarm_Permission_Setting;

    // 재생 텍스트 설정
    Switch textSetting1;
    Switch textSetting2;
    Switch textSetting3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 기능 사용하기 및 알림 설정
        FuncSwitch = findViewById(R.id.function_switch);
        FuncSwitch.setOnCheckedChangeListener(new FuncAndNoticeSettings.FuncSwitchListener());
        NoticeSoundSwitch = findViewById(R.id.notice_sound_switch);
        NoticeSoundSwitch.setOnCheckedChangeListener(new FuncAndNoticeSettings.NoticeSwitchListener());
        // 기능 사용하기 및 알림 설정 끝

        // 알림 안내 권한 설정
        Alarm_Permission_Switch = findViewById(R.id.permission_state_switch);
        Alarm_Permission_Setting = findViewById(R.id.alarm_permission_setting);
        Alarm_Permission_Setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent armSet = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                armSet.addCategory(Intent.CATEGORY_DEFAULT);
                armSet.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(armSet, 0);
            }
        }); // 앱 상세 설정 화면으로 이동
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS))
            Alarm_Permission_Switch.setChecked(false);
        else
            Alarm_Permission_Switch.setChecked(true);
        // 알림 안내 권한 설정 끝

        // 재생 텍스트 설정
        textSetting1 = findViewById(R.id.textSetting1);
        textSetting2 = findViewById(R.id.textSetting2);
        textSetting3 = findViewById(R.id.textSetting3);

        // SMS 수신 허가
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS);

        if(permissionCheck == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(getApplicationContext(), "SMS 수신권한을 사용자가 승인함", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(), "SMS 수신권한을 사용자가 거부함", Toast.LENGTH_SHORT).show();
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS)){
                Toast.makeText(getApplicationContext(), "SMS권한이 필요합니다", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.RECEIVE_SMS}, SMS_RECEIVE_PERMISSON);
            }else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, SMS_RECEIVE_PERMISSON);
            }
        }
        // SMS 수신 허가 끝

        // TTS를 생성하고 OnInitListener로 초기화 한다.
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != ERROR) {
                    // 언어를 선택한다.
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });

        // SeekBar 부분
        seekVolumn = (SeekBar) findViewById(R.id.sound_bar);
        final AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        int nMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int nCurrentVolumn = audioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC);
        seekVolumn.setMax(nMax);
        seekVolumn.setProgress(nCurrentVolumn);
        seekVolumn.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                        progress, 0);
            }
        });


        // Spinner 부분
        Spinner speechSpinner = (Spinner)findViewById(R.id.spinner);
        ArrayAdapter speechAdapter = ArrayAdapter.createFromResource(this,
                R.array.speech_speed, android.R.layout.simple_spinner_item);
        speechAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        speechSpinner.setAdapter(speechAdapter);
        speechSpinner.setSelection(1);  //기본값 지정 보통(1.0)

        speechSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            } //이 오버라이드 메소드에서 position은 몇번째 값이 클릭됬는지 알 수 있습니다.
            //getItemAtPosition(position)를 통해서 해당 값을 받아올수있습니다.

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        Intent passedIntent = getIntent();
        processIntent(passedIntent);

    }

    private void processIntent(Intent intent) {
        Sms_Text = "";
        if (intent != null) {
            String contents = intent.getStringExtra("contents");
            String sender = intent.getStringExtra("sender");
            String time = intent.getStringExtra("receivedDate");
            if(textSetting1.isChecked())
                Sms_Text += sender + " ";
            if(textSetting2.isChecked())
                Sms_Text += contents + " ";
            if(textSetting3.isChecked())
                Sms_Text += time + " ";

            Spinner speechSpinner = (Spinner)findViewById(R.id.spinner);
            String speed=speechSpinner.getSelectedItem().toString();
            String abc[]=speed.split("\\(");
            speed=abc[1].substring(0,abc[1].length()-1);
            TTS_speed=Float.parseFloat(speed);

            Boolean chk=FuncAndNoticeSettings.isFuncOn();
            if (chk){
                tts.setPitch(1.0f);         // 음성 톤은 기본 설정
                tts.setSpeechRate(TTS_speed);    // 읽는 속도
                tts.speak(Sms_Text,TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        processIntent(intent);
        super.onNewIntent(intent);
    }

    // SMS 수신 허가 관련
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int grantResults[]){
        switch(requestCode){
            case SMS_RECEIVE_PERMISSON:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(getApplicationContext(), "SMS권한 승인함", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "SMS권한 거부함", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
    // SMS 수신 허가 관련

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //requestCode = 0는 알림 안내 권한 설정 관련 코드 입니다
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 알림 권한 설정 관련
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS)) {
            Alarm_Permission_Switch.setChecked(false);
        } else {
            Alarm_Permission_Switch.setChecked(true);
        }
        // 알림권한 설정 관련 끝
    }
}
