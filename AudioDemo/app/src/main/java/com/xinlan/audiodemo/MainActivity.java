package com.xinlan.audiodemo;

import android.Manifest;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.xinlan.audiodemo.audio.AudioCapture;
import com.xinlan.audiodemo.audio.AudioFileUtil;
import com.xinlan.audiodemo.audio.AudioPlayer;
import com.xinlan.audiodemo.wave.WaveFileHeader;
import com.xinlan.audiodemo.wave.WaveFileReader;
import com.xinlan.audiodemo.wave.WaveFileWriter;

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
    public static final String TAG = MainActivity.class.getSimpleName();
    private View mStartBtn;
    private View mEndBtn;
    private View mPlayBtn;
    private View mSaveBtn;
    private View mReadBtn;

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
        mReadBtn = findViewById(R.id.read_audio_file);
        mSaveBtn = findViewById(R.id.save_audio_file);

        mStartBtn.setOnClickListener(this);
        mEndBtn.setOnClickListener(this);
        mPlayBtn.setOnClickListener(this);
        mReadBtn.setOnClickListener(this);
        mSaveBtn.setOnClickListener(this);

        mCapture = new AudioCapture();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
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
            case R.id.read_audio_file:
                readAudioFile();
                break;
            case R.id.save_audio_file:
                saveAsWaveFile();
                break;
        }//end switch
    }

    protected void playAudio() {
        if (mAudioFile == null)
            return;

        new Thread(new Runnable() {
            @Override
            public void run() {
                doPlay();
            }
        }).start();
    }

    private void doPlay() {
        try {
            AudioPlayer player = new AudioPlayer();
            player.startPlayer();
            int bufferSize = 2 * player.getMinBufferSize();
            FileInputStream ips = new FileInputStream(mAudioFile);
            int len = 0;
            byte[] buffer = new byte[bufferSize];
            while ((len = ips.read(buffer, 0, bufferSize)) != -1) {
                //System.out.println("read file len = "+len);
                player.play(buffer, 0, len);
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

    protected void readAudioFile() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String readFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/caocao.wav";
                WaveFileReader waveFileReader = new WaveFileReader();
                try {
                    boolean ret = waveFileReader.openFile(readFilePath);
                    if (!ret) {
                        Log.d(TAG, "Read file error");
                    }

                    WaveFileHeader header = waveFileReader.getWaveFileHeader();
                    AudioPlayer player = new AudioPlayer();
                    player.startPlayer(AudioManager.STREAM_MUSIC,header.mSampleRate,
                            AudioFormat.CHANNEL_IN_STEREO,AudioFormat.ENCODING_PCM_16BIT);
                    int bufSize = player.getMinBufferSize();
                    byte[] buff = new byte[bufSize];
                    int len = 0;
                    while((len = waveFileReader.readData(buff,0,bufSize))!=-1){
                        player.play(buff, 0, len);
                    }//end while
                    waveFileReader.closeFile();
                    player.stopPlayer();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    protected void saveAsWaveFile() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                doSaveAsWaveFile();
            }
        }).start();
    }

    private void doSaveAsWaveFile() {
        final File waveFile = AudioFileUtil.getAudioWaveFile("panyi");
        if (waveFile == null && mAudioFile == null) {
            return;
        }
        WaveFileWriter waveFileWriter = new WaveFileWriter();
        try {
            FileInputStream fis = new FileInputStream(mAudioFile);

            waveFileWriter.openFile(waveFile.getAbsolutePath(), AudioCapture.DEFAULT_SAMPLE_RATE, AudioCapture.DEFAULT_CHANNEL_CONFIG,
                    AudioCapture.DEFAULT_AUDIO_FORMAT);

            //write pcm file data
            int len = 0;
            int buffSize = 1024;
            byte[] buff = new byte[buffSize];

            while ((len = fis.read(buff, 0, buffSize)) != -1) {
                waveFileWriter.writeData(buff, 0, len);
            }//end while
            waveFileWriter.closeFile();
            mSaveBtn.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "保存文件为" + waveFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}//end class
