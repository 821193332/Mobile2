package com.xiangmu.l.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangmu.l.R;
import com.xiangmu.l.adapter.SearchAdapter;
import com.xiangmu.l.bean.SearchBean;
import com.xiangmu.l.utils.Constants;
import com.xiangmu.l.utils.JsonParser;
import com.xiangmu.l.utils.LogUtil;
import com.google.gson.Gson;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SearchActivity extends Activity {
    @Bind(R.id.et_search)
    EditText etSearch;
    @Bind(R.id.iv_search_voice)
    ImageView ivSearchVoice;
    @Bind(R.id.tv_search_go)
    TextView tvSearchGo;
    @Bind(R.id.lv_search)
    ListView lvSearch;
    @Bind(R.id.tv_no_result)
    TextView tvNoResult;
    @Bind(R.id.pb_loading)
    ProgressBar pbLoading;
    @Bind(R.id.activity_search)
    LinearLayout activitySearch;
    private SearchAdapter adapter;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);
    }


    private void voiceToText() {
        //1.创建RecognizerDialog对象
        RecognizerDialog mDialog = new RecognizerDialog(this, new MyInitListener());
        //2.设置accent、 language等参数
        mDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");//中文
        mDialog.setParameter(SpeechConstant.ACCENT, "mandarin");//普通话
        //若要将UI控件用于语义理解，必须添加以下参数设置，设置之后onResult回调返回将是语义理解
        //结果
        //3.设置回调接口
        mDialog.setListener(new MyRecognizerDialogListener());
        //4.显示dialog，接收语音输入
        mDialog.show();
    }


    @OnClick({R.id.iv_search_voice, R.id.tv_search_go})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_search_voice:
                //Toast.makeText(this, "语音输入", Toast.LENGTH_SHORT).show();
                voiceToText();
                break;
            case R.id.tv_search_go:
//                Toast.makeText(this, "开始搜索", Toast.LENGTH_SHORT).show();
                getDataFromNet();

                break;
        }
    }

    private void getDataFromNet() {
        pbLoading.setVisibility(View.VISIBLE);
        //关键字
        String word = etSearch.getText().toString().trim();
        try {
            word = URLEncoder.encode(word,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //连接+关键字
        String url = Constants.SEARCH_URL+word;

        RequestParams requestParams = new RequestParams(url);
        x.http().get(requestParams, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                LogUtil.e("搜索联网成功=="+result);
                processData(result);

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                LogUtil.e("搜索联网失败=="+ex.getMessage());
                tvNoResult.setVisibility(View.VISIBLE);
                pbLoading.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {
                LogUtil.e("onFinished==");
            }
        });
    }

    /**
     * 解析和显示数据
     * @param result
     */
    private void processData(String result) {
        SearchBean searchBean = paraseJson(result);
        List<SearchBean.ItemsBean> items = searchBean.getItems();
        if(items != null && items.size() >0){
            tvNoResult.setVisibility(View.GONE);
            //有数据
            //设置适配器
            adapter = new SearchAdapter(SearchActivity.this,items);
            lvSearch.setAdapter(adapter);
            lvSearch.setVisibility(View.VISIBLE);
        }else{
            //没有数据
            tvNoResult.setVisibility(View.VISIBLE);
            lvSearch.setVisibility(View.GONE);
        }

        pbLoading.setVisibility(View.GONE);

    }

    /**
     * 使用gson解析json数据
     * @param result
     * @return
     */
    private SearchBean paraseJson(String result) {
        return new Gson().fromJson(result,SearchBean.class);
    }

    class MyInitListener implements InitListener {

        @Override
        public void onInit(int i) {
            if (i != ErrorCode.SUCCESS) {
                Toast.makeText(SearchActivity.this, "初始化失败了", Toast.LENGTH_SHORT).show();
            }
        }
    }


    class MyRecognizerDialogListener implements RecognizerDialogListener {

        /**
         * 返回的结果
         *
         * @param recognizerResult
         * @param b                是否说话结束
         */
        @Override
        public void onResult(RecognizerResult recognizerResult, boolean b) {
            String result = recognizerResult.getResultString();
            printResult(recognizerResult);
            Log.e("TAG", "result==" + result);
//           Toast.makeText(MainActivity.this, "result=="+result, Toast.LENGTH_SHORT).show();

        }

        /**
         * 出错的回调
         *
         * @param speechError
         */
        @Override
        public void onError(SpeechError speechError) {

        }
    }


    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
        String reuslt = resultBuffer.toString().replace("。", "");

        etSearch.setText(reuslt);
        etSearch.setSelection(etSearch.length());
    }

}
