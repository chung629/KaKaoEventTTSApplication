package com.example.kakaoeventttsapplication;

import android.widget.CompoundButton;

public class FuncAndNoticeSettings {

    public static boolean FuncOn = true;
    public static boolean NoticeSoundOn = true;

    // [기능 사용하기] 스위치 입력 리스너 ------------------------------------------------------------------
    static class FuncSwitchListener implements CompoundButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked)
                FuncOn = true;
            else
                FuncOn = false;
        }
    }

    /*
    [ isFuncOn() ]
    기능 활성 여부를 리턴. {활성:true, 비활성:false}
    나중에 읽기 설정 시 다음과 같이 조건문에 FuncAndNoticeSettings.isFuncOn() 를 넣어주세요.
    ex)
    if(FuncAndNoticeSettings.isFuncOn()){
        // TTS 출력 코드
    }
    */
    public static boolean isFuncOn(){
        return FuncOn;
    }

    // [수신 시 알림음 재생] 스위치 입력 리스너 -------------------------------------------------------------
    static class NoticeSwitchListener implements CompoundButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
            if(isChecked)
                NoticeSoundOn = true;
            else
                NoticeSoundOn = false;
        }
    }

    /*
    [ isNoticeSoundOn() ]
    알림음 활성 여부를 리턴. {활성:true, 비활성:false}
    SmsReceiver 35번 라인에서 사용하였음.
    */
    public static boolean isNoticeSoundOn() {
        return NoticeSoundOn;
    }
}
