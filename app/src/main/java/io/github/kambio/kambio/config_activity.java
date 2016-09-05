package io.github.kambio.kambio;

import io.github.kambio.kambio.R;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class config_activity extends Activity {
	public static final String LOGTAG = "config_activity";

	app_base app;
	TextView tvw_contacts_text_sz;
	TextView tvw_contacts_img_height;
	TextView tvw_contacts_img_width;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		app = (app_base) getApplicationContext();

		setContentView(R.layout.config_layout);

		tvw_contacts_text_sz = (TextView) findViewById(R.id.contacts_text_sz_value);
		tvw_contacts_img_height = (TextView) findViewById(R.id.contacts_image_height_value);
		tvw_contacts_img_width = (TextView) findViewById(R.id.contacts_image_width_value);
		
		String vv1 = "" + app_base.CA_CONTACTS_TEXT_LIST_SZ;
		String vv2 = "" + app_base.CA_CONTACTS_IMAGE_LIST_HEIGHT;
		String vv3 = "" + app_base.CA_CONTACTS_IMAGE_LIST_WIDTH;

		tvw_contacts_text_sz.setText(vv1);
		tvw_contacts_img_height.setText(vv2);
		tvw_contacts_img_width.setText(vv3);
	}

	public void clicked_accept_button(View v) {
		Log.d(LOGTAG, "clicked_accept_button");
		long ii1 = app_base.CA_CONTACTS_TEXT_LIST_SZ;
		long ii2 = app_base.CA_CONTACTS_IMAGE_LIST_HEIGHT;
		long ii3 = app_base.CA_CONTACTS_IMAGE_LIST_WIDTH;
		long aa1 = ii1;
		long aa2 = ii2;
		long aa3 = ii3;
		try {
			String vv1 = tvw_contacts_text_sz.getText().toString();
			String vv2 = tvw_contacts_img_height.getText().toString();
			String vv3 = tvw_contacts_img_width.getText().toString();
			ii1 = Long.parseLong(vv1);
			ii2 = Long.parseLong(vv2);
			ii3 = Long.parseLong(vv3);
		} catch (Exception ee) {
			Log.i(LOGTAG, "Invalid number" + ee.toString());
		}
		
		if (ii1 < app_base.CA_MIN_TEXT_LIST_SZ) {
			ii1 = app_base.CA_MIN_TEXT_LIST_SZ;
		}
		if (ii2 < app_base.CA_MIN_IMAGE_LIST_HEIGHT) {
			ii2 = app_base.CA_MIN_IMAGE_LIST_HEIGHT;
		}
		if (ii3 < app_base.CA_MIN_IMAGE_LIST_WIDTH) {
			ii3 = app_base.CA_MIN_IMAGE_LIST_WIDTH;
		}
		if (ii1 > app_base.CA_MAX_TEXT_LIST_SZ) {
			ii1 = app_base.CA_MAX_TEXT_LIST_SZ;
		}
		if (ii2 > app_base.CA_MAX_IMAGE_LIST_HEIGHT) {
			ii2 = app_base.CA_MAX_IMAGE_LIST_HEIGHT;
		}
		if (ii3 > app_base.CA_MAX_IMAGE_LIST_WIDTH) {
			ii3 = app_base.CA_MAX_IMAGE_LIST_WIDTH;
		}

		boolean has_chged = false;
		if(aa1 != ii1){
			app_base.CA_CONTACTS_TEXT_LIST_SZ = (int)ii1;
			has_chged = true;
		}
		if(aa2 != ii2){
			app_base.CA_CONTACTS_IMAGE_LIST_HEIGHT = (int)ii2;
			has_chged = true;
		}
		if(aa3 != ii3){
			app_base.CA_CONTACTS_IMAGE_LIST_WIDTH = (int)ii3;
			has_chged = true;
		}
		if(has_chged){
			app.ca_refresh_contacts_view = true;
		}
		
		finish();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(LOGTAG, "onPause");
		app.ca_in_actv(app_base.CA_CONFIG_ACTIVITY);
	}
}
