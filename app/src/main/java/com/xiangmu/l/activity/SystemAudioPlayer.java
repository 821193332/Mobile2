package com.xiangmu.l.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangmu.l.IMusicPlayerService;
import com.xiangmu.l.R;
import com.xiangmu.l.bean.MediaItem;
import com.xiangmu.l.service.MusicPlayerService;
import com.xiangmu.l.utils.LogUtil;
import com.xiangmu.l.utils.LyricUtils;
import com.xiangmu.l.utils.Utils;
import com.xiangmu.l.view.BaseVisualizerView;
import com.xiangmu.l.view.LyricShowView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

public class SystemAudioPlayer extends Activity implements View.OnClickListener {

    /**
     * 进度的更新
     */
    private static final int PROGRESS = 0;
    /**
     * 歌词同步
     */
    private static final int SHOW_LYRIC = 1;
    private ImageView ivIcon;
    private BaseVisualizerView mBaseVisualizerView;
    private TextView tvArtist;
    private TextView tvName;
    private TextView tvTime;
    private SeekBar seekbarAudio;
    private Button btnAudioPlaymode;
    private Button btnAudioPre;
    private Button btnAudioStartPause;
    private Button btnAudioNext;
    private Button btnSwitchLyric;
    private LyricShowView lyric_show_view;
    private int position;
    private IMusicPlayerService service;//服务的代理类
    private MyBroadcastReceiver receiver;
    private Utils utils;


    private ServiceConnection conn = new ServiceConnection() {
        /**
         * 当和服务建立连接成功后的回调
         * @param name
         * @param iBinder
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            service = IMusicPlayerService.Stub.asInterface(iBinder);
            if (service != null) {
                try {

                    if (notification) {
                        //状态拦截
//                        showViewData();
                        //再发一次广播
                        service.notifyChang();
                    } else {
                        //列表来的
                        service.openAudio(position);
                    }


                    showButttonState(false);

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 当和服务断开连接的回调
         * @param name
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private boolean notification;
    private Visualizer mVisualizer;

    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2016-11-23 16:16:24 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        setContentView(R.layout.activity_system_audio_player);
        ivIcon = (ImageView) findViewById(R.id.iv_icon);
        mBaseVisualizerView = (BaseVisualizerView) findViewById(R.id.mBaseVisualizerView);
        tvArtist = (TextView) findViewById(R.id.tv_artist);
        tvName = (TextView) findViewById(R.id.tv_name);
        tvTime = (TextView) findViewById(R.id.tv_time);
        seekbarAudio = (SeekBar) findViewById(R.id.seekbar_audio);
        btnAudioPlaymode = (Button) findViewById(R.id.btn_audio_playmode);
        btnAudioPre = (Button) findViewById(R.id.btn_audio_pre);
        btnAudioStartPause = (Button) findViewById(R.id.btn_audio_start_pause);
        btnAudioNext = (Button) findViewById(R.id.btn_audio_next);
        btnSwitchLyric = (Button) findViewById(R.id.btn_switch_lyric);
        lyric_show_view = (LyricShowView) findViewById(R.id.lyric_show_view);

        btnAudioPlaymode.setOnClickListener(this);
        btnAudioPre.setOnClickListener(this);
        btnAudioStartPause.setOnClickListener(this);
        btnAudioNext.setOnClickListener(this);
        btnSwitchLyric.setOnClickListener(this);


        //帧动画
        ivIcon.setBackgroundResource(R.drawable.animation_list);
        AnimationDrawable rocketAnimation = (AnimationDrawable) ivIcon.getBackground();
        rocketAnimation.start();

        //设置SeekBar的监听
        seekbarAudio.setOnSeekBarChangeListener(new MyOnSeekBarChangeListener());

        lyric_show_view.setOnScrollText(new MyOnScrollText());

    }
    class MyOnScrollText implements LyricShowView.OnScrollText {

        @Override
        public void scrollTextBefore() {
            if (lyricUtils.isExistsLyric()) {
                handler.removeMessages(SHOW_LYRIC);
                //xian.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void scrollText(int currentPosition) {
            try {
                if(lyricUtils.isExistsLyric()) {
                    service.seekTo(currentPosition);
                    lyric_show_view.setNextShow(currentPosition);
                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void scrollTextAfter() {
            handler.sendEmptyMessage(SHOW_LYRIC);
            //xian.setVisibility(View.GONE);
        }
    }

    class MyOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                try {
                    service.seekTo(progress);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    /**
     * Handle button click events<br />
     * <br />
     * Auto-created on 2016-11-23 16:16:24 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    @Override
    public void onClick(View v) {
        if (v == btnAudioPlaymode) {
            // Handle clicks for btnAudioPlaymode
            changePlaymode();
        } else if (v == btnAudioPre) {
            try {
                service.pre();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            // Handle clicks for btnAudioPre
        } else if (v == btnAudioStartPause) {
            try {
                if (service.isPlaying()) {
                    //暂停
                    service.pause();
                    //按钮设置-播放
                    btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_start_selector);
                } else {
                    //播放
                    service.start();
                    //设置按钮-暂停
                    btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_pause_selector);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            // Handle clicks for btnAudioStartPause
        } else if (v == btnAudioNext) {
            try {
                service.next();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            // Handle clicks for btnAudioNext
        } else if (v == btnSwitchLyric) {
            // Handle clicks for btnSwitchLyric
        }
    }

    private void changePlaymode() {
        try {
            //改变模式
            int playmode = service.getPlaymode();
            if (playmode == MusicPlayerService.REPEAT_NORMAL) {
                playmode = MusicPlayerService.REPEAT_SINGLE;
            } else if (playmode == MusicPlayerService.REPEAT_SINGLE) {
                playmode = MusicPlayerService.REPEAT_ALL;
            } else if (playmode == MusicPlayerService.REPEAT_ALL) {
                playmode = MusicPlayerService.REPEAT_NORMAL;
            } else {
                playmode = MusicPlayerService.REPEAT_NORMAL;
            }
            //保持模式
            service.setPlaymode(playmode);//保持到服务里面
            showButttonState(true);


        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void showButttonState(boolean isShowToast) throws RemoteException {
        int playmode;//从服务获取最新的播放模式
        playmode = service.getPlaymode();
        if (playmode == MusicPlayerService.REPEAT_NORMAL) {
            btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_normal_selector);
            if (isShowToast) {
                Toast.makeText(this, "顺序播放", Toast.LENGTH_SHORT).show();
            }

        } else if (playmode == MusicPlayerService.REPEAT_SINGLE) {
            btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_single_selector);
            if (isShowToast) {
                Toast.makeText(this, "单曲循环", Toast.LENGTH_SHORT).show();
            }

        } else if (playmode == MusicPlayerService.REPEAT_ALL) {
            btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_all_selector);
            if (isShowToast) {
                Toast.makeText(this, "全部循环", Toast.LENGTH_SHORT).show();
            }

        } else {
            btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_normal_selector);
            if (isShowToast) {
                Toast.makeText(this, "顺序播放", Toast.LENGTH_SHORT).show();
            }

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        findViews();
        getData();
        bindAndStartService();


    }

    private void initData() {
        utils = new Utils();
        //注册监听广播
        receiver = new MyBroadcastReceiver();
        IntentFilter interFilter = new IntentFilter();
        interFilter.addAction(MusicPlayerService.ACTION_OPENAUDIO);
        registerReceiver(receiver, interFilter);

        //1.Eventbus注册
        EventBus.getDefault().register(this);
    }

    class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //播放开始了
            if (MusicPlayerService.ACTION_OPENAUDIO.equals(intent.getAction())) {

                showViewData(null);
            }

        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_LYRIC:
                    try {
                        //得到当前播放进度
                        int currentPosition = service.getCurrentPosition();

                        lyric_show_view.setNextShow(currentPosition);

                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    removeMessages(SHOW_LYRIC);//一定要执行
                    sendEmptyMessage(SHOW_LYRIC);

                    break;
                case PROGRESS:
                    try {
                        int currentPosition = service.getCurrentPosition();
                        int duration = service.getDuration();

                        //更新时间
                        tvTime.setText(utils.stringForTime(currentPosition) + "/" + utils.stringForTime(duration));


                        seekbarAudio.setProgress(currentPosition);

                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    removeMessages(PROGRESS);
                    sendEmptyMessageDelayed(PROGRESS, 1000);

                    break;
            }
        }
    };
    LyricUtils lyricUtils = new LyricUtils();
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showViewData(MediaItem mediaItem) {
        int duration = (int) mediaItem.getDuration();
        seekbarAudio.setMax(duration);

        tvName.setText(mediaItem.getName());
        tvArtist.setText(mediaItem.getName());

        //发消息
        handler.sendEmptyMessage(PROGRESS);


        String audioPath = mediaItem.getData();//音频的地址--//mnt/sdcard/beijingbeijing.mp3
        audioPath = audioPath.substring(0, audioPath.lastIndexOf("."));////mnt/sdcard/beijingbeijing
        File file = new File(audioPath + ".lrc");//mnt/sdcard/beijingbeijing.lrc
        if (!file.exists()) {
            file = new File(audioPath + ".txt");//mnt/sdcard/beijingbeijing.txt
        }
        //解析歌词-列表
        lyricUtils.readLyricFile(file);

        //得到歌词列表
        lyric_show_view.setData(lyricUtils.getLyrics());

        if (lyricUtils.isExistsLyric()) {
            //发消息，歌词同步
            handler.sendEmptyMessage(SHOW_LYRIC);
        } else {
            handler.removeMessages(SHOW_LYRIC);
        }
        lyric_show_view.setNextShow(0);

        try {
            setupVisualizerFxAndUi();
        } catch (RemoteException e) {
            e.printStackTrace();
        }


    }


    /**
     * 生成一个VisualizerView对象，使音频频谱的波段能够反映到 VisualizerView上
     */
    private void setupVisualizerFxAndUi() throws RemoteException {

        int audioSessionid = service.getAudioSessionId();
        System.out.println("audioSessionid==" + audioSessionid);
        mVisualizer = new Visualizer(audioSessionid);
        // 参数内必须是2的位数
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        // 设置允许波形表示，并且捕获它
        mBaseVisualizerView.setVisualizer(mVisualizer);
        mVisualizer.setEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            mVisualizer.release();
        }
    }

    @Override
    protected void onDestroy() {

        //取消绑定服务
        if (conn != null) {
            unbindService(conn);
            conn = null;
        }

        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        //把所有消息和任务从栈中移除
        handler.removeCallbacksAndMessages(null);

        // 2.Eventbus取消注册
        EventBus.getDefault().unregister(this);

        super.onDestroy();
    }

    private void bindAndStartService() {
        LogUtil.e("Activity bindService");
        Intent intent = new Intent(this, MusicPlayerService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
        startService(intent);//屏蔽服务被创建多次
    }

    private void getData() {
        //true:状态栏进入当前页面，false:从列表进入
        notification = getIntent().getBooleanExtra("notification", false);
        if (!notification) {
            //列表
            position = getIntent().getIntExtra("position", 0);
        }


    }
}
