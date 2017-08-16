package com.xinlan.audiodemo.wave;

import android.util.Log;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 读取wav文件
 * Created by panyi on 2017/8/16.
 */
public class WaveFileReader {
    private static final String TAG = WaveFileReader.class.getSimpleName();

    private DataInputStream mDataInputStream;
    private WaveFileHeader mWaveFileHeader;

    public boolean openFile(final String filepath) throws IOException {
        if (mDataInputStream != null) {
            closeFile();
        }
        mDataInputStream = new DataInputStream(new FileInputStream(filepath));
        return readHeader();
    }

    public boolean openFile(InputStream input) throws IOException {
        if (mDataInputStream != null) {
            closeFile();
        }
        mDataInputStream = new DataInputStream(input);
        return readHeader();
    }

    public void closeFile() throws IOException {
        if (mDataInputStream != null) {
            mDataInputStream.close();
            mDataInputStream = null;
        }
    }

    public WaveFileHeader getWaveFileHeader() {
        return mWaveFileHeader;
    }

    /**
     * 从wave文件中读取文件头
     *
     * @return
     */
    private boolean readHeader() {
        if (mDataInputStream == null) {
            return false;
        }

        WaveFileHeader header = new WaveFileHeader();

        byte[] intValue = new byte[4];
        byte[] shortValue = new byte[2];

        try {
            header.mChunkID = "" + (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte() +
                    (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte();
            Log.d(TAG, "Read file chunkID:" + header.mChunkID);

            mDataInputStream.read(intValue);
            header.mChunkSize = byteArrayToInt(intValue);
            Log.d(TAG, "Read file chunkSize:" + header.mChunkSize);

            header.mFormat = "" + (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte() +
                    (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte();
            Log.d(TAG, "Read file format:" + header.mFormat);

            header.mSubChunk1ID = "" + (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte();
            Log.d(TAG, "Read fmt chunkID:" + header.mSubChunk1ID);

            mDataInputStream.read(intValue);
            header.mSubChunk1Size = byteArrayToInt(intValue);
            Log.d(TAG, "Read fmt chunkSize:" + header.mSubChunk1Size);

            mDataInputStream.read(shortValue);
            header.mAudioFormat = byteArrayToShort(shortValue);
            Log.d(TAG, "Read audioFormat:" + header.mAudioFormat);

            mDataInputStream.read(shortValue);
            header.mNumChannel = byteArrayToShort(shortValue);
            Log.d(TAG, "Read channel number:" + header.mNumChannel);

            mDataInputStream.read(intValue);
            header.mSampleRate = byteArrayToInt(intValue);
            Log.d(TAG, "Read samplerate:" + header.mSampleRate);

            mDataInputStream.read(intValue);
            header.mByteRate = byteArrayToInt(intValue);
            Log.d(TAG, "Read byterate:" + header.mByteRate);

            mDataInputStream.read(shortValue);
            header.mBlockAlign = byteArrayToShort(shortValue);
            Log.d(TAG, "Read blockalign:" + header.mBlockAlign);

            mDataInputStream.read(shortValue);
            header.mBitsPerSample = byteArrayToShort(shortValue);
            Log.d(TAG, "Read bitspersample:" + header.mBitsPerSample);

            header.mSubChunk2ID = "" + (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte() +
                    (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte();
            Log.d(TAG, "Read data chunkID:" + header.mSubChunk2ID);

            mDataInputStream.read(intValue);
            header.mSubChunk2Size = byteArrayToInt(intValue);
            Log.d(TAG, "Read data chunkSize:" + header.mSubChunk2Size);

            Log.d(TAG, "Read wav file success !");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        mWaveFileHeader = header;

        return true;
    }

    private static short byteArrayToShort(byte[] b) {
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    private static int byteArrayToInt(byte[] b) {
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    public int readData(byte[] buffer, int offset, int count) {
        if (mDataInputStream == null || mWaveFileHeader == null) {
            return -1;
        }

        try {
            int nbytes = mDataInputStream.read(buffer, offset, count);
            if (nbytes == -1) {
                return 0;
            }
            return nbytes;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return -1;
    }

}//end class
