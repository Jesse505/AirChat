
package com.leadcore.sip.login;

import java.io.IOException;
import java.io.OptionalDataException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.sipdroid.sipua.BaseActivity;
import org.sipdroid.sipua.ui.Receiver;
import org.sipdroid.sipua.ui.Settings;
import org.sipdroid.sipua.utils.ByteUtils;
import org.sipdroid.sipua.utils.MyLog;
import org.sipdroid.sipua.utils.NetworkUtil;
import com.leadcore.sms.entity.Users;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.widget.TextView;
import android.widget.Toast;


public class AdhocManager {

    private static final String  TAG = "AdhocManager";
    public static final String ACTION_ONLINE = "com.example.test_online_lan.personOnLine";
    public static final String ACTION_OFFLINE = "com.example.test_online_lan.personOffLine";
    public static final String ACTION_SCREEN_REFRESH = "com.example.test_online_lan.screen.refresh";
    public static final String ACTION_GROUP_REFRESH = "com.example.test_online_lan.group.refresh";
    public static final long TIME_HEART_PERIOD = 5000L;//心跳周期
    public static final long TIMEOUT_HEART_BEAT = 10000L;//心跳超时时间
    public static final int CMD_TYPE_LOGIN_UDP = 1;//入自组网udp广播
    public static final int CMD_TYPE_HEART_UDP = 2;//心跳udp广播
    
    private Context mContext;
    private static AdhocManager instance = null;
    private String localIp = null;
    private BroadcastReceiver broadcastReceiver;
    private static Person me = null;
    private boolean isStartOK = false;
    private AdhocCommunication adhocCom = null;
    boolean isStopUpdateMe = false;
    
    TextView showText = null;
    
    
    public ConcurrentHashMap<String, Person> childrenMap = new ConcurrentHashMap();
    private List<NodeResource> list = new ArrayList();//用户列表
    private CheckUserOnline mCheckUserOnline = null;
    private UpdateMe mUpdateMe = null;
    private noticeOthers noticeOthers = null;
    
    private final String MUTIBROADCAST_ADDRESS = "239.0.0.56";
    private final String BROADCAST_ADDRESS = "255.255.255.255";
    
    private AdhocManager(Context context) {
        // TODO Auto-generated constructor stub
        mContext = context;
        localIp = NetworkUtil.getLocalWifiIP(mContext);
//        Log.d(TAG, "Adhoc - localIp = " + this.localIp);
        broadcastReceiver = new BroadcastReceiver() {
            
            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO Auto-generated method stub
                String action = intent.getAction();
                if (ACTION_ONLINE.equals(action)) {
                    List localList2 = parserContactsTree();
                    //SystemVarTools.setContactAll(localList2);
                    Intent localIntent3 = new Intent();
                    localIntent3.setAction(ACTION_SCREEN_REFRESH);
                    mContext.sendBroadcast(localIntent3);
//                    showToast("有用户上线!" + localList2.size());
                } else if (ACTION_OFFLINE.equals(action)) {
                    List localList1 = AdhocManager.this.parserContactsTree();
                    //SystemVarTools.setContactAll(localList1);
                    Intent localIntent1 = new Intent();
                    localIntent1.setAction(ACTION_SCREEN_REFRESH);
                    mContext.sendBroadcast(localIntent1);
//                    showToast("有用户下线!" + localList1.size());
                } else {
                    
                }
                
                showContacts ();
            }
        };
        
        IntentFilter localIntentFilter = new IntentFilter();
        localIntentFilter.addAction(ACTION_OFFLINE);
        localIntentFilter.addAction(ACTION_ONLINE);
        mContext.registerReceiver(this.broadcastReceiver, localIntentFilter);
        
    }
    
    public void showToast(String paramString)
    {
      Toast localToast = Toast.makeText(mContext, paramString, 1);
      localToast.setGravity(16, 0, 0);
      localToast.show();
    }
    
    public static AdhocManager getInstance(Context context) {
        if (null == instance) {
        	synchronized (AdhocManager.class) {
				if (null == instance) {
					instance = new AdhocManager(context);
				}
			}
        }
        return instance;
    }
    
    public void StartAdhoc() {
        if (isStartOK) {
            return;
        }
//        BaseActivity.mApplication.initParam();
        this.adhocCom = new AdhocCommunication("自组网");
        this.adhocCom.start();
        getMyInfomation();
        this.mUpdateMe = new UpdateMe();
        this.mUpdateMe.start();
        this.mCheckUserOnline = new CheckUserOnline();
        this.mCheckUserOnline.start();
        this.noticeOthers = new noticeOthers();
        noticeOthers.start();
        this.isStartOK = true;
        showContacts();
    }
    public void StopAdhoc() {
        this.isStartOK = false;
        this.isStopUpdateMe = true;
        if (this.childrenMap != null) {
            this.childrenMap.clear();
        }
        if (this.adhocCom != null) {
            this.adhocCom.release();
            this.adhocCom = null;
        }
        mContext.unregisterReceiver(broadcastReceiver);
        me = null;
        instance = null;
//        Log.d("ServiceAdhoc", "GLE---Service on destory...");
    }
    
    public boolean isStartOK() {
        return this.isStartOK;
    }
    
    private class AdhocCommunication extends Thread {
        private byte[] recvBuffer = null;
        //modify by zyf lc is zubo,wifi is guangbo
		private DatagramSocket socketReceive = null;
        private MulticastSocket mutiSocketReceive = null;
        
        public AdhocCommunication(String paramString) {
            super();
        }
        public void run() {
            super.run();
            
            receive();
            showContacts ();
        }
        
        private void receive(){
        	if (NetworkUtil.network_type == NetworkUtil.ADHOC_NETWORK) {
                try {
                	//modify by zyf lc is zubo,wifi is guangbo
                    this.mutiSocketReceive = new MulticastSocket(5760);
                    InetAddress group = InetAddress.getByName(MUTIBROADCAST_ADDRESS);
                    mutiSocketReceive.joinGroup(group);
//                  this.socketReceive = new DatagramSocket(5760);
                    while (!this.mutiSocketReceive.isClosed()) {
//                    	MyLog.i(TAG, "AdhocCommunication run  while");
                        if (this.mutiSocketReceive == null) {
                            return;
                        }
                        
                        this.recvBuffer = new byte[65535];
                        DatagramPacket localDatagramPacket = new DatagramPacket(this.recvBuffer,
                                this.recvBuffer.length);
                        this.mutiSocketReceive.receive(localDatagramPacket);
                        parsePackage(this.recvBuffer);
                    }
                } catch (Exception localException1) {
                	MyLog.e(TAG, "AdhocCommunication run Exception", localException1);
                    try {
                        if ((this.mutiSocketReceive != null) && (!this.mutiSocketReceive.isClosed())) {
                            this.mutiSocketReceive.close();
                        }
                        localException1.printStackTrace();
                        return;
                    } catch (Exception localException2) {
                        for (;;) {
                            localException2.printStackTrace();
                        }
                    }
                }
			}else if (NetworkUtil.network_type == NetworkUtil.WIFI) {
                try {
                	//modify by zyf lc is zubo,wifi is guangbo
//                    this.mutiSocketReceive = new MulticastSocket(5760);
//                    InetAddress group = InetAddress.getByName(MUTIBROADCAST_ADDRESS);
//                    mutiSocketReceive.joinGroup(group);
                  this.socketReceive = new DatagramSocket(5760);
                    while (!this.socketReceive.isClosed()) {
//                    	MyLog.i(TAG, "AdhocCommunication run  while");
                        if (this.socketReceive == null) {
                            return;
                        }
                        
                        this.recvBuffer = new byte[65535];
                        DatagramPacket localDatagramPacket = new DatagramPacket(this.recvBuffer,
                                this.recvBuffer.length);
                        this.socketReceive.receive(localDatagramPacket);
                        parsePackage(this.recvBuffer);
                    }
                } catch (Exception localException1) {
                	MyLog.e(TAG, "AdhocCommunication run Exception", localException1);
                    try {
                        if ((this.socketReceive != null) && (!this.socketReceive.isClosed())) {
                            this.socketReceive.close();
                        }
                        localException1.printStackTrace();
                        return;
                    } catch (Exception localException2) {
                        for (;;) {
                            localException2.printStackTrace();
                        }
                    }
                }
			}

        }

        private void parsePackage(byte[] paramArrayOfByte)
                throws OptionalDataException, ClassNotFoundException, IOException {
//        	MyLog.i(TAG, "parsePackage");
//          Person localSKSPerson = new Person(new String(paramArrayOfByte));
        	Person localSKSPerson = (Person) ByteUtils.ByteToObject(paramArrayOfByte);
            int i = localSKSPerson.cmdType;
//            Log.d("SKSPerson", "cmdType : " + i);
            switch (i) {
                case CMD_TYPE_LOGIN_UDP:
                    if (!localSKSPerson.ipAddress.equals(AdhocManager.me.ipAddress)) {
                        addPerson(localSKSPerson);
                        MyLog.i(TAG, "CMD_TYPE_LOGIN_UDP");
                        sendMessage(CMD_TYPE_HEART_UDP, localSKSPerson.ipAddress);
                        return;
                    }
                    localSKSPerson.heartbeatTime = System.currentTimeMillis();
                    return;
                case CMD_TYPE_HEART_UDP:
                	if (!localSKSPerson.ipAddress.equals(AdhocManager.me.ipAddress)) {
                		addPerson(localSKSPerson);
					}
                    return;
                default:
                    AdhocManager.this.childrenMap.remove(localSKSPerson.ipAddress);
                    AdhocManager.this.sendPersonHasChangedBroadcast(ACTION_OFFLINE);
                    BaseActivity.mApplication.removeOnlineUser(localSKSPerson.mobileNo, 1); 
                    return;
            }
        }
        
        public void joinOrganization() {
        	//modify by zyf lc is zubo,wifi is guangbo
        	if (NetworkUtil.network_type == NetworkUtil.ADHOC_NETWORK) {
        		sendMutiBroadCastMessage(CMD_TYPE_LOGIN_UDP, MUTIBROADCAST_ADDRESS);
			}else if (NetworkUtil.network_type == NetworkUtil.WIFI) {
	        	sendMessage(CMD_TYPE_LOGIN_UDP, BROADCAST_ADDRESS);
			}
        	

        }
        public void sendHeartbeatPack() {
        	//modify by zyf lc is zubo,wifi is guangbo
        	if (NetworkUtil.network_type == NetworkUtil.ADHOC_NETWORK) {
        		sendMutiBroadCastMessage(CMD_TYPE_HEART_UDP, MUTIBROADCAST_ADDRESS);
			}else if (NetworkUtil.network_type == NetworkUtil.WIFI) {
	        	sendMessage(CMD_TYPE_HEART_UDP, BROADCAST_ADDRESS);
			}
        }
        
        public void sendHeartbeatPackToOne(String dstIP){
        	sendMessage(CMD_TYPE_HEART_UDP, dstIP);
        }
        
        private void release() {
        	if (NetworkUtil.network_type == NetworkUtil.ADHOC_NETWORK) {
        		if (this.mutiSocketReceive != null) {
                    this.mutiSocketReceive.close();
                }
			}else if (NetworkUtil.network_type == NetworkUtil.WIFI) {
				if (this.socketReceive != null) {
                    this.socketReceive.close();
                }
			}
            
        }
        /**
         * 
         * @param paramInt      cmdType
         * @param paramString	BROADCAST_ADDRESS
         */
        public void sendMessage(int paramInt, String paramString) {
            try {
                DatagramSocket localDatagramSocket = new DatagramSocket();
                me.setCmdType(paramInt);
                me.setGroupIps(BaseActivity.mApplication.getOnlineGroupIPs());
                me.setmGroups(Receiver.engine(mContext).getMyGroups());
//              byte[] arrayOfByte = me.toString().getBytes();
                byte[] arrayOfByte = ByteUtils.ObjectToByte(me);
                InetAddress localInetAddress = InetAddress.getByName(paramString);
                localDatagramSocket.send(new DatagramPacket(arrayOfByte, arrayOfByte.length,
                        localInetAddress, 5760));//5760
                localDatagramSocket.close();
                return;
            } catch (Exception e) {
            	MyLog.e(TAG, "noticeOthers sendMessage " ,e);
            }
        }
        /**
         * 
         * @param paramInt      cmdType
         * @param paramString   MUTIBROADCAST_ADDRESS
         */
        public void sendMutiBroadCastMessage(int paramInt, String paramString) {
            try {
                MulticastSocket localDatagramSocket = new MulticastSocket(5760);
                InetAddress group = InetAddress.getByName(MUTIBROADCAST_ADDRESS);
                localDatagramSocket.joinGroup(group);
                localDatagramSocket.setTimeToLive(4);
                me.setCmdType(paramInt);
                me.setGroupIps(BaseActivity.mApplication.getOnlineGroupIPs());
                me.setmGroups(Receiver.engine(mContext).getMyGroups());
//              byte[] arrayOfByte = me.toString().getBytes();
                byte[] arrayOfByte = ByteUtils.ObjectToByte(me);
                InetAddress localInetAddress = InetAddress.getByName(paramString);
                localDatagramSocket.send(new DatagramPacket(arrayOfByte, arrayOfByte.length,
                        localInetAddress, 5760));//5760
                localDatagramSocket.close();
                return;
            } catch (IOException localIOException) {
                localIOException.printStackTrace();
            }
        }
        
    }
    
    private void addPerson(Person paramSKSPerson) {
        paramSKSPerson.heartbeatTime = System.currentTimeMillis();
        if ((AdhocManager.this.childrenMap.size() > 0)
                && (AdhocManager.this.childrenMap.containsKey(paramSKSPerson.ipAddress))) {
            ((Person) AdhocManager.this.childrenMap
                    .get(paramSKSPerson.ipAddress)).heartbeatTime = System.currentTimeMillis();
            return;
        }
        AdhocManager.this.childrenMap.put(paramSKSPerson.ipAddress, paramSKSPerson);
        AdhocManager.this.sendPersonHasChangedBroadcast(ACTION_ONLINE);
        Users user = new Users(paramSKSPerson.mobileNo, paramSKSPerson.personNickeName, paramSKSPerson.ipAddress);
        user.setmGroupIps(paramSKSPerson.getGroupIps());
        user.setmGroups(paramSKSPerson.getmGroups());
        ArrayList<String> groups = (ArrayList<String>) paramSKSPerson.getGroupIps();
        for (int i = 0; i < groups.size(); i++) {
        	MyLog.i("groupMem", "Login addPerson name:" + paramSKSPerson.personNickeName
            		+ "groupIp:" + groups.get(i));
		}
        BaseActivity.mApplication.addOnlineUser(paramSKSPerson.mobileNo, user);
        Receiver.engine(mContext).addUserToDB(user);
        MyLog.i(TAG, "AdhocManager addPerson");
    }
    
    private class noticeOthers extends Thread{

		@Override
		public void run() {
			super.run();
			try {
                for (;;) {
                    if (isStopUpdateMe) {
                        return;
                    }
                    try {
                    	Iterator iterator = childrenMap.entrySet().iterator();
                    	while (iterator.hasNext()) {
							Map.Entry<String, Person> entry = (Entry<String, Person>) iterator.next();
							String dstIP = entry.getKey();
							adhocCom.sendHeartbeatPackToOne(dstIP);
//							MyLog.i(TAG, "noticeOthers dstIP : " + dstIP);
						}
                        sleep(TIME_HEART_PERIOD);
                    } catch (Exception e) {
                        MyLog.e(TAG, "noticeOthers sendHeartbeatPackToOne " ,e);
                    }
                }
			} catch (Exception e) {
				MyLog.e(TAG, "noticeOthers run Exception " ,e);
			}
		}
    	
    }
    
    
    private class UpdateMe extends Thread {
        private UpdateMe() {
        }

        public void run() {
        	try {
//        		MyLog.i(TAG, "joinOrganization");
        		adhocCom.joinOrganization();
                for (;;) {
                    if (isStopUpdateMe) {
                        return;
                    }
                    try {
//                    	MyLog.i(TAG, "sendHeartbeatPack");
                        adhocCom.sendHeartbeatPack();
                        sleep(TIME_HEART_PERIOD);
                    } catch (Exception localException) {
                        localException.printStackTrace();
                    }
                }
			} catch (Exception e) {
				MyLog.e(TAG, "UpdateMe run Exception " ,e);
			}
            
        }
    }
    
    private void getMyInfomation() {
        String str1 = GlobalVar.displayname;
        String str2 = GlobalVar.number;
        updatePreference(str2);
        if ((str1 == null) || (str1.isEmpty()) || (str2 == null) || (str2.isEmpty())) {
//            Log.e(TAG, "nickName=" + str1 + "," + "mobileNo=" + str2);
        }
        long l = System.currentTimeMillis();
        if (me == null) {
              localIp = NetworkUtil.getLocalWifiIP(mContext);
//            Log.d(TAG, "AdhocManager - getMyInfomation()");
//            Log.d(TAG, "AdhocManager - localIp = " + this.localIp);
            me = new Person(this.localIp, str1, str2, l);
            me.heartbeatTime = 0L;
            sendPersonHasChangedBroadcast(ACTION_ONLINE);
            Users user = new Users(str2, str1, localIp);
            BaseActivity.mApplication.addOnlineUser(str2, user); 
            return;
        }
//        Log.d("", "logingTime= " + me.loginTime + "--currentTime=" + l);
    }
    
	private void updatePreference(String number){
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Receiver.mContext);
		Editor editor = preferences.edit();
		editor.putString(Settings.PREF_USERNAME,number);
		editor.putString(Settings.PREF_PASSWORD, number);
		editor.putString(Settings.PREF_PORT, 5060+"");     //default sip port is 5060
		editor.commit();
	}
    
    private void sendPersonHasChangedBroadcast(String paramString) {
        Intent localIntent = new Intent();
        localIntent.setAction(paramString);
        mContext.sendBroadcast(localIntent);
    }
    
    private List<NodeResource> parserContactsTree() {
        if (this.list != null) {
            this.list.clear();
        }
        this.childrenMap.size();
        this.list.add(new NodeResource(me.ipAddress, "我",
                "sip:" + me.mobileNo + "@" + me.ipAddress, me.mobileNo));
        Iterator localIterator = null;
        if (this.childrenMap.size() > 0) {
            localIterator = this.childrenMap.keySet().iterator();
        }
        if (null == localIterator) {
            return list;
        }
        for (;;) {
            if (!localIterator.hasNext()) {
                return this.list;
            }
            String str = (String) localIterator.next();
            Person localSKSPerson = (Person) this.childrenMap.get(str);
            NodeResource localNodeResource = new NodeResource(localSKSPerson.ipAddress,
                    localSKSPerson.personNickeName,
                    "sip:" + localSKSPerson.mobileNo + "@" + localSKSPerson.ipAddress,
                    localSKSPerson.mobileNo);
            this.list.add(localNodeResource);
        }
    }

    public List<NodeResource> getList() {
        return list;
    }

    private class CheckUserOnline extends Thread {
        private CheckUserOnline() {
        }
        @Override
        public void run() {
            super.run();
            if (isStopUpdateMe) {
                return;
            }
            try {
                for (;;) {
                    int i = childrenMap.size();
                    Iterator localIterator = null;
                    if (i > 0) {
                        localIterator = childrenMap.keySet().iterator();
                    }
                    if (null == localIterator) {
                        try {
                            sleep(5000L);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        continue;
                    }
                    for (;localIterator.hasNext();) {
                        String str = (String) localIterator.next();
                        long l = ((Person) AdhocManager.this.childrenMap.get(str)).heartbeatTime;
                        if ((l != 0L)
                                && (!((Person) AdhocManager.this.childrenMap.get(str)).ipAddress
                                        .equals(AdhocManager.me.ipAddress))
                                && (System.currentTimeMillis() - l > TIMEOUT_HEART_BEAT)) {
                        	BaseActivity.mApplication.removeOnlineUser(childrenMap.get(str).mobileNo, 1); 
                            AdhocManager.this.childrenMap.remove(str);
                            sendPersonHasChangedBroadcast(ACTION_OFFLINE);
                        }
                        
                    }
                    try {
                        sleep(1000L);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
			} catch (Exception e) {
				MyLog.e(TAG, "CheckUserOnline run Exception", e);
			}

            
        }
        
    }
    

    public void setShowText(TextView showText) {
        this.showText = showText;
    }
    
    public void showContacts (){
        if (showText == null) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(me.toString());
        
        this.childrenMap.size();
        Iterator localIterator = null;
        if (this.childrenMap.size() > 0) {
            localIterator = this.childrenMap.keySet().iterator();
        }
        if (null == localIterator) {
            showText.setText(builder);
            return;
        }
        
        for (;;) {
            if (!localIterator.hasNext()) {
                break;
            }
            String str = (String) localIterator.next();
            Person localSKSPerson = (Person) this.childrenMap.get(str);
            builder.append(localSKSPerson.toString());
        }
        
        showText.setText(builder);
        
        
    }
}

