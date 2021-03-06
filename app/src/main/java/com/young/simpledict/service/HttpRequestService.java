package com.young.simpledict.service;

import com.young.simpledict.service.event.DownloadBlobRequest;
import com.young.simpledict.service.event.OnApplicationTerminateEvent;
import com.young.simpledict.service.event.SearchWordRequest;
import com.young.simpledict.service.task.BaseTask;
import com.young.simpledict.service.task.DownloadBlobTask;
import com.young.simpledict.service.task.SearchWordTask;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Author: landerlyoung
 * Date:   2014-10-21
 * Time:   15:46
 * Life with passion. Code with creativity!
 */
public class HttpRequestService {
    public static final int MAX_TASK_RETRY_TIMES = 2;
    private ExecutorService mThreadPool;

    public HttpRequestService() {
        mThreadPool = new ThreadPoolExecutor(0, 2, 30,
                TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        EventBus.getDefault().register(this);
    }

    public boolean retryTask(BaseTask<?> task) {
        if (task.increaseRetryCount() <= MAX_TASK_RETRY_TIMES) {
            submitTask(task);
            return true;
        } else {
            task.onTaskFail();
            return false;
        }
    }

    public boolean submitTask(BaseTask<?> task) {
        //TODO add some future related stuff
        mThreadPool.submit(task);
        return true;
    }

    private boolean submitTask(Class<? extends BaseTask<?>> taskClazz, Object... params) {
        BaseTask<?> task = null;
        try {
            Class<?>[] paramType = new Class<?>[params.length];
            for (int i = 0; i < params.length; i++) {
                paramType[i] = params[i].getClass();
            }
            task = taskClazz.getConstructor(paramType).newInstance(params);
            return submitTask(task);
        } catch (Exception e) {
            return false;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SearchWordRequest request) {
        submitTask(SearchWordTask.class, request);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(DownloadBlobRequest request) {
        submitTask(DownloadBlobTask.class, request);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(OnApplicationTerminateEvent e) {
        EventBus.getDefault().unregister(this);
        mThreadPool.shutdownNow();
    }
}
