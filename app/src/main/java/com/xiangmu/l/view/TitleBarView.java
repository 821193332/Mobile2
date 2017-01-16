package com.xiangmu.l.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangmu.l.R;

/**
 * Created by KangYueLong on 2017/1/16.
 */

public class TitleBarView extends LinearLayout implements View.OnClickListener{
    private final Context mContext;
    private TextView tv_search;
    private RelativeLayout rl_game;
    private ImageView iv_record;

    public TitleBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        tv_search=(TextView) getChildAt(1);
        rl_game=(RelativeLayout) getChildAt(2);
        iv_record=(ImageView) getChildAt(3);

        tv_search.setOnClickListener(this);
        rl_game.setOnClickListener(this);
        iv_record.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_search :
                Toast.makeText(mContext, "搜索", Toast.LENGTH_SHORT).show();
                break;
            case R.id.rl_game:
                Toast.makeText(mContext, "游戏", Toast.LENGTH_SHORT).show();
                break;
            case R.id.iv_record:
                Toast.makeText(mContext, "记录", Toast.LENGTH_SHORT).show();
                break;

        }

    }
}

