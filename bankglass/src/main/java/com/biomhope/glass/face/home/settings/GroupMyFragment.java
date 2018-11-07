package com.biomhope.glass.face.home.settings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.biomhope.glass.face.R;
import com.biomhope.glass.face.global.BaseFragment;
import com.biomhope.glass.face.bean.GroupMyItemBean;
import com.biomhope.glass.face.bean.GroupMyResult;
import com.biomhope.glass.face.bean.GroupUserInfo;
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
import java.util.List;

import butterknife.BindView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @author $USER_NAME
 * create at : 2018-10-12
 * description :
 */
public class GroupMyFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    @BindView(R.id.face_lib_group_swipe_refresh)
    SwipeRefreshLayout face_lib_group_swipe_refresh;

    @BindView(R.id.events_list_lv_list)
    ListView events_list_lv_list;

    @BindView(R.id.tv_how_many_groups)
    TextView tv_how_many_groups;

    private static final int msg_what_cancel_dialog = 10;
    private static final int msg_what_update_list = 20;
    private List<GroupMyItemBean> groupMyItemBeans = new ArrayList<>();
    private GroupMyAdapter groupMyAdapter;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case msg_what_cancel_dialog:
                    cancelDialog();
                    break;
                case msg_what_update_list:
                    GroupMyResult.GroupRuslt groupRuslt = (GroupMyResult.GroupRuslt) msg.obj;
                    // 目前三个人脸库固定 直接绑定数据 无视是否为Null
                    groupMyItemBeans.get(0).setList(groupRuslt.viplist);
                    groupMyItemBeans.get(1).setList(groupRuslt.commonlist);
                    groupMyItemBeans.get(2).setList(groupRuslt.blacklist);
                    groupMyAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_face_lib_group_my;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (CommonUtil.isFastDoubleClick()) return;
        GroupMyItemBean item = groupMyAdapter.getItem(position);
        Intent intent = new Intent(context, FaceLibraryGroupActivity.class);
        intent.putExtra("group_name", item.getGroupName());
        intent.putExtra("group_members", item.getMembers());
        intent.putExtra("group_list", item.getList());
        context.startActivity(intent);
        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @SuppressLint("StringFormatMatches")
    @Override
    protected void initialize() {

        face_lib_group_swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                face_lib_group_swipe_refresh.setRefreshing(false);
            }
        });

        groupMyItemBeans.add(new GroupMyItemBean(getResources().getString(R.string.user_type_vip), 0));
        groupMyItemBeans.add(new GroupMyItemBean(getResources().getString(R.string.user_type_com), 0));
        groupMyItemBeans.add(new GroupMyItemBean(getResources().getString(R.string.user_type_black), 0));
        groupMyAdapter = new GroupMyAdapter(groupMyItemBeans);
        events_list_lv_list.setAdapter(groupMyAdapter);
        events_list_lv_list.setOnItemClickListener(GroupMyFragment.this);

        tv_how_many_groups.setText(String.format(getResources().getString(R.string.face_lib_how_many_groups),
                groupMyItemBeans.size()));

    }

    @Override
    public void onResume() {
        super.onResume();
        requestData();
    }

    private void requestData() {
        showLoadingDialog();
        try {
            String requestUrl = Constants.REQUEST_HOST_MAIN_URL + Constants.REQUEST_DO_GETMYCREATELIB;
            LogUtil.i(TAG, "getMyCreateLib request url: " + requestUrl);
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
                    LogUtil.i(TAG, "getMyCreateLib response json: " + json);
                    if (response.code() == Constants.REQUEST_STATUS_SUCCESSFUL) {
                        final GroupMyResult resultTemp = new Gson().fromJson(json, GroupMyResult.class);
                        if (Constants.RESPONSE_STATUS_SUCCESSFUL.equals(resultTemp.exCode)) {
                            GroupMyResult.GroupRuslt groupRuslt = resultTemp.result;
                            Message msg = Message.obtain();
                            msg.what = msg_what_update_list;
                            msg.obj = groupRuslt;
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
    public void onDestroy() {
        cancelDialog();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }

    @Subscribe
    public void getNewRegister(GroupUserInfo newInfo) {
        if (newInfo != null) {
            LogUtil.i(TAG, TAG + " 接收到新注册的数据data: " + newInfo.toString());
//            if (GroupUserInfo.USER_TYPE_COMMON.equals(newInfo.family)) {
//                groupMyItemBeans.get(1).addNewRegister(newInfo);
//            } else if (GroupUserInfo.USER_TYPE_VIP.equals(newInfo.family)) {
//                groupMyItemBeans.get(0).addNewRegister(newInfo);
//            } else if (GroupUserInfo.USER_TYPE_BLACK.equals(newInfo.family)) {
//                groupMyItemBeans.get(2).addNewRegister(newInfo);
//            }
        }
    }

    @Override
    protected boolean isEventSubscribe() {
        return true;
    }

    private class GroupMyAdapter extends BaseAdapter {

        List<GroupMyItemBean> groupMyItemBeans;

        GroupMyAdapter(List<GroupMyItemBean> groupMyItemBeans) {
            this.groupMyItemBeans = groupMyItemBeans;
        }

        @Override
        public int getCount() {
            return groupMyItemBeans == null ? 0 : groupMyItemBeans.size();
        }

        @Override
        public GroupMyItemBean getItem(int position) {
            return groupMyItemBeans.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_face_lib_group_my, null);
            TextView tv_group_name = view.findViewById(R.id.tv_group_name);
            TextView tv_group_members = view.findViewById(R.id.tv_group_members);
            GroupMyItemBean myItemBean = groupMyItemBeans.get(position);
            tv_group_name.setText(myItemBean.getGroupName());
            int members = myItemBean.getMembers();
            String result = "";
            if (members >= 0) {
                result = members + "人";
            }
            tv_group_members.setText(result);
            return view;
        }
    }
}
