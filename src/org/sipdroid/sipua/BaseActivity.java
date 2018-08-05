package org.sipdroid.sipua;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sipdroid.sipua.BaseApplication;
import org.sipdroid.sipua.R;
import org.sipdroid.sipua.ui.Receiver;
import org.sipdroid.sipua.ui.RegisterService;
import org.sipdroid.sipua.ui.Sipdroid;
import org.sipdroid.sipua.utils.ActivityCollectorUtils;
import org.sipdroid.sipua.utils.MyLog;
import org.sipdroid.sipua.widget.FlippingLoadingDialog;
import org.sipdroid.sipua.widget.HandyTextView;





import com.leadcore.sip.ui.CustomDialog;
import com.leadcore.sip.ui.MoreSettingActivity;
import com.leadcore.sms.entity.Message;
import com.leadcore.sms.socket.tcp.TcpClient;
import com.leadcore.sms.socket.tcp.TcpFileClient;
import com.leadcore.sms.socket.tcp.TcpFileServer;
import com.leadcore.sms.socket.tcp.TcpService;
import com.leadcore.sms.socket.udp.IPMSGConst;
import com.leadcore.sms.socket.udp.OnActiveChatActivityListenner;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

public abstract class BaseActivity extends Activity {

    protected static final String GlobalSharedName = "LocalUserInfo"; // SharedPreferences文件名
    protected static OnActiveChatActivityListenner activeChatActivityListenner = null; // 激活的聊天窗口

    public static BaseApplication mApplication;
    protected Context mContext;
//    protected WifiUtils mWifiUtils;
    protected FlippingLoadingDialog mLoadingDialog;
//    protected UDPSocketThread mUDPSocketThread;

    private static int notificationMediaplayerID;
    private static SoundPool notificationMediaplayer;
    private static Vibrator notificationVibrator;
    protected List<AsyncTask<Void, Void, Boolean>> mAsyncTasks = new ArrayList<AsyncTask<Void, Void, Boolean>>();
    
    /**
     * 屏幕的宽度、高度、密度
     */
    protected int mScreenWidth;
    protected int mScreenHeight;
    protected float mDensity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication = BaseApplication.getInstance();
        mLoadingDialog = new FlippingLoadingDialog(this, "请求提交中");
        mContext = getApplicationContext();

        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        mScreenWidth = metric.widthPixels;
        mScreenHeight = metric.heightPixels;
        mDensity = metric.density;

        ActivityCollectorUtils.addActivity(this);

        if (notificationMediaplayer == null) {
            notificationMediaplayer = new SoundPool(3, AudioManager.STREAM_SYSTEM, 5);
            notificationMediaplayerID = notificationMediaplayer.load(this, R.raw.crystalring, 1);
        }
        if (notificationVibrator == null) {
            notificationVibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearAsyncTask();
        ActivityCollectorUtils.removeActivity(this);

    }
    
    @Override
    public void finish() {
        super.finish();
    }

    /** 初始化视图 **/
    protected abstract void initViews();

    /** 初始化事件 **/
    protected abstract void initEvents();

    protected void putAsyncTask(AsyncTask<Void, Void, Boolean> asyncTask) {
        mAsyncTasks.add(asyncTask.execute());
    }

    /** 清理异步处理事件 */
    protected void clearAsyncTask() {
        Iterator<AsyncTask<Void, Void, Boolean>> iterator = mAsyncTasks.iterator();
        while (iterator.hasNext()) {
            AsyncTask<Void, Void, Boolean> asyncTask = iterator.next();
            if (asyncTask != null && !asyncTask.isCancelled()) {
                asyncTask.cancel(true);
            }
        }
        mAsyncTasks.clear();
    }

    /** 添加listener到容器中 **/
    protected void changeActiveChatActivity(OnActiveChatActivityListenner paramListener) {
        activeChatActivityListenner = paramListener;
    }

    /** 从容器中移除相应listener **/
    protected void removeActiveChatActivity() {
        activeChatActivityListenner = null;
    }

    /**
     * 查询正在打开的聊天窗口的监听事件
     * 
     * @return
     */
    public static OnActiveChatActivityListenner getActiveChatActivityListenner() {
        return activeChatActivityListenner;
    }

    /**
     * 判断是否存在正在打开的聊天窗口
     * 
     * @return
     */
    public static boolean isExistActiveChatActivity() {
        return (activeChatActivityListenner == null) ? false : true;
    }  
    
    
    /**
     * 判断是否有已打开的聊天窗口来接收对应的数据。
     * 
     * @param paramMsg
     * @return
     */
    public static boolean isExistActiveActivity(Message msg) {
        if (!isExistActiveChatActivity()) {
            return false;
        }
        else {
            OnActiveChatActivityListenner listenner = getActiveChatActivityListenner();
            return listenner.isThisActivityMsg(msg);
        }
    }

    protected void showLoadingDialog(String text) {
        if (text != null) {
            mLoadingDialog.setText(text);
        }
        mLoadingDialog.show();
    }

    protected void dismissLoadingDialog() {
        if (mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    /** 短暂显示Toast提示(来自res) **/
    protected void showShortToast(int resId) {
        Toast.makeText(this, getString(resId), Toast.LENGTH_SHORT).show();
    }

    /** 短暂显示Toast提示(来自String) **/
    protected void showShortToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    /** 长时间显示Toast提示(来自res) **/
    protected void showLongToast(int resId) {
        Toast.makeText(this, getString(resId), Toast.LENGTH_LONG).show();
    }

    /** 长时间显示Toast提示(来自String) **/
    protected void showLongToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    /** 显示自定义Toast提示(来自res) **/
    protected void showCustomToast(int resId) {
        View toastRoot = LayoutInflater.from(BaseActivity.this)
                .inflate(R.layout.common_toast, null);
        ((HandyTextView) toastRoot.findViewById(R.id.toast_text)).setText(getString(resId));
        Toast toast = new Toast(BaseActivity.this);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(toastRoot);
        toast.show();
    }

    /** 显示自定义Toast提示(来自String) **/
    protected void showCustomToast(String text) {
        View toastRoot = LayoutInflater.from(BaseActivity.this)
                .inflate(R.layout.common_toast, null);
        ((HandyTextView) toastRoot.findViewById(R.id.toast_text)).setText(text);
        Toast toast = new Toast(BaseActivity.this);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(toastRoot);
        toast.show();
    }    

    /** 通过Class跳转界面 **/
    protected void startActivity(Class<?> cls) {
        startActivity(cls, null);
    }

    /** 含有Bundle通过Class跳转界面 **/
    protected void startActivity(Class<?> cls, Bundle bundle) {
        Intent intent = new Intent();
        intent.setClass(this, cls);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    /** 通过Action跳转界面 **/
    protected void startActivity(String action) {
        startActivity(action, null);
    }

    /** 含有Bundle通过Action跳转界面 **/
    protected void startActivity(String action, Bundle bundle) {
        Intent intent = new Intent();
        intent.setAction(action);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    /** 含有标题和内容的对话框 **/
    protected AlertDialog showAlertDialog(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).setTitle(title).setMessage(message)
                .show();
        return alertDialog;
    }

    /** 含有标题、内容、两个按钮的对话框 **/
    protected AlertDialog showAlertDialog(String title, String message, String positiveText,
            DialogInterface.OnClickListener onPositiveClickListener, String negativeText,
            DialogInterface.OnClickListener onNegativeClickListener) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).setTitle(title).setMessage(message)
                .setPositiveButton(positiveText, onPositiveClickListener)
                .setNegativeButton(negativeText, onNegativeClickListener).show();
        return alertDialog;
    }

    /** 含有标题、内容、图标、两个按钮的对话框 **/
    protected AlertDialog showAlertDialog(String title, String message, int icon,
            String positiveText, DialogInterface.OnClickListener onPositiveClickListener,
            String negativeText, DialogInterface.OnClickListener onNegativeClickListener) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).setTitle(title).setMessage(message)
                .setIcon(icon).setPositiveButton(positiveText, onPositiveClickListener)
                .setNegativeButton(negativeText, onNegativeClickListener).show();
        return alertDialog;
    }

    /**
     * 消息处理
     * 
     * <p>
     * 相关子类需要重写此函数，以完成各自的UI更新
     * 
     * @param msg
     *            接收到的消息对象
     */
    public abstract void processMessage(android.os.Message msg);

    /**
     * 新消息提醒 - 包括声音提醒、振动提醒
     */
    public static void playNotification() {
        if (BaseApplication.getSoundFlag()) {
            notificationMediaplayer.play(notificationMediaplayerID, 1, 1, 0, 0, 1);
        }
        if (BaseApplication.getVibrateFlag()) {
            notificationVibrator.vibrate(200);
        }

    }

    public static void sendEmptyMessage(int what) {
        handler.sendEmptyMessage(what);
    }

    public static void sendMessage(android.os.Message msg) {
        handler.sendMessage(msg);
    }

    private static Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
//            TabHomeActivity.sendEmptyMessage(); // 更新Tab信息
            if (ActivityCollectorUtils.getActivitiesNum() > 0)
                ActivityCollectorUtils.getLastActivity().processMessage(msg);
            if (msg.what != IPMSGConst.IPMSG_REFRESH ) {
            	playNotification(); // 新消息响提醒
			}
        }
    };
    
    public void exit(){
    	if (TcpClient.getInstance(mContext).hasFileSending()
    			|| TcpService.getInstance(mContext).hasFileReceving()) {
    		showFileDoingDialog();
		}else {
			exitApp();
		}
    }
    
    private void showFileDoingDialog(){
    	CustomDialog.Builder builder = new CustomDialog.Builder(this);
		builder.setMessage(R.string.dialog_message_confirm_exit);
		builder.setTitle(R.string.dialog_titlt_confirm_exit);
		builder.setPositiveButton(R.string.clear_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				exitApp();						
				dialog.dismiss();  
			}
		});

		builder.setNegativeButton(R.string.clear_cancel,
				new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();  
					}
				});

		builder.create().show();
    }
    
    private void exitApp(){
        putAsyncTask(new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                
                 showLoadingDialog(getString(R.string.dialog_exiting));

            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                	TcpFileServer.getInstance(mContext).release();
                	TcpFileClient.getInstance(mContext).release();
                	TcpService.getInstance(mContext).release();
                	TcpClient.getInstance(mContext).release();
                	Receiver.engine(mContext).logout(mContext);
            		Receiver.engine(mContext).halt();
            		Receiver.mSipdroidEngine = null;
//            		mContext.stopService(new Intent(mContext,RegisterService.class));
            		mApplication.clearParam();
                    return true;
                }
                catch (Exception e) {
                	MyLog.e("zhaoyifei", "exit " ,e);
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                dismissLoadingDialog();
                if (result) {
                    dismissLoadingDialog();
                    ActivityCollectorUtils.finishAllActivities(mApplication, mContext);
                }
                else {
                    showShortToast(R.string.dialog_toast_delect_failue);
                }
            }
        });
    }
//	long mExitTime;
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (keyCode == KeyEvent.KEYCODE_BACK) {
//			if (System.currentTimeMillis() - mExitTime > 2000) {
//				mExitTime = System.currentTimeMillis();
//				Toast.makeText(this, R.string.confirm_exit, Toast.LENGTH_SHORT).show();
//			}else {
//				finish();
//			}
//			return true;
//		}
//		return super.onKeyDown(keyCode, event);
//	}
    
}
