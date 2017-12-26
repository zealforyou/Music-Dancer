package com.zz.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import java.io.IOException;

/**
 * Created by Administrator on 2017/12/10.
 */

public class HelloView extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder mHolder;
    private boolean init;
    private Visualizer mVisualizer;
    private MediaPlayer mediaPlayer;
    private Rect mRect;
    private float[] mPoints;
    private Paint mForePaint;
    private TextPaint mTextPain;
    private PorterDuffXfermode CLEAR;
    private PorterDuffXfermode SRC;

    public HelloView(Context context) {
        super(context);
        init();
    }

    public HelloView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HelloView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        CLEAR = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
        SRC = new PorterDuffXfermode(PorterDuff.Mode.SRC);
        mForePaint = new Paint();
        mTextPain = new TextPaint();
        mTextPain.setColor(Color.GREEN);
        mTextPain.setTextSize(60);
        mForePaint.setColor(Color.RED);
        mRect = new Rect();
        mHolder = getHolder();
        mHolder.addCallback(this);
        mediaPlayer = new MediaPlayer();
        mVisualizer = new Visualizer(mediaPlayer.getAudioSessionId());
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                Log.i("www", "" + waveform.length);
                if (init) {
                    Canvas canvas = mHolder.lockCanvas();
                    drawLine(canvas, waveform);
                    mHolder.unlockCanvasAndPost(canvas);
                }
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {

            }
        }, Visualizer.getMaxCaptureRate() / 2, true, false);
    }

    private void initMedia() {
        String path = "http://192.168.2.213:8080/mp3res/Big%20Z,Jackson%20Breit%20-%20Fool%20For%20You.mp3";
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            mVisualizer.setEnabled(true);
            mediaPlayer.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        init = true;
        new Thread(() -> {
            initMedia();
        }).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        ViewGroup v;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        init = false;
        mHolder = null;
        mVisualizer.setEnabled(false);
        mVisualizer.release();
        mVisualizer = null;
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    private void drawLine(Canvas canvas, byte[] mBytes) {
        if (mBytes == null) {
            return;
        }
        mForePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawPaint(mForePaint);
        mForePaint.setXfermode(SRC);
        if (mPoints == null || mPoints.length < mBytes.length * 4) {
            //mPoints主要用来存储要画直线的4个坐标（每个点两个坐标，所以一条直线需要两个点，也就是4个坐标）
            mPoints = new float[mBytes.length * 4];
        }
        mRect.set(0, 0, getWidth(), getHeight());
        //xOrdinate是x轴的总刻度，因为一次会传输过来1024个数据，每两个数据要画成一条直线
        // ，所以x轴我们分成1023段。你要是觉的太多了，也可以像我一样除以2，看自己需求了。
        int xOrdinate = (mBytes.length - 1) / 20;
        //以下的for循环将利用mBytes[i] mBytes[i+1] 这两个数据去生成4个坐标值，从而在刻画成两个坐标，来画线条
        for (int i = 0; i < xOrdinate; i++) {

            //第i个点在总横轴上的坐标，
            mPoints[i * 4] = mRect.width() * i / xOrdinate;
            mPoints[i * 4 + 1] = mRect.height() / 2 + ((byte) (mBytes[i] + 128)) * (mRect.height() / 4) /
                    128;
            //以下就是刻画第i+1个数据了，原理和刻画第i个一样
            mPoints[i * 4 + 2] = mRect.width() * (i + 1) / xOrdinate;
            mPoints[i * 4 + 3] = mRect.height() / 2 + ((byte) (mBytes[i + 1] + 128)) * (mRect.height() / 4)
                    / 128;
        }
        canvas.drawLines(mPoints, mForePaint);

    }
}
