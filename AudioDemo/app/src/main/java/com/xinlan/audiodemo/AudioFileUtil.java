package com.xinlan.audiodemo;

import android.os.Environment;

import java.io.File;
import java.io.IOException;

/**
 * Created by panyi on 2017/8/15.
 */

public class AudioFileUtil {
    /**
     * 创建一个用户存贮音频原始数据的文件
     *
     * @param filename
     * @return
     */
    public static File getAudioPcmFile(final String filename) {
        File audioFile = null;
        File fpath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/data/record/");
        fpath.mkdirs();//创建文件夹
        try {
            audioFile = new File(fpath ,filename + ".pcm");
            audioFile.deleteOnExit();
            audioFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return audioFile;
    }
}
