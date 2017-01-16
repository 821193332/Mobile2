package com.xiangmu.l.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.xiangmu.l.IMusicPlayerService;
import com.xiangmu.l.R;
import com.xiangmu.l.activity.SystemAudioPlayer;
import com.xiangmu.l.bean.MediaItem;
import com.xiangmu.l.utils.CacheUtils;
import com.xiangmu.l.utils.LogUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;


public class MusicPlayerService extends Service {
    public static final String ACTION_OPENAUDIO = "com.xiangmu.l_openAudo";
    private ArrayList<MediaItem> mediaItems;
    private MediaItem mediaItem;
    private int position;
    private MediaPlayer mediaPlayer;

    /**
     * 顺序播放
     */
    public static final int REPEAT_NORMAL = 0;
    /**
     * 单曲循环
     */
    public static final int REPEAT_SINGLE = 1;


    /**
     * 全部循环
     */
    public static final int REPEAT_ALL = 2;

    /**
     * 播放模式
     */
    private int playmode = REPEAT_NORMAL;

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.e("服务创建了onCreate()=====");
        playmode = CacheUtils.getPlaymdoe(this,"playmode");
        //得到音频
        getDataFromLocal();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.e("服务销毁了-onDestroy");
    }

    private void getDataFromLocal() {

        //在子线程加载数据
        new Thread() {
            @Override
            public void run() {
                super.run();
                mediaItems = new ArrayList<MediaItem>();//创建集合
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] objects = {
                        MediaStore.Audio.Media.DISPLAY_NAME,//在sdcard显示的文件名称
                        MediaStore.Audio.Media.DURATION,//视频持续播放的时长，毫秒
                        MediaStore.Audio.Media.SIZE,//视频的大小byte
                        MediaStore.Audio.Media.DATA,//视频的播放路径
                        MediaStore.Audio.Media.ARTIST//艺术家
                };
                Cursor cursor = getContentResolver().query(uri, objects, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {


                        String name = cursor.getString(0);
                        long duration = cursor.getLong(1);
                        long size = cursor.getLong(2);
                        String data = cursor.getString(3);
                        String artist = cursor.getString(4);
                        MediaItem mediaItem = new MediaItem(name, duration, size, data, artist);

                        //添加到集合中
                        mediaItems.add(mediaItem);

                    }

                }
                //发消息-加载数据完成

            }
        }.start();

    }

    IMusicPlayerService.Stub stub = new IMusicPlayerService.Stub() {
        /**
         * 代表服务的实例
         */
        MusicPlayerService service = MusicPlayerService.this;

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public void start() throws RemoteException {
            service.start();
        }

        @Override
        public void pause() throws RemoteException {
            service.pause();
        }

        @Override
        public String getArtist() throws RemoteException {
            return service.getArtist();
        }

        @Override
        public String getAudioName() throws RemoteException {
            return service.getAudioName();
        }

        @Override
        public String getAudioPath() throws RemoteException {
            return service.getAudioPath();
        }

        @Override
        public int getDuration() throws RemoteException {
            return service.getDuration();
        }

        @Override
        public int getCurrentPosition() throws RemoteException {
            return service.getCurrentPosition();
        }

        @Override
        public void seekTo(int position) throws RemoteException {
            service.seekTo(position);
        }

        @Override
        public void setPlaymode(int playmode) throws RemoteException {
            service.setPlaymode(playmode);
        }

        @Override
        public int getPlaymode() throws RemoteException {
            return service.getPlaymode();
        }

        @Override
        public void pre() throws RemoteException {
            service.pre();
        }

        @Override
        public void next() throws RemoteException {
            service.next();
        }

        @Override
        public void openAudio(int position) throws RemoteException {
            service.openAudio(position);
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return mediaPlayer.isPlaying();
        }

        @Override
        public void notifyChang() throws RemoteException {
            service.notifyChange(ACTION_OPENAUDIO);
        }

        @Override
        public int getAudioSessionId() throws RemoteException {
            return mediaPlayer.getAudioSessionId();
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //千万不要忘记写
        LogUtil.e("服务绑定了onBind=====");
        return stub;
    }

    /**
     * 根据对应的位置播放音乐
     *
     * @param position
     */
    private void openAudio(int position) {
        if (mediaItems != null && mediaItems.size() > 0) {
            this.position = position;
            //已经加载音频-并且有
            mediaItem = mediaItems.get(position);

            //MediaPlayer
            //先释放-停止
            if (mediaPlayer != null) {
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            }

            try {

                //重新创建和设置
                mediaPlayer = new MediaPlayer();
                //设置监听
                //设置准备监听
                mediaPlayer.setOnPreparedListener(new MyOnPreparedListener());
                //设置播放完成监听
                mediaPlayer.setOnCompletionListener(new MyOnCompletionListener());
                //设置出错监听
                mediaPlayer.setOnErrorListener(new MyOnErrorListener());
                //设置播放地址

                mediaPlayer.setDataSource(mediaItem.getData());

                //准备
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }


        } else {
            Toast.makeText(this, "您手机没有音频或者还没有加载完成", Toast.LENGTH_SHORT).show();
        }

    }

    class MyOnErrorListener implements MediaPlayer.OnErrorListener {

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            next();
            return true;
        }
    }

    private boolean isOnCompletion = false;
    class MyOnCompletionListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            isOnCompletion = true;
            next();
            isOnCompletion = false;
        }
    }

    class MyOnPreparedListener implements MediaPlayer.OnPreparedListener {

        @Override
        public void onPrepared(MediaPlayer mp) {
            //发广播
            notifyChange(ACTION_OPENAUDIO);
            start();

        }
    }

    /**
     * 发广播
     *
     * @param action
     */
    private void notifyChange(String action) {
//        Intent intent = new Intent(action);
//        sendBroadcast(intent);

        EventBus.getDefault().post(mediaItem);

    }


    private NotificationManager manager;

    /**
     * 播放音乐
     */
    private void start() {
        mediaPlayer.start();

        //状态显示播放状态
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //延期意图
        Intent intent = new Intent(this, SystemAudioPlayer.class);
        intent.putExtra("notification", true);
        PendingIntent peningIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification notification = new Notification.Builder(this).setSmallIcon(R.drawable.notification_music_playing)
                .setContentTitle("321音乐")
                .setContentText("正在播放:" + getAudioName())
                .setContentIntent(peningIntent)

                .build();
        //设置点击后还存在
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        manager.notify(1, notification);


    }

    /**
     * 暂停音乐
     */
    private void pause() {
        mediaPlayer.pause();
        //取消显示
        manager.cancel(1);
    }

    /**
     * 得到艺术家
     *
     * @return
     */
    private String getArtist() {
        if (mediaItem != null) {
            return mediaItem.getArtist();
        }
        return "";
    }

    /**
     * 得到歌曲名称
     *
     * @return
     */
    private String getAudioName() {
        if (mediaItem != null) {
            return mediaItem.getName();
        }
        return "";
    }


    /**
     * 得到歌曲的路径
     *
     * @return
     */
    private String getAudioPath() {
        return "";
    }

    /**
     * 得到总时长
     *
     * @return
     */
    private int getDuration() {
        return mediaPlayer.getDuration();
    }

    /**
     * 得到当前进度
     *
     * @return
     */
    private int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    /**
     * 拖动视频
     *
     * @param position
     */
    private void seekTo(int position) {
        mediaPlayer.seekTo(position);
    }

    /**
     * 设置播放模式
     *
     * @param playmode
     */
    private void setPlaymode(int playmode) {
        this.playmode = playmode;
        CacheUtils.putPlaymode(this,"playmode",playmode);

    }

    /**
     * 得到播放模式
     *
     * @return
     */
    private int getPlaymode() {
        return playmode;
    }

    /**
     * 播放上一个视频
     */
    private void pre() {
        //1.根据不同的播放模式设置下一个应该有的位置
        setPrePosition();

        //2.根据当前的位置打开
        openPreAudio();
    }

    private void openPreAudio() {
        int playmode = getPlaymode();
        if (playmode == MusicPlayerService.REPEAT_NORMAL) {
            //顺序
            if(position >= 0){
                openAudio(position);
            }else{
                position = 0;//有必要代码
            }
        } else if (playmode == MusicPlayerService.REPEAT_SINGLE) {
            //单曲循环
            openAudio(position);

        } else if (playmode == MusicPlayerService.REPEAT_ALL) {
            //全部
            openAudio(position);
        } else {
            //顺序
            if(position >= 0){
                openAudio(position);
            }else{
                position = 0;//有必要代码
            }
        }
    }

    private void setPrePosition() {
        int playmode = getPlaymode();
        if (playmode == MusicPlayerService.REPEAT_NORMAL) {
            //顺序
            position --;
        } else if (playmode == MusicPlayerService.REPEAT_SINGLE) {
            //单曲循环
            if(!isOnCompletion){
                position --;
                if(position < 0){
                    position = mediaItems.size()-1;
                }
            }


        } else if (playmode == MusicPlayerService.REPEAT_ALL) {
            //全部
            position --;
            if(position < 0){
                position = mediaItems.size()-1;
            }
        } else {
            //顺序
            position --;
        }


    }

    /**
     * 播放下一个视频-手动
     */
    private void next() {
        //1.根据不同的播放模式设置下一个应该有的位置
        setNextPosition();

        //2.根据当前的位置打开
        openNextAudio();

    }

    private void openNextAudio() {
        int playmode = getPlaymode();
        if (playmode == MusicPlayerService.REPEAT_NORMAL) {
            //顺序
            if(position < mediaItems.size()){
                openAudio(position);
            }else{
                position = mediaItems.size()-1;//有必要代码
            }
        } else if (playmode == MusicPlayerService.REPEAT_SINGLE) {
            //单曲循环
            openAudio(position);

        } else if (playmode == MusicPlayerService.REPEAT_ALL) {
            //全部
            openAudio(position);
        } else {
            //顺序
            if(position < mediaItems.size()){
                openAudio(position);
            }else{
                position = mediaItems.size()-1;//有必要代码
            }
        }


    }

    private void setNextPosition() {
        int playmode = getPlaymode();
        if (playmode == MusicPlayerService.REPEAT_NORMAL) {
            //顺序
            position ++;
        } else if (playmode == MusicPlayerService.REPEAT_SINGLE) {
            //单曲循环
            if(!isOnCompletion){
                position ++;
                if(position >mediaItems.size()-1){
                    position = 0;
                }
            }


        } else if (playmode == MusicPlayerService.REPEAT_ALL) {
            //全部
            position ++;
            if(position >mediaItems.size()-1){
                position = 0;
            }
        } else {
            //顺序
            position ++;
        }


    }


}
