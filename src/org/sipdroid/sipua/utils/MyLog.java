package org.sipdroid.sipua.utils;

import android.util.Log;

public class MyLog
{

    public static final boolean DEBUG = true;
    public static final boolean LOGIN_DEBUG = true;
    public static final boolean CALL_DEBUG = true;
    public static final boolean FACETIME_DEBUG = true;
    public static final boolean SMS_DEBUG = true;

    public static void i(String tag, String msg)
    {
        if (DEBUG)
        {
            Log.i("Sipdroid_"+tag, msg);
        }
    }

    public static void d(String tag, String msg)
    {
        if (DEBUG)
        {
            Log.d("Sipdroid_"+tag, msg);
        }
    }
    
    public static void e(String tag, String msg)
    {
        if (DEBUG)
        {
            Log.e("Sipdroid_"+tag,  msg);
        }
    }

    public static void e(String tag, String msg, Throwable tr)
    {
        if (DEBUG)
        {
            Log.e("Sipdroid_"+tag, msg, tr);
        }
    }


    /**
     * Loginģ��info log
     */
    public static void login_i(String msg){
    	if (DEBUG) {
			if (LOGIN_DEBUG) {
				Log.i("Sipdroid_login_log", msg);
			}
		}
    }
    /**
     * Loginģ��error log
     */
    public static void login_e(String msg){
    	if (DEBUG) {
			if (LOGIN_DEBUG) {
				Log.e("Sipdroid_login_log", msg);
			}
		}
    }
    
    public static void login_e(String msg,Throwable e){
    	if (DEBUG) {
			if (LOGIN_DEBUG) {
				Log.e("Sipdroid_login_log", msg, e);
			}
		}
    }
    
    public static void call_i(String msg){
    	if (DEBUG) {
			if (CALL_DEBUG) {
				Log.i("Sipdroid_call_log", msg);
			}
		}
    }
    
    public static void call_e(String msg){
    	if (DEBUG) {
			if (CALL_DEBUG) {
				Log.e("Sipdroid_call_log", msg);
			}
		}
    }
    
    public static void call_e(String msg , Throwable e){
    	if (DEBUG) {
			if (CALL_DEBUG) {
				Log.e("Sipdroid_call_log", msg , e);
			}
		}
    }
    
    public static void facetime_i(String msg){
    	if (DEBUG) {
			if (FACETIME_DEBUG) {
				Log.i("Sipdroid_facetime", msg);
			}
		}
    }
    public static void facetime_e(String msg){
    	if (DEBUG) {
			if (FACETIME_DEBUG) {
				Log.e("Sipdroid_facetime", msg);
			}
		}
    }
    public static void facetime_e(String msg , Throwable e){
    	if (DEBUG) {
			if (FACETIME_DEBUG) {
				Log.e("Sipdroid_facetime", msg , e);
			}
		}
    }
    public static void sms_i(String msg){
    	if (DEBUG) {
			if (SMS_DEBUG) {
				Log.i("Sipdroid_sms_log", msg);
			}
		}
    }
    public static void sms_e(String msg){
    	if (DEBUG) {
			if (SMS_DEBUG) {
				Log.e("Sipdroid_sms_log", msg);
			}
		}
    }
    public static void sms_e(String msg , Throwable e){
    	if (DEBUG) {
			if (SMS_DEBUG) {
				Log.e("Sipdroid_sms_log", msg , e);
			}
		}
    }
}