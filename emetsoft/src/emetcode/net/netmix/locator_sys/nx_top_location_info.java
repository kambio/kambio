package emetcode.net.netmix.locator_sys;

import emetcode.crypto.bitshake.utils.convert;
import emetcode.net.netmix.bad_netmix;
import emetcode.util.devel.logger;

public class nx_top_location_info {
	public static final boolean IN_DEBUG_1 = false;

	String addr;
	long time_first_conn;
	long num_conn;
	long time_last_conn;

	public nx_top_location_info(String dat) {
		init_nx_top_location_info(dat);
	}

	void init_nx_top_location_info(String dat) {
		addr = "";
		time_first_conn = 0;
		num_conn = 0;
		time_last_conn = 0;
		
		if (dat != null) {
			init_with_hex_str(dat);
		}
	}

	private void init_with_hex_str(String dat) {
		if(IN_DEBUG_1){
			logger.info("init_top_loc=" + dat);
		}
		
		int col_pos1 = dat.indexOf('|');
		int col_pos2 = dat.indexOf('|', col_pos1 + 1);
		int col_pos3 = dat.indexOf('|', col_pos2 + 1);

		if (col_pos1 == -1) {
			throw new bad_netmix(2);
		}
		if (col_pos2 == -1) {
			throw new bad_netmix(2);
		}
		if (col_pos3 == -1) {
			throw new bad_netmix(2);
		}
		if ((col_pos2 + 1) >= dat.length()) {
			throw new bad_netmix(2);
		}
		if ((col_pos3 + 1) >= dat.length()) {
			throw new bad_netmix(2);
		}

		String hex_addr_str = null;
		String tm_first_cnn_str = null;
		String n_cnn_str = null;
		String tm_last_cnn_str = null;
		try {
			hex_addr_str = dat.substring(0, col_pos1);
			tm_first_cnn_str = dat.substring(col_pos1 + 1, col_pos2);
			n_cnn_str = dat.substring(col_pos2 + 1, col_pos3);
			tm_last_cnn_str = dat.substring(col_pos3 + 1);

			if (!hex_addr_str.isEmpty()) {
				addr = convert.hex_string_to_string(hex_addr_str);
			}
			if (!tm_first_cnn_str.isEmpty()) {
				time_first_conn = Long.parseLong(tm_first_cnn_str);
			}
			if (!n_cnn_str.isEmpty()) {
				num_conn = Long.parseLong(n_cnn_str);
			}
			if (!tm_last_cnn_str.isEmpty()) {
				time_last_conn = Long.parseLong(tm_last_cnn_str);
			}
		} catch (Exception ex) {
			if(IN_DEBUG_1){
				logger.info("hex_addr_str=" + hex_addr_str + "\n"
						+ "tm_first_cnn_str=" + tm_first_cnn_str + "\n"
						+ "n_cnn_str=" + n_cnn_str + "\n"
						+ "tm_last_cnn_str=" + tm_last_cnn_str + "\n");
			}
			throw new bad_netmix(2, ex.toString());
		}
	}

	public String to_hex_str() {
		String out_str = convert.string_to_hex_string(addr) + "|" + time_first_conn
				+ "|" + num_conn + "|" + time_last_conn;
		if(IN_DEBUG_1){
			logger.info("top_loc_hex=" + out_str + " addr=" + addr);
		}
		return out_str;
	}

	public String to_print_str() {
		String out_str = addr + "|" + time_first_conn
				+ "|" + num_conn + "|" + time_last_conn;
		return out_str;
	}

	public String toString() {
		return to_print_str();
	}
	
}
