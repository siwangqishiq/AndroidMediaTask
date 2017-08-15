package com.xinlan.audiodemo;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 2. 在 Android 平台使用 AudioRecord 和 AudioTrack API 完成音频
 * PCM 数据的采集和播放，并实现读写音频 wav 文件
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private View mStartBtn;
    private View mEndBtn;
    private View mPlayBtn;

    private AudioCapture mCapture;

    private static final int RECORD_PERMISSON_RECORD = 1;

    private File mAudioFile;
    private FileOutputStream fos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStartBtn = findViewById(R.id.start_record);
        mEndBtn = findViewById(R.id.end_record);
        mPlayBtn = findViewById(R.id.play_audio);

        mStartBtn.setOnClickListener(this);
        mEndBtn.setOnClickListener(this);
        mPlayBtn.setOnClickListener(this);

        mCapture = new AudioCapture();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    RECORD_PERMISSON_RECORD);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_record:
                startRecord();
                break;
            case R.id.end_record:
                endRecord();
                break;
            case R.id.play_audio:
                playAudio();
                break;
        }//end switch
    }

    protected void playAudio(){
        if(mAudioFile == null)
            return;

        new Thread(new Runnable() {
            @Override
            public void run() {
                doPlay();
            }
        }).start();
    }

    private void doPlay(){
        try {
            AudioPlayer player = new AudioPlayer();
            player.startPlayer();
            int bufferSize = 2 *player.getMinBufferSize();
            FileInputStream ips = new FileInputStream(mAudioFile);
            int len = 0;
            byte[] buffer = new byte[bufferSize];
            while((len = ips.read(buffer,0,bufferSize))!=-1){
                //System.out.println("read file len = "+len);
                player.play(buffer, 0 , len);
            }//end while
            player.stopPlayer();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void startRecord() {
        mAudioFile = AudioFileUtil.getAudioPcmFile("panyi_record");
        if (mAudioFile == null) {
            return;
        }

        //开通输出流到指定的文件
        try {
            fos = new FileOutputStream(mAudioFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        mCapture.setOnAudioFrameCapturedListener(new AudioCapture.OnAudioFrameCapturedListener() {
            @Override
            public void onAudioFrameCaptured(byte[] audioData, int buffSize, int len) {
                //System.out.println("record 音频数据 ---> "+audioData.length);
                try {
                    fos.write(audioData, 0, len);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onRecordEnd() {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        mCapture.startCapture();
    }

    protected void endRecord() {
        mCapture.stopCapture();
        Toast.makeText(this, "保存文件到" + mAudioFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
    }
}//end class
