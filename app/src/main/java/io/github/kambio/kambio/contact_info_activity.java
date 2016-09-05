package io.github.kambio.kambio;

import java.io.File;

import io.github.kambio.kambio.R;
import emetcode.economics.passet.channel;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class contact_info_activity extends Activity {
	public static final String LOGTAG = "see_contact_activity";

	app_base app;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		app = (app_base) getApplicationContext();

		setContentView(R.layout.contact_info_layout);
		
		ImageView ivw_im = (ImageView) findViewById(R.id.user_photo);
		TextView tvw_id = (TextView) findViewById(R.id.user_id_value);
		TextView tvw_cc = (TextView) findViewById(R.id.user_country_code_value);
		TextView tvw_nm = (TextView) findViewById(R.id.user_name_value);
		TextView tvw_ph = (TextView) findViewById(R.id.user_phone_number_value);
		TextView tvw_em = (TextView) findViewById(R.id.user_email_value);

		channel chn = app.ca_contact_info;
		
		File usu_img_ff = chn.img_file;
		if(usu_img_ff != null){ 
			Uri uu = Uri.fromFile(usu_img_ff);
			ivw_im.setImageURI(uu);
		}
		tvw_id.setText(chn.trader.legal_id);
		tvw_cc.setText(chn.trader.contry_code);
		tvw_nm.setText(chn.trader.legal_name);
		tvw_ph.setText(chn.trader.phone_number);
		tvw_em.setText(chn.trader.email);
	}

	public void clicked_accept_button(View v) {
		Log.d(LOGTAG, "clicked_accept_button");
		
		finish();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(LOGTAG, "onPause");
		app.ca_in_actv(app_base.CA_ADD_CONTACT_ACTIVITY);
	}
}
