package emetcode.net.netmix.mudp_adapter;

import java.io.File;
import java.net.InetSocketAddress;

import emetcode.net.mudp.mudp_manager;
import emetcode.net.netmix.bad_netmix;
import emetcode.net.netmix.nx_context;
import emetcode.net.netmix.nx_dir_base;
import emetcode.net.netmix.nx_peer;
import emetcode.util.devel.net_funcs;

public class nx_mudp_context extends nx_context {

	public mudp_manager udp_mgr;
	
	public nx_mudp_context(nx_dir_base bb, InetSocketAddress sok_addr) {
		super(bb);
		if((bb != null) && (bb.get_net_type() != net_funcs.MUDP_NET)){
			throw new bad_netmix(2);
		}
		udp_mgr = new mudp_manager(sok_addr);
		udp_mgr.start_service();
	}

	public nx_mudp_context(File r_dir, InetSocketAddress sok_addr) {
		super(r_dir, net_funcs.MUDP_NET);
		udp_mgr = new mudp_manager(sok_addr);
		udp_mgr.start_service();
	}

	public nx_peer make_peer() {
		nx_peer pp = new nx_mudp_peer();
		pp.set_context(this);
		return pp;
	}

	public void finish_context() {
		udp_mgr.complete_service();
	}

	public void stop_context() {
		udp_mgr.stop_service();
	}
}
