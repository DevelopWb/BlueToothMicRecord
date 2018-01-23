package com.bluetoothmicrecord.activity;

import android.app.Dialog;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bluetoothmicrecord.R;
import com.bluetoothmicrecord.adapter.RecordAudiosActivityAdapter;
import com.bluetoothmicrecord.bean.RecordAudioFile;
import com.bluetoothmicrecord.interfaces.RecordAudiosActivityAdapterInterface;
import com.bluetoothmicrecord.interfaces.UpdateProgressInterface;
import com.bluetoothmicrecord.upload.UploadFileManager;
import com.bluetoothmicrecord.upload.uploadUtil.NetworkManager;
import com.bluetoothmicrecord.upload.uploadUtil.PreferenceUtil;
import com.bluetoothmicrecord.utils.ActivityManager;
import com.bluetoothmicrecord.utils.DaoUtils;
import com.bluetoothmicrecord.utils.DividerItemDecoration;
import com.bluetoothmicrecord.utils.PubUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.bluetoothmicrecord.utils.PubUtils.dip2px;
import static com.bluetoothmicrecord.utils.PubUtils.selectedFiles;

public class RecordAudiosActivity extends BaseActivity implements View.OnClickListener {

    private ImageView mTitleLeftBackIv;
    /**
     * 蓝牙列表
     */
    private TextView mTitleNameTv;
    private ImageView mTitleRightAddIv;
    private RecyclerView mRecordAudioActivityRv;
    private RecordAudiosActivityAdapter adapter;
    private AudioManager mAudioManager = null;
    private DaoUtils greenDaoUtil;
    private UploadFileManager uploadFileManager;
    private MediaPlayer player;
    private ImageView mSelectAllAudiosIv;//全选按钮
    private LinearLayout mSelectAllLl;//全选按钮的父控件
    private LinearLayout mUploadAllLl;//上传按钮的父控件
    private LinearLayout mDeleteAllLl;//删除按钮的父控件
    private LinearLayout mTitleParentLl;
    private LinearLayout mRecordAudioSelecteLl;
    private boolean selecteStatus = false;//全选状态

    private List<RecordAudioFile> savedFiles = new ArrayList<>();//所有的录制的音频文件
    private ImageView mUploadAllAudiosIv;//上传按钮
    private ImageView mDeleteAllAudiosIv;//删除按钮
    private PreferenceUtil spUtil;
    private boolean updateAdapter = true;
    private RecordAudioFile CurrentRecordAudioFile = null;//当前播放的音频文件
    private String Tag = "888";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_audios);
        //初始化MediaPlayer
        ActivityManager.getInstance().addActivity(this);
        greenDaoUtil = new DaoUtils(this);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        spUtil = new PreferenceUtil(this, "APPSET");
        initView();
        uploadFileManager = new UploadFileManager(this, new UpdateProgressInterface() {
            @Override
            public void sendMsg(RecordAudioFile bean) {
                Message msg100 = new Message();
                msg100.what = 1;
                msg100.obj = bean;
                mHandler.sendMessage(msg100);
                if (spUtil.getBoolean("DELETEFILE")) {
                    if (bean.getUpLoadStatus().equals("3")) {
                        String path = bean.getFilePath();
                        File file = new File(path);
                        file.delete();
                        greenDaoUtil.deleteEntity(bean);
                        adapter.setData(getDataForAdapter());
                    }
                }
            }
        });
        player = new MediaPlayer();

    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (spUtil.getBoolean("AUTOUPLOAD")) {//如果开启自动上传，遍历数据，刷新adapter
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    while (updateAdapter) {
//                        if (updateAdapter()) {
//                            mHandler.sendEmptyMessage(10);
//                        } else {
//                            updateAdapter = false;
//                            mHandler.sendEmptyMessage(10);
//                        }
//                    }
//                }
//            }).start();
//        } else {
//            adapter.setData(getDataForAdapter());
//        }
        adapter.setData(getDataForAdapter());
    }

    /**
     * selectedFiles和savedFiles两个集合size的对比值
     * 0代表一个也没有选择，1代表有选择的item,但数量不是全部
     *
     * @return
     */
    private Integer getComparedValueForSelecteAllBt() {
        savedFiles = getDataForAdapter();
        if (selectedFiles.size() == 0) {
            return 0;
        } else if (selectedFiles.size() > 0 && selectedFiles.size() < savedFiles.size()) {
            return 1;
        } else {
            return 2;
        }
    }

    /**
     * 初始化全选标题栏的状态
     *
     * @return
     */
    private void initRecordAudioFileSelectedTitleStatus() {
//        mSelectAllAudiosIv
//                mUploadAllAudiosIv
//        mDeleteAllAudiosIv
        switch (getComparedValueForSelecteAllBt()) {
            case 0://上传按钮和删除按钮都提示选择item。全选按钮false
                mSelectAllAudiosIv.setImageResource(R.drawable.un_selected_icon);
                break;
            case 1://上传按钮和删除按钮可点击。全选按钮false
                mSelectAllAudiosIv.setImageResource(R.drawable.un_selected_icon);
                break;
            case 2://上传按钮和删除按钮可点击。全选按钮true,
                mSelectAllAudiosIv.setImageResource(R.drawable.selected_icon);
                break;
            default:
                break;
        }
    }

    /**
     * 查询单个蓝牙设备所有录制音频文件数据
     *
     * @return
     */
    private List<RecordAudioFile> getDataForAdapter() {
        List<RecordAudioFile> arrays = new ArrayList<>();
        String fileDir = PubUtils.AudiosDevice.getDeviceName().trim() + "-" + PubUtils.AudiosDevice.getDeviceAccount().trim();
        arrays = greenDaoUtil.queryAllRecordAudioFiles(fileDir);
        Collections.reverse(arrays);
        return arrays;
    }

    /**
     * 查询单个蓝牙设备所有录制音频文件中正在上传的个数
     *
     * @return
     */
    private boolean updateAdapter() {
        boolean updateable = false;
        List<RecordAudioFile> arrays = new ArrayList<>();
        List<RecordAudioFile> arraysUpdating = new ArrayList<>();
        String fileDir = PubUtils.AudiosDevice.getDeviceName().trim() + "-" + PubUtils.AudiosDevice.getDeviceAccount().trim();
        arrays = greenDaoUtil.queryAllRecordAudioFiles(fileDir);
        if (arrays.size() == 0) {
            updateable = false;
        } else {
            for (RecordAudioFile array : arrays) {
                if (array.getUpLoadStatus().equals("1")) {
                    arraysUpdating.add(array);
                }

            }
            if (arraysUpdating.size() > 0) {
                updateable = true;
            } else {
                updateable = false;
            }
        }
        return updateable;
    }


    /**
     * 开始播放音频
     */
    private void audioFilePlay(RecordAudioFile bean,ImageView view) {
        try {
            if (CurrentRecordAudioFile==null||!CurrentRecordAudioFile.equals(bean)) {
                adapter.setPlayingBean(bean);
                player.reset();
                player.setDataSource(bean.getFilePath());//重新设置要播放的音频
                player.prepare();//预加载音频
                CurrentRecordAudioFile = bean;
            }

            if (!player.isPlaying()) {
                player.start();//开始播放
                view.setImageResource(R.drawable.pause_normal);
            }else{
                player.pause();//暂停播放
                view.setImageResource(R.drawable.start_normal);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void initView() {
        mTitleLeftBackIv = (ImageView) findViewById(R.id.title_left_back_iv);
        mTitleLeftBackIv.setOnClickListener(this);
        mTitleNameTv = (TextView) findViewById(R.id.title_name_tv);
        mTitleNameTv.setText(PubUtils.AudiosDevice.getDeviceName() + "-录制的音频");
        mTitleRightAddIv = (ImageView) findViewById(R.id.title_right_add_iv);
        mTitleRightAddIv.setVisibility(View.INVISIBLE);
        mRecordAudioActivityRv = (RecyclerView) findViewById(R.id.record_audio_activity_rv);
        adapter = new RecordAudiosActivityAdapter(this, greenDaoUtil);
        adapter.setOnItemClickListener(new RecordAudiosActivityAdapterInterface() {
            @Override
            public void itemClick(RecordAudioFile bean) {
                if (selecteStatus) {//选择item条目
                    if (!selectedFiles.contains(bean)) {
                        selectedFiles.add(bean);
                    } else {
                        selectedFiles.remove(bean);
                    }
                    initRecordAudioFileSelectedTitleStatus();
                    adapter.notifyItemChanged(bean.getPosition(), "clicked");
                }
//                startPlayThoughSystemPlayer(bean);
            }

            @Override
            public void itemLongClick(RecordAudioFile bean) {//弹出全选布局
                showSelecterLayout();

            }

            @Override
            public void uploadFile(RecordAudioFile bean, int position) {
                uploadAudioFiles(bean);
            }

            @Override
            public void startPlayAudio(RecordAudioFile bean, final ImageView view) {//开始播放音频文件

                audioFilePlay(bean,view);
                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        view.setImageResource(R.drawable.start_normal);
                    }
                });
            }

            @Override
            public void deleteAudio(RecordAudioFile bean) {
                mHandler.sendEmptyMessage(10);
            }
        });

        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecordAudioActivityRv.setLayoutManager(manager);
        mRecordAudioActivityRv.setAdapter(adapter);
//        mRecordAudioActivityRv.scrollToPosition(adapter.getItemCount() - 1);
        mRecordAudioActivityRv.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.HORIZONTAL));


        mSelectAllAudiosIv = (ImageView) findViewById(R.id.select_all_audios_iv);
        mSelectAllLl = (LinearLayout) findViewById(R.id.select_all_ll);
        mSelectAllLl.setOnClickListener(this);
        mUploadAllLl = (LinearLayout) findViewById(R.id.upload_all_ll);
        mUploadAllLl.setOnClickListener(this);
        mDeleteAllLl = (LinearLayout) findViewById(R.id.delete_all_ll);
        mDeleteAllLl.setOnClickListener(this);
        mTitleParentLl = (LinearLayout) findViewById(R.id.title_parent_ll);
        mRecordAudioSelecteLl = (LinearLayout) findViewById(R.id.record_audio_selecte_ll);
        mUploadAllAudiosIv = (ImageView) findViewById(R.id.upload_all_audios_iv);
        mDeleteAllAudiosIv = (ImageView) findViewById(R.id.delete_all_audios_iv);
    }

    /**
     * 将选中的文件上传
     *
     * @param bean
     */
    private void uploadAudioFiles(RecordAudioFile bean) {
        if (!NetworkManager.isConnected(this)) {
            Toast.makeText(getApplicationContext(), "请确保网络正常", Toast.LENGTH_LONG).show();
            return;
        }
        if (bean.getUpLoadStatus().equals("0")) {//未上传，开始上传
            uploadFileManager.startUpLoad(bean);
            bean.setFileNameFromFtp(PubUtils.getRemoteFileName());
            bean.setUpLoadStatus("1");
            greenDaoUtil.updateEntity(bean);
        } else if (bean.getUpLoadStatus().equals("1")) {//正在上传
            uploadFileManager.pause(bean);
            bean.setUpLoadStatus("2");
            greenDaoUtil.updateEntity(bean);
        } else if (bean.getUpLoadStatus().equals("2")) {//已暂停
            uploadFileManager.startUpLoad(bean);
            bean.setUpLoadStatus("1");
            greenDaoUtil.updateEntity(bean);
        } else {
//                    uploadFileManager.startUpLoad(bean);
//                    bean.setUpLoadStatus("0");
//                    greenDaoUtil.updateEntity(bean);
            bean.setUpLoadStatus("3");
            greenDaoUtil.updateEntity(bean);
            uploadFileManager.startUpLoad(bean);
        }
        Message msg = new Message();
        msg.what = 8;
        msg.obj = bean;
        mHandler.sendMessage(msg);
    }

    /**
     * 弹出全选布局
     */
    private void showSelecterLayout() {
        mTitleParentLl.setVisibility(View.GONE);//隐藏标题布局
        mRecordAudioSelecteLl.setVisibility(View.VISIBLE);//展示全选布局
        adapter.setItemSelected(true);//更新adapter中的布局
        selecteStatus = true;
        initRecordAudioFileSelectedTitleStatus();
    }

    /**
     * 隐藏全选布局
     */
    private void hiddeSelecterLayout() {
        mTitleParentLl.setVisibility(View.VISIBLE);//隐藏标题布局
        mRecordAudioSelecteLl.setVisibility(View.GONE);//展示全选布局
        adapter.setItemSelected(false);//更新adapter中的布局
        selecteStatus = false;
        if (PubUtils.selectedFiles != null) {
            PubUtils.selectedFiles.clear();
        }

    }

    @Override
    public void onBackPressed() {
        if (selecteStatus) {
            hiddeSelecterLayout();
        } else {
            PubUtils.PwdDialogCanBeShow = true;
            super.onBackPressed();
        }


    }


    /**
     * 删除对象的对话框
     */
    private void showDeleteDialog(final List<RecordAudioFile> audioFiles) {
        View v = LayoutInflater.from(this).inflate(R.layout.delete_dialog, null);
        final Dialog dialog_c = new Dialog(this, R.style.DialogStyle);
        dialog_c.setCanceledOnTouchOutside(false);
        dialog_c.show();
        Window window = dialog_c.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        window.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        lp.width = dip2px(this, 300); // 宽度
        lp.height = dip2px(this, 230); // 高度
        // lp.alpha = 0.7f; // 透明度
        window.setAttributes(lp);
        window.setContentView(v);
        TextView confirm_tv = (TextView) v.findViewById(R.id.confirm_tv);
        TextView cancel_tv = (TextView) v.findViewById(R.id.cancel_tv);
        TextView warn_content_tv = (TextView) v.findViewById(R.id.warn_content_tv);
        warn_content_tv.setText("删除后录制音频也将被删除，确定删除吗？");
        cancel_tv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog_c.dismiss();
            }
        });
        confirm_tv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog_c.dismiss();
                for (RecordAudioFile audioFile : audioFiles) {

                    File file = new File(audioFile.getFilePath());
                    if (file.exists()) {
                        file.delete();
                        greenDaoUtil.deleteEntity(audioFile);
                    }
                }
                adapter.setData(getDataForAdapter());
                hiddeSelecterLayout();
                PubUtils.selectedFiles.clear();
                Toast.makeText(getApplicationContext(), "已删除", Toast.LENGTH_LONG).show();

            }
        });

    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100://上传进度
                    Message msg100 = new Message();
                    msg100.what = 1;
                    msg100.obj = msg.obj;
                    mHandler.sendMessage(msg100);
                    break;
                case 1:
                    RecordAudioFile recordAudioFile = (RecordAudioFile) msg.obj;
                    greenDaoUtil.updateEntity(recordAudioFile);
//                    adapter.notifyDataSetChanged();
                    adapter.notifyItemChanged(recordAudioFile.getPosition(), "change");
                    break;
                case 8://刷新item
                    RecordAudioFile recordAudioFile8 = (RecordAudioFile) msg.obj;
                    adapter.notifyDataSetChanged();
//                    adapter.notifyItemChanged(recordAudioFile8.getPosition(), "change");
                    break;
                case 9:
                    Toast.makeText(getApplicationContext(), "文件不存在", Toast.LENGTH_LONG).show();
                    break;
                case 10:
                    adapter.setData(getDataForAdapter());
                    break;
                default:
                    break;
            }


            super.handleMessage(msg);
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.title_left_back_iv://返回按钮
                PubUtils.PwdDialogCanBeShow = true;
                finish();
                break;
            case R.id.select_all_ll://全部选中音频文件

                if (getComparedValueForSelecteAllBt() == 2) {//
                    PubUtils.selectedFiles.clear();
                    mSelectAllAudiosIv.setImageResource(R.drawable.un_selected_icon);
                } else {
                    PubUtils.selectedFiles.clear();
                    mSelectAllAudiosIv.setImageResource(R.drawable.selected_icon);
                    //将所有录制视频添加到PubUtils.selectedFiles中
                    savedFiles = getDataForAdapter();
                    for (RecordAudioFile savedFile : savedFiles) {
                        PubUtils.selectedFiles.add(savedFile);
                    }
                }
                initRecordAudioFileSelectedTitleStatus();
                adapter.notifyDataSetChanged();
                break;
            case R.id.upload_all_ll://上传选中文件
                if (getComparedValueForSelecteAllBt() == 0) {//提示选择item
                    Toast.makeText(getApplicationContext(), "请选择需要上传的音频文件", Toast.LENGTH_LONG).show();
                    return;
                } else {

                    for (RecordAudioFile selectedFile : PubUtils.selectedFiles) {
                        uploadAudioFiles(selectedFile);
                    }
                    PubUtils.selectedFiles.clear();
                    hiddeSelecterLayout();
                }

                break;
            case R.id.delete_all_ll:
                if (getComparedValueForSelecteAllBt() == 0) {//提示选择item
                    Toast.makeText(getApplicationContext(), "请选择需要删除的音频文件", Toast.LENGTH_LONG).show();
                    return;
                } else {
                    showDeleteDialog(PubUtils.selectedFiles);
                }

                break;
        }
    }

    @Override
    protected void onDestroy() {
        updateAdapter = false;
        if (player.isPlaying()) {
            player.stop();//停止音频的播放
        }
        player.release();//释放资源
        super.onDestroy();
    }


}
