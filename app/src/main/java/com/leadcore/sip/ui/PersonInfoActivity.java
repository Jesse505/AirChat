package com.leadcore.sip.ui;

import org.sipdroid.sipua.BaseActivity;
import org.sipdroid.sipua.R;
import org.sipdroid.sipua.ui.Receiver;
import com.leadcore.sms.entity.Users;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class PersonInfoActivity extends BaseActivity implements OnClickListener {

	private Button btn_call;
	private Button btn_sms;
	private Button btn_facetime;
	private TextView txtPersonName;//add by lss for modifying the UI of PersonInfoActivity
	
	private String name;
	private String number;
	private String ip;
	private Users mPeople;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initViews();
		initEvents();
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_call:
			
			Receiver.engine(mContext).call(number,ip,"audio");
			break;
		case R.id.btn_sms:
	        goChatActivity();
			break;
		case R.id.btn_facetime:
			Receiver.engine(mContext).call(number,ip,"video");
			break;
		default:
			break;
		}
	}

	@Override
	protected void initViews() {
//		mPeople = getIntent().getParcelableExtra(Users.ENTITY_PEOPLE);
		mPeople = (Users) getIntent().getSerializableExtra(Users.ENTITY_PEOPLE);
		name = mPeople.getNickname();
		number = mPeople.getIMEI();
		ip = mPeople.getIpaddress();
		setContentView(R.layout.activity_person_info);
		btn_call = (Button) findViewById(R.id.btn_call);
		btn_sms = (Button) findViewById(R.id.btn_sms);
		btn_facetime = (Button) findViewById(R.id.btn_facetime);
		//add by lss for modifying the UI of PersonInfoActivity
		txtPersonName=(TextView)findViewById(R.id.txt_person_name);
		txtPersonName.setText(name); 
		//add end
	}

	@Override
	protected void initEvents() {
		btn_call.setOnClickListener(this);
		btn_sms.setOnClickListener(this);
		btn_facetime.setOnClickListener(this);
	}

	@Override
	public void processMessage(android.os.Message msg) {
		// TODO Auto-generated method stub
		
	}

	private void goChatActivity(){

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(Users.ENTITY_PEOPLE, mPeople);
        startActivity(intent);
	}

	
}
