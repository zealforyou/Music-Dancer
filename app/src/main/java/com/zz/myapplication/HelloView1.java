package com.zz.myapplication;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
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

import java.io.IOException;

/**
 * Created by Administrator on 2017/12/10.
 */

public class HelloView1 extends SurfaceView implements SurfaceHolder.Callback {
    public static int TYPE_REC = 0;
    public static int TYPE_LINE = 1;
    public static int TYPE_CIRCLE = 2;
    public static int TYPE_CIRCLE_LINE = 3;
    public static int TYPE_CIRCLE_ONE = 4;

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
    private int colors[];
    private int count = 10;
    private int type = TYPE_REC;
    private boolean isStop;
    private String[] mp3 = {"Closer.mp3", "Fool For You.mp3", "Marry You.mp3"};
    private int currentMusic = 0;

    public HelloView1(Context context) {
        super(context);
        init();
    }

    public HelloView1(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HelloView1(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void setColor() {
        if (colors == null) return;
        for (int i = 0; i < count; i++) {
            colors[i] = Color.rgb((int) (Math.random() * 256), (int) (Math.random() * 256), (int) (Math
                    .random() * 256));
        }
    }

    private void init() {
        colors = new int[count];
        setColor();
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
//                Log.i("www", "" + waveform.length);
                if (init) {
                    Canvas canvas = mHolder.lockCanvas();
                    if (type == TYPE_REC) {
                        drawRec(canvas, waveform);
                    } else if (type == TYPE_LINE) {
                        drawLine(canvas, waveform);
                    } else if (type == TYPE_CIRCLE_LINE) {
                        drawCircleLine(canvas, waveform);
                    } else if (type == TYPE_CIRCLE_ONE) {
                        drawCircleOne(canvas, waveform);
                    } else {
                        drawCircle(canvas, waveform);
                    }
                    mHolder.unlockCanvasAndPost(canvas);


                }
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {

            }
        }, Visualizer.getMaxCaptureRate() / 2, true, false);
    }

    public void start() {
        if (init) {
            if (isStop) {
                try {
                    mediaPlayer.prepare();
                    isStop = false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (!mediaPlayer.isPlaying()) {
                Log.i("www", "!isPlaying");
                mediaPlayer.start();
                mVisualizer.setEnabled(true);
            } else {
                Log.i("www", "isPlaying");
                mediaPlayer.pause();
                mVisualizer.setEnabled(false);
            }
        }
    }

    public void reSet() {
        if (init) {
            setColor();
            mediaPlayer.stop();
            isStop = true;
            start();
        }
    }

    public void switchMusic() {
        if (mediaPlayer != null && switchMusic0()) {
            mediaPlayer.start();
            mVisualizer.setEnabled(true);
        }
    }

    private boolean switchMusic0() {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
        }
        AssetFileDescriptor as = null;
        try {
            as = getContext().getAssets().openFd(mp3[currentMusic % mp3.length]);
            mediaPlayer.setDataSource(as.getFileDescriptor(), as.getStartOffset(), as.getLength());
            mediaPlayer.prepare();
            currentMusic++;
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (as != null) {
                try {
                    as.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;

    }

    public void setType(int type) {
        this.type = type;
    }

    private void initMedia() {
        switchMusic0();
        init = true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        new Thread(() -> {
            initMedia();
        }).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

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

    private void drawRec(Canvas canvas, byte[] mBytes) {
        if (mBytes == null) {
            return;
        }
        mForePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawPaint(mForePaint);
        mForePaint.setXfermode(SRC);
        mRect.set(0, 0, getWidth(), getHeight());
        int count = 10;
        float wd = 1f * mRect.width() / (2 * count + 1);
        for (int i = 0; i < count; i++) {
            float left = wd * (2 * i + 1);
            float right = 2 * wd * (i + 1);
            float top = mRect.height() - mRect.height()
                    * Math.abs((byte) (mBytes[mBytes.length / count * i] + 128)) / 128;
            float bottom = mRect.height();
            mForePaint.setColor(colors[i]);
            canvas.drawRect(left, top, right, bottom, mForePaint);
        }
    }

    private void drawLine(Canvas canvas, byte[] mBytes) {
        if (mBytes == null) {
            return;
        }
        mForePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawPaint(mForePaint);
        mForePaint.setXfermode(SRC);
        if (mPoints == null || mPoints.length < mBytes.length * 4) {
            mPoints = new float[mBytes.length * 4];
        }
        mRect.set(0, 0, getWidth(), getHeight());
        int xOrdinate = (mBytes.length - 1) / 20;
        for (int i = 0; i < xOrdinate; i++) {
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

    private void drawCircle(Canvas canvas, byte[] mBytes) {
        if (mBytes == null) {
            return;
        }
        mForePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawPaint(mForePaint);
        mForePaint.setXfermode(SRC);
        mRect.set(0, 0, getWidth(), getHeight());
        int count = 3;
        int space = 50;
        float maxR = 1f * (mRect.width() - (count + 1) * space) / count / 2;
        for (int i = 0; i < count; i++) {
            float r = maxR * Math.abs((byte) (mBytes[mBytes.length / count * i] + 128)) / 128;
            mForePaint.setColor(colors[i]);
            float cx;
            if (i == 0) {
                cx = maxR + space;
            } else {
                cx = (1 + 2 * i) * maxR + (i + 1) * space;
            }
            float cy = mRect.height() / 2;
            canvas.drawCircle(cx, cy, r, mForePaint);
        }
    }

    private void drawCircleLine(Canvas canvas, byte[] mBytes) {
        if (mBytes == null) {
            return;
        }
        mForePaint.setStyle(Paint.Style.STROKE);
        mForePaint.setAntiAlias(true);
        mForePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawPaint(mForePaint);
        mForePaint.setXfermode(SRC);
        mRect.set(0, 0, getWidth(), getHeight());
        int circleCount = 3;
        int space = 20;
        float maxR = 1f * (mRect.width() - (circleCount + 1) * space) / circleCount / 2;
        int lineCount = 10;
        float rRadio = maxR / lineCount;
        for (int i = 0; i < circleCount; i++) {
            float r = maxR * Math.abs((byte) (mBytes[mBytes.length / circleCount * i] + 128)) / 128;
            int currentCount = (int) (r / rRadio);
            mForePaint.setColor(colors[i]);
            float cx;
            if (i == 0) {
                cx = maxR + space;
            } else {
                cx = (1 + 2 * i) * maxR + (i + 1) * space;
            }
            float cy = mRect.height() / 2;
            for (int j = 0; j < currentCount; j++) {
                canvas.drawCircle(cx, cy, j * rRadio, mForePaint);
            }
        }
        mForePaint.setStyle(Paint.Style.FILL);
        mForePaint.setAntiAlias(false);
    }

    private void drawCircleOne(Canvas canvas, byte[] mBytes) {
        if (mBytes == null) {
            return;
        }
        mForePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawPaint(mForePaint);
        mForePaint.setXfermode(SRC);
        mRect.set(0, 0, getWidth(), getHeight());
        int count = 10;
        float maxR = Math.min(mRect.width(), mRect.height()) / 2f;
        for (int i = 0; i < count; i++) {
            float r = maxR * Math.abs((byte) (mBytes[mBytes.length / count * i] + 128)) / 128;
            mForePaint.setColor(colors[i % colors.length]);
            float cx = mRect.width() / 2;
            float cy = mRect.height() / 2;
            canvas.drawCircle(cx, cy, r, mForePaint);
        }
    }
}
