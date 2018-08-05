package com.leadcore.sip.ui;

import org.sipdroid.sipua.BaseActivity;
import org.sipdroid.sipua.R;
import org.sipdroid.sipua.ui.Receiver;
import org.sipdroid.sipua.utils.ActivityCollectorUtils;

import android.os.Message;

public class TabItemActivity extends BaseActivity {

    protected Long exitTime = (long) 0;

    protected void init() {
    }

    @Override
    protected void initViews() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void initEvents() {
        // TODO Auto-generated method stub

    }

    @Override
    public void processMessage(Message msg) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onBackPressed() { // 返回桌面
        if (TabHomeActivity.getIsTabActive()) {
            System.out.println(System.currentTimeMillis() - exitTime);
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                showShortToast(R.string.confirm_exit);
                exitTime = System.currentTimeMillis();
            }
            else {
            	exit();
            }
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public void finish() {
        super.finish();
    }

}
