package com.biomhope.glass.face.home.session;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.global.BaseFragment;
import com.biomhope.glass.face.global.Constants;
import com.biomhope.glass.face.home.MainActivity;
import com.biomhope.glass.face.utils.CommonUtil;
import com.biomhope.glass.face.utils.LogUtil;
import com.biomhope.glass.face.home.session.audio.AudioRecordButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

import static android.app.Activity.RESULT_OK;

/**
 * author:BH
 * create at:2018/9/5
 * description: 专家会话
 */
public class ExpertSessionFragment extends BaseFragment {

    @BindView(R.id.tv_center_title)
    TextView tv_center_title;

    @BindView(R.id.edt_sendmsg_expert)
    EditText edt_sendmsg_expert;

    @BindView(R.id.iv_switch_voice)
    ImageView iv_switch_voice;

    @BindView(R.id.btn_send)
    Button btn_send;

    @BindView(R.id.btn_audio_record)
    AudioRecordButton audioRecordButton;

    @BindView(R.id.image_more)
    ImageView image_more;

    @BindView(R.id.browse_list)
    RecyclerView browse_list;

    @BindView(R.id.layout_more)
    LinearLayout layout_more;

    @BindView(R.id.send_msg_aera)
    RelativeLayout send_msg_aera;

    // 三种输入模式 文字/语音
    // 默认是文本输入模式
    private static final int STATE_SEND_TEXT = 1;
    private static final int STATE_SEND_VOICE = 2;
    // 发送图片状态是否展开
    private boolean state_send_image = false;

    private int mCurrentSendState = 1;
    private String mCurrentSendEdt;
    private ChatRecycleViewAdapter chatRecycleViewAdapter;
    private ArrayList<ChatItemBean> chatList = new ArrayList<>();

    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int GALLERY_REQUEST_CODE = 2;
    private Uri takePicTargetUri;// 相机输出结果

    private static final int MSG_WHAT_SCROLL_TO_LAST = 0;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_WHAT_SCROLL_TO_LAST:
                    if (chatRecycleViewAdapter.getItemCount() > 2) {
                        browse_list.smoothScrollToPosition(chatRecycleViewAdapter.getItemCount() - 1);
                    }
                    break;
            }
        }
    };

    @OnClick({R.id.iv_switch_voice, R.id.edt_sendmsg_expert, R.id.btn_send, R.id.image_more
            , R.id.photo_tv, R.id.pictures_tv})
    void click(View v) {
        if (CommonUtil.isFastDoubleClick()) return;
        switch (v.getId()) {
            case R.id.iv_switch_voice:
                // 切换语音文本输入
                if (mCurrentSendState == STATE_SEND_TEXT) {
                    switchSendState(STATE_SEND_VOICE);
                } else if (mCurrentSendState == STATE_SEND_VOICE) {
                    switchSendState(STATE_SEND_TEXT);
                }
                break;
            case R.id.btn_send:
//                if (!isWebClientSocket) {
//                    performSendKey();
//                } else {
//                    performSendKeyByWebSocket();
//                }
                send();
                break;
            case R.id.image_more:
                switchSendImageState(!state_send_image);
                break;
            case R.id.edt_sendmsg_expert:
                LogUtil.i(TAG, "点击了编辑栏");
                break;
            case R.id.photo_tv:
                openSystemGallery();
                break;
            case R.id.pictures_tv:
                openSystemCamera();
                break;
        }
    }

    private void send() {
        ChatItemBean clientItem = new ChatItemBean();
        clientItem.type = ChatItemBean.RIGHT_CLIENT_SESSION;
        clientItem.msg = mCurrentSendEdt;
        chatRecycleViewAdapter.insertLastItemData(clientItem);
        edt_sendmsg_expert.getText().clear();
        // 定位到最下面
        browse_list.smoothScrollToPosition(chatRecycleViewAdapter.getItemCount() - 1);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_expert_session;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initialize() {
        tv_center_title.setText(getResources().getString(R.string.tab_video_actived));

        edt_sendmsg_expert.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCurrentSendEdt = s.toString();
                if (TextUtils.isEmpty(mCurrentSendEdt)) {
                    image_more.setVisibility(View.VISIBLE);
                    btn_send.setVisibility(View.INVISIBLE);
                } else {
                    image_more.setVisibility(View.INVISIBLE);
                    btn_send.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        edt_sendmsg_expert.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // 获取焦点时隐藏More区域
                if (hasFocus) {
                    LogUtil.i(TAG, "编辑栏获取到焦点");
                    if (state_send_image) {
                        layout_more.setVisibility(View.GONE);
                        state_send_image = false;
                    }
                    // recyclerView滑动到最底部
                    mHandler.sendEmptyMessageDelayed(MSG_WHAT_SCROLL_TO_LAST, 200);

                    ((MainActivity) context).setBottomMenuStatus(true);
                } else {
                    ((MainActivity) context).setBottomMenuStatus(false);
                }
            }
        });

        // 监听软键盘发送键
        edt_sendmsg_expert.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    send();
//                    if (!isWebClientSocket) {
//                        performSendKey();
//                    } else {
//                        performSendKeyByWebSocket();
//                    }
                }
                return false;
            }
        });

        // 配置聊天界面
        browse_list.setLayoutManager(new LinearLayoutManager(context));
        // 设置Item增加、移除动画
        browse_list.setItemAnimator(new DefaultItemAnimator());
        chatRecycleViewAdapter = new ChatRecycleViewAdapter(context, chatList);
        browse_list.setAdapter(chatRecycleViewAdapter);
        browse_list.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // 隐藏软键盘
                    CommonUtil.hideKeyboard(context, edt_sendmsg_expert);
                    // 编辑栏失去焦点
                    edt_sendmsg_expert.clearFocus();
                    if (layout_more.getVisibility() == View.VISIBLE) {
                        layout_more.setVisibility(View.GONE);
                    }
                }
                return false;
            }
        });

        layout_more.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (layout_more.getVisibility() == View.VISIBLE) {
                        layout_more.setVisibility(View.GONE);
                    }
                }
                return false;
            }
        });

        audioRecordButton.setAudioFinishRecorderListener(new AudioRecordButton.AudioFinishRecorderListener() {
            @Override
            public void onFinished(float seconds, String filePath) {
                if (!TextUtils.isEmpty(filePath)) {
                    ChatItemBean clientItem = new ChatItemBean();
                    clientItem.type = ChatItemBean.RIGHT_CLIENT_SESSION;
                    clientItem.voiceFilePath = filePath;
                    clientItem.voiceLength = Math.round(seconds);
                    LogUtil.v(TAG, String.format("保存文件路径[%s]声音长度为[%s]", filePath, String.valueOf(seconds)));
                    chatRecycleViewAdapter.insertLastItemData(clientItem);
                    // 定位到最下面
                    browse_list.smoothScrollToPosition(chatRecycleViewAdapter.getItemCount() - 1);
                }
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        try {
            if (requestCode == CAMERA_REQUEST_CODE) {
                if (takePicTargetUri != null) {
                    insertOneImgItem(takePicTargetUri);
                }
            } else if (requestCode == GALLERY_REQUEST_CODE) {
                if (data != null) {
                    insertOneImgItem(data.getData());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertOneImgItem(Uri uri) {
        ChatItemBean clientItem = new ChatItemBean();
        clientItem.type = ChatItemBean.RIGHT_CLIENT_SESSION;
        clientItem.imageSize = getImageSize(context, uri);

        chatRecycleViewAdapter.insertLastItemData(clientItem);
        // 隐藏选图区域
        switchSendImageState(false);
        // 定位到最下面
//        browse_list.smoothScrollToPosition(chatRecycleViewAdapter.getItemCount() - 1);
        mHandler.sendEmptyMessageDelayed(MSG_WHAT_SCROLL_TO_LAST, 200);
    }

    private void switchSendState(int state) {
        switch (state) {
            case STATE_SEND_TEXT:
                iv_switch_voice.setImageResource(R.drawable.btn_voice);
                audioRecordButton.setVisibility(View.GONE);
                edt_sendmsg_expert.setVisibility(View.VISIBLE);
                // 在非图片展开模式时显示软键盘
                if (!state_send_image) {
                    edt_sendmsg_expert.requestFocus();
                    CommonUtil.showKeyboard(context, edt_sendmsg_expert);
                }
                break;
            case STATE_SEND_VOICE:
                edt_sendmsg_expert.setVisibility(View.GONE);
                if (state_send_image) {
                    layout_more.setVisibility(View.GONE);
                    state_send_image = false;
                }
                iv_switch_voice.setImageResource(R.drawable.btn_keyboard);
                audioRecordButton.setVisibility(View.VISIBLE);
                // 隐藏软键盘
                CommonUtil.hideKeyboard(context, edt_sendmsg_expert);
                break;
        }
        mCurrentSendState = state;
    }

    private void switchSendImageState(boolean imageState) {
        state_send_image = imageState;
        // 需要展开
        if (imageState) {
            mHandler.sendEmptyMessageDelayed(MSG_WHAT_SCROLL_TO_LAST, 200);
            layout_more.setVisibility(View.VISIBLE);
            if (mCurrentSendState == STATE_SEND_TEXT) {
                // 隐藏软键盘 显示选图区域
                edt_sendmsg_expert.clearFocus();
                CommonUtil.hideKeyboard(context, edt_sendmsg_expert);
            } else if (mCurrentSendState == STATE_SEND_VOICE) {
                // 切换成文本状态
                switchSendState(STATE_SEND_TEXT);
            }
        } else {
            // 缩回
            layout_more.setVisibility(View.GONE);
        }
    }

    // 打开相机拍摄
    private void openSystemCamera() {
        File targetFile = null;
        File storageDir;
        String timeStamp = "takeAt" + CommonUtil.formatMills("yyyy_MM_dd_HH_mm_ss", System.currentTimeMillis()) + ".png";
        try {
            // glassIcons 文件名必须和filepaths中一致
            String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "glassIcons";
            storageDir = new File(dirPath);
            // 不创建目录则保存失败
            if (!storageDir.exists())
                storageDir.mkdirs();
            targetFile = new File(dirPath, timeStamp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 7.0以后替换
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // authority对应manifest一致
            takePicTargetUri = FileProvider.getUriForFile(context, Constants.FILE_PROVIDER_AUTHORITY, targetFile);
        } else {
            takePicTargetUri = Uri.fromFile(targetFile);
        }
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 输出路径
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, takePicTargetUri);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);

    }

    // 打开相册选图
    private void openSystemGallery() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    private ImageSize getImageSize(Context context, Uri uri) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (bitmap == null || bitmap.isRecycled()) {
            return null;
        }
        ImageSize imageSize = new ImageSize();
        imageSize.uri = uri;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] toByteArray = bos.toByteArray();
        try {
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(toByteArray, 0, toByteArray.length, options);
        int outWidth = options.outWidth;
        int outHeight = options.outHeight;
        // 最大宽度为屏幕1/3 高是1/4
        int srceenWidth = CommonUtil.getScreenWidth(context);
        int srceenHeight = CommonUtil.getScreenHeight(context);

        // 1.原图宽>高
        if (outWidth > outHeight) {
            imageSize.width = srceenWidth / 3;
            imageSize.height = imageSize.width * outHeight / outWidth;
        } else if (outWidth == outHeight) {
            // 2.原图宽=高
            imageSize.width = imageSize.height = srceenWidth / 2 - CommonUtil.dp2px(context, 55);
        } else if (outWidth < outHeight) {
            // 3.原图宽<高
            if (outWidth == srceenWidth && outHeight == srceenHeight) {
                imageSize.width = srceenWidth / 6;
                imageSize.height = srceenHeight / 6;
            } else {
                imageSize.width = srceenWidth / 4;
                imageSize.height = imageSize.width * outHeight / outWidth;
            }
        }
        LogUtil.w(TAG, "图片原始宽高是: " + outWidth + "*" + outHeight + " ------ 处理后图片宽高是: " + imageSize.width + "*" + imageSize.height);
        return imageSize;
    }

    @Override
    protected boolean isEventSubscribe() {
        return false;
    }
}
