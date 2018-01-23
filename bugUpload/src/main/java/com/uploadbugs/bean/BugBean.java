package com.uploadbugs.bean;

/**
 * Created by ${王sir} on 2017/10/26.
 * application 软件bug实体类，用于提交到服务端的
 */

public class BugBean {


    private String mobileType;//机型
    private String mobileOS;//手机操作系统
    private String mobileImei;//手机imei 唯一标识
    private String softwareVersion;//软件版本号
    private String regCode;//软件注册码
    private String softwareMark;//软件标识
    private String netWorkType;//网络类型
    private String appearTime;//bug出现的时间
    private String bugInfo;//bug内容

    public String getAppearTime() {
        return appearTime;
    }

    public void setAppearTime(String appearTime) {
        this.appearTime = appearTime;
    }

    public String getBugInfo() {
        return bugInfo;
    }

    public void setBugInfo(String bugInfo) {
        this.bugInfo = bugInfo;
    }

    public String getMobileType() {
        return mobileType;
    }

    public void setMobileType(String mobileType) {
        this.mobileType = mobileType;
    }

    public String getMobileOS() {
        return mobileOS;
    }

    public void setMobileOS(String mobileOS) {
        this.mobileOS = mobileOS;
    }

    public String getMobileImei() {
        return mobileImei;
    }

    public void setMobileImei(String mobileImei) {
        this.mobileImei = mobileImei;
    }

    public String getRegCode() {
        return regCode;
    }

    public void setRegCode(String regCode) {
        this.regCode = regCode;
    }

    public String getSoftwareMark() {
        return softwareMark;
    }

    public void setSoftwareMark(String softwareMark) {
        this.softwareMark = softwareMark;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    public String getNetWorkType() {
        return netWorkType;
    }

    public void setNetWorkType(String netWorkType) {
        this.netWorkType = netWorkType;
    }
}
