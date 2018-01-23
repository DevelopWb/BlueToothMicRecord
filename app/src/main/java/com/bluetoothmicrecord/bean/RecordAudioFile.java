package com.bluetoothmicrecord.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by ${王sir} on 2017/9/20.
 * application  录制音频文件的实体类
 *
 */
@Entity
public class RecordAudioFile {
   @Id
    private Long id;
    @Property
    private String fileName;//文件名称
    @Property
    private String fileDir;//存放文件目录
    @Property
    private String filePath;//文件存放路径
    @Property
    private String upLoadStatus;//文件上传状态，0代表未上传，1代表上传中，2代表已暂停，3代表已上传
    @Property
    private String recordTime;//文件录制时间
@Property
    private Integer uploadProgress;//上传进度
@Property
    private String FileNameFromFtp;//上传到ftp服务端的文件名
@Property
    private Integer position;//对应item中的position
@Generated(hash = 2105599646)
public RecordAudioFile(Long id, String fileName, String fileDir,
        String filePath, String upLoadStatus, String recordTime,
        Integer uploadProgress, String FileNameFromFtp, Integer position) {
    this.id = id;
    this.fileName = fileName;
    this.fileDir = fileDir;
    this.filePath = filePath;
    this.upLoadStatus = upLoadStatus;
    this.recordTime = recordTime;
    this.uploadProgress = uploadProgress;
    this.FileNameFromFtp = FileNameFromFtp;
    this.position = position;
}
@Generated(hash = 819359348)
public RecordAudioFile() {
}
public Long getId() {
    return this.id;
}
public void setId(Long id) {
    this.id = id;
}
public String getFileName() {
    return this.fileName;
}
public void setFileName(String fileName) {
    this.fileName = fileName;
}
public String getFileDir() {
    return this.fileDir;
}
public void setFileDir(String fileDir) {
    this.fileDir = fileDir;
}
public String getFilePath() {
    return this.filePath;
}
public void setFilePath(String filePath) {
    this.filePath = filePath;
}
public String getUpLoadStatus() {
    return this.upLoadStatus;
}
public void setUpLoadStatus(String upLoadStatus) {
    this.upLoadStatus = upLoadStatus;
}
public String getRecordTime() {
    return this.recordTime;
}
public void setRecordTime(String recordTime) {
    this.recordTime = recordTime;
}
public Integer getUploadProgress() {
    return this.uploadProgress;
}
public void setUploadProgress(Integer uploadProgress) {
    this.uploadProgress = uploadProgress;
}
public String getFileNameFromFtp() {
    return this.FileNameFromFtp;
}
public void setFileNameFromFtp(String FileNameFromFtp) {
    this.FileNameFromFtp = FileNameFromFtp;
}
public Integer getPosition() {
    return this.position;
}
public void setPosition(Integer position) {
    this.position = position;
}

}
