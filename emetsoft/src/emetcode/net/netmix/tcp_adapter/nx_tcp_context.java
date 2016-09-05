package emetcode.net.netmix.tcp_adapter;

import java.io.File;

import emetcode.net.netmix.bad_netmix;
import emetcode.net.netmix.nx_context;
import emetcode.net.netmix.nx_dir_base;
import emetcode.net.netmix.nx_peer;
import emetcode.util.devel.net_funcs;

public class nx_tcp_context extends nx_context {

	public nx_tcp_context(nx_dir_base bb) {
		super(bb);
		if((bb != null) && (bb.get_net_type() != net_funcs.TCP_NET)){
			throw new bad_netmix(2);
		}
	}

	public nx_tcp_context(File r_dir) {
		super(r_dir, net_funcs.TCP_NET);
	}

	public nx_peer make_peer() {
		nx_peer pp = new nx_tcp_peer();
		pp.set_context(this);
		return pp;
	}

}
