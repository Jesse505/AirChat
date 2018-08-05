package com.leadcore.sip.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import org.sipdroid.codecs.Codecs;
import org.sipdroid.sipua.R;
import org.sipdroid.sipua.ui.Receiver;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.leadcore.sms.sql.SqlDBOperate;

/**
 * 
 * @author liushasha
 *
 */
public class MoreSettingActivity extends PreferenceActivity implements OnClickListener, OnSharedPreferenceChangeListener{

	
	private SqlDBOperate mDBOperate;
	private static SharedPreferences settings;
	TextView view;
	private Button back;
	public static final String CLEAR_ALL_CHATTING = "clear_all_chatting";
	public static final String CLEAR_ALL_CALLINFO = "clear_all_callinfo";
	public static final String PREF_EARGAIN = "eargain";
	public static final String PREF_MICGAIN = "micgain";
	public static final String PREF_HEARGAIN = "heargain";
	public static final String PREF_HMICGAIN = "hmicgain";
	private MoreSettingActivity context = null;
	public static final float	DEFAULT_EARGAIN = (float) 0.25;
	public static final float	DEFAULT_MICGAIN = (float) 0.25;
	public static final float	DEFAULT_HEARGAIN = (float) 0.25;
	public static final float	DEFAULT_HMICGAIN = (float) 1.0;
	private final String sharedPrefsFile = "org.sipdroid.sipua_preferences";
	private final static String profilePath = "/sdcard/Sipdroid/";	
	// IDs of the menu items
	private static final int MENU_IMPORT = 0;
	private static final int MENU_DELETE = 1;
	private static final int MENU_EXPORT = 2;
	private String[] profileFiles = null;
	private final String sharedPrefsPath = "/data/data/org.sipdroid.sipua/shared_prefs/";
	
	private int profileToDelete;
	 public static String[] getProfileList() {
    	File dir = new File(profilePath);
    	return dir.list();
    }
    
        @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	    menu.add(0, MENU_IMPORT, 0, getString(R.string.settings_profile_menu_import)).setIcon(android.R.drawable.ic_menu_upload);
	    menu.add(0, MENU_EXPORT, 0, getString(R.string.settings_profile_menu_export)).setIcon(android.R.drawable.ic_menu_save);
	    menu.add(0, MENU_DELETE, 0, getString(R.string.settings_profile_menu_delete)).setIcon(android.R.drawable.ic_menu_delete);
        return true;
    }
    
     public void copyFile(File in, File out) throws Exception {
        FileInputStream  fis = new FileInputStream(in);
        FileOutputStream fos = new FileOutputStream(out);
        try {
            byte[] buf = new byte[1024];
            int i = 0;
            while ((i = fis.read(buf)) != -1) {
                fos.write(buf, 0, i);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (fis != null) fis.close();
            if (fos != null) fos.close();
        }
    }
    
    
     private void exportSettings() {
		
	        try {
	        	// Create the directory for the profiles
	        	new File(profilePath).mkdirs();
	
	        	// Copy shared preference file on the SD card
	        	copyFile(new File(sharedPrefsPath + sharedPrefsFile + ".xml"), new File(profilePath + getProfileNameString()));
	        } catch (Exception e) {
	            Toast.makeText(this, getString(R.string.settings_profile_export_error), Toast.LENGTH_SHORT).show();
	        }
    }
    
    private String getProfileNameString() {
    	return null;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
    	context = this;

    	switch (item.getItemId()) {
            case MENU_IMPORT:
            	// Get the content of the directory
            	profileFiles = getProfileList();
            	if (profileFiles != null && profileFiles.length > 0) {
	            	// Show dialog with the files
	    			new AlertDialog.Builder(this)
	    			.setTitle(getString(R.string.settings_profile_dialog_profiles_title))
	    			.setIcon(android.R.drawable.ic_menu_upload)
	    			.setItems(profileFiles, profileOnClick)
	    			.show();
            	} else {
	                Toast.makeText(this, "No profile found.", Toast.LENGTH_SHORT).show();
            	}
                return true;
                
            case MENU_EXPORT:
            	exportSettings();
            	break;

            case MENU_DELETE:
            	// Get the content of the directory
            	profileFiles = getProfileList();
            	new AlertDialog.Builder(this)
                .setTitle(getString(R.string.settings_profile_dialog_delete_title))
                .setIcon(android.R.drawable.ic_menu_delete)
    			.setItems(profileFiles, new DialogInterface.OnClickListener() {
    				// Ask the user to be sure to delete it
    				public void onClick(DialogInterface dialog, int whichItem) {
        				profileToDelete = whichItem;
    					new AlertDialog.Builder(context)
    	                .setIcon(android.R.drawable.ic_dialog_alert)
    	                .setTitle(getString(R.string.settings_profile_dialog_delete_title))
    	                .setMessage(getString(R.string.settings_profile_dialog_delete_text, profileFiles[whichItem]))
    	                .setPositiveButton(android.R.string.ok, deleteOkButtonClick)
    	                .setNegativeButton(android.R.string.cancel, null)
    	                .show();
    				}
    			})
                .show();
                return true;
        }

        return false;
    }
	
	public static float getEarGain() {
		try {
			return Float.valueOf(PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getString(Receiver.headset > 0 ? PREF_HEARGAIN : PREF_EARGAIN, "" + DEFAULT_EARGAIN));
		} catch (NumberFormatException i) {
			return DEFAULT_EARGAIN;
		}			
	}
	public void updateSummaries(){
	
		fill(PREF_EARGAIN,  "" + DEFAULT_EARGAIN,  R.array.eargain_values, R.array.eargain_display_values);
    	fill(PREF_MICGAIN,  "" + DEFAULT_MICGAIN,  R.array.eargain_values, R.array.eargain_display_values);
    	fill(PREF_HEARGAIN, "" + DEFAULT_HEARGAIN, R.array.eargain_values, R.array.eargain_display_values);
    	fill(PREF_HMICGAIN, "" + DEFAULT_HMICGAIN, R.array.eargain_values, R.array.eargain_display_values);
	
	}
	
	int updateSleepPolicy() {
        ContentResolver cr = getContentResolver();
		int get = android.provider.Settings.System.getInt(cr, android.provider.Settings.System.WIFI_SLEEP_POLICY, -1);
		int set = get;
		boolean wlan = false,g3 = true,valid = false;
		return set;
    }
	
	void updateSleep() {
        ContentResolver cr = getContentResolver();
		int get = android.provider.Settings.System.getInt(cr, android.provider.Settings.System.WIFI_SLEEP_POLICY, -1);
		int set = updateSleepPolicy();

		if (set != get) {
			Toast.makeText(this, set == android.provider.Settings.System.WIFI_SLEEP_POLICY_DEFAULT?
					R.string.settings_policy_default:R.string.settings_policy_never, Toast.LENGTH_LONG).show();
			android.provider.Settings.System.putInt(cr, android.provider.Settings.System.WIFI_SLEEP_POLICY, set);
		}
	}
	
	private void setDefaultValues() {
		 settings = getSharedPreferences(sharedPrefsFile, MODE_PRIVATE);
		 settings.registerOnSharedPreferenceChangeListener(this);
		 updateSummaries();		
		 Codecs.check();		
	}
	
	public static float getMicGain() {
		if (Receiver.headset > 0 || Receiver.bluetooth > 0) {
			try {
				return Float.valueOf(PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getString(PREF_HMICGAIN, "" + DEFAULT_HMICGAIN));
			} catch (NumberFormatException i) {
				return DEFAULT_HMICGAIN;
			}			
		}

		try {
			return Float.valueOf(PreferenceManager.getDefaultSharedPreferences(Receiver.mContext).getString(PREF_MICGAIN, "" + DEFAULT_MICGAIN));
		} catch (NumberFormatException i) {
			return DEFAULT_MICGAIN;
		}			
	}
	
	
	void fill(String pref,String def,int val,int disp) {
    	for (int i = 0; i < getResources().getStringArray(val).length; i++) {
        	if (settings.getString(pref, def).equals(getResources().getStringArray(val)[i])) {
        		getPreferenceScreen().findPreference(pref).setSummary(getResources().getStringArray(disp)[i]);
        	}
    	}
    }
	
	void reload() {
		setPreferenceScreen(null);
		addPreferencesFromResource(R.xml.more_preferences);		
	}
	
	private android.content.DialogInterface.OnClickListener profileOnClick = new DialogInterface.OnClickListener() {
	public void onClick(DialogInterface dialog, int whichItem) {
			int set = updateSleepPolicy();
			
			settings.unregisterOnSharedPreferenceChangeListener(context);
			setDefaultValues();
	
	       	// Restart the engine
	   		Receiver.engine(context).halt();
			Receiver.engine(context).StartEngine();
			
			reload();
			settings.registerOnSharedPreferenceChangeListener(context);
			updateSummaries();
			if (set != updateSleepPolicy())
				updateSleep();
		
		}
	};

	private android.content.DialogInterface.OnClickListener deleteOkButtonClick = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
        	File profile = new File(profilePath + profileFiles[profileToDelete]);
        	boolean rv = false;
        	// Check if the file exists and try to delete it
        	if (profile.exists()) {
        		rv = profile.delete();
        	}
        	if (rv) {
        		Toast.makeText(context, getString(R.string.settings_profile_delete_confirmation), Toast.LENGTH_SHORT).show();
        	} else {
        		Toast.makeText(context, getString(R.string.settings_profile_delete_error), Toast.LENGTH_SHORT).show();
        	}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		final boolean isCustom =requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.more_preferences);
		
		mDBOperate=new SqlDBOperate(this);
		PreferenceScreen ps = (PreferenceScreen) getPreferenceScreen().findPreference(CLEAR_ALL_CHATTING);
		ps.setOnPreferenceClickListener(new OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference preference) {
				// TODO Auto-generated method stub
				CustomDialog.Builder builder = new CustomDialog.Builder(MoreSettingActivity.this);
				builder.setMessage(R.string.alert_clear);
				builder.setTitle(R.string.clear_all_chatting);
				builder.setPositiveButton(R.string.clear_ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						mDBOperate.deteleAllGroupChattingInfo();//add by lss for delete all group message 
						mDBOperate.deteleAllChatMessageInfo();
						mDBOperate.deteleAllChattingInfo();
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
				return false;
			}
			
		});
		//add by lss for clear all call info
		PreferenceScreen clearAllCall = (PreferenceScreen) getPreferenceScreen().findPreference(CLEAR_ALL_CALLINFO);
		clearAllCall.setOnPreferenceClickListener(new OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference preference) {
				// TODO Auto-generated method stub
				CustomDialog.Builder builder = new CustomDialog.Builder(MoreSettingActivity.this);
				builder.setMessage(R.string.alert_clear_call);
				builder.setTitle(R.string.clear_all_call);
				builder.setPositiveButton(R.string.clear_ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
												
						mDBOperate.deteleAllCallInfo();
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
				return false;
			}
			
		});
		//add end
		
		if(isCustom){
				getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.include_tab_more_tobar);
			}
			
		view = (TextView) findViewById(R.id.txt_more);
		view.setText(getResources().getString(R.string.setting ));
		back=(Button)findViewById(R.id.btn_back);
		
		//mDBOperate=new SqlDBOperate(this);
		back.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
				
			}
			
		});
		
		setDefaultValues();
	
	}
	

	@Override
	protected void onDestroy() {
		super.onDestroy();	
		settings.unregisterOnSharedPreferenceChangeListener(this);	
	}
	
	@Override
	public void finish()
	{
		if(null!=mDBOperate){
			mDBOperate.close();
			mDBOperate=null;	
		}
		super.finish();
	}
	@Override 
	protected void onResume(){
		//view.setGravity(Gravity.CENTER);
		super.onResume();
	}
	EditText transferText;
	String mKey;
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		Editor edit = settings.edit();
 		edit.putString(mKey, transferText.getText().toString());
		edit.commit();
	}
	
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// TODO Auto-generated method stub
		if (!Thread.currentThread().getName().equals("main"))
    		return;
		updateSummaries();
	}

}
