package io.github.kambio.kambio;

import java.io.File;

import io.github.kambio.kambio.R;
import emetcode.crypto.bitshake.utils.file_funcs;
import emetcode.economics.passet.config;
import emetcode.economics.passet.paccount;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class get_key_activity extends Activity {
	public static final String LOGTAG = "get_key_activity";

	app_base app;
	TextView vw_key;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.get_key_layout);

		app = (app_base) getApplicationContext();
		if (!app.ca_has_root()) {
			app.ca_init_root();
		}

		if (app.ca_has_empty_root()) {
			TextView vw_info = (TextView) findViewById(R.id.password_info);
			vw_info.setText(R.string.you_have_no_accounts_type_a_new_password);
		}
		
		vw_key = (TextView) findViewById(R.id.password_field);
		vw_key.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_FLAG_MULTI_LINE
				| InputType.TYPE_TEXT_VARIATION_PASSWORD);
		
		//debug_set_img();
	}
	
	void debug_set_img(){
		//Uri uu = Uri.parse("android.resource://io.github.kambio.kambio/");
		ImageView img = (ImageView) findViewById(R.id.accept_button);
		//Uri uu = Uri.parse("file:///android_asset/" + misce.get_futbol_image_name(1));
		//img.setImageURI(uu);
		//img.setImageDrawable(misce.read_futbol_img(this, 1));
		//img.setIma
		/*
		key_owner owr1 = new key_owner("1234".getBytes(config.UTF_8));
		passet pss1 = new passet();
		pss1.set_base_dir(app.ca_get_root(), owr1);
		File img_ff = pss1.get_current_user_image_file();
		Uri uu = Uri.fromFile(img_ff);
		img.setImageURI(uu);
		*/
		//Uri uu = Uri.parse("android.resource://io.github.kambio.kambio/assets/" +
		//		misce.get_futbol_image_name(1));
		String pth = "android.resource://io.github.kambio.kambio/drawable/camera"; // OK;
		//String pth = "file:///android_asset/" + misce.get_futbol_image_name(1);
		//String pth = "file:///android_asset/fu.jpeg";
		Uri uu = Uri.parse(pth);
		Log.i(LOGTAG, "uri='" + pth + "'");
		img.setImageURI(uu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		try {
			switch (item.getItemId()) {
			case R.id.delete_all_menu_item:
				misce.show_message(this, R.string.delete_all_files_question,
						test_doer(), false);
				Log.i(LOGTAG, "delete_all_menu_item");
				break;
			case R.id.nav_files_menu_item:
				Log.i(LOGTAG, "nav_files_menu_item");
				start_nav_files();
				break;
			case R.id.set_pwd_base_file_menu_item:
				Log.i(LOGTAG, "set_pwd_base_file_menu_item");
				break;
			}
		} catch (Exception e) {
			Log.i(LOGTAG, e.toString());
		}
		return true;
	}

	private ok_doer test_doer(){
		ok_doer dd = new ok_doer(){
			public void on_ok_do(){
				delete_all();
			}
		};
		return dd;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.get_key_menu, menu);
		return true;
	}

	public void delete_all() {
		File rr_dir = app.ca_get_root();
		file_funcs.delete_dir(rr_dir);
		Log.i(LOGTAG, "deleted full dir=" + rr_dir);
	}

	public void clicked_accept_button(View v) {
		String txt_key = vw_key.getText().toString();
		if(txt_key.length() < app_base.MIN_KEY_LENGT){
			misce.show_message(this, R.string.key_too_short, null, false);
			return;
		}

		byte[] the_key = txt_key.getBytes(config.UTF_8);
		app.ca_init_l_owner(the_key);

		if (!paccount.is_user(app.ca_get_root(), app.ca_l_owner)) {
			if (app.ca_has_empty_root()) {
				Intent tt = new Intent(this, verify_key_activity.class);
				startActivity(tt);
				finish();
				return;
			}
			app.ca_l_owner = null;
			Log.d(LOGTAG, "NO user with given <key>. See passet_issuer.");
			misce.show_message(this, R.string.no_such_user, null, true);
			return;
		} else {
			Intent tt = new Intent(this, contacts_activity.class);
			startActivity(tt);
			finish();
		}
		finish();
	}

	public void start_nav_files() {
		Intent tt = new Intent(this, nav_files_activity.class);
		startActivity(tt);
	}
	
}
