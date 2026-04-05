package com.ta.model;

public class Attachment {
    private String id;
    private String fileName;
    private String fileType;           // 文件原始扩展名: .pdf, .docx, .xlsx, .jpg, .png
    private String label;              // 标签: 简历, 证书佐证, 成绩单
    private long fileSize;             // 字节
    private String uploadedAt;         // ISO-8601 timestamp

    public Attachment() {
    }

    public Attachment(String id, String fileName, String fileType, String label, long fileSize, String uploadedAt) {
        this.id = id;
        this.fileName = fileName;
        this.fileType = fileType;
        this.label = label;
        this.fileSize = fileSize;
        this.uploadedAt = uploadedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(String uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
