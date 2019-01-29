package org.sipdroid.sipua.utils;

import java.util.LinkedList;

import org.sipdroid.sipua.BaseActivity;
import org.sipdroid.sipua.BaseApplication;
import org.sipdroid.sipua.ui.Receiver;


import android.content.Context;


/**
 * @fileName ActivityCollectorUtils.java
 * @package szu.wifichat.android.util
 * @description 活动管理类
 **/
public class ActivityCollectorUtils {

    private static LinkedList<BaseActivity> queue = new LinkedList<BaseActivity>();

    public static void addActivity(BaseActivity activity) {
        queue.add(activity);
    }

    public static void removeActivity(BaseActivity activity) {
        queue.remove(activity);
    }

    public static void finishAllActivities(BaseApplication mApplication, Context context) {  
        for (BaseActivity activity : queue) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
    }    
    
    public static int getActivitiesNum(){
        if(!queue.isEmpty()){
            return queue.size();
        }
        return 0;
    }

    public static BaseActivity getLastActivity(){
        if(!queue.isEmpty()){
            return queue.getLast();
        }
        return null;
    }
}
