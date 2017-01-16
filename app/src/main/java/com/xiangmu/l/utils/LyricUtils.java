package com.xiangmu.l.utils;

import com.xiangmu.l.bean.Lyric;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class LyricUtils {
    /**
     * 得到歌词列表
     *
     * @return
     */
    public ArrayList<Lyric> getLyrics() {
        return lyrics;
    }

    private  ArrayList<Lyric> lyrics;
    public static long lyricTime;

    /**
     * 是否有歌词存在
     *
     * @return
     */
    public boolean isExistsLyric() {
        return isExistsLyric;
    }

    private boolean isExistsLyric = false;

    public void readLyricFile(File file) {
        if (file == null || !file.exists()) {
            //没有歌词文件
            lyrics = new ArrayList<>();
            isExistsLyric = false;
        } else {
            //有歌词文件
            lyrics = new ArrayList<>();
            isExistsLyric = true;

            try {
                String line;
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), getCharset(file)));
                while ((line = reader.readLine()) != null) {
                    //解析这一句--[02:04.12][03:37.32][00:59.73]我在这里欢笑
                    line = paraseLyric(line);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            //解析歌词


            //for

            //排序
            Collections.sort(lyrics, new Comparator<Lyric>() {
                @Override
                public int compare(Lyric o1, Lyric o2) {
                    if(o1.getTimePoint() <o2.getTimePoint() ){
                        return -1;
                    }else if(o1.getTimePoint() > o2.getTimePoint()){
                        return  1;
                    }else {
                        return  0;
                    }
                }
            });


            //计算每一句高亮显示的时间
            for (int i=0;i<lyrics.size();i++){
                //第一句
                Lyric oneLyric = lyrics.get(i);

                //第二句
                if(i+1 < lyrics.size()){
                    Lyric twoLyric = lyrics.get(i+1);
                    oneLyric.setSleepTime(twoLyric.getTimePoint() - oneLyric.getTimePoint());
                }
            }


        }

    }

    /**
     * 判断文件编码
     * @param file 文件
     * @return 编码：GBK,UTF-8,UTF-16LE
     */
    public String getCharset(File file) {
        String charset = "GBK";
        byte[] first3Bytes = new byte[3];
        try {
            boolean checked = false;
            BufferedInputStream bis = new BufferedInputStream(
                    new FileInputStream(file));
            bis.mark(0);
            int read = bis.read(first3Bytes, 0, 3);
            if (read == -1)
                return charset;
            if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
                charset = "UTF-16LE";
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xFE
                    && first3Bytes[1] == (byte) 0xFF) {
                charset = "UTF-16BE";
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xEF
                    && first3Bytes[1] == (byte) 0xBB
                    && first3Bytes[2] == (byte) 0xBF) {
                charset = "UTF-8";
                checked = true;
            }
            bis.reset();
            if (!checked) {
                int loc = 0;
                while ((read = bis.read()) != -1) {
                    loc++;
                    if (read >= 0xF0)
                        break;
                    if (0x80 <= read && read <= 0xBF)
                        break;
                    if (0xC0 <= read && read <= 0xDF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF)
                            continue;
                        else
                            break;
                    } else if (0xE0 <= read && read <= 0xEF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) {
                            read = bis.read();
                            if (0x80 <= read && read <= 0xBF) {
                                charset = "UTF-8";
                                break;
                            } else
                                break;
                        } else
                            break;
                    }
                }
            }
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return charset;
    }

    /**
     * [02:04.12][03:37.32][00:59.73]我在这里欢笑
     *
     * @param line
     * @return
     */
    private String paraseLyric(String line) {
        int pos1 = line.indexOf("[");//0,如果没有返回-1
        int pos2 = line.indexOf("]");//9,如果没有返回-1
        if (pos1 == 0 && pos2 != -1) {//至少有一句

            long[] longTimes = new long[getTagCount(line)];//long类型的时间

            String strTime = line.substring(pos1+1,pos2);//02:04.12
            longTimes[0] = strTime2Long(strTime);//02:04.12-->long类型的时间
            if(longTimes[0] ==-1){
                return "";
            }

            int i = 1;//3
            String content = line;//[02:04.12][03:37.32][00:59.73]我在这里欢笑
            while (pos1 == 0 && pos2 != -1){

                 content = content.substring(pos2+1);//[03:37.32][00:59.73]我在这里欢笑--->[00:59.73]我在这里欢笑--->我在这里欢笑
                 pos1 = content.indexOf("[");//0,如果没有返回-1-->-1
                 pos2 = content.indexOf("]");//9,如果没有返回-1-->-1

                if(pos2 != -1){//还有其他句
                    strTime = content.substring(pos1+1,pos2);//03:37.32--->00:59.73
                    longTimes[i] = strTime2Long(strTime);//00:59.73-->long毫秒
                    if(longTimes[i] ==-1){
                        return "";
                    }
                    i++;

                }

            }

            Lyric lyric = new Lyric();
            for (int j=0;j<longTimes.length;j++){
                if(longTimes[j] != 0){
                    //设置歌词内容
                    lyric.setContent(content);
                    lyric.setTimePoint(longTimes[j]);
                    //添加到集合中
                    lyrics.add(lyric);
                    //重新创建
                    lyric = new Lyric();
                }

            }


            return content;

        }

        return null;
    }

    /**
     * 02:04.12-->long类型毫秒
     * @param strTime
     * @return
     */
    private long strTime2Long(String strTime) {
        long result = -1;
        try {
            //1.把02:04.12 按照:切割成，02和04.12
            String[] s1  = strTime.split(":");
            //2.把04.12按照.切割成04和12
            String[] s2 = s1[1].split("\\.");
            //3.转换成毫秒
            //分
            long min = Long.valueOf(s1[0]);
            //秒
            long second = Long.valueOf(s2[0]);

            //毫秒
            long mil = Long.valueOf(s2[1]);

            lyricTime=result = min*60*1000 + second * 1000 + mil * 10;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }


        return result;
    }

    /**
     * 判断有多少句
     * [02:04.12][03:37.32][00:59.73]我在这里欢笑
     *
     * @param line
     * @return
     */
    private int getTagCount(String line) {
        int number = 0;
        String[] left = line.split("\\[");
        String[] right = line.split("\\]");
        if (left.length == 0 && right.length == 0) {
            number = 1;
        } else if (left.length > right.length) {
            number = left.length;
        } else {
            number = right.length;
        }
        return number;
    }
}
