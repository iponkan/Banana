package com.ponkan.banana.camera.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.ponkan.banana.R;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 视频拍摄的控件
 * <p>
 * 1.提供isTakedMatch()提供给外部，是否满足拍摄最低要求了
 * 2.在一些数据的初始化移到onSizeChanged里面去做，OnMeasure会调用多次，没必要
 * 3.主要提供 updateTakenTime 外界添加控制需要绘制视频长度的控制，避免视频拍摄太长时间
 */
public class SegmentBar extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "SegmentBar";

    private static final int FRAME_FRESH_INTERVAL = 16;//在刷新拍摄进度的时候的帧率问题，多少MS一帧
    private static final float DEFAULT_TOTAL_TIME = 10;//s
    private static final float DEFAULT_LEAST_TAKEN_TIME = 2;//s
    private static final int DEFAULT_CURSOR_UPDATE_INTERVAL = 500;//ms

    /**
     * 总时间(s为单位)
     */
    private float mTotalTime;
    /**
     * 拍摄时间最低限制(s为单位）
     */
    private float mLeastTakenTime = 0;
    /**
     * 光标闪动时间间隔(ms为单位)
     */
    private int mCursorUpdateInterval;
    /**
     * 是否绘制光标
     */
    private boolean mDrawCursor;


    private int mMeasureWidth;
    private int mMeasureHeight;

    private int mBackColor;
    private int mProgressColor;
    private int mCursorColor;
    private int mLineColor;
    private int mDeleteColor;
    /**
     * 分割线的宽度
     */
    private int LINE_WIDTH = 4;
    /**
     * 光标的宽度
     */
    private int CURSOR_WIDTH = 10;

    /**
     * 刷新光标所用到的临时变量
     */
    private long mCursorUpdateRecordTime;
    /**
     * 拍摄视频过程中所用到刷新的临时变量
     */
    private long mTakingRefreshLastTime;

    /**
     * 根据总长度和总时间计算每1MS是多长的宽度，整数
     */
    private float mUnitWidth = 0;

    /**
     * 每段分割线的位置，也标志着每段视频
     */
    private ArrayList<Float> mTakenTimeArray = new ArrayList<>();
    /**
     * 记录每一段视频的分别长度
     */
    private ArrayList<Long> mSectionList = new ArrayList<>();
    /**
     * 当前视频总长度(外面通知需要绘画的总长度)
     */
    private AtomicLong mTakenTime = new AtomicLong(0);
    /**
     * 当前已经绘画的
     */
    private AtomicLong mDrewTime = new AtomicLong(0);

    /**
     * 当前光标的位置，光标的位置及视频所到的位置
     */
    private float mCursorPos = 0;


    /**
     * 由最低拍摄长度转成的宽度(反应在位置上)
     */
    private float mLeastTakenTimeWidth = 0;

    private Paint mCursorPaint;
    private Paint mBackPaint;
    private Paint mProgressPaint;
    private Paint mLinePaint;
    private Paint mDeletePaint;
    private Paint mLeastTimeLinePaint;

    private ITakeController mIController;

    private volatile int mState = 0;
    /**
     * 是否正在当前段的拍摄过程中的标志位(即处于点击状态的拍摄状态当中)，标志位1处于拍摄,0为false
     */
    private static final int TAKING_MASK = 1;
    /**
     * 是否处于删除状态的标志位(即已经选取了一段待删除的片段), 标志位1处于删除态,0为false
     */
    private static final int DELETE_MASK = 2;

    /**
     * 标识开始录制的start的值
     */
    private long mStartSectionTime;
    /**
     * 回复状态使用
     */
    private boolean mNeedCallRestore = false;

    private DrawThread mDrawProgressBar;
    private SurfaceHolder mSurfaceHolder;
    private Thread mRecoverThread = null;
    private final Handler mUIHandler = new Handler(Looper.getMainLooper());

    public SegmentBar(Context context) {
        super(context);
        init();
    }

    public SegmentBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SegmentBar);
        mTotalTime = typedArray.getFloat(R.styleable.SegmentBar_totalTime, DEFAULT_TOTAL_TIME);
        mLeastTakenTime = typedArray.getFloat(R.styleable.SegmentBar_leastTakenTime, DEFAULT_LEAST_TAKEN_TIME);
        mCursorUpdateInterval = typedArray.getInt(R.styleable.SegmentBar_cursorUpdateInterval, DEFAULT_CURSOR_UPDATE_INTERVAL);

        mBackColor = typedArray.getColor(R.styleable.SegmentBar_backColor, Color.GRAY);
        mCursorColor = typedArray.getColor(R.styleable.SegmentBar_cursorColor, Color.YELLOW);
        mProgressColor = typedArray.getColor(R.styleable.SegmentBar_progressColor, Color.GREEN);
        mLineColor = typedArray.getColor(R.styleable.SegmentBar_lineColor, Color.BLUE);
        mDeleteColor = typedArray.getColor(R.styleable.SegmentBar_deleteColor, Color.RED);

        typedArray.recycle();

        init();
    }

    private void init() {
        initPaint();
        mCursorUpdateRecordTime = System.currentTimeMillis();
        LINE_WIDTH = (int) (1.5 * getResources().getDisplayMetrics().density);
        CURSOR_WIDTH = (int) (5 * getResources().getDisplayMetrics().density);
        getHolder().addCallback(this);
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        mLeastTimeLinePaint = new Paint();
        mLeastTimeLinePaint.setColor(Color.parseColor("#80FFFFFF"));

        mBackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackPaint.setColor(mBackColor);

        mCursorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCursorPaint.setColor(mCursorColor);

        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setColor(mProgressColor);

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(mLineColor);

        mDeletePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDeletePaint.setColor(mDeleteColor);

        setZOrderOnTop(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;
        startDrawThread();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopDrawThread();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mMeasureWidth = getMeasuredWidth();
        mMeasureHeight = getMeasuredHeight();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mTotalTime != 0 && !(oldw == w && oldh == h)) {
            mUnitWidth = mMeasureWidth / (mTotalTime * 1000);
            mLeastTakenTimeWidth = mLeastTakenTime * 1000 * mUnitWidth;
            if (mNeedCallRestore) {
                mNeedCallRestore = false;
                restoreDividerPoint();
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    private volatile boolean isDrawing = true;

    /**
     * 外部调用start
     */
    public void startNewSection() {
        if (isDeletingState()) {
            // 如果之前已经标记了一段要删除，那么这时候要取消
            setDeletingState(false);
            invalidate();
        }
        if (isEnded()) {
            if (mIController != null) {
                mIController.takenOver();
            }
            return;
        }
        setSectionTakingState(true);
        mStartSectionTime = mTakenTime.get();
        Log.d(TAG, "startNewSection====" + mStartSectionTime);
    }

    private void startDrawThread() {
        isDrawing = true;
        mDrawProgressBar = new DrawThread();
        mDrawProgressBar.start();
    }

    private void stopDrawThread() {
        isDrawing = false;
        mDrawProgressBar = null;
    }

    /**
     * 绘制进度条线程
     */
    private final class DrawThread extends Thread {

        DrawThread() {
            setName("thread-progressBar");
        }

        @Override
        public void run() {
            if (null != mRecoverThread) {
                try {
                    mRecoverThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    mRecoverThread = null;// 使用完就置为null
                }
            }
            Surface mSurface = null;
            while (isDrawing && (null != mSurfaceHolder)) {
                synchronized (mSurfaceHolder) {
                    try {
                        doRealVideoDraw();
                        Canvas canvas = null;
                        try {
                            mSurface = mSurfaceHolder.getSurface();
                            /*
                             * lockCanvas在4.3及以上系统中会出现底层崩溃。因此加上"mSurface.isValid()"方法。该方法的API描述如下：
                             * Does this object hold a valid surface? Returns true if it holds a physical surface, so lockCanvas() will succeed. Otherwise returns false.
                             *
                             * 之前遇到的路径是：从拍摄页进入视频确认页，再返回到拍摄页，4.3系统上高概率出现崩溃。
                             */
                            if (null != mSurface && mSurface.isValid()) {
                                canvas = mSurfaceHolder.lockCanvas();
                            } else {
                                Log.w(TAG, "mSurface is invalid");
                            }
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                            canvas = null;
                        }
                        if (null != canvas) {
                            drawProgressBar(canvas);
                            if (null != mSurfaceHolder) {
                                mSurfaceHolder.unlockCanvasAndPost(canvas);
                                Thread.sleep(FRAME_FRESH_INTERVAL);
                            }
                        }
                    } catch (Throwable e) {
                        Log.e(TAG, e.toString());
                        break;
                    }
                }
            }
        }
    }

    /**
     * 绘制进度条
     */
    private void drawProgressBar(Canvas canvas) {
        if (null == canvas) {
            return;
        }
        // 画背景
        canvas.drawRect(0, 0, mMeasureWidth, mMeasureHeight, mBackPaint);
        Log.d(TAG, "mCursorPos====" + mCursorPos);
        Log.d(TAG, "mMeasureHeight====" + mMeasureHeight);
        // 画拍摄段
        if (mCursorPos != 0 && mCursorPos <= mMeasureWidth) {
            canvas.drawRect(0, 0, mCursorPos, mMeasureHeight, mProgressPaint);
        }
        // 画每段的分割线
        float lastSectionPos = 0;// 标识上一次画分割线的位置，是分割线右边位置，为了避免多段分割线重叠成黑色
        for (int i = 0; i < mTakenTimeArray.size(); i++) {
            float tmpPos = mTakenTimeArray.get(i);
            // Debug.d(TAG, "lastSectionPos:" + lastSectionPos + " tmpPos:" + tmpPos);
            if (tmpPos > (lastSectionPos + 0.5)) {
                // 需要绘画
                lastSectionPos = tmpPos + LINE_WIDTH;
                canvas.drawRect(tmpPos, 0, lastSectionPos, mMeasureHeight, mLinePaint);
            }
        }
        // 画待删删除的删除段
        if (isDeletingState() && mTakenTimeArray.size() > 0) {
            // 需要标记一段待删除的段
            float start =
                    mTakenTimeArray.size() == 1 ? 0 : mTakenTimeArray.get(mTakenTimeArray.size() - 2) + LINE_WIDTH;
            float end = mTakenTimeArray.get(mTakenTimeArray.size() - 1) + LINE_WIDTH;
            canvas.drawRect(start, 0, end, mMeasureHeight, mDeletePaint);
        }
        // 画最低分隔线
        if (mCursorPos < mLeastTakenTimeWidth) {
            canvas.drawRect(mLeastTakenTimeWidth, 0, mLeastTakenTimeWidth + LINE_WIDTH, mMeasureHeight,
                    mLeastTimeLinePaint);
        }
        if (mIController != null) {
            int time = (int) (mCursorPos / mUnitWidth);
            mIController.takenTimeCallback(time);
        }
        // 画光标位置,光标显示不显示，由自己去控制
        if (isSectionTakingState() || isNeedShowCursor()) {
            canvas.drawRect(mCursorPos, 0, mCursorPos + CURSOR_WIDTH, mMeasureHeight, mCursorPaint);
        }
    }

    public ArrayList<Long> getSelectionList() {
        return mSectionList;
    }

    /**
     * 恢复状态
     */
    public void restoreState(ArrayList<Long> sectionList) {
        if (sectionList == null || sectionList.size() <= 0) {
            return;
        }
        mSectionList = sectionList;
        long totalTime = 0;
        for (int i = 0; i < sectionList.size(); i++) {
            totalTime += sectionList.get(i);
        }
        mDrewTime.set(totalTime);
        mTakenTime.set(totalTime);

        if (mUnitWidth != 0) {
            restoreDividerPoint();
        } else {
            mNeedCallRestore = true;
        }

    }

    private void restoreDividerPoint() {
        if (null == mTakenTimeArray) {
            Log.w(TAG, "mTakenTimeArray is null");
            return;
        }
        Log.d(TAG, "---- restoreDividerPoint ----");
        mRecoverThread = new Thread(new Runnable() {

            @Override
            public void run() {
                long tmpTime = 0;
                synchronized (mTakenTimeArray) {
                    mTakenTimeArray.clear();
                    for (int i = 0; i < mSectionList.size(); i++) {
                        tmpTime += mSectionList.get(i);
                        float lSectionPos = tmpTime * mUnitWidth;
                        mTakenTimeArray.add(lSectionPos - LINE_WIDTH);
                    }
                    mCursorPos =
                            mTakenTimeArray.size() > 0 ? mTakenTimeArray.get(mTakenTimeArray.size() - 1) + LINE_WIDTH : 0;
                    if (mCursorPos < 0) {
                        mCursorPos = 0;
                    } else if (mCursorPos > mMeasureWidth) {
                        mCursorPos = mMeasureWidth;
                    }
                    Log.d(TAG, "restoreDividerPoint-> mCursorPos = " + mCursorPos);
                }
            }
        });
        mRecoverThread.setName("thread-recover");
        Log.d(TAG, mRecoverThread.getName() + " is starting...");
        mRecoverThread.start();
    }

    /**
     * 是否显示光标
     */
    private boolean isNeedShowCursor() {
        long curTime = System.currentTimeMillis();
        long timePassed = curTime - mCursorUpdateRecordTime;
        if (timePassed >= mCursorUpdateInterval) {
            mDrawCursor = !mDrawCursor;
            mCursorUpdateRecordTime = curTime;
        }
        return mDrawCursor;
    }

    /**
     * 停止
     */
    public void stopCurrentSection() {
        if (!isSectionTakingState()) {
            return;
        }
        setSectionTakingState(false);

        long lEndSectionEndTime = mTakenTime.get();
        long lSectionTime = lEndSectionEndTime - mStartSectionTime;
        synchronized (mSectionList) {
            mSectionList.add(lSectionTime);
        }
        float lSectionPos = lEndSectionEndTime * mUnitWidth;
        synchronized (mTakenTimeArray) {
            mTakenTimeArray.add(lSectionPos - LINE_WIDTH);
        }
    }

    public void updateTakenTime(long newTime) {
        mTakenTime.addAndGet(newTime);
    }

    /**
     * 取消上一段的拍摄
     */
    public void cancelLastSection() {
         Log.d(TAG, "delete 1 STACK_FILE_SIZE = "+mTakenTimeArray.size());
        if (!isDeletingState()) {
            // Debug.d(TAG, "delete 1 state STACK_FILE_SIZE = "+mTakenTimeArray.size());
            // 处于拍摄过程中，这时候要去选取上一段的拍摄数据，标记出来
            // 判断是否有的可选
            if (mTakenTimeArray.size() > 0) {
                setDeletingState(true);
                invalidate();
                mIController.deleteStateChange(2);
            } else {
                if (mIController != null) {
                    mIController.deleteStateChange(0);
                }
            }
        } else {
             Log.d(TAG, "delete 1 state STACK_FILE_SIZE = "+mTakenTimeArray.size());
            // 已经选好一段标记了，进行删除
            if (mTakenTimeArray.size() > 0) {
                if (mIController != null) {
                    boolean delResult = mIController.deleteLastSection();
                    if (delResult) {
                        if (mTakenTimeArray.size() > 1) {
                            mIController.deleteStateChange(1);
                        } else {
                            mIController.deleteStateChange(0);
                        }
                        int lTakenLen = mTakenTimeArray.size();
                        float lCursorPos = mCursorPos;
                        mCursorPos = lTakenLen > 1 ? mTakenTimeArray.get(mTakenTimeArray.size() - 2) + LINE_WIDTH : 0;
                        if (mCursorPos > mMeasureWidth) {
                            mCursorPos = mMeasureWidth;
                        }
                        long lastSectionLen = mSectionList.get(mSectionList.size() - 1);
                        removeTakenLength();
                        mDrewTime.addAndGet(-lastSectionLen);
                        mTakenTime.addAndGet(-lastSectionLen);

                        boolean call2UnMatch = checkRemove2LimitUnMatch(lCursorPos);
                        if (call2UnMatch && mIController != null) {
                            mUIHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    mIController.leastLimitUnMatch();
                                }
                            });
                        }
                    } else {
                        mIController.deleteStateChange(1);
                        mIController.deleteLastSectionFailed();
                    }
                    setDeletingState(false);
                    invalidate();
                }
            } else {
                if (mIController != null) {
                    mIController.deleteStateChange(0);
                }
            }
        }
    }

    /**
     * @return 如果在往前画的过程中，则是true，否则就是false
     */
    private boolean doRealVideoDraw() {
        long curTime = mDrewTime.get();
        Log.d(TAG, "curTime====" + curTime);
        long takenTime = mTakenTime.get();
        Log.d(TAG, "takenTime====" + takenTime);
        long curTimeMillis = System.currentTimeMillis();
        Log.d(TAG, "curTimeMillis====" + curTimeMillis);
        Log.d(TAG, "mTakingRefreshLastTime====" + mTakingRefreshLastTime);

        long passTime = curTimeMillis - mTakingRefreshLastTime;
        mTakingRefreshLastTime = curTimeMillis;
        if ((curTime + passTime) > takenTime) {
            passTime = takenTime - curTime;
        }
        if (passTime == 0) {
            return false;
        }
        mDrewTime.addAndGet(passTime);
        float addedWidth = mUnitWidth * passTime;
        boolean call2LimitMatch = checkAdd2LimitMatch(addedWidth);
        if (call2LimitMatch && mIController != null) {
            mUIHandler.post(new Runnable() {

                @Override
                public void run() {
                    mIController.leastLimitMatch();
                }
            });
        }
        mCursorPos += addedWidth;
        if (mCursorPos >= mMeasureWidth) {
            mCursorPos = mMeasureWidth;
            if (mIController != null) {
                mUIHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        mIController.takenOver();
                    }
                });
            }
            stopCurrentSection();
        }
        return true;
    }

    private boolean checkAdd2LimitMatch(float addWidth) {
         Log.d(TAG, "checkAdd2LimitMatch: mCursorPos" + mCursorPos +
         " addWidth:" + addWidth
         + " mLeastTakenTimeWidth:" + mLeastTakenTimeWidth);
        return mCursorPos < mLeastTakenTimeWidth && (mCursorPos + addWidth) >= mLeastTakenTimeWidth;
    }

    private boolean checkRemove2LimitUnMatch(float oldCurPos) {
        return oldCurPos > mLeastTakenTimeWidth && mCursorPos <= mLeastTakenTimeWidth;
    }

    private void removeTakenLength() {
        synchronized (mTakenTimeArray) {
            if (mTakenTimeArray.size() > 0) {
                mTakenTimeArray.remove(mTakenTimeArray.size() - 1);
            }
        }
        synchronized (mSectionList) {
            if (mSectionList.size() > 0) {
                mSectionList.remove(mSectionList.size() - 1);
            }
        }
    }

    private boolean isSectionTakingState() {
        return (mState & TAKING_MASK) > 0;
    }

    private boolean isDeletingState() {
        return (mState & DELETE_MASK) > 0;
    }

    private void setSectionTakingState(boolean taking) {
        if (taking) {
            mState |= TAKING_MASK;
        } else {
            mState &= ~TAKING_MASK;
        }
    }

    public void setDeletingState(boolean deleting) {
        if (deleting) {
            mState |= DELETE_MASK;
        } else {
            mState &= ~DELETE_MASK;
        }
    }

    /**
     * 判断当前拍摄的是否已经符合最低要求了
     */
    public boolean isTakenMatch() {
        return mLeastTakenTimeWidth != 0 && mCursorPos >= mLeastTakenTimeWidth;
    }

    ;

    public boolean isEnded() {
        return mCursorPos >= mMeasureWidth;
    }

    public int getTakenTimeArrayLength() {
        return mTakenTimeArray.size();
    }

    public float getCursorPos() {
        return mCursorPos;
    }

    /**
     * 外部空间需要继承的接口
     */
    public interface ITakeController {

        /**
         * 拍摄长度超过最低限制的时候会回调
         */
        void leastLimitMatch();

        /**
         * 拍摄长度删除到不满足最低限制的时候会回调
         */
        void leastLimitUnMatch();

        /**
         * 所有的总长度都拍摄完成了，这时候会回调结束
         */
        void takenOver();

        /**
         * 真正删除一段后会回调
         */
        boolean deleteLastSection();

        /**
         * 拍摄视频时间长度的回调，这个回调是异步的注意
         */
        void takenTimeCallback(int time);

        /**
         * 删除模式变化，选中等
         * 0不可点击状态、1选中删除状态 2可删除状态
         */
        void deleteStateChange(int state);

        /**
         * 回删失败时回调
         */
        void deleteLastSectionFailed();
    }

    public void setITakeController(ITakeController iTakeController) {
        mIController = iTakeController;
    }

    /**
     * 设置总时长限制，默认10s，长视频设置60s
     *
     * @param time 单位 s
     */
    public void setTotalTime(int time) {
        if (time != 0 && mTotalTime != time) {
            this.mTotalTime = time;
            mUnitWidth = mMeasureWidth / (mTotalTime * 1000);
            mLeastTakenTimeWidth = mLeastTakenTime * 1000 * mUnitWidth;
            restoreDividerPoint();
        }
    }

    /**
     * 获取当前视频的总时长
     *
     * @return 时长 (单位:毫秒)
     */
    public long getCurrentVideoDuration() {
        return mTakenTime.get();
    }

    public int getCurrentVideoSectionCount() {
        if (mTakenTimeArray != null) {
            return mTakenTimeArray.size();
        }
        return 0;
    }
}
