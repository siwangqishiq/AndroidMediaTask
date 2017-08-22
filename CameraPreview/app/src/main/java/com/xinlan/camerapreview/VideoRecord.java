package com.xinlan.camerapreview;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by 潘易 on 2017/8/21.
 */

public class VideoRecord {
    public static final String TAG = VideoRecord.class.getSimpleName();
    private boolean mVideoEncoderLoop;
    private boolean mRecordPause;
    private File mRecordFile;
    private FileOutputStream mFileSteam;
    private BlockingQueue<byte[]> mQueue  = new LinkedBlockingQueue<byte[]>();

    public VideoRecord() {
        mVideoEncoderLoop = false;
        mRecordPause = false;
    }

    public void startRecord(File file) throws IOException {
        if (mVideoEncoderLoop) {
            throw new RuntimeException("必须先停止");
        }

        this.mRecordFile = file;
        mVideoEncoderLoop = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "开始视频记录");
                try {
                    mFileSteam = new FileOutputStream(mRecordFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                while (mVideoEncoderLoop) {
                    try {
                        byte[] data = mQueue.take();
                        if (mRecordPause) {//暂停时 不存数据
                            continue;
                        }
                        recordVideoFile(data);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }//end while
                recordEnd();
            }
        }).start();
    }

    private void recordVideoFile(byte[] data) {
            try {
                if(mFileSteam!=null){
                    Log.i(TAG, "记录视频数据");
                    mFileSteam.write(data, 0 , data.length);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private void recordEnd() {
        try {
            if (mFileSteam != null) {
                mFileSteam.close();
                mFileSteam = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "停止记录");
        Log.i(TAG, "保存文件到"+mRecordFile.getAbsolutePath());
    }

    public void putVideoData(byte[] data) {
        if(!mVideoEncoderLoop || mQueue==null)
            return;
        try {
            Log.i(TAG, "put video data");
            mQueue.put(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stopRecord(Context context) {
        mVideoEncoderLoop = false;
        mRecordPause = false;
        Log.i(TAG, "run: 停止音频编码");
        try {
            mQueue.put(new byte[1]);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Toast.makeText(context,"保存文件到"+mRecordFile.getAbsolutePath(),Toast.LENGTH_LONG).show();
    }

    /**
     * 创建一个用户存贮音频原始数据的文件
     *
     * @param filename
     * @return
     */
    public static File getSaveFile(final String filename) {
        File saveFile = null;
        File fpath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/data/record/");
        fpath.mkdirs();//创建文件夹
        try {
            saveFile = new File(fpath ,filename + ".yuv");
            saveFile.deleteOnExit();
            saveFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return saveFile;
    }
}//end class
