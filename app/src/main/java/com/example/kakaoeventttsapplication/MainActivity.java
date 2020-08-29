package com.example.kakaoeventttsapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final int SMS_RECEIVE_PERMISSON = 0;

    // 기능사용하기, 알림 버튼
    Switch FuncSwitch;
    Switch NoticeSoundSwitch;

    // 알림 안내 권한 설정
    Switch Alarm_Permission_Switch;
    Button Alarm_Permission_Setting;

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
