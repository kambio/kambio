
package io.github.kambio.kambio;

import io.github.kambio.kambio.R;
import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class choose_ip_activity extends ListActivity {
	public static final String LOGTAG = "choose_ip";
	
	app_base app;
	String[] all_ip;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		app = (app_base) getApplicationContext();
		app.ca_init_net_addr();
		all_ip = app.ca_all_ip; 

        setListAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_activated_1, all_ip));
        getListView().setTextFilterEnabled(true);
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        getListView().setItemChecked(0, true);
        
        setTitle(R.string.choose_net_addr);
    }
        
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        getListView().setItemChecked(position, true);
        String ip_str = all_ip[position];
        Log.d(LOGTAG, "Selected ip=" + ip_str);
        app.ca_l_descr = ip_str;
    }
}
