package com.example.kakaoeventttsapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
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

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final int SMS_RECEIVE_PERMISSON = 0;

    // 기능사용하기, 알림 버튼
    Switch FuncSwitch;
    Switch NoticeSoundSwitch;

    SeekBar seekVolumn;
    static String Sms_Text;
    TextToSpeech tts;
    float TTS_speed;

    // 알림 안내 권한 설정
    Switch Alarm_Permission_Switch;
    Button Alarm_Permission_Setting;

    // 블루투스
    BluetoothAdapter mBluetoothAdapter;
    static final int REQUEST_ENABLE_BT=3;
    private Set<BluetoothDevice> devices;
    private BluetoothDevice conntedDevice;
    BluetoothSocket mBluetoothSocket;
    BluetoothServerSocket mBluetoothServerSocket;


    Switch btSwitch;
    Button btSetting;

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


        // ------------------------------------------------------------
        // 블루투스 기능
        btSetting=findViewById(R.id.bluetooth_setting_btn);
        btSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivityForResult(intent,0);   //startActivityForResult() 는 호출한 Activity로 부터 결과를 받을 경우 사용.
            }
        });
        btSwitch =findViewById(R.id.bluetooth_state);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    }

    private void processIntent(Intent intent) {
        if (intent != null) {
            String contents = intent.getStringExtra("contents");
            String sender = intent.getStringExtra("sender");
            Sms_Text = sender +" "+ contents;

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
        switch(requestCode) {
            case REQUEST_ENABLE_BT:
                if(resultCode == RESULT_OK) {
                    // 블루투스가 활성 상태로 변경됨
                }
                else if(resultCode == RESULT_CANCELED) {
                    // 블루투스가 비활성 상태임
//                    finish();  //  어플리케이션 종료
                }
                break;
        }
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

        //-------------------- Bluetooth 기능 구현- --------

        //블루투스가 활성화 여부에 따라 스위치 on/off 변경
        if(!mBluetoothAdapter.isEnabled()) {
            btSwitch.setChecked(false);
        }
        else{
            btSwitch.setChecked(true);
        }
//            if (mBluetoothAdapter.getName())
//        devices.toString() // 페어링된 기기 주소들 모음 mBluetoothAdapter.getName() 폰기종
//        mBluetoothAdapter.getAddress()

        devices=mBluetoothAdapter.getBondedDevices();
        if(devices.size()>0){
//            Toast.makeText(getApplicationContext(),"0"+mBluetoothAdapter.getRemoteDevice("18:54:CF:C8:16:EA").getName(), Toast.LENGTH_SHORT).show(); // 저 주소해당되는 페어링된 기기 이름
//            Toast.makeText(getApplicationContext(),"1"+mBluetoothAdapter.getRemoteDevice(mBluetoothAdapter.getAddress()).getName(), Toast.LENGTH_SHORT).show(); // 핸드폰의 주소의 네임을 하는듯

//            Toast.makeText(getApplicationContext(),"addr list"+"/"+devices.toString(), Toast.LENGTH_SHORT).show(); //페어링된 기기들의 리스트
            Log.d("TAG",devices.toString());

            for (int i=0;i<devices.size();i++)
            {
//            Toast.makeText(getApplicationContext(),"i"+i+"/"+devices.toArray()[i], Toast.LENGTH_SHORT).show();
//            mBluetoothAdapter.getState()//12나옴    mBluetoothAdapter.getRemoteDevice(devices.toArray()[i]+"").getBondState()  동일하게 12
                Log.d("TAG","["+devices.toArray()[i]+"]"+mBluetoothAdapter.getRemoteDevice(devices.toArray()[i]+"").getName()+" bondstate:"+mBluetoothAdapter.getRemoteDevice(devices.toArray()[i]+"").getBondState()+" getstate:"+mBluetoothAdapter.getState());
//                mBluetoothAdapter.getRemoteDevice("");
                if (mBluetoothSocket.isConnected())
                {
                 Log.d("TAG","YYYYYYYYEEEEEEEEESSSSSS");
                }
            }
        }
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices == null || pairedDevices.size() == 0) {
            Toast.makeText(getApplicationContext(),"No Paired Devices Found",Toast.LENGTH_SHORT).show();
        } else {
            ArrayList<BluetoothDevice> list = new ArrayList<BluetoothDevice>();
            list.addAll(pairedDevices);

//            Toast.makeText(getApplicationContext(),"list2 "+pairedDevices.toString(),Toast.LENGTH_SHORT).show(); // devices랑 똑같이 페어링된 기기목록
            Toast.makeText(getApplicationContext(),"2"+list.get(1).getName(),Toast.LENGTH_SHORT).show();
        }

        {

//            Toast.makeText(getApplicationContext(),"3"+mBluetoothAdapter.getState()+mBluetoothAdapter.getAddress(),Toast.LENGTH_SHORT).show();
//            Toast.makeText(getApplicationContext(),"4"+mBluetoothAdapter.getBluetoothLeScanner().toString(),Toast.LENGTH_SHORT).show(); //이상한거나옴
//            Toast.makeText(getApplicationContext(),"5"+mBluetoothAdapter.getRemoteDevice("18:54:CF:C8:16:EA").getName(),Toast.LENGTH_SHORT).show(); // 저 주소해당되는 페어링된 기기 이름
//            Toast.makeText(getApplicationContext(),"5"+mBluetoothAdapter.getBluetoothLeScanner(),Toast.LENGTH_SHORT).show(); //이상한주소같은느낌
        }


    }

    @Override
    protected void onStart() {
        super.onStart();

        if(mBluetoothAdapter == null) {
            //장치가 블루투스를 지원하지 않는 경우.
            Toast.makeText(getApplicationContext(), "블루투스 기능을 지원하지 않음", Toast.LENGTH_SHORT).show();
            finish();
        }
        else {
            // 장치가 블루투스를 지원하는 경우.
            if(!mBluetoothAdapter.isEnabled()) {
                // 블루투스 활성화 요청
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }


}
