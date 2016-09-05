package emetcode.net.netmix.locator_sys;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import emetcode.crypto.bitshake.utils.convert;
import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.crypto.bitshake.utils.mem_file;
import emetcode.net.netmix.bad_netmix;
import emetcode.net.netmix.config;
import emetcode.net.netmix.nx_conn_id;
import emetcode.net.netmix.nx_dir_base;

public class nx_top_locations {

	static final int MAX_NUM_TOP = 10;

	Map<String, nx_top_location_info> all_top;

	public nx_top_locations() {
		all_top = null;
	}

	public void read_top_loc(File ff, key_owner owr) {
		byte[] all_bts = mem_file.concurrent_read_encrypted_bytes(ff, owr);
		List<String> all_str = convert.bytes_to_string_list(all_bts);
		all_top = string_list_to_map(all_str);
	}

	public void write_top_loc(File ff, key_owner owr) {
		List<String> all_str = map_to_string_list(all_top);
		byte[] all_bts = convert.string_list_to_bytes(all_str);
		mem_file.concurrent_write_encrypted_bytes(ff, owr, all_bts);
	}

	public List<String> get_print_list(){
		List<String> all_str = new ArrayList<String>();
		if (all_top == null) {
			return all_str;
		}
		for (Map.Entry<String, nx_top_location_info> map_val : all_top
				.entrySet()) {
			nx_top_location_info top_loc = map_val.getValue();
			all_str.add(top_loc.to_print_str());
		}
		return all_str;
	}
	
	public List<String> get_locations_list(){
		List<String> all_str = new ArrayList<String>();
		if (all_top == null) {
			return all_str;
		}
		for (Map.Entry<String, nx_top_location_info> map_val : all_top
				.entrySet()) {
			nx_top_location_info top_loc = map_val.getValue();
			all_str.add(top_loc.addr);
		}
		return all_str;
	}
	
	private static List<String> map_to_string_list(
			Map<String, nx_top_location_info> the_map) {
		List<String> all_str = new ArrayList<String>();
		if (the_map == null) {
			return all_str;
		}
		for (Map.Entry<String, nx_top_location_info> map_val : the_map
				.entrySet()) {
			nx_top_location_info top_loc = map_val.getValue();
			all_str.add(top_loc.to_hex_str());
		}

		return all_str;
	}

	private static Map<String, nx_top_location_info> string_list_to_map(
			List<String> all_str) {
		Map<String, nx_top_location_info> the_amp = new TreeMap<String, nx_top_location_info>();
		if (all_str == null) {
			return the_amp;
		}
		for (String the_str : all_str) {
			nx_top_location_info top_loc = new nx_top_location_info(the_str);
			the_amp.put(top_loc.addr, top_loc);
		}
		return the_amp;
	}

	nx_top_location_info get_loc(String the_addr) {
		nx_top_location_info top_loc = all_top.get(the_addr);
		if (top_loc == null) {
			if (all_top.size() >= MAX_NUM_TOP) {
				remove_oldest();
			}
			nx_top_location_info n_loc = new nx_top_location_info(null);
			n_loc.addr = the_addr;
			all_top.put(n_loc.addr, n_loc);
			top_loc = all_top.get(the_addr);
		}
		if (top_loc == null) {
			throw new bad_netmix(2);
		}
		return top_loc;
	}

	private void remove_oldest() {
		nx_top_location_info oldest = null;
		for (Map.Entry<String, nx_top_location_info> map_val : all_top
				.entrySet()) {
			nx_top_location_info loc = map_val.getValue();
			if (oldest == null) {
				oldest = loc;
				continue;
			}
			if (loc.time_last_conn < oldest.time_last_conn) {
				oldest = loc;
			}
		}
		if (oldest != null) {
			all_top.remove(oldest.addr);
		}
	}

	public static void inc_num_conn_for(File ff, key_owner owr, String the_addr) {
		nx_top_locations all_loc = new nx_top_locations();
		all_loc.read_top_loc(ff, owr);

		nx_top_location_info top_loc = all_loc.get_loc(the_addr);
		top_loc.num_conn++;
		if (top_loc.num_conn == 1) {
			top_loc.time_first_conn = System.currentTimeMillis();
		}
		top_loc.time_last_conn = System.currentTimeMillis();
		all_loc.write_top_loc(ff, owr);
	}

	public static File get_remote_top_locations_file(nx_dir_base dir_b,
			nx_conn_id the_coid) {
		File rem_dd = dir_b.get_remote_nx_dir(the_coid);
		File top_ff = new File(rem_dd, config.TOP_LOCATIONS_FNAM);
		return top_ff;
	}

}
