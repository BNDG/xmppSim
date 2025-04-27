package com.hjq.demo.chat.entity;

public class FileModel {
    private String fileName;
    private String filePath;
    // 文件来源
    private String fileFrom;

    // 文件日期
    private String fileDate;


    public FileModel(String fileName, String filePath, String fileFrom, String fileDate) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileFrom = fileFrom;
        this.fileDate = fileDate;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileFrom() {
        return fileFrom;
    }

    public String getFileDate() {
        return fileDate;
    }

}