package emetcode.economics.passet;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import emetcode.net.netmix.nx_std_coref;

public class tag_trader {

	public tag_accoglid tr_glid;
	public tag_time tr_time;

	public tag_trader() {
		init_tag_trader();
	}

	public tag_trader(nx_std_coref orig_gli) {
		init_tag_trader();
		if (orig_gli != null) {
			tr_glid = new tag_accoglid(orig_gli);
		}
	}

	public tag_trader(tag_trader orig) {
		if (orig == null) {
			init_tag_trader();
			return;
		}
		tr_glid = new tag_accoglid(orig.tr_glid);
		tr_time = new tag_time(orig.tr_time);
	}

	void init_tag_trader() {
		tr_glid = new tag_accoglid();
		tr_time = new tag_time();
	}

	void set_now() {
		tr_time.set_to_now();
	}

	List<String> get_trader_lines(String title) {
		List<String> txt = new ArrayList<String>();
		add_trader_lines_to(txt, title);
		return txt;
	}

	void add_trader_lines_to(List<String> txt, String title) {

		tr_glid.add_accoglid_lines_to(txt, title);
		tr_time.add_time_lines_to(txt, title);

	}

	public void init_trader_with(List<String> all_lines, String title) {
		parse.check_line_list(all_lines);

		ListIterator<String> it1 = all_lines.listIterator();
		init_trader_with(it1, title);
	}

	public void init_trader_with(ListIterator<String> it1, String title) {

		tr_glid.init_accoglid(it1, title);
		tr_time.init_time_with(it1, title);
	}

	public nx_std_coref get_glid() {
		return tr_glid.the_glid;
	}
	
	public boolean has_valid_glid(){
		return tr_glid.has_valid_glid();
	}
}
