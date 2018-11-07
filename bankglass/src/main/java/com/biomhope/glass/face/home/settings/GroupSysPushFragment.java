package com.biomhope.glass.face.home.settings;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.global.BaseFragment;
import com.biomhope.glass.face.bean.ComResponseResult;
import com.biomhope.glass.face.bean.FaceDelRequestBean;
import com.biomhope.glass.face.bean.GroupSysPushBean;
import com.biomhope.glass.face.bean.GroupUserInfo;
import com.biomhope.glass.face.bean.eventvo.EditUserMessage;
import com.biomhope.glass.face.global.Constants;
import com.biomhope.glass.face.utils.CommonUtil;
import com.biomhope.glass.face.utils.LogUtil;
import com.biomhope.glass.face.utils.OkHttpUtil;
import com.biomhope.glass.face.utils.SharedPreferencesUtils;
import com.google.gson.Gson;

import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @author $USER_NAME
 * create at : 2018-10-12
 * description :
 */
public class GroupSysPushFragment extends BaseFragment {

    @BindView(R.id.face_lib_group_swipe_refresh)
    SwipeRefreshLayout face_lib_group_swipe_refresh;

    @BindView(R.id.sys_push_recyclerView)
    RecyclerView sys_push_recyclerView;

    private Dialog longClickDialog;
    private static final int requestcode_to_edt_face_lib = 40;
    private static final int msg_what_cancel_dialog = 10;
    private static final int msg_what_show_list = 20;
    private static final int msg_del_result_content = 30;
    private ArrayList<GroupUserInfo> sysPushBeanArrayList = new ArrayList<>();
    private FaceLibGroupAdapter groupAdapter;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case msg_what_cancel_dialog:
                    cancelDialog();
                    break;
                case msg_what_show_list:
                    sysPushBeanArrayList = (ArrayList<GroupUserInfo>) msg.obj;
                    groupAdapter.notifyData(sysPushBeanArrayList);
                    break;
                case msg_del_result_content:
                    cancelDialog();
                    String result = (String) msg.obj;
                    if (TextUtils.isEmpty(result) && msg.arg1 != -1) {
                        groupAdapter.removeItemData(msg.arg1);
//                        tv_how_many_groups.setText(String.format(getResources().getString(R.string.face_lib_how_many_groups_members),
//                                groupAdapter.getItemCount()));
                    } else {
                        showToast(result);
                    }
                    break;
            }
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_face_lib_group_syspush;
    }

    @Override
    protected void initialize() {
        face_lib_group_swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                face_lib_group_swipe_refresh.setRefreshing(false);
            }
        });
        groupAdapter = new FaceLibGroupAdapter(context, sysPushBeanArrayList, new FaceLibGroupAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(GroupUserInfo sysPushBean) {
                if (CommonUtil.isFastDoubleClick()) return;
                // 点击跳转到注册详情 还原数据
                Intent intent = new Intent(context, FaceLibGroupDetailActivity.class);
                intent.putExtra("bean", sysPushBean);
                intent.putExtra("group_name", "");
                startActivity(intent);
            }

            @Override
            public void onLongItemClick(int position, GroupUserInfo sysPushBean) {
                LogUtil.i(TAG, "当前点击的是[" + position + "] ,客户Id是[" + sysPushBean.clientId + "]");
                showLongClickDialog(position, sysPushBean);
            }
        });
        sys_push_recyclerView.setLayoutManager(new LinearLayoutManager(context));
        sys_push_recyclerView.setItemAnimator(new DefaultItemAnimator());
        sys_push_recyclerView.setAdapter(groupAdapter);
        requestData();
    }

    private void showLongClickDialog(final int position, final GroupUserInfo sysPushBean) {
        if (longClickDialog == null) {
            longClickDialog = new Dialog(context);
        }
        View view = View.inflate(context, R.layout.dialog_face_lib_long_click, null);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.width = CommonUtil.getScreenWidth(context) * 3 / 4;
        longClickDialog.setContentView(view, layoutParams);
        longClickDialog.setCanceledOnTouchOutside(true);
        longClickDialog.setCancelable(true);

        view.findViewById(R.id.more_dialog_rename).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 编辑
                longClickDialog.dismiss();
                Intent editIntent = new Intent(context, FaceLibEditActivity.class);
                editIntent.putExtra("sysPushBean", sysPushBean);
                editIntent.putExtra("position", position);
                editIntent.putExtra("type", 0);
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

    @Subscribe
    public void getEditGroupUserInfo(EditUserMessage editUserMessage) {
        if (editUserMessage != null && editUserMessage.type == 0) {
            LogUtil.i(TAG, "onActivityResult 新修改第[" + editUserMessage.position + "]行的数据: " + editUserMessage.editUserInfo.toString());
            groupAdapter.updateItemData(editUserMessage.position, editUserMessage.editUserInfo);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == requestcode_to_edt_face_lib && resultCode == 666) {
            GroupUserInfo registerBean = (GroupUserInfo) data.getExtras().get("registerBean");
            int position = data.getExtras().getInt("position", 0);
            LogUtil.i(TAG, "onActivityResult 新修改第[" + position + "]行的数据: " + registerBean.toString());
            groupAdapter.updateItemData(position, registerBean);
        }
    }

    private void requestDeteleFace(final int position, String clientId) {
        String userId = (String) SharedPreferencesUtils.get(context, "userId", "");
        String requestUrl = Constants.REQUEST_HOST_MAIN_URL + Constants.REQUEST_DO_CUSTOMERDELETE;
        LogUtil.i(TAG, "customerDelete request url: " + requestUrl);
        LogUtil.i(TAG, "删除第[" + position + "]行用户信息: " + new FaceDelRequestBean(userId, clientId).toString());
        OkHttpUtil.getInstance().doAsyncOkHttpPost(requestUrl, new Gson().toJson(new FaceDelRequestBean(userId, clientId)), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtil.e(TAG, "onFailure: " + e.getMessage());
                mHandler.sendEmptyMessage(msg_what_cancel_dialog);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                LogUtil.i(TAG, "onResponse: json = " + json);

                Message msg = Message.obtain();
                msg.what = msg_del_result_content;
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

    private void requestData() {
        showLoadingDialog();
        try {
            String requestUrl = Constants.REQUEST_HOST_MAIN_URL + Constants.REQUEST_DO_GETSERVERPUSHLIB;
            LogUtil.i(TAG, "getServerPushLib request url: " + requestUrl);
            String userId = (String) SharedPreferencesUtils.get(context, "userId", "");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userId", userId);
            OkHttpUtil.getInstance().doAsyncOkHttpPost(requestUrl, jsonObject.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    LogUtil.e(TAG, "onFailure: " + e.getMessage());
                    mHandler.sendEmptyMessage(msg_what_cancel_dialog);
                    showConnectHttpError();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    mHandler.sendEmptyMessage(msg_what_cancel_dialog);
                    String json = response.body().string();
                    LogUtil.i(TAG, "getServerPushLib response json: " + json);
                    if (response.code() == Constants.REQUEST_STATUS_SUCCESSFUL) {
                        final GroupSysPushBean resultTemp = new Gson().fromJson(json, GroupSysPushBean.class);
                        if (Constants.RESPONSE_STATUS_SUCCESSFUL.equals(resultTemp.exCode)) {
                            ArrayList<GroupUserInfo> sysPushBeanArrayList = resultTemp.result;
                            Message msg = Message.obtain();
                            msg.what = msg_what_show_list;
                            msg.obj = sysPushBeanArrayList;
                            mHandler.sendMessage(msg);
                        } else {
                            // 请求成功 但数据错误
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showToast(resultTemp.exMsg);
                                }
                            }, 0);
                        }
                    } else {
                        // 请求失败
                        showResponseHttpError();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected boolean isEventSubscribe() {
        return true;
    }
}
