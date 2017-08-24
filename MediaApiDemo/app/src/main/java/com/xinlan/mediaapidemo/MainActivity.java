package com.xinlan.mediaapidemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 参考文章
 *
 * http://ticktick.blog.51cto.com/823160/1710743
 *
 * https://github.com/Jhuster/Android/tree/master/MediaDemo
 *
 *
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String SDCARD_PATH = Environment.getExternalStorageDirectory().getPath()+"/";
    private static final String name = "note.mp4";

    private View mExtratorBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission();
        mExtratorBtn = findViewById(R.id.extrator);
        mExtratorBtn.setOnClickListener(this);
    }


    protected void requestPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA , Manifest.permission.WRITE_EXTERNAL_STORAGE},100);
            }
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.extrator:
                extratorFile(SDCARD_PATH+name);
                break;
        }
    }

    protected void extratorFile(final String filepath){
        new Thread(new Runnable() {
            @Override
            public void run() {
                doExtratorFile(filepath);
            }
        }).start();
    }

    /**
     * 分离视频文件
     * @param filepath
     */
    protected void doExtratorFile(final String filepath){
        MediaExtractor mediaExtractor = new MediaExtractor();
        try {
            mediaExtractor.setDataSource(filepath);
            int trackCount = mediaExtractor.getTrackCount();
            int videoTrackIndex = -1;
            int audioTrackIndex = -1;
            int framerate = 0;
            MediaFormat videoFormat = null;

            for(int i = 0;i<trackCount ; i++){
                MediaFormat format = mediaExtractor.getTrackFormat(i);
                System.out.println("format = "+format);

                String mime = format.getString(MediaFormat.KEY_MIME);
                if(mime.startsWith("video/")) {
                    videoTrackIndex = i;
                    framerate = format.getInteger(MediaFormat.KEY_FRAME_RATE);
                    videoFormat = format;
                    System.out.println("frame_rate = "+framerate);
                } else if(mime.startsWith("audio/")) {
                    audioTrackIndex = i;
                }
            }//end for i

            if(videoTrackIndex< 0){
                mediaExtractor.release();
                return;
            }

            mediaExtractor.selectTrack(videoTrackIndex);

            MediaMuxer muxer = new MediaMuxer(SDCARD_PATH+"output.mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            videoTrackIndex = muxer.addTrack(videoFormat);
            muxer.start();

            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            info.presentationTimeUs = 0;
            ByteBuffer buffer = ByteBuffer.allocate(500 * 1024);

            while(true){
                int sampleSize = mediaExtractor.readSampleData(buffer, 0);
                if(sampleSize < 0) {
                    break;
                }

                mediaExtractor.advance();

                /**
                 * info.size 必须填入数据的大小
                 info.flags 需要给出是否为同步帧/关键帧
                 info.presentationTimeUs 必须给出正确的时间戳，注意单位是 us，例如，对于帧率为 x f/s 的视频而言，时间戳的间隔就是 1000/x ms
                 */
                info.offset = 0;
                info.size = sampleSize;
                info.flags = MediaCodec.BUFFER_FLAG_SYNC_FRAME;
                info.presentationTimeUs += 1000*1000/framerate;
                System.out.println("write data sample size = "+sampleSize);
                muxer.writeSampleData(videoTrackIndex , buffer , info);
            }//end while

            muxer.stop();
            muxer.release();
            mediaExtractor.release();

            System.out.println("weite file complete! ");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}//end class
