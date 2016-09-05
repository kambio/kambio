package emetcode.economics.passet;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import emetcode.net.netmix.nx_std_coref;

public class tag_filglid {

	static final String INVALID_FILGLID = "INVALID_FILGLID";
	
	static final String filglid_field = "global id.";

	private nx_std_coref the_filglid;

	tag_filglid() {
		init_tag_filglid(INVALID_FILGLID);
	}
	
	public tag_filglid(tag_filglid orig) {
		if(orig == null){
			throw new bad_passet(2);
		}
		init_tag_filglid(orig.the_filglid.get_str());
	}

	public tag_filglid(String val) {
		init_tag_filglid(val);
	}

	void init_tag_filglid(String val) {
		if(val == null){
			throw new bad_passet(2);
		}
		the_filglid = new nx_std_coref(val);
	}

	public List<String> get_filglid_lines(String title) {
		List<String> txt = new ArrayList<String>();
		add_filglid_lines_to(txt, title);
		return txt;
	}

	void add_filglid_lines_to(List<String> txt, String title) {

		String filglid_val = config.UNKNOWN_STR;
		if(the_filglid != null){
			filglid_val = the_filglid.toString();
		}		
		
		parse.add_next_field_to(txt, title, filglid_field, filglid_val);

		parse.check_line_list(txt);
	}

	public void init_filglid_with(List<String> all_lines, String title) {
		parse.check_line_list(all_lines);

		ListIterator<String> it1 = all_lines.listIterator();
		init_filglid(it1, title);
	}

	public void init_filglid(ListIterator<String> it1, String title) {
		String gli_str = parse.get_next_field_from(it1, title, filglid_field);
		
		if(gli_str.equals(config.UNKNOWN_STR)){
			the_filglid = null;
		} else {
			the_filglid = new nx_std_coref(gli_str);
		}
	}

	public boolean equals(Object obj){
		if(obj == null){
			return false;
		}
		if(! (obj instanceof tag_filglid)){
			throw new bad_passet(2);
		}
		tag_filglid t2 = (tag_filglid)obj;
		return the_filglid.equals(t2.the_filglid);
	}
	
	public String get_str(){
		return the_filglid.get_str();
	}
	
	public String toString(){
		return get_str();
	}
}
