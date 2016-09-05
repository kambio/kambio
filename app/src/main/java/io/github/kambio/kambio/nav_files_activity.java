package io.github.kambio.kambio;

import java.io.File;
import java.io.IOException;

import io.github.kambio.kambio.R;

import android.app.ListActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class nav_files_activity extends ListActivity {
	public static final String LOGTAG = "nav_files";

	app_base app;
	File curr_dir;
	int curr_idx;
	String[] names;
	TextView vw_curr_pth;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nav_files_layout);

		app = (app_base) getApplicationContext();
		app.ca_chosen_file = null;

		vw_curr_pth = (TextView) findViewById(R.id.current_path);
		vw_curr_pth.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_FLAG_MULTI_LINE);

		set_dir(getFilesDir());
		curr_idx = 0;
	}

	private void set_dir(File dd) {
		curr_dir = dd;
		names = curr_dir.list();

		setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_activated_1, names));
		getListView().setTextFilterEnabled(true);
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		getListView().setItemChecked(0, true);

		try {
			vw_curr_pth.setText(dd.getCanonicalPath());
		} catch (IOException ee) {
			Log.d(LOGTAG, ee.toString());
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		getListView().setItemChecked(position, true);
		int prev_idx = curr_idx;
		curr_idx = position;
		File tmp_ff = new File(curr_dir, names[curr_idx]);
		if ((prev_idx == curr_idx) && tmp_ff.isDirectory()) {
			set_dir(tmp_ff);
			curr_idx = 0;
		} else {
			try {
				vw_curr_pth.setText(tmp_ff.getCanonicalPath());
			} catch (IOException ee) {
				Log.d(LOGTAG, ee.toString());
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.nav_files, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		try {
			switch (item.getItemId()) {
			case R.id.go_up_menu_item:
				Log.i(LOGTAG, "go_up_menu_item");
				File top = getFilesDir();
				if (!curr_dir.equals(top)) {
					File tmp_ff = curr_dir.getParentFile();
					set_dir(tmp_ff);
				}
				break;
			}
		} catch (Exception e) {
			Log.i(LOGTAG, e.toString());
		}
		return true;
	}

	public void clicked_accept_button(View v) {
		app.ca_chosen_file = new File(vw_curr_pth.getText().toString());
		finish();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(LOGTAG, "onPause");
		app.ca_in_actv(app_base.CA_CHOOSE_FILE_ACTIVITY);
	}
}
