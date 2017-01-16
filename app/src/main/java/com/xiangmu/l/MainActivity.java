package com.xiangmu.l;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.RadioGroup;

import com.xiangmu.l.base.BaseFragment;
import com.xiangmu.l.fragment.NetAudioFragment;
import com.xiangmu.l.fragment.RecyclerViewFragment;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private RadioGroup rg_main;
    private ArrayList<BaseFragment> fragments;
    private  int position;
    private Fragment tempFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rg_main = (RadioGroup) findViewById(R.id.rg_main);
        initFragment();
        initListenter();
    }

    private void initListenter() {
        rg_main.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.rb_net_audio:
                        position =0;
                        break;
                    case R.id.rb_recyclerview:
                        position =1;
                        break;
                }
                Fragment currentFragment = fragments.get(position);
                switchFragment(currentFragment);

            }

        });
        rg_main.check(R.id.rb_net_audio);
    }
    private void switchFragment(Fragment currentFragment) {
        if (tempFragment != currentFragment){
            FragmentTransaction ft =getSupportFragmentManager().beginTransaction();
            if (currentFragment !=null){
                if (!currentFragment.isAdded()){
                    if (tempFragment !=null){
                        ft.hide(tempFragment);
                    }
                    ft.add(R.id.frameLayout,currentFragment);
                }else {
                    if (tempFragment !=null){
                        ft.hide(tempFragment);
                    }
                    ft.show(currentFragment);
                }
                ft.commit();
            }
            tempFragment =currentFragment;
        }
    }


    private void initFragment() {
        fragments =new ArrayList<>();

        fragments.add(new NetAudioFragment());

        fragments.add(new RecyclerViewFragment());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("TAG","onDestroy");
    }
    public static boolean isGrantExternaIRW(Activity activity){
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M && activity.checkSelfPermission
                (Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            activity.requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,


            },1);
            return  false;
        }
        return  true;
    }
}
