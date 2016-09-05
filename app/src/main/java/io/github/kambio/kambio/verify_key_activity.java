
package io.github.kambio.kambio;

import java.util.Arrays;

import io.github.kambio.kambio.R;
import emetcode.economics.passet.config;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class verify_key_activity extends Activity {
	public static final String LOGTAG = "verify_key_activity";

	app_base app;
	
	TextView vw_key2;
	TextView vw_key3;

	TextView vw_info;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.verify_key_layout);

		app = (app_base) getApplicationContext();
		
		vw_info = (TextView) findViewById(R.id.info_text);
		
		vw_key2 = (TextView) findViewById(R.id.password_field2);
		vw_key2.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_FLAG_MULTI_LINE
				| InputType.TYPE_TEXT_VARIATION_PASSWORD);
		vw_key3 = (TextView) findViewById(R.id.password_field3);
		vw_key3.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_FLAG_MULTI_LINE
				| InputType.TYPE_TEXT_VARIATION_PASSWORD);
	}

	public void clicked_accept_button(View v) {
		byte[] kk1 = app.ca_l_owner.get_copy_of_secret_key();
		
		String txt_key2 = vw_key2.getText().toString();
		String txt_key3 = vw_key3.getText().toString();
		byte[] kk2 = txt_key2.getBytes(config.UTF_8);
		byte[] kk3 = txt_key3.getBytes(config.UTF_8);
		
		vw_key2.setText("");
		vw_key3.setText("");
		
		if(! Arrays.equals(kk2, kk3) || ! Arrays.equals(kk2, kk1)){
			vw_info.setText(R.string.password_verification_failed);
			return;
		}
		vw_info.setText(R.string.password_accepted_creating_account);
		
		Intent tt = new Intent(this, user_info_activity.class);
	    startActivity(tt);
	    
	    finish();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(LOGTAG, "onPause");
		app.ca_in_actv(app_base.CA_VERIFY_KEY_ACTIVITY);
	}	
}
