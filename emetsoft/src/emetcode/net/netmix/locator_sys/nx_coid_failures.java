package emetcode.net.netmix.locator_sys;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;

import emetcode.crypto.bitshake.utils.convert;
import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.crypto.bitshake.utils.mem_file;
import emetcode.net.netmix.config;
import emetcode.net.netmix.nx_conn_id;
import emetcode.net.netmix.nx_dir_base;

public class nx_coid_failures {
	
	static int ARR_SZ = 13;
	
	public long[] failures;
	
	public nx_coid_failures(){
		failures = new long[ARR_SZ];
		Arrays.fill(failures, 0);
	}	

	public void read_failures(File ff, key_owner owr){
		byte[] all_bts = mem_file.concurrent_read_encrypted_bytes(ff, owr);
		if(all_bts == null){
			return;
		}
		failures = convert.to_long_array(all_bts);
	}

	public void write_failures(File ff, key_owner owr){
		byte[] all_bts = convert.to_byte_array(failures);
		mem_file.concurrent_write_encrypted_bytes(ff, owr, all_bts);
	}

	void set_last_failure_tm(long tm){
		failures[0] = tm;
	}
	
	long get_last_failure_tm(){
		return failures[0];
	}
	
	int get_last_month(){
		int lst_mth = get_month(get_last_failure_tm());
		return lst_mth;
	}
	
	void reset_month(int idx){
		failures[idx] = 0;
	}
	
	void inc_month(int idx){
		failures[idx]++;
	}
	
	public static int get_month(long tm){
		int idx = -1;
		Calendar cc = Calendar.getInstance();
		cc.setTimeInMillis(tm);
		idx = cc.get(Calendar.MONTH) + 1;
		return idx;
	}
		
	public static void inc_num_failed(File ff, key_owner owr){
		long curr_tm = System.currentTimeMillis();
		inc_num_failed_at(ff, owr, curr_tm);
	}
	
	public static void inc_num_failed_at(File ff, key_owner owr, long curr_tm){
		nx_coid_failures all_fail = new nx_coid_failures();
		all_fail.read_failures(ff, owr);
		
		int lst_mth = all_fail.get_last_month();
		int curr_mth = get_month(curr_tm);
		if(lst_mth != curr_mth){
			all_fail.reset_month(curr_mth);
		}
		all_fail.inc_month(curr_mth);
		all_fail.set_last_failure_tm(curr_tm);

		all_fail.write_failures(ff, owr);
	}
	
	public static File get_remote_year_failures_file(nx_dir_base dir_b,
			nx_conn_id the_coid) {
		File rem_dd = dir_b.get_remote_nx_dir(the_coid);
		File fali_ff = new File(rem_dd, config.YEAR_FAILURES_FNAM);
		return fali_ff;
	}

	public long calc_tot_failures() {
		long tot_fail = 0;
		for(int aa = 1; aa < failures.length; aa++){
			tot_fail += failures[aa];
		}
		return tot_fail;
	}
	
}
