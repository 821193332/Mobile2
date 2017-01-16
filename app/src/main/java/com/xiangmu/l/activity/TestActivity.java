package com.xiangmu.l.activity;

import android.app.Activity;
import android.os.Bundle;

import com.atguigu.mobileplayer.R;
import com.atguigu.mobileplayer.utils.LogUtil;

public class TestActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.e("onCreate");
        setContentView(R.layout.activity_test);
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogUtil.e("onStart");
    }



    @Override
    protected void onRestart() {
        super.onRestart();
        LogUtil.e("onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.e("onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtil.e("onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtil.e("onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.e("onDestroy");
    }

}
