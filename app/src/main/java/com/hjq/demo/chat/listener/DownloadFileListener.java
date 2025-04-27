package com.hjq.demo.chat.listener;

import java.io.File;

/**
 * @author r
 * @date 2024/7/30
 * @description Brief description of the file content.
 */
public interface DownloadFileListener {
    void onComplete(File var1);
    void onError(File var1, Throwable var2);
}
