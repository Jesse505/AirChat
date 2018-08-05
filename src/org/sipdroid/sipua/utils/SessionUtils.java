package org.sipdroid.sipua.utils;

import java.util.HashMap;

import org.sipdroid.sipua.BaseApplication;

import com.leadcore.sms.entity.Users;

/**
 * 
 * @author zhaoyifei
 * 保存本机用户信息，包括ID,ip,name,IMEI
 */

public class SessionUtils {
    private static HashMap<String, String> mlocalUserSession = BaseApplication.getInstance()
            .getUserSession();

    /**
     * 获取用户数据库id
     * @return
     */
    public static int getLocalUserID(){
        return Integer.parseInt(mlocalUserSession.get(Users.ID));
    }

    /**
     * 获取本地IP
     * 
     * @return localIPaddress
     */
    public static String getLocalIPaddress() {
        return mlocalUserSession.get(Users.IPADDRESS);
    }

    /**
     * 获取热点IP
     * 
     * @return serverIPaddress
     */
    public static String getServerIPaddress() {
        return mlocalUserSession.get(Users.SERVERIPADDRESS);
    }

    /**
     * 获取昵称
     * 
     * @return Nickname
     */
    public static String getNickname() {
        return mlocalUserSession.get(Users.NICKNAME);
    }


    /**
     * 获取IMEI
     * 
     * @return IMEI
     */
    public static String getIMEI() {
        return mlocalUserSession.get(Users.IMEI);
    }

    
    /**
     * 设置用户数据库id
     * @param paramID
     */
    public static void setLocalUserID(int paramID){
        mlocalUserSession.put(Users.ID, String.valueOf(paramID));
    }


    /**
     * 设置本地IP
     * 
     * @param paramLocalIPaddress
     *            本地IP地址值
     */
    public static void setLocalIPaddress(String paramLocalIPaddress) {
        mlocalUserSession.put(Users.IPADDRESS, paramLocalIPaddress);
    }

    /**
     * 设置热点IP
     * 
     * @param paramServerIPaddress
     *            热点IP地址值
     */
    public static void setServerIPaddress(String paramServerIPaddress) {
        mlocalUserSession.put(Users.SERVERIPADDRESS, paramServerIPaddress);
    }

    /**
     * 设置昵称
     * 
     * @param paramNickname
     * 
     */
    public static void setNickname(String paramNickname) {
        mlocalUserSession.put(Users.NICKNAME, paramNickname);
    }


    /**
     * 设置IMEI
     * 
     * @param paramIMEI
     *            本机的IMEI值
     */
    public static void setIMEI(String paramIMEI) {
    	if (null != paramIMEI ) {
    		mlocalUserSession.put(Users.IMEI, paramIMEI);
		}else {
			mlocalUserSession.put(Users.IMEI, "");
		}
        
    }


    
    public static boolean isItself(String paramIMEI){
        if(paramIMEI == null){
            return false;
        }
        else if(getIMEI().equals(paramIMEI)){
            return true;
        }
        return false;
    }
    
    public static boolean isItGroupSelf(String SenderIMEI){
    	if (SenderIMEI == null) {
			return false;
		}
    	else if (getIMEI().equals(SenderIMEI)) {
			return true;
		}
    	return false;
    }

    /** 清空全局登陆Session信息 **/
    public static void clearSession() {
        mlocalUserSession.clear();
    }

}
