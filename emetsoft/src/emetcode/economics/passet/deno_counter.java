package emetcode.economics.passet;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

public class deno_counter {

	public static final int MIN_EXPO = tag_denomination.MIN_EXPO;
	public static final int MAX_EXPO = tag_denomination.MAX_EXPO;

	public static final String currency_idx_field = "<currency_idx>";

	public static final String end_of_deno_mark = 
			"end_of_deno_mark";
	
	public int currency_idx;

	deno_count[] neg_denos;
	deno_count[] zer_denos;
	deno_count[] pos_denos;

	public deno_counter() {
		reset_deno_counts(config.DEFAULT_CURRENCY);
	}

	public deno_counter(int currcy_idx) {
		reset_deno_counts(currcy_idx);
	}

	public static deno_counter copy_counter(deno_counter orig) {
		deno_counter n_cter = new deno_counter();

		n_cter.currency_idx = orig.currency_idx;

		for (int aa = 0; aa < orig.neg_denos.length; aa++) {
			n_cter.neg_denos[aa] = new deno_count(orig.neg_denos[aa]);
		}
		for (int aa = 0; aa < orig.zer_denos.length; aa++) {
			n_cter.zer_denos[aa] = new deno_count(orig.zer_denos[aa]);
		}
		for (int aa = 0; aa < orig.pos_denos.length; aa++) {
			n_cter.pos_denos[aa] = new deno_count(orig.pos_denos[aa]);
		}

		return n_cter;
	}

	public deno_count get_deno_count(tag_denomination deno) {
		return get_deno_count(deno, true);
	}

	private static int get_arr_idx(tag_denomination deno) {
		if (deno == null) {
			return -1;
		}
		if (!deno.is_valid_deno()) {
			return -1;
		}

		int expo = deno.ten_exponent;
		int mult = deno.multiplier;

		int blk = (expo > 0) ? (expo) : (-expo);
		int dis = mult;
		if (mult == 5) {
			dis = 3;
		}

		if (expo == 0) {
			return (dis - 1);
		}

		int idx = ((blk - 1) * 3) + (dis - 1);
		return idx;
	}

	public deno_count get_deno_count(tag_denomination deno, boolean create) {
		if (!deno.is_valid_deno()) {
			throw new bad_passet(2);
		}

		int expo = deno.ten_exponent;
		int idx = get_arr_idx(deno);
		deno_count[] arr = null;
		if (expo < 0) {
			arr = neg_denos;
		}
		if (expo == 0) {
			arr = zer_denos;
		}
		if (expo > 0) {
			arr = pos_denos;
		}

		if (create && (arr[idx] == null)) {
			arr[idx] = new deno_count();
			arr[idx].deno = new tag_denomination(deno);
		}
		deno_count cc = arr[idx];
		return cc;
	}

	void reset_deno_counts(int currcy_idx) {
		currency_idx = currcy_idx;

		neg_denos = new deno_count[MAX_EXPO * 3];
		zer_denos = new deno_count[3];
		pos_denos = new deno_count[MAX_EXPO * 3];

		Arrays.fill(neg_denos, null);
		Arrays.fill(zer_denos, null);
		Arrays.fill(pos_denos, null);
	}

	public void print_all_deno_count(PrintStream ps) {
		StringBuilder the_sb = get_all_deno_count_string(new StringBuilder());
		ps.println(the_sb.toString());
	}
	
	public List<deno_count> get_working_counts() {
		List<deno_count> all_deno = new ArrayList<deno_count>();
		tag_denomination curr_dd = tag_denomination
				.first_deno(currency_idx);
		while (true) {
			deno_count deco = get_deno_count(curr_dd, false);
			if(deco != null){
				all_deno.add(deco);
			}
			if (curr_dd.is_last_deno()) {
				break;
			}
			curr_dd.inc_deno();
		}
		return all_deno;
	}
	
	public StringBuilder get_all_deno_count_string(StringBuilder full_str) {
		boolean prt_some = false;

		List<deno_count> w_cnts = get_working_counts();
		for(deno_count deco: w_cnts){
			full_str.append(deco.toString() + "\n");
			prt_some = true;
		}
		if (!prt_some) {
			String msg = "NO '" + iso.get_currency_code(currency_idx)
					+ "' (" + iso.get_currency_name(currency_idx) + ")"
					+ " notes found. Please issue or get some.";
			full_str.append(msg);
		}
		
		return full_str;
	}

	public void clear_zeros() {
		for (int aa = 0; aa < neg_denos.length; aa++) {
			deno_count deco = neg_denos[aa];
			if ((deco != null) && deco.is_zero()) {
				neg_denos[aa] = null;
			}
		}
		for (int aa = 0; aa < zer_denos.length; aa++) {
			deno_count deco = zer_denos[aa];
			if ((deco != null) && deco.is_zero()) {
				zer_denos[aa] = null;
			}
		}
		for (int aa = 0; aa < pos_denos.length; aa++) {
			deno_count deco = pos_denos[aa];
			if ((deco != null) && deco.is_zero()) {
				pos_denos[aa] = null;
			}
		}
	}

	public void add_counter(deno_counter cnt_2) {
		if (currency_idx != cnt_2.currency_idx) {
			throw new bad_passet(2);
		}
		List<deno_count> w_cnts = cnt_2.get_working_counts();
		for(deno_count deco2: w_cnts){			
			deno_count deco1 = get_deno_count(deco2.deno, true);
			deco1.add_count(deco2);
		}
	}

	/*
	 * void filter_device_lines(){ net_kind = parse.filter_string(net_kind); }
	 */

	public List<String> get_deno_counter_lines(String title) {
		List<String> txt = new ArrayList<String>();
		add_deno_counter_lines_to(txt, title);
		return txt;
	}

	void add_deno_counter_lines_to(List<String> txt, String title) {

		parse.add_next_title_to(txt, title);
		parse.add_next_field_to(txt, null, currency_idx_field, "" + currency_idx);

		List<deno_count> w_cnts = get_working_counts();
		for(deno_count deco1: w_cnts){			
			String nxt_ln = deco1.as_text_deno_count();
			nxt_ln = parse.filter_string(nxt_ln);
			txt.add(nxt_ln);
		}		
		parse.add_mark_to(txt, end_of_deno_mark);

		parse.check_line_list(txt);
	}

	public void init_deno_counter_with(List<String> all_lines, String title) {
		parse.check_line_list(all_lines);

		ListIterator<String> it1 = all_lines.listIterator();
		init_deno_counter_with(it1, title);
	}

	public void init_deno_counter_with(ListIterator<String> it1, String title) {
		parse.get_next_title_from(it1, title);
		String idx_str = parse.get_next_field_from(it1, null, currency_idx_field);
		currency_idx = Integer.parseInt(idx_str);

		while (it1.hasNext()) {
			String nxt_ln = it1.next();
//			if (parse.is_end_of_title(nxt_ln)) {
//				break;
//			}
			if (parse.is_mark(nxt_ln, end_of_deno_mark)) {
				break;
			}
			deno_count deco2 = deno_count.parse_text_deno_count(nxt_ln,
					currency_idx);
			deno_count deco1 = get_deno_count(deco2.deno, true);
			deco1.add_count(deco2);
		}
	}

	void init_with_denos(Collection<tag_denomination> all_deno) {
		if (all_deno == null) {
			return;
		}
		Collection<Object> all_obj = new ArrayList<Object>();
		all_obj.addAll(all_deno);
		init_with_objs(all_obj, false);
	}
	
	private void init_with_objs(Collection<Object> all_deno, boolean add_to_prev) {
		reset_deno_counts(config.DEFAULT_CURRENCY);
		if (all_deno == null) {
			return;
		}
		int currcy_idx = -1;

		for (Object oo : all_deno) {
			tag_transfer tt = null;
			tag_denomination dd = null;
			if(oo instanceof tag_transfer){
				tt = (tag_transfer)oo;
				dd = tt.get_out_amount();
			} else
			if(oo instanceof tag_denomination){
				dd = (tag_denomination)oo;
			} else {
				throw new bad_passet(2);
			}
			
			if (currcy_idx == -1) {
				currcy_idx = dd.currency_idx;
				if (currcy_idx == -1) {
					throw new bad_passet(2);
				}
			}
			if (dd.currency_idx != currcy_idx) {
				throw new bad_passet(2);
			}
			deno_count cc = get_deno_count(dd);
			cc.num_have++;
			if(add_to_prev && (tt != null)){
				cc.joined_denos.add(tt);
			}
		}
		if (!iso.is_valid_currency_idx(currcy_idx)) {
			throw new bad_passet(2);
		}
		currency_idx = currcy_idx;
	}
	
	void init_with_transfers(Collection<tag_transfer> all_dat) {
		if (all_dat == null) {
			return;
		}
		Collection<Object> all_obj = new ArrayList<Object>();
		all_obj.addAll(all_dat);
		init_with_objs(all_obj, true);
	}
	
	public boolean equals(Object obj){
		if(obj == null){
			return false;
		}
		if(! (obj instanceof deno_counter)){
			throw new bad_passet(2);
		}
		deno_counter dc2 = (deno_counter)obj;
		List<deno_count> all_cc1 = get_working_counts();
		List<deno_count> all_cc2 = dc2.get_working_counts();
		boolean eq_ll = all_cc1.equals(all_cc2);
		return eq_ll;
	}	
}
