package emetcode.economics.passet;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class tag_device {

	public static device_filler device_initer = new default_device_filler();

	public static final String net_kind_field = " net kind.";
	public static final String address_field = " reported net address.";
	public static final String seen_address_field = " observed net address.";
	public static final String machine_id_field = " machine id.";
	public static final String os_id_field = " OS id.";

	public String net_kind;
	public String address;
	public String seen_address;
	public String machine_id;
	public String os_id;

	public tag_device() {
		init_tag_device();
		if (device_initer != null) {
			device_initer.fill_device(this);
		}
	}

	public tag_device(tag_device orig) {
		init_tag_device();
		if (orig == null) {
			return;
		}
		net_kind = orig.net_kind;
		address = orig.address;
		seen_address = orig.seen_address;
		machine_id = orig.machine_id;
		os_id = orig.os_id;
	}

	void init_tag_device() {
		net_kind = config.UNKNOWN_STR;
		address = config.UNKNOWN_STR;
		seen_address = config.UNKNOWN_STR;
		machine_id = config.UNKNOWN_STR;
		os_id = config.UNKNOWN_STR;
	}

	void filter_device_lines() {
		net_kind = parse.filter_string(net_kind);
		address = parse.filter_string(address);
		seen_address = parse.filter_string(seen_address);
		machine_id = parse.filter_string(machine_id);
		os_id = parse.filter_string(os_id);
	}

	List<String> get_device_lines(String title) {
		List<String> txt = new ArrayList<String>();
		add_device_lines_to(txt, title);
		return txt;
	}

	void add_device_lines_to(List<String> txt, String title) {
		filter_device_lines();

		// parse.add_next_title_to(txt, title);
		parse.add_next_field_to(txt, title, net_kind_field, net_kind);
		parse.add_next_field_to(txt, title, address_field, address);
		parse.add_next_field_to(txt, title, seen_address_field, seen_address);
		parse.add_next_field_to(txt, title, machine_id_field, machine_id);
		parse.add_next_field_to(txt, title, os_id_field, os_id);
		// txt.add(parse.end_of_title);

		parse.check_line_list(txt);
	}

	public void init_device_with(List<String> all_lines, String title) {
		parse.check_line_list(all_lines);

		ListIterator<String> it1 = all_lines.listIterator();
		init_device_with(it1, title);
	}

	public void init_device_with(ListIterator<String> it1, String title) {
		// parse.get_next_title_from(it1, title);
		net_kind = parse.get_next_field_from(it1, title, net_kind_field);
		address = parse.get_next_field_from(it1, title, address_field);
		seen_address = parse
				.get_next_field_from(it1, title, seen_address_field);
		machine_id = parse.get_next_field_from(it1, title, machine_id_field);
		os_id = parse.get_next_field_from(it1, title, os_id_field);
		// parse.get_next_end_of_title(it1);
	}

	// MACHINE_ID=
	// byte[] ni = NetworkInterface.getHardwareAddress();
	// for (int k = 0; k < mac.length; k++) {
	// System.out.format("%02X%s", mac[k], (i < mac.length - 1) ? "-" : "");
	// }
	// OS_ID=
	// System.getProperty("os.name")
}
