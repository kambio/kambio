package io.github.kambio.kambio;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import io.github.kambio.kambio.R;
import emetcode.economics.netpasser.trans_operator;
import emetcode.economics.netpasser.transaction;
import emetcode.economics.passet.config;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class add_contact_activity extends Activity {
	public static final String LOGTAG = "add_contact_activity";

	app_base app;
	Activity the_act;
	TextView tvw_ip;
	TextView tvw_key;

	ProgressDialog creating_contact_dialog;

	boolean create_dropbox;
	MenuItem add_dropbox_1_menu_item;
	MenuItem add_dropbox_2_menu_item;

	boolean with_secret_key;
	MenuItem with_key_menu_item;

	trans_operator user_oper;
	BlockingQueue<transaction> 	from_net_oper;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		app = (app_base) getApplicationContext();
		the_act = this;

		creating_contact_dialog = null;

		create_dropbox = false;
		add_dropbox_1_menu_item = null;
		add_dropbox_2_menu_item = null;

		with_secret_key = false;
		with_key_menu_item = null;

		user_oper = null;
		from_net_oper = new LinkedBlockingQueue<transaction>(1);
		
		app.ca_added_contact_coid = null;
				
		set_layout();
	}

	void set_layout() {
		if (with_secret_key) {
			setContentView(R.layout.add_contact_with_key_layout);
		} else {
			setContentView(R.layout.add_contact_layout);
		}

		TextView tvw_own_ip = (TextView) findViewById(R.id.cash_address_of_this_device_value);
		String own_ip = app.ca_l_peer.get_description();
		tvw_own_ip.setText(own_ip);

		tvw_ip = (TextView) findViewById(R.id.cash_address_to_connect_to_value);
		tvw_key = null;
		if (with_secret_key) {
			tvw_key = (TextView) findViewById(R.id.add_contact_first_key_value);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.add_contact_menu, menu);

		add_dropbox_1_menu_item = menu.findItem(R.id.add_dropbox_menu_item);
		add_dropbox_2_menu_item = menu.findItem(R.id.add_drop_box_ck_menu_item);
		create_dropbox = add_dropbox_2_menu_item.isChecked();

		with_key_menu_item = menu.findItem(R.id.with_key_ck_menu_item);
		with_secret_key = with_key_menu_item.isChecked();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent tt = null;
		try {
			switch (item.getItemId()) {
			case R.id.add_dropbox_menu_item:
				Log.i(LOGTAG, "add_dropbox_menu_item");
			case R.id.add_drop_box_ck_menu_item:
				Log.i(LOGTAG, "add_drop_box_ck_menu_item");
				create_dropbox = !create_dropbox;
				if (create_dropbox) {
					add_dropbox_1_menu_item
							.setIcon(R.drawable.create_dropbox_on_drw);
					add_dropbox_2_menu_item.setChecked(true);
				} else {
					add_dropbox_1_menu_item
							.setIcon(R.drawable.create_dropbox_off_drw);
					add_dropbox_2_menu_item.setChecked(false);
				}
				break;
			case R.id.with_key_ck_menu_item:
				with_secret_key = !with_secret_key;
				if (with_secret_key) {
					with_key_menu_item.setChecked(true);
				} else {
					with_key_menu_item.setChecked(false);
				}
				set_layout();
				Log.i(LOGTAG, "with_key_ck_menu_item");
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
			default:
				return super.onOptionsItemSelected(item);
			}
		} catch (Exception e) {
			Log.i(LOGTAG, e.toString());
		}
		return true;
	}

	public void clicked_accept_button(View v) {
		Log.d(LOGTAG, "clicked_accept_button");

		String cash_addr = tvw_ip.getText().toString();
		String ip_addr = null;
		String box_nm = null;
		ip_addr = cash_addr;
		String fst_kk = null;
		if (with_secret_key) {
			fst_kk = tvw_key.getText().toString();
		}

		do_create_contact_task(ip_addr, box_nm, fst_kk);

		// finish();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(LOGTAG, "onPause");
		app.ca_in_actv(app_base.CA_ADD_CONTACT_ACTIVITY);
	}

	public void do_create_contact_task(final String ip_addr,
			final String box_nm, final String fst_kk) {
		the_act = this;

		new AsyncTask<Void, Void, Void>() {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				creating_contact_dialog = ProgressDialog.show(the_act,
						getString(R.string.please_wait),
						getString(R.string.creating_contact), true);
			}

			@Override
			protected Void doInBackground(Void... params) {
				create_new_contact(ip_addr, box_nm, fst_kk);
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				creating_contact_dialog.dismiss();
				the_act.finish();
			}
		}.execute();
	}

	void create_new_contact(String ip_addr, String box_nm, String fst_kk) {
		byte[] start_kk = null;
		if (fst_kk != null) {
			start_kk = fst_kk.getBytes(config.UTF_8);
		}

		if (!app.ca_has_passet()) {
			return;
		}
		if (!app.ca_has_l_owner()) {
			return;
		}

		transaction cli_trans = app.ca_get_new_cli_trans();
		user_oper = get_user_oper();
		cli_trans.init_callers(user_oper, app.ca_fl_op);
		cli_trans.state_oper = transaction.NET_SEND_CREATE_CHANN_OPER;
		cli_trans.r_peer_descr = ip_addr;
		cli_trans.start_key = start_kk;
		//cli_trans.trust_level = transaction.TRUST_ANY;
		
		app.ca_cli_net_op.queue_transaction(cli_trans);

		Log.d(LOGTAG, "started create_new_contact");

		transaction trans = null;
		long tm0 = System.currentTimeMillis();
		while(true){
			trans = user_oper.wait_for_transaction(null);
			if(trans == null){
				misce.show_message(this, R.string.operation_failed, null, false);
				break;
			}
			long tm1 = System.currentTimeMillis();
			long diff_tm = tm1 - tm0;
			long max_tm = config.MIN_MILLIS * 8;
			if(diff_tm > max_tm){
				misce.show_message(this, R.string.operation_timeout, null, false);
				break;
			}
			if(trans.state_oper == transaction.USER_TELL_FINISHED_OPER){
				break;
			}
			//trans.trust_level = transaction.TRUST_ANY;
			trans.answer_oper(transaction.NET_CONTINUE_OPER);
		}
		
		if((trans != null) && (trans.has_finished_all())){
			if(app.ca_added_contact_coid == null){
				app.ca_added_contact_coid = trans.coid;
			}
		}
		
		Log.d(LOGTAG, "finished create_new_contact");
	}

	private trans_operator get_user_oper() {
		return new trans_operator() {
			public void queue_transaction(transaction trans) {
				try {
					from_net_oper.put(trans);
				} catch (InterruptedException ex) {
					throw new bad_cashapp(2, ex.toString());
				}		
			}

			public transaction wait_for_transaction(transaction working_trans) {
				transaction trans = null;
				try {
					trans = from_net_oper.poll(2, TimeUnit.MINUTES);
					if(trans == null){
						return null;
					}
					if(trans.state_oper == transaction.INVALID_OPER){
						return null;
					}
				} catch (InterruptedException e) {
					return null;
				}
				return trans;
			}
		};
	}	
}
