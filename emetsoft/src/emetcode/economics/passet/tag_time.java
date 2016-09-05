package emetcode.economics.passet;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.TimeZone;

import emetcode.crypto.bitshake.utils.convert;

public class tag_time {

	public static final String utc_mili_secs_field = " UTC in mili secs.";

	public static final String utc_txt_field = " UTC as text.";

	public static final long STD_ERR_TM = config.MIN_MILLIS * 10;
	
	public long milis_time;
	public String num_str;
	public String txt_str;

	public tag_time() {
		set_to_now();
	}

	public tag_time(long milis) {
		set_to(milis);
	}

	public tag_time(tag_time orig) {
		if (orig == null) {
			set_to_now();
			return;
		}
		set_to(orig.milis_time);
	}

	void set_to_now() {
		set_to(System.currentTimeMillis());
	}

	boolean is_valid() {
		return (milis_time != 0);
	}

	void set_to(long milis) {
		milis_time = milis;
		num_str = "" + milis_time;
		if (milis_time != 0) {
			txt_str = convert.utc_to_string(milis_time);
		} else {
			txt_str = config.UNKNOWN_STR;
		}
	}

	public static String utc_to_short_string(long utc_millis) {
		if (utc_millis == 0) {
			return config.UNKNOWN_STR;
		}

		String DATEFORMAT = "yyyy-MM-dd HH:mm";
		SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

		String utc_str = sdf.format(new Date(utc_millis));
		return utc_str;
	}

	public String short_txt() {
		return utc_to_short_string(milis_time);
	}

	List<String> get_time_lines(String title) {
		List<String> txt = new ArrayList<String>();
		add_time_lines_to(txt, title);
		return txt;
	}

	void add_time_lines_to(List<String> txt, String title) {

		parse.add_next_field_to(txt, title, utc_mili_secs_field, num_str);
		parse.add_next_field_to(txt, title, utc_txt_field, txt_str);

		parse.check_line_list(txt);
	}

	public void init_time_with(List<String> all_lines, String title) {
		parse.check_line_list(all_lines);

		ListIterator<String> it1 = all_lines.listIterator();
		init_time_with(it1, title);
	}

	public void init_time_with(ListIterator<String> it1, String title) {
		String tmp1 = parse.get_next_field_from(it1, title, utc_mili_secs_field);
		String tmp2 = parse.get_next_field_from(it1, title, utc_txt_field);

		long mm = convert.parse_long(tmp1);
		set_to(mm);

		if (!num_str.equals(tmp1)) {
			throw new bad_passet(2);
		}
		if (!txt_str.equals(tmp2)) {
			throw new bad_passet(2);
		}
	}

	public boolean equals(Object obj){
		if(obj == null){
			return false;
		}
		if(! (obj instanceof tag_time)){
			throw new bad_passet(2);
		}
		tag_time t2 = (tag_time)obj;
		boolean eq1 = (milis_time == t2.milis_time);
		if(eq1){
			boolean v1 = num_str.equals(t2.num_str);
			boolean v2 = txt_str.equals(t2.txt_str);
			if(! v1){
				throw new bad_passet(2);
			}
			if(! v2){
				throw new bad_passet(2);
			}
		}
		return eq1;
	}
	
	public static int cmp_aprox_time(long tm1, long tm2, long err){
		long aa = Math.abs(tm1 - tm2);
		if(aa < err){
			return 0;
		}
		if(tm1 < tm2){
			return -1;
		}
		return 1;
	}

	public static int cmp_aprox_tag_time(tag_time tm1, tag_time tm2, long err){
		return cmp_aprox_time(tm1.milis_time, tm2.milis_time, err);
	}
	
	public int cmp_aprox_to(tag_time tm2){
		return cmp_aprox_tag_time(this, tm2, STD_ERR_TM);
	}
	
	public int cmp_to(tag_time tm2){
		return convert.cmp_long(milis_time, tm2.milis_time);
	}
}
