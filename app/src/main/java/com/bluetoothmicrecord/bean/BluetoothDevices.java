package com.bluetoothmicrecord.bean;


import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by ${王sir} on 2017/9/6.
 * application
 */
@Entity
public class BluetoothDevices {


    @Id
    //标明主键
    private Long id;
    @Property
    private String mac;//设备唯一标识，mac地址
    @Property
    private String deviceName;//设备名称
    @Property
    private String deviceAccount;//设备账号
    @Property
    private String devicePassword;//设备密码
    @Property
    private String addTime;//添加时间
    @Property
    private String matchStatus;//设备配对状态
    @Property
    private String connectStatus;//设备连接状态
    @Property
    private boolean hasRecordAudio;//是否有录制音频
    @Generated(hash = 845024162)
    public BluetoothDevices(Long id, String mac, String deviceName,
            String deviceAccount, String devicePassword, String addTime,
            String matchStatus, String connectStatus, boolean hasRecordAudio) {
        this.id = id;
        this.mac = mac;
        this.deviceName = deviceName;
        this.deviceAccount = deviceAccount;
        this.devicePassword = devicePassword;
        this.addTime = addTime;
        this.matchStatus = matchStatus;
        this.connectStatus = connectStatus;
        this.hasRecordAudio = hasRecordAudio;
    }
    @Generated(hash = 1417236847)
    public BluetoothDevices() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getMac() {
        return this.mac;
    }
    public void setMac(String mac) {
        this.mac = mac;
    }
    public String getDeviceName() {
        return this.deviceName;
    }
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
    public String getDeviceAccount() {
        return this.deviceAccount;
    }
    public void setDeviceAccount(String deviceAccount) {
        this.deviceAccount = deviceAccount;
    }
    public String getDevicePassword() {
        return this.devicePassword;
    }
    public void setDevicePassword(String devicePassword) {
        this.devicePassword = devicePassword;
    }
    public String getAddTime() {
        return this.addTime;
    }
    public void setAddTime(String addTime) {
        this.addTime = addTime;
    }
    public String getMatchStatus() {
        return this.matchStatus;
    }
    public void setMatchStatus(String matchStatus) {
        this.matchStatus = matchStatus;
    }
    public String getConnectStatus() {
        return this.connectStatus;
    }
    public void setConnectStatus(String connectStatus) {
        this.connectStatus = connectStatus;
    }
    public boolean getHasRecordAudio() {
        return this.hasRecordAudio;
    }
    public void setHasRecordAudio(boolean hasRecordAudio) {
        this.hasRecordAudio = hasRecordAudio;
    }

}
