package io.github.kambio.kambio;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import io.github.kambio.kambio.R;
import emetcode.crypto.bitshake.utils.convert;
import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.economics.passet.paccount;
import emetcode.economics.passet.trissuers;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class issuers_adapter extends BaseAdapter {
	public static final String LOGTAG = "issuers_adapter";

	public static final int TRUSTED_DOMS = 4001;
	public static final int TRUSTED_BY_CONTACTS_DOMS = 4002;
	public static final int NOT_TRUSTED_DOMS = 4003;
	public static final int NOT_TRUSTED_BY_CONTACTS_DOMS = 4004;

	app_base app;
	issuers_activity ctx;
	LayoutInflater row_inflater;

	List<String> trusted_doms;
	List<String> not_trusted_doms;
	List<String> trusted_by_contacts_doms;
	List<String> not_trusted_by_contacts_doms;

	Set<String> not_trusted_by_contacts_set;

	int current_kind;
	List<String> working_doms;

	int selected_pos;

	public issuers_adapter(issuers_activity the_ctx) {

		app = (app_base) the_ctx.getApplicationContext();

		ctx = the_ctx;

		row_inflater = LayoutInflater.from(ctx);

		trusted_doms = null;
		not_trusted_doms = null;
		trusted_by_contacts_doms = null;
		not_trusted_by_contacts_doms = null;

		not_trusted_by_contacts_set = null;

		current_kind = TRUSTED_DOMS;
		working_doms = null;

		selected_pos = -1;

		// dbg_fill_issuers();
		fill_issuers();
		Log.d(LOGTAG, "finished creation of issuers_adapter");
	}

	private void set_view_sizes(view_holder holder) {
		holder.issuer_domain.setTextSize(app_base.CA_CONTACTS_TEXT_LIST_SZ);
	}

	String get_sel_dom() {
		return get_dom(selected_pos);
	}

	String get_dom(int idx) {
		if (working_doms == null) {
			return null;
		}
		if (idx < 0) {
			return null;
		}
		if (idx >= working_doms.size()) {
			return null;
		}
		return working_doms.get(idx);
	}

	private void set_dom(View item_view, view_holder holder, int idx) {
		set_view_sizes(holder);
		String dom = get_dom(idx);
		if (dom == null) {
			holder.issuer_domain.setText("");
			return;
		}

		set_txt(holder.issuer_domain, dom);
		if (ctx.highlight_not_trusted_by_contacts
				&& (not_trusted_by_contacts_set != null)
				&& (working_doms != not_trusted_by_contacts_doms)
				&& not_trusted_by_contacts_set.contains(dom)) {
			item_view.setBackgroundResource(android.R.color.holo_orange_light);
		} else {
			item_view.setBackgroundResource(android.R.color.white);
		}
	}

	private void set_txt(TextView vw, String txt) {
		if (txt == null) {
			vw.setText("");
			return;
		}
		vw.setText(txt);
	}

	public int getCount() {
		if (working_doms != null) {
			return working_doms.size();
		}
		return 0;
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View old_view, ViewGroup parent) {
		view_holder holder;

		if (old_view == null) {
			old_view = row_inflater.inflate(R.layout.issuer_item, null);

			holder = new view_holder();
			holder.issuer_domain = (TextView) old_view
					.findViewById(R.id.issuer_domain);

			old_view.setTag(holder);
		} else {
			holder = (view_holder) old_view.getTag();
		}

		holder.pos = position;
		set_dom(old_view, holder, position);

		if (position == selected_pos) {
			old_view.setBackgroundResource(android.R.color.holo_blue_light);
		}

		return old_view;
	}

	static class view_holder {
		int pos = -1;
		TextView issuer_domain = null;
	}

	private void fill_issuers() {
		paccount pss = app.ca_get_passet();
		key_owner owr = app.ca_l_owner;
		
		trissuers t_grps = new trissuers();
		t_grps.init_trissuers(pss, owr);
		//pss.init_dom_trusted(owr);

		trusted_doms = new ArrayList<String>();
		not_trusted_doms = new ArrayList<String>();

		trusted_doms.addAll(t_grps.trusted);
		not_trusted_doms.addAll(t_grps.not_trusted);
	}

	void set_kind(int kk) {
		current_kind = kk;
		switch (kk) {
		case TRUSTED_DOMS:
			selected_pos = -1;
			working_doms = trusted_doms;
			break;
		case NOT_TRUSTED_DOMS:
			selected_pos = -1;
			working_doms = not_trusted_doms;
			break;
		case TRUSTED_BY_CONTACTS_DOMS:
			selected_pos = -1;
			working_doms = trusted_by_contacts_doms;
			break;
		case NOT_TRUSTED_BY_CONTACTS_DOMS:
			selected_pos = -1;
			working_doms = not_trusted_by_contacts_doms;
			break;
		}
	}

	void add_issuer(final String iss) {
		List<String> add_lst = working_doms;
		if (working_doms == trusted_by_contacts_doms) {
			misce.show_message(ctx, R.string.adding_issuer_to_trusted_list,
					null, false);
			add_lst = trusted_doms;
		}
		if (working_doms == not_trusted_by_contacts_doms) {
			misce.show_message(ctx, R.string.adding_issuer_to_trusted_list,
					null, false);
			add_lst = trusted_doms;
		}
		if ((add_lst != null) && (iss != null) && !iss.isEmpty()) {
			if (!add_lst.contains(iss)) {
				List<String> not_t = not_trusted_by_contacts_doms;
				if ((not_t != null) && not_t.contains(iss)) {
					misce.show_message(ctx,
							R.string.issuer_not_trusted_by_contacts_add_anyway,
							ok_add_issuer(add_lst, iss), false);
				} else {
					add_lst.add(iss);
				}
			} else {
				String msg = "'" + iss + "'\n"
						+ ctx.getString(R.string.issuer_already_in_list);
				misce.show_message(ctx, msg, null, false);
			}
		}
	}

	private ok_doer ok_add_issuer(final List<String> add_lst, final String iss) {
		ok_doer dd = new ok_doer() {
			public void on_ok_do() {
				add_lst.add(iss);
			}
		};
		return dd;
	}

	boolean is_valid_pos(List<?> lst, int pos) {
		if (pos < 0) {
			return false;
		}
		if (pos >= lst.size()) {
			return false;
		}
		return true;

	}

	void set_new_pos(int pos) {
		String val = null;
		if (!ck_trusted_by_contacts()) {
			return;
		}
		if (pos < 0) {
			return;
		}
		if (pos == selected_pos) {
			String msg = "'" + selected_pos + "'\n"
					+ ctx.getString(R.string.issuer_already_in_that_position);
			misce.show_message(ctx, msg, null, false);
			return;
		}
		if ((working_doms != null) && is_valid_pos(working_doms, selected_pos)) {
			val = working_doms.remove(selected_pos);
		}
		if (val == null) {
			misce.show_message(ctx, R.string.issuer_none_selected, null, false);
			return;
		}
		int sz = working_doms.size();
		if (pos > sz) {
			pos = sz;
		}
		working_doms.add(pos, val);
		selected_pos = pos;
	}

	void delete_current() {
		if (!ck_trusted_by_contacts()) {
			return;
		}
		if ((working_doms != null) && is_valid_pos(working_doms, selected_pos)) {
			working_doms.remove(selected_pos);
		}
	}

	void delete_all() {
		if (!ck_trusted_by_contacts()) {
			return;
		}
		if ((working_doms != null)) {
			working_doms.clear();
		}
	}

	boolean ck_trusted_by_contacts() {
		if (working_doms == trusted_by_contacts_doms) {
			misce.show_message(ctx, R.string.cannot_change_trusted_by_contacts,
					null, false);
			return false;
		}
		if (working_doms == not_trusted_by_contacts_doms) {
			misce.show_message(ctx, R.string.cannot_change_trusted_by_contacts,
					null, false);
			return false;
		}
		return true;
	}

	void dbg_fill_issuers() {
		int max_iss = 2000;
		long val = app.ca_l_owner.new_random_long();
		int sz_1 = (int) convert.to_interval(val, 0, max_iss);
		val = app.ca_l_owner.new_random_long();
		int sz_2 = (int) convert.to_interval(val, 0, max_iss);
		val = app.ca_l_owner.new_random_long();
		int sz_3 = (int) convert.to_interval(val, 0, max_iss);
		val = app.ca_l_owner.new_random_long();
		int sz_4 = (int) convert.to_interval(val, 0, max_iss);

		List<String> trusted = new ArrayList<String>();

		for (int aa = 0; aa < sz_1; aa++) {
			trusted.add("trusted_issuer_" + aa);
		}
		trusted_doms = trusted;

		List<String> not_trusted = new ArrayList<String>();
		for (int aa = 0; aa < sz_2; aa++) {
			not_trusted.add("not_trusted_issuer_" + aa);
		}
		not_trusted_doms = not_trusted;

		app.ca_all_trusted_by_contacs = new trissuers();
		List<String> trusted_by_c = new ArrayList<String>();
		for (int aa = 0; aa < sz_3; aa++) {
			trusted_by_c.add("trusted_by_contact_issuer_" + aa);
		}
		app.ca_all_trusted_by_contacs.trusted = trusted_by_c;
		trusted_by_contacts_doms = trusted_by_c;

		List<String> not_trusted_by_c = new ArrayList<String>();
		for (int aa = 0; aa < sz_4; aa++) {
			long val2 = app.ca_l_owner.new_random_long();
			int lst_cho = (int) convert.to_interval(val2, 0, 3);
			List<String> lst = null;
			switch (lst_cho) {
			case 0:
				lst = trusted;
				break;
			case 1:
				lst = not_trusted;
				break;
			case 2:
			default:
				lst = trusted_by_c;
				break;
			}
			long val_idx = app.ca_l_owner.new_random_long();
			int idx_cho = (int) convert.to_interval(val_idx, 0, lst.size());
			String vv = lst.get(idx_cho);
			not_trusted_by_c.add(vv);
		}
		app.ca_all_trusted_by_contacs.not_trusted = not_trusted_by_c;
		not_trusted_by_contacts_doms = not_trusted_by_c;

		not_trusted_by_contacts_set = new TreeSet<String>();
		not_trusted_by_contacts_set.addAll(not_trusted_by_c);

		Log.d(LOGTAG, "finished dbg_fill_issuers");
	}

}
