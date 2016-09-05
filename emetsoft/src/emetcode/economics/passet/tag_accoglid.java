package emetcode.economics.passet;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import emetcode.net.netmix.nx_std_coref;

public class tag_accoglid implements Comparable<tag_accoglid> {

	public static final String acoglid_field = " global id.";
	public static final String temp_domain_field = " last known domain.";

	public nx_std_coref the_glid;

	public tag_accoglid() {
		init_tag_accoglid();
	}

	public tag_accoglid(tag_accoglid orig) {
		init_tag_accoglid();
		if ((orig != null) && (orig.the_glid != null)) {
			the_glid = new nx_std_coref(orig.the_glid);
		}
	}

	public tag_accoglid(nx_std_coref o_gli) {
		init_tag_accoglid();
		if (o_gli != null) {
			the_glid = new nx_std_coref(o_gli);
		}
	}

	public tag_accoglid(String str) {
		init_tag_accoglid();
		the_glid = new nx_std_coref(str);
	}
	
	void init_tag_accoglid() {
		the_glid = null;
	}

	public List<String> get_accoglid_lines(String title) {
		List<String> txt = new ArrayList<String>();
		add_accoglid_lines_to(txt, title);
		return txt;
	}

	void add_accoglid_lines_to(List<String> txt, String title) {

		String acoglid_val = config.UNKNOWN_STR;
		if (the_glid != null) {
			acoglid_val = the_glid.toString();
		}

		parse.add_next_field_to(txt, title, acoglid_field, acoglid_val);

		parse.check_line_list(txt);
	}

	public void init_accoglid_with(List<String> all_lines, String title) {
		parse.check_line_list(all_lines);

		ListIterator<String> it1 = all_lines.listIterator();
		init_accoglid(it1, title);
	}

	public void init_accoglid(ListIterator<String> it1, String title) {
		String gli_str = parse.get_next_field_from(it1, title, acoglid_field);

		if (gli_str.equals(config.UNKNOWN_STR)) {
			the_glid = null;
		} else {
			the_glid = new nx_std_coref(gli_str);
		}
	}

	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof tag_accoglid)) {
			throw new bad_passet(2);
		}
		tag_accoglid t2 = (tag_accoglid) obj;
		if (the_glid == null) {
			return false;
		}
		return the_glid.equals(t2.the_glid);
	}

	public boolean has_valid_glid() {
		boolean ok_gli = ((the_glid != null) && the_glid.is_glid());
		return ok_gli;
	}
	
	public String get_str(){
		return the_glid.get_str();
	}
	
	public String toString(){
		return get_str();
	}
	
	public int compareTo(tag_accoglid acc){
		if(acc == null){
			throw new bad_passet(2);
		}
		String str_1 = get_str();
		String str_2 = acc.get_str();
		
		return str_1.compareTo(str_2);
	}
}
