package io.github.kambio.kambio;

import io.github.kambio.kambio.R;
import emetcode.economics.passet.iso;
import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class choose_currency_activity extends ListActivity {
	public static final String LOGTAG = "choose_currency_activity";

	// ListView vw_all_currcy;
	app_base app;
	String[] all_currcy_str;
	TextView tvw_currency_id;
	int selec_idx;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		app = (app_base) getApplicationContext();

		setContentView(R.layout.choose_item_layout);

		String[][] all_ccy = iso.currencies_array;
		all_currcy_str = new String[all_ccy.length];
		for (int aa = 0; aa < all_ccy.length; aa++) {
			all_currcy_str[aa] = "" + all_ccy[aa][0] + " \t " + all_ccy[aa][1]
					+ " (" + all_ccy[aa][2] + ")";
		}

		tvw_currency_id = (TextView) findViewById(R.id.chosen_id);
		
		int currcy_idx = app.ca_currcy_idx;
		if(iso.is_valid_currency_idx(currcy_idx)){
			tvw_currency_id.setText(iso.get_currency_code(currcy_idx));			
		}

		setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_activated_1, all_currcy_str));
		getListView().setTextFilterEnabled(true);

		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		getListView().setItemChecked(0, true);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		getListView().setItemChecked(position, true);
		tvw_currency_id.setText(iso.get_currency_code(position));
		selec_idx = position;
		Log.d(LOGTAG, "currecncy=" + all_currcy_str[position]);
	}

	public void clicked_accept_button(View v) {
		Log.d(LOGTAG,
				"clicked_accept_button with= " + iso.get_currency_code(selec_idx));
		app.ca_currcy_idx = selec_idx;
		finish();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(LOGTAG, "onPause");
		app.ca_in_actv(app_base.CA_CHOOSE_CURRENCY_CODE_ACTIVITY);
	}
}
