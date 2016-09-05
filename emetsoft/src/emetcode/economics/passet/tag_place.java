package emetcode.economics.passet;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class tag_place {

	public static place_filler place_initer = null;

	public static final String longitude_coordinate_field = " longitude.";
	public static final String latitude_coordinate_field = " latitude.";
	public static final String altitude_coordinate_field = " altitude.";

	public String lon_coor;
	public String lat_coor;
	public String alt_coor;

	public tag_place() {
		init_tag_place();
		if (place_initer != null) {
			place_initer.fill_place(this);
		}
	}

	public tag_place(tag_place orig) {
		init_tag_place();
		if (orig == null) {
			return;
		}
		lon_coor = orig.lon_coor;
		lat_coor = orig.lat_coor;
		alt_coor = orig.alt_coor;
	}

	void init_tag_place() {
		lon_coor = config.UNKNOWN_STR;
		lat_coor = config.UNKNOWN_STR;
		alt_coor = config.UNKNOWN_STR;
	}

	void filter_place_lines() {
		lon_coor = parse.filter_string(lon_coor);
		lat_coor = parse.filter_string(lat_coor);
		alt_coor = parse.filter_string(alt_coor);
	}

	List<String> get_place_lines(String title) {
		List<String> txt = new ArrayList<String>();
		add_place_lines_to(txt, title);
		return txt;
	}

	void add_place_lines_to(List<String> txt, String title) {
		filter_place_lines();

//		parse.add_next_title_to(txt, title);
		parse.add_next_field_to(txt, title, longitude_coordinate_field, lon_coor);
		parse.add_next_field_to(txt, title, latitude_coordinate_field, lat_coor);
		parse.add_next_field_to(txt, title, altitude_coordinate_field, alt_coor);
//		txt.add(parse.end_of_title);

		parse.check_line_list(txt);
	}

	public void init_place_with(List<String> all_lines, String title) {
		parse.check_line_list(all_lines);

		ListIterator<String> it1 = all_lines.listIterator();
		init_place_with(it1, title);
	}

	public void init_place_with(ListIterator<String> it1, String title) {
//		parse.get_next_title_from(it1, title);
		lon_coor = parse.get_next_field_from(it1, title, longitude_coordinate_field);
		lat_coor = parse.get_next_field_from(it1, title, latitude_coordinate_field);
		alt_coor = parse.get_next_field_from(it1, title, altitude_coordinate_field);
//		parse.get_next_end_of_title(it1);
	}

}
