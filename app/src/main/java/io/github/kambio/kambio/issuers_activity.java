package io.github.kambio.kambio;

import java.util.TreeSet;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.TextView;

import io.github.kambio.kambio.R;
import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.economics.passet.channel;
import emetcode.economics.passet.paccount;
import emetcode.economics.passet.trissuers;

public class issuers_activity extends Activity {
	public static final String LOGTAG = "issuers_activity";

	app_base app;

	ProgressDialog load_trusted_by_contacts_dialog;
	Activity the_act;
	ListView the_list;

	TextView the_trusted_bar;
	TextView the_text_param;

	boolean highlight_not_trusted_by_contacts;
	
	boolean menu_inited;
	MenuItem show_trusted_item;
	MenuItem show_not_trusted_item;
	MenuItem show_trusted_ck_item;
	MenuItem show_not_trusted_ck_item;
	MenuItem show_trusted_by_contacts_ck_item;
	MenuItem show_not_trusted_by_contacts_ck_item;
	MenuItem highlight__not_trusted_by_contacts_ck_item;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		app = (app_base) getApplicationContext();
		the_act = this;

		setContentView(R.layout.issuers_layout);

		int titleId = Resources.getSystem().getIdentifier("action_bar_title",
				"id", "android");
		the_trusted_bar = (TextView) findViewById(titleId);
		// yourTextView.setTextColor(colorId);

		// the_trusted_bar = findViewById(R.id.trusted_issuers_bar);
		the_text_param = (TextView) findViewById(R.id.text_param_value);
		the_list = (ListView) findViewById(R.id.issuers_list);

		the_list.setEmptyView(findViewById(R.id.empty));
		the_list.setOnItemClickListener(get_click_listener());
		the_list.setOnItemLongClickListener(get_long_click_listener());

		issuers_adapter the_adap = new issuers_adapter(this);

		the_list.setAdapter(the_adap);
		the_list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		the_list.setItemChecked(0, true);
		set_option(issuers_adapter.TRUSTED_DOMS);
		
		highlight_not_trusted_by_contacts = false;

		menu_inited = false;

		show_trusted_item = null;
		show_not_trusted_item = null;

		show_trusted_ck_item = null;
		show_not_trusted_ck_item = null;
		show_trusted_by_contacts_ck_item = null;
		show_not_trusted_by_contacts_ck_item = null;
		highlight__not_trusted_by_contacts_ck_item = null;

		Log.d(LOGTAG, "finished onCreate");
	}

	@Override
	protected void onResume() {
		super.onResume();

		Log.d(LOGTAG, "onResume");

		app_base app = (app_base) getApplicationContext();

		if (!app.ca_has_root()) {
			finish();
			return;
		}

		if (!app.ca_has_l_owner()) {
			finish();
			return;
		}

		if (!app.ca_has_currency()) {
			finish();
			return;
		}

		if (!app.ca_is_net_ok()) {
			finish();
			return;
		}

		if (!app.ca_has_net_addr()) {
			finish();
			return;
		}

		if (!app.ca_started_l_peer()) {
			finish();
			return;
		}

		if (!app.ca_started_net_opers()) {
			finish();
			return;
		}

		if (!app.ca_running_net_opers()) {
			finish();
			return;
		}

		Log.d(LOGTAG, "finished onResume");
	}

	public void load_trusted_by_contacts_task(final int kk) {
		the_act = this;
		if (!app.ca_has_passet()) {
			return;
		}
		if (!app.ca_has_l_owner()) {
			return;
		}
		if (!app.ca_has_all_chn()) {
			return;
		}
		if (app.ca_has_trusted_by_contacts()) {
			return;
		}

		new AsyncTask<Void, Void, Void>() {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				load_trusted_by_contacts_dialog = ProgressDialog.show(the_act,
						getString(R.string.please_wait),
						getString(R.string.updating_issuers), true);
			}

			@Override
			protected Void doInBackground(Void... params) {
				load_trusted_by_contacts();
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				load_trusted_by_contacts_dialog.dismiss();

				highlight__not_trusted_by_contacts_ck_item.setEnabled(true);
				
				issuers_adapter adpt = (issuers_adapter) the_list.getAdapter();

				adpt.trusted_by_contacts_doms = app.ca_all_trusted_by_contacs.trusted;
				adpt.not_trusted_by_contacts_doms = app.ca_all_trusted_by_contacs.not_trusted;
				adpt.not_trusted_by_contacts_set = new TreeSet<String>();
				adpt.not_trusted_by_contacts_set.addAll(adpt.not_trusted_by_contacts_doms);			

				adpt.set_kind(kk);
				adpt.notifyDataSetChanged();
			}
		}.execute();
	}

	void load_trusted_by_contacts() {
		if (!app.ca_has_passet()) {
			return;
		}
		if (!app.ca_has_l_owner()) {
			return;
		}
		if (!app.ca_has_all_chn()) {
			return;
		}
		if (app.ca_has_trusted_by_contacts()) {
			return;
		}

		paccount pss = app.ca_get_passet();
		key_owner owr = app.ca_l_owner;

		trissuers all_lst = null;
		all_lst = channel.get_trissuers(pss, app.ca_all_chn, owr);
		app.ca_all_trusted_by_contacs = all_lst;

		Log.d(LOGTAG, "finished load_trusted_by_contacts");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.issuers_menu, menu);
		show_trusted_item = menu.findItem(R.id.show_trusted_menu_item);
		show_not_trusted_item = menu.findItem(R.id.show_not_trusted_menu_item);

		show_trusted_ck_item = menu.findItem(R.id.show_trusted_ck_menu_item);
		show_not_trusted_ck_item = menu
				.findItem(R.id.show_not_trusted_ck_menu_item);
		show_trusted_by_contacts_ck_item = menu
				.findItem(R.id.show_trusted_by_contacts_ck_menu_item);
		show_not_trusted_by_contacts_ck_item = menu
				.findItem(R.id.show_not_trusted_by_contacts_ck_menu_item);
		highlight__not_trusted_by_contacts_ck_item = menu
				.findItem(R.id.highlight_not_trusted_by_contacts_menu_item);
		show_trusted_ck_item.setChecked(true);
		highlight__not_trusted_by_contacts_ck_item.setEnabled(false);
		
		if(app.ca_has_trusted_by_contacts()){
			highlight__not_trusted_by_contacts_ck_item.setEnabled(true);
		}
		
		menu_inited = true;
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		Intent tt = null;
		issuers_adapter adpt = null;
		try {
			switch (item.getItemId()) {
			case R.id.show_trusted_menu_item:
			case R.id.show_trusted_ck_menu_item:
				set_option(issuers_adapter.TRUSTED_DOMS);
				Log.i(LOGTAG, "show_trusted_ck_menu_item");
				break;
			case R.id.show_not_trusted_menu_item:
			case R.id.show_not_trusted_ck_menu_item:
				set_option(issuers_adapter.NOT_TRUSTED_DOMS);
				Log.i(LOGTAG, "show_not_trusted_ck_menu_item");
				break;
			case R.id.show_trusted_by_contacts_ck_menu_item:
				set_option(issuers_adapter.TRUSTED_BY_CONTACTS_DOMS);
				Log.i(LOGTAG, "show_trusted_by_contacs_ck_menu_item");
				break;
			case R.id.show_not_trusted_by_contacts_ck_menu_item:
				set_option(issuers_adapter.NOT_TRUSTED_BY_CONTACTS_DOMS);
				Log.i(LOGTAG, "show_not_trusted_by_contacts_ck_menu_item");
				break;
			case R.id.highlight_not_trusted_by_contacts_menu_item:
				highlight_not_trusted_by_contacts = ! highlight_not_trusted_by_contacts;
				if(highlight_not_trusted_by_contacts){
					highlight__not_trusted_by_contacts_ck_item.setChecked(true);
				} else {
					highlight__not_trusted_by_contacts_ck_item.setChecked(false);
				}
				adpt = (issuers_adapter) the_list.getAdapter();
				adpt.notifyDataSetChanged();
				Log.i(LOGTAG, "show_not_trusted_by_contacts_ck_menu_item");
				break;
			case R.id.delete_selected_menu_item:
				Log.i(LOGTAG, "delete_selected_menu_item");
				adpt = (issuers_adapter) the_list.getAdapter();
				adpt.delete_current();
				adpt.notifyDataSetChanged();
				break;
			case R.id.delete_all_menu_item:
				Log.i(LOGTAG, "delete_all_menu_item");
				misce.show_message(this, R.string.delete_all_issuers_question,
						ok_delete_all(), false);
				break;
			case R.id.nav_files_menu_item:
				Log.i(LOGTAG, "nav_files_menu_item");
				tt = new Intent(this, nav_files_activity.class);
				startActivity(tt);
				break;
			case R.id.config_menu_item:
				Log.i(LOGTAG, "config_menu_item");
				tt = new Intent(this, config_activity.class);
				startActivity(tt);
				break;
			}
		} catch (Exception e) {
			Log.i(LOGTAG, e.toString());
		}
		return true;
	}

	private ok_doer ok_delete_all() {
		ok_doer dd = new ok_doer() {
			public void on_ok_do() {
				issuers_adapter adpt = (issuers_adapter) the_list.getAdapter();
				adpt.delete_all();
				adpt.notifyDataSetChanged();
			}
		};
		return dd;
	}

	OnItemClickListener get_click_listener() {
		return new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.d(LOGTAG, "clicked pos=" + position);

				issuers_adapter adpt = (issuers_adapter) the_list.getAdapter();

				int old_pos = adpt.selected_pos;
				adpt.selected_pos = position;

				String sel_dom = adpt.get_sel_dom();
				if (sel_dom != null) {
					the_text_param.setText(sel_dom);
				} else {
					the_text_param.setText("");
				}

				adpt.notifyDataSetChanged();

				if (old_pos == position) {
				}
			}
		};
	}

	OnItemLongClickListener get_long_click_listener() {
		return new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.d(LOGTAG, "long_clicked pos=" + position);

				issuers_adapter adpt = (issuers_adapter) the_list.getAdapter();
				adpt.selected_pos = position;

				String sel_dom = adpt.get_sel_dom();
				if (sel_dom != null) {
					the_text_param.setText(sel_dom);
				} else {
					the_text_param.setText("");
				}

				adpt.notifyDataSetChanged();

				return true;
			}
		};
	}

	void set_option(int option) {
		switch (option) {
		case issuers_adapter.TRUSTED_DOMS:
			the_trusted_bar.setTextColor(Color.GREEN);
			setTitle(R.string.issuers_trusted);
			if (menu_inited) {
				show_trusted_item.setIcon(R.drawable.look_trusted_green_on_drw);
				show_not_trusted_item
						.setIcon(R.drawable.look_trusted_red_off_drw);
				show_trusted_ck_item.setChecked(true);
			}
			break;
		case issuers_adapter.NOT_TRUSTED_DOMS:
			the_trusted_bar.setTextColor(Color.RED);
			setTitle(R.string.issuers_not_trusted);
			if (menu_inited) {
				show_trusted_item
						.setIcon(R.drawable.look_trusted_green_off_drw);
				show_not_trusted_item
						.setIcon(R.drawable.look_trusted_red_on_drw);
				show_not_trusted_ck_item.setChecked(true);
			}
			break;
		case issuers_adapter.TRUSTED_BY_CONTACTS_DOMS:
			the_trusted_bar.setTextColor(Color.WHITE);
			setTitle(R.string.issuers_trusted_by_contacts);
			if (menu_inited) {
				show_trusted_item
						.setIcon(R.drawable.look_trusted_green_off_drw);
				show_not_trusted_item
						.setIcon(R.drawable.look_trusted_red_off_drw);
				show_trusted_by_contacts_ck_item.setChecked(true);
			}
			if (!app.ca_has_trusted_by_contacts()) {
				load_trusted_by_contacts_task(option);
				return;
			}
			break;
		case issuers_adapter.NOT_TRUSTED_BY_CONTACTS_DOMS:
			the_trusted_bar.setTextColor(Color.WHITE);
			setTitle(R.string.issuers_not_trusted_by_contacts);
			if (menu_inited) {
				show_trusted_item
						.setIcon(R.drawable.look_trusted_green_off_drw);
				show_not_trusted_item
						.setIcon(R.drawable.look_trusted_red_off_drw);
				show_not_trusted_by_contacts_ck_item.setChecked(true);
			}
			if (!app.ca_has_trusted_by_contacts()) {
				load_trusted_by_contacts_task(option);
				return;
			}
			break;
		}

		issuers_adapter adpt = (issuers_adapter) the_list.getAdapter();
		adpt.set_kind(option);
		adpt.notifyDataSetChanged();
	}

	public void clicked_set_issuer_pos_button(View vw) {
		Log.d(LOGTAG, "clicked_set_issuer_pos_button");

		String pp = the_text_param.getText().toString();

		int val = -1;
		try {
			val = Integer.parseInt(pp);
		} catch (NumberFormatException ee) {
		}
		if (val < 0) {
			String msg = "'" + pp + "'\n"
					+ getString(R.string.is_not_a_valid_position);
			misce.show_message(this, msg, null, false);
		}

		the_text_param.setText("");

		issuers_adapter adpt = (issuers_adapter) the_list.getAdapter();
		adpt.set_new_pos(val);
		adpt.notifyDataSetChanged();
	}

	public void clicked_add_issuer_button(View vw) {
		Log.d(LOGTAG, "clicked_add_issuer_button");

		String pp = the_text_param.getText().toString();
		the_text_param.setText("");

		if (!pp.isEmpty()) {
			issuers_adapter adpt = (issuers_adapter) the_list.getAdapter();
			adpt.add_issuer(pp);
			adpt.notifyDataSetChanged();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(LOGTAG, "onPause");
		if (app.ca_has_passet()) {
			issuers_adapter adpt = (issuers_adapter) the_list.getAdapter();
			paccount pss = app.ca_get_passet();
			
			trissuers t_grps = new trissuers();
			t_grps.trusted = adpt.trusted_doms;
			t_grps.not_trusted = adpt.not_trusted_doms;
			t_grps.update_trissuer_files(pss, app.ca_l_owner);
		}
	}
}
