package com.xinlan.mediaapidemo;

import android.media.MediaExtractor;
import android.media.MediaMuxer;

import java.io.IOException;

/**
 * Created by panyi on 2017/8/22.
 */

public class ReadMediaFile {

    public void readFile(){
        MediaExtractor extractor = new MediaExtractor();
        try {
            extractor.setDataSource("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}//end class
