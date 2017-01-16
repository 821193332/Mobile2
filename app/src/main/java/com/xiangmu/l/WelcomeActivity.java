package com.xiangmu.l;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;

public class WelcomeActivity extends AppCompatActivity {
    private Handler handler =new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                statrtMainActivity();
            }
        },2000);
    }
    private boolean isStartMain =false;
    private  void statrtMainActivity(){
        startActivity(new Intent(this,MainActivity.class));
        finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        statrtMainActivity();
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}

