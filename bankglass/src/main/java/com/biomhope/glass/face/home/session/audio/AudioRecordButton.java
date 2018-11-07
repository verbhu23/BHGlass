package com.biomhope.glass.face.home.session.audio;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.utils.FileUtils;
import com.biomhope.glass.face.utils.LogUtil;

import java.util.concurrent.Executors;

/**
 * @author $USER_NAME
 * create at : 2018-10-24
 * description :
 */
public class AudioRecordButton extends android.support.v7.widget.AppCompatButton implements AudioManager.AudioStageListener {

    private static final String TAG = AudioRecordButton.class.getSimpleName();

    // 上下文
    private Context mContext;
    // 正在录音标记
    private boolean isRecording = false;
    // 录音对话框
    private DialogManager mDialogManager;
    //核心录音类
    private AudioManager mAudioManager;
    //当前录音时长
    private float mTime = 0;
    // 是否触发了onlongclick，准备好了
    private boolean mLongClickReady;
    //标记是否强制终止
    private boolean isOverTime = false;
    //三个对话框的状态常量
    private static final int STATE_NORMAL = 1;
    private static final int STATE_RECORDING = 2;
    private static final int STATE_WANT_TO_CANCEL = 3;

    //垂直方向滑动取消的临界距离
    private static final int DISTANCE_Y_CANCEL = 50;
    //取消录音的状态值
    private static final int MSG_VOICE_STOP = 4;
    //当前状态
    private int mCurrentState = STATE_NORMAL;
    // 三个状态
    private static final int MSG_AUDIO_PREPARED = 0X110;
    private static final int MSG_VOICE_CHANGE = 0X111;
    private static final int MSG_DIALOG_DIMISS = 0X112;
    private static final int MSG_DIALOG_RECORDING = 0X113;

    public boolean isHasRecordPromission() {
        return true;
    }

    @SuppressLint("HandlerLeak")
    private final Handler mStateHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_AUDIO_PREPARED:
                    // 显示应该是在audio end prepare之后回调
//                    mDialogManager.showRecordingDialog();
                    isRecording = true;
                    // 需要开启一个线程来变换音量
                    Executors.newSingleThreadExecutor().execute(mGetVoiceLevelRunnable);
                    break;
                case MSG_VOICE_CHANGE:
                    //剩余10s
//                    showRemainedTime(); 录入倒计时
                    mDialogManager.updateVoiceLevel(mAudioManager.getVoiceLevel(7));
                    break;
                case MSG_DIALOG_DIMISS:
                    mDialogManager.dimissDialog();
                    break;
                case MSG_DIALOG_RECORDING:
                    mDialogManager.showRecordingDialog();
                    break;
                case MSG_VOICE_STOP:
                    // 已经录制了1分钟
                    isOverTime = true;//超时
                    mDialogManager.dimissDialog();
                    mAudioManager.release();// release释放一个mediarecorder
                    mListener.onFinished(mTime, mAudioManager.getCurrentFilePath());
                    reset();// 恢复标志位
                    break;

            }
        }

    };

    // 获取音量大小的runnable
    private Runnable mGetVoiceLevelRunnable = new Runnable() {

        @Override
        public void run() {
            while (isRecording) {
                try {
                    // 假定最长时间为一分钟mMaxRecordTimes = 60
                    if (mTime > 60) {
                        mStateHandler.sendEmptyMessage(MSG_VOICE_STOP);
                        return;
                    }

                    Thread.sleep(100);
                    mTime += 0.1f;
                    if (mCurrentState != STATE_WANT_TO_CANCEL) {
                        mStateHandler.sendEmptyMessage(MSG_VOICE_CHANGE);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public AudioRecordButton(Context context) {
        this(context, null);
    }

    public AudioRecordButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;

        //初始化语音对话框
        mDialogManager = new DialogManager(getContext());

        //获取录音保存位置
        String dir = FileUtils.getAppRecordDir(mContext).toString();
        //实例化录音核心类
        mAudioManager = AudioManager.getInstance(dir);

        mAudioManager.setOnAudioStageListener(this);

        // 长按才录音 避免按下抬起时间太短 MediaRecorder开关崩溃
        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                LogUtil.e(TAG, "触发长按 开启录音准备");
                if (isHasRecordPromission()) {
                    mLongClickReady = true;
                    mAudioManager.prepareAudio();
//                    changeState(STATE_RECORDING);
                    return false;
                } else {
                    return true;
                }
            }
        });
    }

    @Override
    public void wellPrepared() {
        // AudioManager.prepareAudio完成后 开始录音
        mStateHandler.sendEmptyMessage(MSG_AUDIO_PREPARED);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                LogUtil.i(TAG, "手指按下");
                // 按下就变色
                if (!isOverTime) {
                    mDialogManager.showRecordingDialog();
                    changeState(STATE_RECORDING);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isRecording) {
                    // 根据x，y来判断用户是否想要取消
                    // Y值移动超过控件且处于提示非取消状态
                    if (wantToCancel(y)) {
                        changeState(STATE_WANT_TO_CANCEL);
                    } else {
                        if (!isOverTime) {
                            changeState(STATE_RECORDING);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                LogUtil.e(TAG, "手指取消");
//                break;
            case MotionEvent.ACTION_UP:
                LogUtil.i(TAG, "手指抬起");
                // 首先判断是否有触发onlongclick事件，没有的话直接返回reset
                if (!mLongClickReady) {
                    LogUtil.e(TAG, "未触发长按录音 直接取消");
                    reset();
                    return super.onTouchEvent(event);
                }
                // 如果按的时间太短，还没准备好或者时间录制太短，就离开了，则显示这个dialog
                if (!isRecording || mTime < 0.8f) {
                    LogUtil.e(TAG, "当前录音时间过短");
                    mDialogManager.tooShort();
                    mDialogManager.dimissDialog();
                    mAudioManager.cancel();
//                    mStateHandler.sendEmptyMessageDelayed(MSG_DIALOG_DIMISS, 800);
                } else if (mCurrentState == STATE_RECORDING) {//正常录制结束
                    if (isOverTime) return super.onTouchEvent(event);//超时
                    mDialogManager.dimissDialog();
                    mAudioManager.release();// release释放一个mediarecorder

                    if (mListener != null) {// 保存录音
                        mListener.onFinished(mTime, mAudioManager.getCurrentFilePath());
                        LogUtil.e(TAG, "录音完成 时长: " + mTime);
                    }
                } else if (mCurrentState == STATE_WANT_TO_CANCEL) {
                    LogUtil.e(TAG, "取消此次录音");
                    // cancel 释放资源删除文件
                    mAudioManager.cancel();
                    mDialogManager.dimissDialog();
                }
                reset();// 恢复标志位
                break;
        }
        return super.onTouchEvent(event);
    }

    private boolean wantToCancel(int y) {
//        if (x < 0 || x > getWidth()) {// 判断是否在左边，右边，上边，下边
//            return true;
//        }
        // 小于临界值(在屏幕左边。。。)或者超过控件高度 + DISTANCE_Y_CANCEL视为取消
//        if (y < -DISTANCE_Y_CANCEL || y > getHeight() + DISTANCE_Y_CANCEL) {
//            return true;
//        }
        return (y < -DISTANCE_Y_CANCEL || y > getHeight() + DISTANCE_Y_CANCEL);
    }

    public interface AudioFinishRecorderListener {
        void onFinished(float seconds, String filePath);
    }

    private AudioFinishRecorderListener mListener;

    public void setAudioFinishRecorderListener(AudioFinishRecorderListener listener) {
        mListener = listener;
    }

    //是否触发过震动
    boolean isShcok;

    /**
     * 回复标志位以及状态
     */
    private void reset() {
        LogUtil.e(TAG, "恢复标志位状态");
        isRecording = false;
        changeState(STATE_NORMAL);
        mLongClickReady = false;
        mTime = 0;

        isOverTime = false;
        isShcok = false;
    }

    private void changeState(int state) {
        LogUtil.v(TAG, "当前录音状态是: " + mCurrentState + " ,切换至: " + state);
        if (mCurrentState != state) {
            mCurrentState = state;
            switch (mCurrentState) {
                case STATE_NORMAL:
                    // 按住说话 无背景 初始化状态
                    setText(mContext.getString(R.string.hang_up_finsh));
                    setBackgroundResource(R.drawable.btn_record_normal);
                    mDialogManager.dimissDialog();
                    break;
                case STATE_RECORDING:
                    // 松开取消 有背景
                    setText(R.string.release_cancel);
                    setBackgroundResource(R.drawable.btn_record_press);
//                    if (isRecording) {
                    mDialogManager.recording();
//                    }
                    break;
                case STATE_WANT_TO_CANCEL:
                    // 松开取消 有背景 按压状态不变
//                    setText(R.string.release_cancel);
//                    setBackgroundResource(R.drawable.btn_record_press);
                    // dialog want to cancel
                    mDialogManager.wantToCancel();
                    break;

            }
        }
    }

    @Override
    public boolean isInEditMode() {
        return true;
    }

    @Override
    public boolean onPreDraw() {
        return false;
    }
}
