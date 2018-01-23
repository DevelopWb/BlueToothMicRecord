package com.bluetoothmicrecord.upload;

import android.content.Context;

import com.bluetoothmicrecord.bean.RecordAudioFile;
import com.bluetoothmicrecord.interfaces.UpdateProgressInterface;
import com.bluetoothmicrecord.upload.uploadUtil.TaskThread;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by ${王sir} on 2017/9/21.
 * application
 */

public class UploadFileManager {
    private Context context;
    private static ExecutorService mUploadFixedThreadPool;
    public Map<String, Updater> MyUpdaters = new HashMap<String, Updater>();//上传文件路径和上传器的映射
    private UpdateProgressInterface updateProgressInterface;

    public UploadFileManager(Context context, UpdateProgressInterface updateProgressInterface) {
        this.context = context;
        this.updateProgressInterface = updateProgressInterface;
        if (null == mUploadFixedThreadPool) {
            mUploadFixedThreadPool = Executors.newFixedThreadPool(3);
        }
    }


    /**
     * 功能： 执行线程池任务.
     * <p>
     * 任务类型
     * 参数信息，创建任务对象的参数
     */
    private void execute(Updater mUpdater) {

        TaskThread taskThread = new TaskThread(mUpdater);
        // 把线程追加到线程池中
        mUploadFixedThreadPool.execute(taskThread);
    }

    /**
     * 开始上传文件
     *
     * @param
     */
    public void startUpLoad(RecordAudioFile bean) {
//		if (UpdatingState.pause == mFile.getUpdatingState())
//			return;

//		if (checkNetwork()) {
        // 添加线程管理
        Updater mUpdater = MyUpdaters.get(bean.getFilePath());
        if (mUpdater == null) {
            mUpdater = new Updater(context, bean,updateProgressInterface);
            MyUpdaters.put(bean.getFilePath(), mUpdater);
        }
//        if (mUpdater.isupdating()) {
//            return;
//        }

        execute(mUpdater);
    }

    /**
     * @param infos
     *            批量上传
     */
    public void batchupdate(List<RecordAudioFile> infos) {
//        if (checkNetwork()) {
            if (null != infos) {
                for (RecordAudioFile mFile : infos) {
                    startUpLoad(mFile); // 批量上传
                }
            }
//        }
    }

    /**
     * 暂停上传文件
     * @param file
     */
    public  void pause(RecordAudioFile file) {
        Updater mUpdater = MyUpdaters.get(file.getFilePath());


        //加一个判断防止出现空指针
        if (mUpdater!=null) {
            mUpdater.pause();
            MyUpdaters.remove(file.getFilePath());
        }



    }

    /**
     * 删除上传文件
     * @param file
     */
    public void delet(RecordAudioFile file) {
        Updater mUpdater = MyUpdaters.get(file.getFilePath());
        if (null != mUpdater) {
            mUpdater.pause();
            MyUpdaters.remove(file.getFilePath());
        }
    }
}
