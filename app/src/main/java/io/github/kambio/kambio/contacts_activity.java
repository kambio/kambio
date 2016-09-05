package io.github.kambio.kambio;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import io.github.kambio.kambio.R;
import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.economics.passet.channel;
import emetcode.economics.passet.config;
import emetcode.economics.passet.iso;
import emetcode.economics.passet.paccount;

public class contacts_activity extends Activity {
	public static final String LOGTAG = "contacts_activity";

	static final int[] all_top_img = { R.drawable.add_contact_drw };
	static final int[] all_top_text_resrce = { R.string.new_contact };

	app_base app;
	TextView info_bar;
	TextView filter_val;

	List<channel> sel_chn;

	ProgressDialog loading_contacts_dialog;
	Activity the_act;
	ListView the_list;

	ImageView curr_user_img;
	boolean curr_img_ok;

	MenuItem currency_menu_item;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		sel_chn = null;

		app = (app_base) getApplicationContext();

		setContentView(R.layout.contacts_layout);

		info_bar = (TextView) findViewById(R.id.infor_bar);
		filter_val = (TextView) findViewById(R.id.contact_filter_value);
		the_list = (ListView) findViewById(R.id.contacts_list);

		the_list.setEmptyView(findViewById(R.id.empty));
		the_list.setOnItemClickListener(get_click_listener());
		the_list.setOnItemLongClickListener(get_long_click_listener());

		curr_user_img = (ImageView) findViewById(R.id.current_user_button);
		curr_img_ok = false;

		currency_menu_item = null;
	}

	OnItemClickListener get_click_listener() {
		return new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.d(LOGTAG, "clicked pos=" + position);

				contacts_adapter adpt = (contacts_adapter) the_list
						.getAdapter();

				int old_pos = adpt.selected_pos;

				adpt.selected_pos = position;
				adpt.notifyDataSetChanged();

				set_current_contact(position);

				if (old_pos == position) {
					open_current_contact();
				}
			}
		};
	}

	OnItemLongClickListener get_long_click_listener() {
		return new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.d(LOGTAG, "long_clicked pos=" + position);

				contacts_adapter adpt = (contacts_adapter) the_list
						.getAdapter();
				adpt.selected_pos = position;
				adpt.notifyDataSetChanged();

				set_current_contact(position);
				open_current_contact();
				return true;
			}
		};
	}

	@Override
	protected void onResume() {
		super.onResume();

		Log.d(LOGTAG, "onResume");

		app_base app = (app_base) getApplicationContext();
		if (!app.ca_has_root()) {
			app.ca_init_root();
		}

		if (!app.ca_has_l_owner()) {
			Intent tt = new Intent(this, get_key_activity.class);
			startActivity(tt);
			finish();
			return;
		}
		app.ca_init_passet();

		if (app.ca_has_passet() && !curr_img_ok) {
			curr_img_ok = true;
			File ff = app.ca_get_passet().get_current_user_image_file();
			Uri uu = Uri.fromFile(ff);
			curr_user_img.setImageURI(uu);
		}

		if (!app.ca_has_chosen_currency()) {
			Intent tt = new Intent(this, choose_currency_activity.class);
			startActivity(tt);
			return;
		}

		if (!app.ca_has_currency() || app.ca_has_changed_currency()) {
			app.ca_init_currency();
			if (currency_menu_item != null) {
				int curr_idx = app.ca_get_passet().get_working_currency();
				currency_menu_item.setTitle(iso.get_currency_code(curr_idx));
			}
		}

		if (!app.ca_is_net_ok()) {
			misce.show_message(this, R.string.network_not_found, null, true);
			// finish();
			return;
		}

		app.ca_init_net_addr();
		if (!app.ca_has_net_addr()) {
			if (app.ca_all_ip.length > 0) {
				Intent tt = new Intent(this, choose_ip_activity.class);
				startActivity(tt);
			} else {
				misce.show_message(this, R.string.network_not_found, null, true);
				// finish();
			}
			return;
		}

		app.ca_init_l_peer();

		app.ca_start_l_peer();
		if (!app.ca_started_l_peer()) {
			misce.show_message(this, R.string.network_not_started, null, true);
			// finish();
			return;
		}

		info_bar.setText(app.ca_l_peer.get_description());

		if (!app.ca_started_net_opers()) {
			Log.i(LOGTAG, "Starting network and file threads.");
			app.ca_start_net_opers();
		}

		if (!app.ca_running_net_opers()) {
			Log.i(LOGTAG,
					"Network or file thread not running properly. Cashme sttopping.");
			misce.show_message(this, R.string.stopping_file_or_network_error,
					null, true);
			// finish();
			return;
		}

		if (!app.ca_has_all_chn()) {
			load_channels_task();
		}

		if (app.ca_has_new_contact()) {
			app.ca_add_new_contact();
			sel_chn = null;
		}

		if ((sel_chn == null) || app.ca_filter_contacts) {
			app.ca_filter_contacts = false;
			filter_channels();
			refresh_adapter();
		}

		if ((sel_chn != null) && app.ca_refresh_contacts_view) {
			app.ca_refresh_contacts_view = false;
			refresh_adapter();
		}

		Log.d(LOGTAG, "finished onResume");
	}

	public void load_channels_task() {
		the_act = this;
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				loading_contacts_dialog = ProgressDialog.show(the_act,
						getString(R.string.please_wait),
						getString(R.string.loading_contacts), true);
			}

			@Override
			protected Void doInBackground(Void... params) {
				load_channels();
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				loading_contacts_dialog.dismiss();
				filter_channels();
				refresh_adapter();
			}
		}.execute();
	}

	void load_channels() {
		if (app.ca_has_all_chn()) {
			return;
		}

		paccount pss = app.ca_get_passet();
		key_owner owr = app.ca_l_owner;
		int kk = channel.BY_NAME_FILTER;
		app.ca_all_chn = channel.read_all_channels(pss, owr, kk, "");
		app.ca_all_acc = channel.read_all_accounts(pss, owr);
		//app.ca_all_dbx = channel.read_all_own_dbox(pss, owr);

		Log.d(LOGTAG, "found " + app.ca_all_chn.size() + " channels");
	}

	void filter_channels() {
		if (!app.ca_has_all_chn()) {
			sel_chn = null;
			return;
		}
		// passet pss = app.ca_get_passet();
		// key_owner owr = app.ca_l_owner;
		String vv = filter_val.getText().toString();
		sel_chn = channel.filter_channels_by_name(app.ca_all_chn, vv);
	}

	void refresh_adapter() {
		contacts_adapter the_adap = new contacts_adapter(this,
				app_base.CA_CONTACTS_TEXT_LIST_SZ,
				app_base.CA_CONTACTS_IMAGE_LIST_HEIGHT,
				app_base.CA_CONTACTS_IMAGE_LIST_WIDTH);
		fill_adapter(the_adap);

		the_list.setAdapter(the_adap);
		the_list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		the_list.setItemChecked(0, true);

		if ((the_adap.all_text != null) && (the_adap.all_text.length > 0)) {
			the_adap.selected_pos = 0;
		}
	}

	void dbg_fill_adapter2(contacts_adapter adp) {
		int arr_sz = 200;

		adp.all_img_resrce = null;
		adp.all_img_files = null;
		adp.all_text = new String[arr_sz];
		for (int aa = 0; aa < arr_sz; aa++) {
			adp.all_text[aa] = "prueba_" + aa;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.contacts_menu, menu);
		currency_menu_item = menu.findItem(R.id.current_currency_menu_item);

		if (app.ca_has_passet() && app.ca_has_chosen_currency()) {
			int curr_idx = app.ca_get_passet().get_working_currency();
			currency_menu_item.setTitle(iso.get_currency_code(curr_idx));
		}
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		Intent tt = null;
		try {
			switch (item.getItemId()) {
			case R.id.change_issuers_menu_item:
			case R.id.issuers_menu_item:
				Log.i(LOGTAG, "issuers_menu_item");
				tt = new Intent(this, issuers_activity.class);
				startActivity(tt);
				break;
			case R.id.change_currency_menu_item:
			case R.id.current_currency_menu_item:
				Log.i(LOGTAG, "current_currency_menu_item");
				tt = new Intent(this, choose_currency_activity.class);
				startActivity(tt);
				break;
			case R.id.see_contact_info_menu_item:
				Log.i(LOGTAG, "see_contact_info_menu_item");
				open_current_contact();
				break;
			case R.id.user_profile_menu_item:
				Log.i(LOGTAG, "profile_menu_item");
				tt = new Intent(this, user_info_activity.class);
				startActivity(tt);
				break;
			case R.id.delete_contact_menu_item:
				Log.i(LOGTAG, "delete_contact_menu_item");
				delete_current_contact();
				break;
			case R.id.config_menu_item:
				Log.i(LOGTAG, "config_menu_item");
				tt = new Intent(this, config_activity.class);
				startActivity(tt);
				break;
			case R.id.nav_files_menu_item:
				Log.i(LOGTAG, "nav_files_menu_item");
				tt = new Intent(this, nav_files_activity.class);
				startActivity(tt);
				break;
			case R.id.create_dbg_channels_menu_item:
				Log.i(LOGTAG, "create_dbg_channels_menu_item");
				if (app.ca_has_passet()) {
					debug_funcs.create_all_dbg_channels(this,
							app.ca_get_passet(), app.ca_l_owner);
				}
				break;
			case R.id.create_dbg_passets_menu_item:
				if (app.ca_has_passet()) {
					paccount pss = app.ca_get_passet();
					int curr_idx = pss.get_working_currency();
					Log.i(LOGTAG, "create_dbg_passets_menu_item currency="
							+ iso.get_currency_code(curr_idx));
					debug_funcs.create_all_test_passets(pss, curr_idx,
							app.ca_l_owner);
				}
				break;
			}
		} catch (Exception e) {
			Log.i(LOGTAG, e.toString());
		}
		return true;
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.d(LOGTAG, "onRestart");
	}

	public void clicked_people_give_cash_button(View vw) {
		Log.d(LOGTAG, "give_cash_button");
		if (app.ca_contact_info == null) {
			contacts_adapter adpt = (contacts_adapter) the_list.getAdapter();
			if (adpt.selected_pos == 0) {
				Intent tt = new Intent(this, add_contact_activity.class);
				startActivity(tt);
			}
			return;
		}
		Intent tt = new Intent(this, cash_activity.class);
		startActivity(tt);
	}

	public void clicked_people_receive_cash_button(View vw) {
		Log.d(LOGTAG, "receive_cash_button");
	}

	public void clicked_people_add_person_button(View vw) {
		Log.d(LOGTAG, "add_person_button");
		Intent tt = new Intent(this, add_contact_activity.class);
		startActivity(tt);
	}

	public void clicked_current_user_button(View vw) {
		Log.d(LOGTAG, "clicked_current_user_button");
		Intent tt = new Intent(this, user_info_activity.class);
		startActivity(tt);
	}

	public void clicked_accept_button(View v) {
		Log.d(LOGTAG, "clicked_accept_button");
		filter_channels();
		refresh_adapter();
	}

	void set_current_contact(int idx) {
		int top_sz = all_top_img.length;
		if (idx < top_sz) {
			app.ca_contact_info = null;
		}

		idx -= top_sz;
		if ((sel_chn != null) && (idx >= 0) && (idx < sel_chn.size())) {
			app.ca_contact_info = sel_chn.get(idx);
		}

		idx -= sel_chn.size();
		List<channel> all_acc = app.ca_all_acc;
		if ((all_acc != null) && (idx >= 0) && (idx < all_acc.size())) {
			app.ca_contact_info = all_acc.get(idx);
		}
	}

	void open_current_contact() {
		if (app.ca_contact_info == null) {
			contacts_adapter adpt = (contacts_adapter) the_list.getAdapter();
			if (adpt.selected_pos == 0) {
				Intent tt = new Intent(this, add_contact_activity.class);
				startActivity(tt);
			}
			return;
		}

		Intent tt = new Intent(this, contact_info_activity.class);
		startActivity(tt);
	}

	void fill_adapter(contacts_adapter adp) {
		int top_sz = all_top_img.length;
		adp.all_img_resrce = all_top_img;
		adp.all_text_resrce = all_top_text_resrce;

		if ((sel_chn == null) || sel_chn.isEmpty()) {
			adp.all_img_files = null;
			adp.all_text = null;
			return;
		}

		List<channel> all_acc = app.ca_all_acc;
		//List<channel> all_dbx = app.ca_all_dbx;

		int arr_sz = top_sz + sel_chn.size() + all_acc.size();
		adp.all_kind_img_resrce = new int[arr_sz];
		adp.all_img_files = new File[arr_sz];
		adp.all_text = new String[arr_sz];

		for (int aa = 0; aa < top_sz; aa++) {
			adp.all_kind_img_resrce[aa] = -1;
			adp.all_img_files[aa] = null;
			adp.all_text[aa] = null;
		}

		int base_idx = top_sz;
		for (int aa = 0; aa < sel_chn.size(); aa++) {
			channel chn = sel_chn.get(aa);
			String nm = chn.trader.legal_name;

			String dom = chn.trader.network_domain_name;
			if ((dom != null) && (dom != config.UNKNOWN_STR)) {
				nm += "(" + chn.trader.network_domain_name + ")";
			}
			adp.all_kind_img_resrce[aa + base_idx] = R.drawable.contact_drw;

			adp.all_img_files[aa + base_idx] = chn.img_file;
			adp.all_text[aa + base_idx] = nm;

		}

		base_idx += sel_chn.size();
		for (int aa = 0; aa < all_acc.size(); aa++) {
			channel chn = all_acc.get(aa);
			adp.all_kind_img_resrce[aa + base_idx] = R.drawable.own_contact_drw;
			adp.all_img_files[aa + base_idx] = chn.img_file;
			adp.all_text[aa + base_idx] = chn.trader.legal_name;
		}
	}

	private void delete_current_contact() {
		if (app.ca_contact_info == null) {
			misce.show_message(this, R.string.no_selected_contact, null, false);
			return;
		}
		// channel del_chn = app.ca_contact_info;

	}
}
