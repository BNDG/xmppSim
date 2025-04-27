package com.bndg.smack.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.bndg.smack.SmartCommHelper;
import com.bndg.smack.SmartIMClient;
import com.bndg.smack.utils.SmartTrace;

/**
 * @author r
 * @date 2024/8/16
 * @description Brief description of the file content.
 */
public class SmartIMService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        SmartTrace.w("执行了onCreat()");
        SmartIMClient.getInstance().checkConnection();
    }

    /**
     * 如果服务被启动（从onStartCommand返回后）后，服务所在进程被杀死，
     * 此时保持服务的启动状态，但是不保持服务之前的Intent。稍后系统会尝试重新创建该服务。
     * 因为该服务目前是启动状态，因此，创建新的服务实例后，一定会调用onStartCommand；
     * 当没有将要传递给服务启动命令时，服务将会被唤醒并附带空的Intent对象，请注意检查该Intent是否为空。
     *
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SmartTrace.w(">>>>", intent + "被杀后会自动重启 执行了onStartCommand()");
        // 【解释】被杀后自动重启，保持启动状态，不保持Intent，
        // 重新调用onStartCommand，无新Intent则为空Intent—杀死重启后，
        // 不继续执行先前任务，能接受新任务
        SmartTrace.w(">>>>", intent + "被杀后会自动重启 执行了onStartCommand()");
        SmartIMClient.getInstance().checkConnection();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SmartTrace.w("执行了onDestory()");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    //新建一个子类继承自Binder类
    public class SmartCommBinder extends Binder {
        public SmartIMService getService() {
            return SmartIMService.this;
        }

    }

    private SmartCommBinder mBinder = new SmartCommBinder();
}
