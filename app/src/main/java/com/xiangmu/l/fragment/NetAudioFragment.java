package com.xiangmu.l.fragment;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xiangmu.l.R;
import com.xiangmu.l.activity.ShowImageAndGifActivity;
import com.xiangmu.l.adapter.NetAudioFragmentAdapter;
import com.xiangmu.l.base.BaseFragment;
import com.xiangmu.l.base.NetAudioBean;
import com.xiangmu.l.utils.CacheUtils;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

import java.util.List;

/**
 * Created by KangYueLong on 2017/1/16.
 */

public class NetAudioFragment extends BaseFragment {

    private static final String TAG = NetAudioFragment.class.getSimpleName();
    @Bind(R.id.listview)
    ListView listview;
    @Bind(R.id.progressbar)
    ProgressBar progressbar;
    @Bind(R.id.tv_nomedia)
    TextView tvNomedia;
    private NetAudioFragmentAdapter myAdapter;
    private List<NetAudioBean.ListBean> datas;

    @Override
    public View initView() {
        Log.e(TAG, "网络音频UI被初始化了");
        View view = View.inflate(mContext, R.layout.fragment_net_audio, null);
        ButterKnife.bind(this, view);

        //设置点击事件
        //设置点击事件
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                NetAudioBean.ListBean listEntity = datas.get(position);
                if(listEntity !=null ){
                    //3.传递视频列表
                    Intent intent = new Intent(mContext,ShowImageAndGifActivity.class);
                    if(listEntity.getType().equals("gif")){
                        String url = listEntity.getGif().getImages().get(0);
                        intent.putExtra("url",url);
                        mContext.startActivity(intent);
                    }else if(listEntity.getType().equals("image")){
                        String url = listEntity.getImage().getBig().get(0);
                        intent.putExtra("url",url);
                        mContext.startActivity(intent);
                    }
                }


            }
        });

        return view;
    }

    @Override
    public void initData() {
        super.initData();
        Log.e("TAG", "网络视频数据初始化了...");




        String saveJson = CacheUtils.getString(mContext, Constants.NET_AUDIO_URL);
        if(!TextUtils.isEmpty(saveJson)){
            processData(saveJson);
        }

        getDataFromNet();
    }

    private void getDataFromNet() {
        RequestParams reques = new RequestParams(Constants.NET_AUDIO_URL);
        x.http().get(reques, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {

                CacheUtils.putString(mContext,Constants.NET_AUDIO_URL,result);
                LogUtil.e("onSuccess==" + result);
                processData(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                LogUtil.e("onError==" + ex.getMessage());
            }

            @Override
            public void onCancelled(CancelledException cex) {
                LogUtil.e("onCancelled==" + cex.getMessage());
            }

            @Override
            public void onFinished() {
                LogUtil.e("onFinished==");
            }
        });

    }

    private void processData(String json) {
        NetAudioBean netAudioBean = paraseJons(json);
//        LogUtil.e(netAudioBean.getList().get(0).getText()+"--------------");
        datas = netAudioBean.getList();
        if(datas != null && datas.size() >0){
            //有视频
            tvNomedia.setVisibility(View.GONE);
            //设置适配器
            myAdapter = new NetAudioFragmentAdapter(mContext,datas);
            listview.setAdapter(myAdapter);
        }else{
            //没有视频
            tvNomedia.setVisibility(View.VISIBLE);
        }

        progressbar.setVisibility(View.GONE);



    }

    /**
     * json解析数据
     * @param json
     * @return
     */
    private NetAudioBean paraseJons(String json) {
        return new Gson().fromJson(json,NetAudioBean.class);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
