package com.leadcore.sms.socket.udp;

import android.R.integer;

public class IPMSGConst {
	public static final int VERSION = 0x001;		// 版本号
	public static final int PORT = 0x0979;			// 端口号，飞鸽协议默认端口2425
	public static final int GROUP_PORT = 10061;
	// 命令
	public static final int IPMSG_NOOPERATION		 = 0x00000000;	//不进行任何操作
	public static final int IPMSG_BR_ENTRY			 = 0x00000001;	//用户上线
	public static final int IPMSG_BR_EXIT		 	 = 0x00000002;	//用户退出
	public static final int IPMSG_ANSENTRY			 = 0x00000003;	//通报在线
	public static final int IPMSG_BR_ABSENCE		 = 0x00000004;	//改为缺席模式
	
	public static final int IPMSG_BR_ISGETLIST		 = 0x00000010;	//寻找有效的可以发送用户列表的成员
	public static final int IPMSG_OKGETLIST			 = 0x00000011;	//通知用户列表已经获得
	public static final int IPMSG_GETLIST			 = 0x00000012;	//用户列表发送请求
	public static final int IPMSG_ANSLIST			 = 0x00000013;	//应答用户列表发送请求
	
	public static final int IPMSG_SENDMSG 			 = 0x00000020;	//发送消息
	public static final int IPMSG_RECVMSG 			 = 0x00000021;	//通报收到消息
	public static final int IPMSG_READMSG 			 = 0x00000030;	//消息打开通知
	public static final int IPMSG_DELMSG 			 = 0x00000031;	//消息丢弃通知
	public static final int IPMSG_ANSREADMSG		 = 0x00000032;	//消息打开确认通知（version-8中添加）
	public static final int IPMSG_REFRESH 			 = 0x00000033;	//刷新界面
	public static final int IPMSG_SEND_FILE          = 0x00000034;  //发送文件
	public static final int IPMSG_RECEIVE_FILE       = 0x00000035;  //接收文件
	public static final int IPMSG_SENDMSG_TEST       = 0x00000036;
	
	public static final int IPMSG_GETINFO			 = 0x00000040;	//获得IPMSG版本信息
	public static final int IPMSG_SENDINFO			 = 0x00000041;	//发送IPMSG版本信息
	
	public static final int IPMSG_GETABSENCEINFO	 = 0x00000050;	//获得缺席信息
	public static final int IPMSG_SENDABSENCEINFO	 = 0x00000051;	//发送缺席信息

    public static final int IPMSG_UPDATE_FILEPROCESS = 0x00000060;  //更新文件传输进度
    public static final int IPMSG_SEND_FILE_SUCCESS  = 0x00000060;   //文件发送成功
    
	public static final int IPMSG_SEND_IMAGE_DATA    = 0x00000063;  //图片发送请求
	public static final int IPMSG_RECEIVE_IMAGE_DATA = 0x00000064;  //图片接收确认
	public static final int IPMSG_SEND_IMAGE_SUCCESS = 0x00000065;  //图片接收成功
	public static final int IPMSG_SEND_VOICE_DATA    = 0x00000066;  //录音发送请求
	public static final int IPMSG_RECIEVE_VOICE_DATA = 0x00000067;  //录音接收确认
	public static final int IPMSG_SEMD_VOICE_SUCCESS = 0x00000068;  //录音接收成功
	public static final int IPMSG_SEND_FILE_DATA     = 0x00000069;  //文件发送请求
	public static final int IPMSG_RECIEVE_FILE_DATA  = 0x00000070;  //文件接收确认
	public static final int IPMSG_GET_FILE_SUCCESS   = 0x00000071;  //文件接收成功
	
	public static final int IPMSG_GETPUBKEY			 = 0x00000072;	//获得RSA公钥
	public static final int IPMSG_ANSPUBKEY			 = 0x00000073;	//应答RSA公钥
	
	public static final int IPMSG_NEW_GROUP          = 0x00000080;  //创建组
	public static final int IPMSG_GROUP_SENDMSG      = 0x00000081;  //发送组消息
	public static final int IPMSG_GROUP_SEND_VOICE   = 0x00000082;
	public static final int IPMSG_GROUP_RECEIVE_VOICE =0x00000083;
	public static final int IPMSG_GROUP_SEND_IMAGE   = 0x00000084;
	public static final int IPMSG_GROUP_RECEIVE_IMAGE =0x00000085;
	public static final int IPMSG_GROUP_QUIT         = 0x00000086;
	public static final int IPMSG_GROUP_DISMISS      = 0x00000087;
	public static final int IPMSG_GROUP_DEL_MEM      = 0x00000088;
	public static final int IPMSG_GROUP_ADD_MEM      = 0x00000089;
	public static final int IPMSG_GROUP_LAUNCH_PTT   = 0x00000090;
	public static final int IPMSG_GROUP_DISMISS_PTT  = 0x00000091;
	
	/* option for all command */
	public static final int IPMSG_ABSENCEOPT 		 = 0x00000100;	//缺席模式
	public static final int IPMSG_SERVEROPT 		 = 0x00000200;	//服务器（保留）
	public static final int IPMSG_DIALUPOPT 		 = 0x00010000;	//发送给个人
	public static final int IPMSG_FILEATTACHOPT 	 = 0x00200000;	//附加文件
	public static final int IPMSG_ENCRYPTOPT		 = 0x00400000;	//加密

}
