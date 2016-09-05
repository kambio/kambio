package emetcode.net.netmix;

import java.io.File;

import emetcode.crypto.bitshake.utils.file_funcs;
import emetcode.util.devel.thread_funcs;

public class nx_context {

	protected nx_dir_base b_dir;

	public nx_context(nx_dir_base bb) {
		b_dir = bb;
	}
	
	public nx_context(File r_dir, int the_type) {
		File nx_dd = new File(r_dir, config.DN_NETMIX_DIR);
		init_nx_context(nx_dd, the_type);
	}
	
	void init_nx_context(File r_dir, int the_type){
		b_dir = new nx_dir_base(r_dir, the_type);
	}

	public int get_net_type() {
		if(b_dir == null){
			throw new bad_netmix(2);
		}
		return b_dir.get_net_type();
	}

//	private void set_dir_base(nx_dir_base base) {
//		b_dir = base;
//	}
	
	private nx_dir_base get_dir_base() {
		if(b_dir == null){
			throw new bad_netmix(2);
		}		
		return b_dir;
	}
	
	public File get_net_dir(String sufix_dir) {
		return file_funcs.get_dir(get_dir_base().get_netmix_base_dir(), sufix_dir);
	}

	public nx_peer make_peer(){
		throw new bad_netmix(2);
	}

	public static Thread start_thread(String nam, Runnable code, boolean as_daemon) {
		return thread_funcs.start_thread(nam, code, as_daemon);
	}

	public void finish_context() {
	}

	public void stop_context() {
	}
}
