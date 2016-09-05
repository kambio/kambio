package io.github.kambio.kambio;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import io.github.kambio.kambio.R;
import emetcode.crypto.bitshake.utils.file_funcs;
import emetcode.crypto.bitshake.utils.mem_file;
import emetcode.economics.passet.iso;
import emetcode.economics.passet.tag_person;
import emetcode.util.devel.bad_emetcode;

public class user_info_activity extends Activity {
	public static final String LOGTAG = "user_info_activity";
	public static final String PHOTO_SUF = ".photo";

	public static final int TAKE_PHOTO_CODE = 100;
	public static final int CHOOSE_PHOTO_CODE = 101;

	app_base app;
	ImageView the_pic;
	TextView country_val;
	File media_ff;
	File tmp_photo_ff;
	boolean comes_from_verif_kk;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		app = (app_base) getApplicationContext();

		comes_from_verif_kk = app.ca_was_in_actv(app_base.CA_VERIFY_KEY_ACTIVITY);
		
		setContentView(R.layout.user_info_layout);

		tmp_photo_ff = get_photo_tmp_file();
		if (tmp_photo_ff.exists()) {
			tmp_photo_ff.delete();
		}

		the_pic = (ImageView) findViewById(R.id.user_photo);
		country_val = (TextView) findViewById(R.id.user_country_code_value);
		media_ff = null;

		read_user_info();
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		try {
			switch (item.getItemId()) {
			case R.id.cancel_menu_item:
				Log.i(LOGTAG, "cancel_menu_item");
				finish();
				break;
			case R.id.accept_menu_item:
				Log.i(LOGTAG, "accept_menu_item");
				write_user_info();
				break;
			}
		} catch (Exception e) {
			Log.i(LOGTAG, e.toString());
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.user_info_menu, menu);
		return true;
	}

	private File get_photo_tmp_file() {
		String nm = "TEMP_PICTURE" + PHOTO_SUF;
		if (app.ca_has_l_owner()) {
			nm = app.ca_l_owner.get_mikid() + PHOTO_SUF;
		}
		File ph_ff = new File(app.ca_get_root(), nm);
		return ph_ff;
	}

	public void clicked_user_photo(View vw) {
		Intent signal = new Intent(Intent.ACTION_PICK,
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		if (is_intent_available(this, signal)) {
			startActivityForResult(signal, CHOOSE_PHOTO_CODE);
		} else {
			misce.show_message(this, R.string.cannot_use_option_at_this_time, null, false);
		}
	}

	public void clicked_user_take_photo(View vw) {
		Log.d(LOGTAG, "clicked_user_take_photo");
		the_pic.setImageResource(R.drawable.person_drw);

		media_ff = misce.get_new_media_file();
		if (media_ff == null) {
			Log.d(LOGTAG, "cannot get new media file");
			return;
		}
		Uri ff_uri = Uri.fromFile(media_ff);

		Intent signal = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (is_intent_available(this, signal)) {
			signal.putExtra(MediaStore.EXTRA_OUTPUT, ff_uri);
			startActivityForResult(signal, TAKE_PHOTO_CODE);
		}
	}

	public void clicked_accept_button(View vw) {
		Log.d(LOGTAG, "clicked_accepted_key");
		write_user_info();
	}

	public void clicked_user_country_code_button(View vw) {
		// Intent target = new Intent(this, get_key_activity.class);
		Intent target = new Intent(this, choose_country_activity.class);
		startActivity(target);
	}

	void copy_compressed_uri_to_local_file(Uri the_uri, File ff) {
		try {
			Bitmap bitmap = Media.getBitmap(getContentResolver(), the_uri);
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
			byte[] data = bytes.toByteArray();
			mem_file.concurrent_write_encrypted_bytes(ff, null, data);
		} catch (FileNotFoundException e) {
			Log.i(LOGTAG, e.toString());
		} catch (IOException e) {
			Log.i(LOGTAG, e.toString());
		} catch (bad_emetcode e) {
			Log.i(LOGTAG, e.toString());
		}
	}

	void copy_uri_to_local_file(Uri the_uri, File ff) {
		String pth = misce.get_uri_path(this, the_uri);
		File src = new File(pth);
		if (!src.exists()) {
			Log.d(LOGTAG, "CANNOT FIND SRC FILE=" + src);
		} else {
			if (ff.exists()) {
				ff.delete();
			}
		}
		file_funcs.concurrent_copy_file(src, ff);
	}

	@Override
	protected void onActivityResult(int my_code, int result_code, Intent data) {
		super.onActivityResult(my_code, result_code, data);

		if (my_code == CHOOSE_PHOTO_CODE) {
			if (result_code == RESULT_OK) {
				the_pic.setImageResource(R.drawable.person_drw);

				Uri selected_pic = data.getData();

				String pth = misce.get_uri_path(this, selected_pic);
				Log.d(LOGTAG, "IMAGE_REAL_PATH=" + pth);

				File dest = tmp_photo_ff;
				copy_compressed_uri_to_local_file(selected_pic, dest);

				if (dest.exists()) {
					Log.d(LOGTAG, "picture_file=" + dest);
					Uri dest_uri = Uri.fromFile(dest);
					the_pic.setImageURI(dest_uri);
				} else {
					Log.d(LOGTAG, "CANNOT FIND FILE=" + dest);
				}
			}
		}
		if (my_code == TAKE_PHOTO_CODE) {
			if (result_code == RESULT_OK) {
				Log.d(LOGTAG, "GOT NEW IMAGE");
			}
			if (media_ff.exists()) {
				Uri dest_uri = Uri.fromFile(media_ff);
				File dest = tmp_photo_ff;
				copy_compressed_uri_to_local_file(dest_uri, dest);
				the_pic.setImageURI(dest_uri);
				media_ff = null;
			}
		}
	}

	public static boolean is_intent_available(Context context, Intent intent) {
		final PackageManager packageManager = context.getPackageManager();
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		if (app.ca_was_in_actv(app_base.CA_CHOOSE_COUNTRY_CODE_ACTIVITY)) {
			int co_idx = app.ca_country_idx;
			country_val.setText(iso.get_country_code(co_idx));
		}
		Log.d(LOGTAG, "onRestart");
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(LOGTAG, "onStart");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(LOGTAG, "onResume");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(LOGTAG, "onStop");
		// The activity is no longer visible (it is now "stopped")
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(LOGTAG, "onDestroy");
		if ((tmp_photo_ff != null) && tmp_photo_ff.exists()) {
			tmp_photo_ff.delete();
		}
	}

	void read_user_info() {
		if (app.ca_has_passet()) {
			File img_ff = app.ca_get_passet().get_current_user_image_file();
			if (img_ff.exists()) {
				file_funcs.concurrent_copy_file(img_ff, tmp_photo_ff);
				Uri dest_uri = Uri.fromFile(tmp_photo_ff);
				the_pic.setImageURI(dest_uri);
			}

			TextView vw_val = null;
			tag_person per = app.ca_get_passet().curr_user;

			vw_val = (TextView) findViewById(R.id.user_id_value);
			vw_val.setText(per.legal_id);

			vw_val = (TextView) findViewById(R.id.user_country_code_value);
			vw_val.setText(per.contry_code);

			vw_val = (TextView) findViewById(R.id.user_name_value);
			vw_val.setText(per.legal_name);

			vw_val = (TextView) findViewById(R.id.user_phone_number_value);
			vw_val.setText(per.phone_number);

			vw_val = (TextView) findViewById(R.id.user_email_value);
			vw_val.setText(per.email);
		}
	}

	void write_user_info() {
		if (!tmp_photo_ff.exists()) {
			misce.show_message(this, R.string.user_image_is_obligatory, null, false);
			return;
		}
		app.ca_init_passet();
		if (app.ca_has_passet()) {
			File img_ff = app.ca_get_passet().get_current_user_image_file();
			file_funcs.concurrent_copy_file(tmp_photo_ff, img_ff);

			TextView vw_val = null;
			String val = null;
			tag_person per = app.ca_get_passet().curr_user;

			vw_val = (TextView) findViewById(R.id.user_id_value);
			val = vw_val.getText().toString();
			if (!val.isEmpty()) {
				per.legal_id = val;
			}

			vw_val = (TextView) findViewById(R.id.user_country_code_value);
			val = vw_val.getText().toString();
			if (iso.is_country_code(val)) {
				per.contry_code = val;
			}

			vw_val = (TextView) findViewById(R.id.user_name_value);
			val = vw_val.getText().toString();
			if (!val.isEmpty()) {
				per.legal_name = val;
			}

			vw_val = (TextView) findViewById(R.id.user_phone_number_value);
			val = vw_val.getText().toString();
			if (!val.isEmpty()) {
				per.phone_number = val;
			}

			vw_val = (TextView) findViewById(R.id.user_email_value);
			val = vw_val.getText().toString();
			if (!val.isEmpty()) {
				per.email = val;
			}

			app.ca_get_passet().write_current_user(app.ca_l_owner);
		}
		if (comes_from_verif_kk) {
			Intent target = new Intent(this, contacts_activity.class);
			startActivity(target);
		}
		finish();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(LOGTAG, "onPause");
		app.ca_in_actv(app_base.CA_USER_INFO_ACTIVITY);
	}
}
