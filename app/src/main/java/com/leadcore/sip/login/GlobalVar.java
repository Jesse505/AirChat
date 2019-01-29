
package com.leadcore.sip.login;

public class GlobalVar {
    public static String number;
    public static boolean bADHocMode = false;
    public static boolean bBackOrSwitch;
    public static String displayname = "";
    public static boolean isLandscap;
    public static boolean isVideoDisp;
    public static boolean mCameraIsUsed = false;
    public static String mLocalNum;
    public static boolean mLogout;
    public static int mMyPid;
    public static boolean mSendVideo;
    public static String videoMonitorPrefix;

    static {
        number = "";
        bBackOrSwitch = false;
        isVideoDisp = false;
        isLandscap = true;
        mSendVideo = true;
        mLogout = false;
        videoMonitorPrefix = "815";
    }
}
