package com.biomhope.glass.face.home.settings;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.global.BaseActivity;
import com.biomhope.glass.face.bean.ComResponseResult;
import com.biomhope.glass.face.bean.FaceDelRequestBean;
import com.biomhope.glass.face.bean.GroupUserInfo;
import com.biomhope.glass.face.bean.eventvo.EditUserMessage;
import com.biomhope.glass.face.global.Constants;
import com.biomhope.glass.face.utils.CommonUtil;
import com.biomhope.glass.face.utils.LogUtil;
import com.biomhope.glass.face.utils.OkHttpUtil;
import com.biomhope.glass.face.utils.SharedPreferencesUtils;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @author $USER_NAME
 * create at : 2018-10-12
 * description : 人脸库分组详情（点击小组进入）
 */
public class FaceLibraryGroupActivity extends BaseActivity {

    @BindView(R.id.ib_back)
    RelativeLayout ib_back;

    @BindView(R.id.tv_center_title)
    TextView tv_center_title;

    @BindView(R.id.tv_how_many_groups)
    TextView tv_how_many_groups;

    @BindView(R.id.group_recyclerView)
    RecyclerView group_recyclerView;

    @BindView(R.id.face_lib_group_swipe_refresh)
    SwipeRefreshLayout face_lib_group_swipe_refresh;

    @BindView(R.id.ib_action_end)
    ImageButton ib_action_end;

    private final static int MSG_HIDE_PROGRESS_FLAG = 2;
    private final static int MSG_DEL_RESULT_CONTENT = 3;
    private static final int requestcode_to_register_face_lib = 1;
    private static final int requestcode_to_edt_face_lib = 4;
    private String group_name;
    private FaceLibGroupAdapter groupAdapter;
    private ArrayList<GroupUserInfo> sysPushBeanArrayList = new ArrayList<>();

    @OnClick({R.id.ib_back, R.id.ib_action_end})
    void click(View v) {
        if (CommonUtil.isFastDoubleClick()) return;
        switch (v.getId()) {
            case R.id.ib_back:
                finish();
                break;
            case R.id.ib_action_end:
                // 注册一个新客户人脸
                Intent intent = new Intent(this, FaceLibRegisterActivity.class);
                intent.putExtra("group_name", group_name);
                startActivityForResult(intent, requestcode_to_register_face_lib);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MSG_HIDE_PROGRESS_FLAG:
                    cancelDialog();
                    break;
                case MSG_DEL_RESULT_CONTENT:
                    cancelDialog();
                    String result = (String) msg.obj;
                    if (TextUtils.isEmpty(result) && msg.arg1 != -1) {
                        groupAdapter.removeItemData(msg.arg1);
                        tv_how_many_groups.setText(String.format(getResources().getString(R.string.face_lib_how_many_groups_members),
                                groupAdapter.getItemCount()));
                    } else {
                        showToast(result);
                    }
                    break;
            }
        }
    };

    @Override
    protected void setContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_face_lib_group);
    }

    @SuppressLint("StringFormatMatches")
    @Override
    protected void initialize() {
        ib_back.setVisibility(View.VISIBLE);
        ib_action_end.setVisibility(View.VISIBLE);
        ib_action_end.setBackgroundResource(R.drawable.to_register);
        group_name = getIntent().getExtras().getString("group_name");
        int group_members = getIntent().getExtras().getInt("group_members", 0);
        // 可能为空
        ArrayList<GroupUserInfo> sysPushList = (ArrayList<GroupUserInfo>) getIntent().getExtras().get("group_list");

        tv_center_title.setText(group_name);
        tv_how_many_groups.setText(String.format(getResources().getString(R.string.face_lib_how_many_groups_members),
                group_members));
        face_lib_group_swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                face_lib_group_swipe_refresh.setRefreshing(false);
            }
        });
        if (sysPushList != null && !sysPushList.isEmpty()) {
            sysPushBeanArrayList.clear();
            sysPushBeanArrayList.addAll(sysPushList);
        }
        // 点击跳转到注册详情 还原数据
        groupAdapter = new FaceLibGroupAdapter(this, sysPushBeanArrayList, new FaceLibGroupAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(GroupUserInfo sysPushBean) {
                if (CommonUtil.isFastDoubleClick()) return;
                // 点击跳转到注册详情 还原数据
                Intent intent = new Intent(FaceLibraryGroupActivity.this, FaceLibGroupDetailActivity.class);
                intent.putExtra("bean", sysPushBean);
                intent.putExtra("group_name", group_name);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }

            @Override
            public void onLongItemClick(int position, GroupUserInfo sysPushBean) {
                LogUtil.i(TAG, "当前点击的是[" + position + "] ,客户Id是[" + sysPushBean.clientId + "]");
                showLongClickDialog(position, sysPushBean);
            }
        });
        group_recyclerView.setLayoutManager(new LinearLayoutManager(this));
        group_recyclerView.setItemAnimator(new DefaultItemAnimator());
        group_recyclerView.setAdapter(groupAdapter);
    }

    private Dialog longClickDialog;

    private void showLongClickDialog(final int position, final GroupUserInfo sysPushBean) {
        if (longClickDialog == null) {
            longClickDialog = new Dialog(this);
        }
        View view = View.inflate(this, R.layout.dialog_face_lib_long_click, null);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.width = CommonUtil.getScreenWidth(this) * 3 / 4;
        longClickDialog.setContentView(view, layoutParams);
        longClickDialog.setCanceledOnTouchOutside(true);
        longClickDialog.setCancelable(true);

        view.findViewById(R.id.more_dialog_rename).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 编辑
                longClickDialog.dismiss();
                Intent editIntent = new Intent(FaceLibraryGroupActivity.this, FaceLibEditActivity.class);
                editIntent.putExtra("sysPushBean", sysPushBean);
                editIntent.putExtra("position", position);
                editIntent.putExtra("type", 1);
                startActivity(editIntent);
            }
        });
        view.findViewById(R.id.more_dialog_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 删除
                longClickDialog.dismiss();
                showLoadingDialog();
                requestDeteleFace(position, sysPushBean.clientId);

            }
        });
        longClickDialog.show();
    }

    private void requestDeteleFace(final int position, String clientId) {
        String userId = (String) SharedPreferencesUtils.get(this, "userId", "");
        String requestUrl = Constants.REQUEST_HOST_MAIN_URL + Constants.REQUEST_DO_CUSTOMERDELETE;
        LogUtil.i(TAG, "customerDelete request url: " + requestUrl);
        LogUtil.i(TAG, "删除第[" + position + "]行用户信息: " + new FaceDelRequestBean(userId, clientId).toString());
        OkHttpUtil.getInstance().doAsyncOkHttpPost(requestUrl, new Gson().toJson(new FaceDelRequestBean(userId, clientId)), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtil.e(TAG, "onFailure: " + e.getMessage());
                mHandler.sendEmptyMessage(MSG_HIDE_PROGRESS_FLAG);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                LogUtil.i(TAG, "onResponse: json = " + json);

                Message msg = Message.obtain();
                msg.what = MSG_DEL_RESULT_CONTENT;
                msg.arg1 = -1;
                if (response.code() == Constants.REQUEST_STATUS_SUCCESSFUL) {
                    ComResponseResult comResponseResult = new Gson().fromJson(json, ComResponseResult.class);
                    if (Constants.RESPONSE_STATUS_SUCCESSFUL.equals(comResponseResult.exCode)) {
                        // 删除成功
                        msg.obj = "";
                        msg.arg1 = position;
                    } else {
                        // 弹出删除失败原因
                        msg.obj = comResponseResult.exMsg;
                    }
                } else {
                    msg.obj = getResources().getString(R.string.net_status_error);
                }
                mHandler.sendMessage(msg);
            }
        });
    }

    @Subscribe
    public void getEditGroupUserInfo(EditUserMessage editUserMessage) {
        if (editUserMessage != null && editUserMessage.type == 1) {
            LogUtil.i(TAG, "onActivityResult 新修改第[" + editUserMessage.position + "]行的数据: " + editUserMessage.editUserInfo.toString1());
            groupAdapter.updateItemData(editUserMessage.position, editUserMessage.editUserInfo);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == requestcode_to_register_face_lib && resultCode == 555) {
            GroupUserInfo registerBean = (GroupUserInfo) data.getExtras().get("registerBean");
            LogUtil.i(TAG, "onActivityResult 新注册的数据: " + registerBean.toString());

            // 当列表为空时直接刷新
            if (sysPushBeanArrayList.isEmpty()) {
                groupAdapter.notifySingleData(registerBean);
            } else {
                // 在list列表中添加新注册的用户item
                groupAdapter.insertHeaderData(registerBean);
                group_recyclerView.scrollToPosition(0);
            }
            tv_how_many_groups.setText(String.format(getResources().getString(R.string.face_lib_how_many_groups),
                    groupAdapter.getItemCount()));

            // 存储到首页集合中
            EventBus.getDefault().post(registerBean);
        } else if (requestCode == requestcode_to_edt_face_lib && resultCode == 666) {
            GroupUserInfo registerBean = (GroupUserInfo) data.getExtras().get("registerBean");
            int position = data.getExtras().getInt("position", 0);
            LogUtil.i(TAG, "onActivityResult 新修改第[" + position + "]行的数据: " + registerBean.toString());
            groupAdapter.updateItemData(position, registerBean);
        }
    }

    @Override
    protected boolean isEventSubscribe() {
        return true;
    }
}
