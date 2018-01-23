package com.bluetoothmicrecord.utils;

import android.content.Context;

import com.bluetoothmicrecord.BluetoothDevicesDao;
import com.bluetoothmicrecord.DaoSession;
import com.bluetoothmicrecord.RecordAudioFileDao;
import com.bluetoothmicrecord.bean.BluetoothDevices;
import com.bluetoothmicrecord.bean.RecordAudioFile;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

/**
 * Created by Koterwong on 2016/7/31.
 */
public class DaoUtils<Entity> {

    private static final String TAG = "DaoUtils";

    private final DaoSession daoSession;

    public DaoUtils(Context context) {
        DaoManager manager = DaoManager.getInstance(context);
        daoSession = manager.getDaoSession();
    }

    /**
     * 插入蓝牙设备操作
     *
     * @return boolean
     */
    public void insertBluetoothDevices(BluetoothDevices bluetoothDevices) {
        daoSession.getBluetoothDevicesDao().insert(bluetoothDevices);
    }

    /**
     * 插入录制音频文件
     *
     * @return boolean
     */
    public void insertRecordAudioFile(RecordAudioFile recordAudioFile) {
        daoSession.getRecordAudioFileDao().insert(recordAudioFile);
    }

    /**
     * 插入录制音频文件
     */
    public void insertRecordAudioFiles(List<RecordAudioFile> recordAudioFileList) {
        if (recordAudioFileList == null || recordAudioFileList.isEmpty()) {
            return;
        }
        RecordAudioFileDao userDao = daoSession.getRecordAudioFileDao();
        userDao.insertInTx(recordAudioFileList);
    }


    /**
     * 删除录制音频文件记录
     */
    public void deleteRecordAudioFile(RecordAudioFile recordAudioFile) {
        RecordAudioFileDao userDao = daoSession.getRecordAudioFileDao();
        userDao.delete(recordAudioFile);
    }

    /**
     * 更新操作
     *
     * @param entity entity
     * @return boolean
     */
    public boolean updateEntity(Entity entity) {
        boolean flag = false;
        try {
            daoSession.update(entity);
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 删除操作
     *
     * @param entity entity
     * @return boolean
     */
    public boolean deleteEntity(Entity entity) {
        boolean flag = false;
        try {
            daoSession.delete(entity);
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 删除所有记录
     *
     * @param entity entityClass
     * @return boolean
     */
    public boolean deleteAll(Entity entity) {
        boolean flag = false;
        try {
            daoSession.deleteAll(entity.getClass());
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 查询所有记录
     *
     * @return List<Entity>
     */
    @SuppressWarnings("unchecked")
    public List<Entity> listAll(Entity entity) {
        return (List<Entity>) daoSession.loadAll(entity.getClass());
    }

    /**
     * @desc 查询所有添加蓝牙设备数据
     **/
    public List<Entity> queryAllBluetoothDevices() {
        QueryBuilder<Entity> builder = (QueryBuilder<Entity>) daoSession.getBluetoothDevicesDao().queryBuilder();

        return builder.build().list();
    }

    /**
     * @desc 查询单个蓝牙设备所有录制音频文件数据
     **/
    public List<RecordAudioFile> queryAllRecordAudioFiles(String mac) {
        QueryBuilder<RecordAudioFile> builder = (QueryBuilder<RecordAudioFile>) daoSession.getRecordAudioFileDao().queryBuilder();
        builder.where(RecordAudioFileDao.Properties.FileDir.eq(mac)).orderAsc(RecordAudioFileDao.Properties.FileDir);
        return builder.list();
    }

    /**
     * @desc 查询所有蓝牙设备对应录制音频文件数据
     **/
    public List<RecordAudioFile> queryAllRecordAudioFiles() {
        QueryBuilder<RecordAudioFile> builder = (QueryBuilder<RecordAudioFile>) daoSession.getRecordAudioFileDao().queryBuilder();
        return builder.list();
    }


    /**
     * 查询用户是否添加
     */
    public boolean checkDeviceWhetherSaved(String mac) {
        BluetoothDevicesDao bluetoothDevicesDao = daoSession.getBluetoothDevicesDao();
        QueryBuilder<BluetoothDevices> qb = bluetoothDevicesDao.queryBuilder();
        qb.where(BluetoothDevicesDao.Properties.Mac.eq(mac)).orderAsc(BluetoothDevicesDao.Properties.Mac);
        List<BluetoothDevices> list = qb.list();
        if (list.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 查询单个用户
     */
    public BluetoothDevices getDeviceThroughMac(String mac) {
        BluetoothDevicesDao bluetoothDevicesDao = daoSession.getBluetoothDevicesDao();
        QueryBuilder<BluetoothDevices> qb = bluetoothDevicesDao.queryBuilder();
        qb.where(BluetoothDevicesDao.Properties.Mac.eq(mac)).orderAsc(BluetoothDevicesDao.Properties.Mac);
        return qb.list().get(0);
    }


//    /**
//     * 查询用户列表
//     */
//    public List<User> queryUserList() {
//        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
//        DaoSession daoSession = daoMaster.newSession();
//        UserDao userDao = daoSession.getUserDao();
//        QueryBuilder<User> qb = userDao.queryBuilder();
//        List<User> list = qb.list();
//        return list;
//    }

//    /**
//     * 查询用户列表
//     */
//    public List<User> queryUserList(int age) {
//        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
//        DaoSession daoSession = daoMaster.newSession();
//        UserDao userDao = daoSession.getUserDao();
//        QueryBuilder<User> qb = userDao.queryBuilder();
//        qb.where(UserDao.Properties.Age.gt(age)).orderAsc(UserDao.Properties.Age);
//        List<User> list = qb.list();
//        return list;
//    }
}
