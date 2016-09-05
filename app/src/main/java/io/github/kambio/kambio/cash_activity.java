package io.github.kambio.kambio;

import java.math.BigDecimal;

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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import io.github.kambio.kambio.R;
import io.github.kambio.kambio.cash_adapter.view_holder;
import emetcode.crypto.bitshake.utils.convert;
import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.economics.passet.channel;
import emetcode.economics.passet.deno_count;
import emetcode.economics.passet.deno_counter;
import emetcode.economics.passet.iso;
import emetcode.economics.passet.paccount;
import emetcode.economics.passet.tag_denomination;
import emetcode.net.netmix.nx_conn_id;

public class cash_activity extends Activity {
	public static final String LOGTAG = "cash_activity";

	app_base app;

	deno_counter loaded_counter;
	deno_counter cash_counter;

	ProgressDialog loading_cashnotes_dialog;
	Activity the_act;
	ListView the_list;

	ImageView user_img;
	TextView total_chosen_txt;

	boolean only_chosen;
	MenuItem chosen_m_item;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		loaded_counter = null;
		cash_counter = null;

		app = (app_base) getApplicationContext();

		setContentView(R.layout.cash_layout);

		the_list = (ListView) findViewById(R.id.cash_list);

		user_img = (ImageView) findViewById(R.id.cash_user_image);
		total_chosen_txt = (TextView) findViewById(R.id.cash_total_chosen);

		only_chosen = false;
		chosen_m_item = null;

		channel chn = app.ca_contact_info;
		if (chn.img_file != null) {
			Uri uu = Uri.fromFile(chn.img_file);
			user_img.setImageURI(uu);
		}
		String nm = chn.trader.legal_name;
		if (nm != null) {
			setTitle(nm);
		}

		the_list.setEmptyView(findViewById(R.id.empty));
		the_list.setOnItemClickListener(get_click_listener());
	}

	OnItemClickListener get_click_listener() {
		return new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				view_holder holder = (view_holder) view.getTag();

				Log.d(LOGTAG, "clicked pos=" + position + " id=" + id
						+ " holder.deno=" + holder.deno.getText().toString());

				cash_adapter adpt = (cash_adapter) the_list.getAdapter();

				adpt.selected_pos = position;
				adpt.notifyDataSetChanged();
			}
		};
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

		if (app.ca_has_changed_currency()) {
			app.ca_init_currency();
			loaded_counter = null;
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

		if (!app.ca_has_contact()) {
			misce.show_message(this, R.string.no_selected_contact, null, true);
			//finish();
			return;
		}

		if (!app.ca_has_remote_passet()) {
			nx_conn_id sele_coid = app.ca_contact_info.coid;
			app.ca_remote_pss = app.ca_get_passet().get_sub_paccount(
					sele_coid);
		}

		if (loaded_counter == null) {
			load_cashnotes_task();
		}

		Log.d(LOGTAG, "finished onResume");
	}

	public void load_cashnotes_task() {
		the_act = this;
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				loading_cashnotes_dialog = ProgressDialog.show(the_act,
						getString(R.string.please_wait),
						getString(R.string.finding_cashnotes), true);
			}

			@Override
			protected Void doInBackground(Void... params) {
				// load_cashnotes();
				dbg_init_counters();
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				loading_cashnotes_dialog.dismiss();
				reset_adapter();
			}
		}.execute();
	}

	void load_cashnotes() {
		if (!app.ca_has_passet()) {
			return;
		}
		if (!app.ca_has_remote_passet()) {
			return;
		}

		paccount pss = app.ca_get_passet();
		paccount remote_pss = app.ca_remote_pss;
		key_owner owr = app.ca_l_owner;

		pss.fill_all_deno_count(owr, remote_pss, 100);

		loaded_counter = deno_counter.copy_counter(pss.deno_cter);

		Log.d(LOGTAG, "finished load_cashnotes");
	}

	void reset_total() {
		if (app.ca_has_passet()) {
			int currcy_idx = app.ca_get_passet().get_working_currency();
			String tot_str = BigDecimal.ZERO.toPlainString()
					+ app_base.CHAR_SPC + iso.get_currency_code(currcy_idx);
			total_chosen_txt.setText(tot_str);
		}
	}

	void update_chosen_item(boolean is_zero) {
		if (chosen_m_item != null) {
			if (only_chosen) {
				chosen_m_item.setIcon(R.drawable.look_on_drw);
			} else {
				if (is_zero) {
					chosen_m_item.setIcon(R.drawable.look_dis_drw);
				} else {
					chosen_m_item.setIcon(R.drawable.look_off_drw);
				}
			}
		}
	}

	void switch_only_chosen() {
		only_chosen = !only_chosen;
		refresh_adapter();
	}

	void reset_adapter() {
		only_chosen = false;
		reset_total();

		if (loaded_counter == null) {
			cash_counter = null;
			refresh_adapter();
			return;
		}
		cash_counter = deno_counter.copy_counter(loaded_counter);
		refresh_adapter();
	}

	void refresh_adapter() {
		cash_adapter the_adap = new cash_adapter(this, cash_counter);
		if (only_chosen) {
			the_adap.update_chosen_denos();
		}

		the_list.setAdapter(the_adap);
		the_list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		the_list.setItemChecked(0, true);

		update_chosen_item(the_adap.is_sum_zero());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.cash_menu, menu);
		chosen_m_item = menu.findItem(R.id.filter_chosen_menu_item);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		Intent tt = null;
		try {
			switch (item.getItemId()) {
			case R.id.cancel_menu_item:
				Log.i(LOGTAG, "cancel_menu_item");
				reset_adapter();
				break;
			case R.id.filter_chosen_menu_item:
				switch_only_chosen();
				Log.i(LOGTAG, "filter_chosen_menu_item");
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
			case R.id.change_currency_menu_item:
				Log.i(LOGTAG, "change_currency_menu_item");
				tt = new Intent(this, choose_currency_activity.class);
				startActivity(tt);
				break;
			}
		} catch (Exception e) {
			Log.i(LOGTAG, e.toString());
		}
		return true;
	}

	public void clicked_cash_action_button(View vw) {
		Log.d(LOGTAG, "clicked_cash_action_button");
	}

	public void clicked_cash_wait_connect_button(View vw) {
		Log.d(LOGTAG, "receive_cash_button");
	}

	public void clicked_cash_connect_button(View vw) {
		Log.d(LOGTAG, "clicked_cash_connect_button");
		// Intent tt = new Intent(this, add_activity.class);
		// startActivity(tt);
	}

	public void clicked_cash_item_one_more_button(View vw) {
		View pnt = (View) vw.getParent();
		view_holder hld = (view_holder) pnt.getTag();
		Log.d(LOGTAG, "clicked_cash_item_one_more_button idx=" + hld.pos
				+ " deno=" + hld.deno.getText().toString());

		cash_adapter adpt = (cash_adapter) the_list.getAdapter();
		adpt.selected_pos = hld.pos;
		adpt.selected_action = cash_adapter.ONE_MORE_NOTE;

		adpt.notifyDataSetChanged();

	}

	public void clicked_cash_item_one_less_button(View vw) {
		View pnt = (View) vw.getParent();
		view_holder hld = (view_holder) pnt.getTag();
		Log.d(LOGTAG, "clicked_cash_item_one_less_button idx=" + hld.pos
				+ " deno=" + hld.deno.getText().toString());

		cash_adapter adpt = (cash_adapter) the_list.getAdapter();
		adpt.selected_pos = hld.pos;
		adpt.selected_action = cash_adapter.ONE_LESS_NOTE;

		adpt.notifyDataSetChanged();
	}

	void choose_cashnotes() {
		if (loaded_counter == null) {
			return;
		}
		if (cash_counter == null) {
			return;
		}
		if (!app.ca_has_passet()) {
			return;
		}
		if (!app.ca_has_remote_passet()) {
			return;
		}

		paccount pss = app.ca_get_passet();
		paccount remote_pss = app.ca_remote_pss;
		key_owner owr = app.ca_l_owner;

		int currcy_idx = cash_counter.currency_idx;
		tag_denomination curr_dd = tag_denomination.first_deno(currcy_idx);
		while (true) {
			deno_count deco_1 = loaded_counter.get_deno_count(curr_dd, false);
			deno_count deco_2 = cash_counter.get_deno_count(curr_dd, false);
			boolean has_1 = (deco_1 != null);
			boolean has_2 = (deco_2 != null);
			if (has_1 != has_2) {
				throw new bad_cashapp(2);
			}
			if (has_1 && has_2) {
				int num_1 = deco_1.num_chosen;
				int num_2 = deco_2.num_chosen;
				int diff_num = num_2 - num_1;
				pss.choose_num_passets(owr, remote_pss, curr_dd, diff_num);
			}

			if (curr_dd.is_last_deno()) {
				break;
			}
			curr_dd.inc_deno();
		}
	}

	void dbg_init_counters() {
		key_owner owr = app.ca_l_owner;

		int currcy_idx = app.ca_get_passet().get_working_currency();
		loaded_counter = new deno_counter(currcy_idx);
		cash_counter = new deno_counter(currcy_idx);

		tag_denomination curr_dd = tag_denomination.first_deno(currcy_idx);
		while (true) {
			deno_count deco_1 = loaded_counter.get_deno_count(curr_dd, true);
			deno_count deco_2 = cash_counter.get_deno_count(curr_dd, true);

			long vv1 = owr.new_random_long();
			int num_have = (int) convert.to_interval(vv1, 0, 100);
			long vv2 = owr.new_random_long();
			int num_can_give = (int) convert.to_interval(vv2, 0, num_have);

			deco_1.num_have = num_have;
			deco_2.num_have = num_have;
			deco_1.num_can_give = num_can_give;
			deco_2.num_can_give = num_can_give;

			if (curr_dd.is_last_deno()) {
				break;
			}
			curr_dd.inc_deno();
		}
	}

}
