package com.test.x264encoderdemo;

public class YuvUtils {
    private static YuvUtils instance = new YuvUtils();

    static {
        System.loadLibrary("yuvutils");
        System.loadLibrary("yuv");
    }

    public static YuvUtils getInstance() {
        return instance;
    }

    private YuvUtils(){

    }

    public native void yuv420Rotate90(byte[] src_data, byte[] dst_data, int width, int height, int degree);


}
