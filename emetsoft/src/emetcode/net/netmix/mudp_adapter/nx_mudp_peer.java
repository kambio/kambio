package emetcode.net.netmix.mudp_adapter;

import java.net.InetSocketAddress;

import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.net.mudp.mudp_connection;
import emetcode.net.mudp.mudp_manager;
import emetcode.net.mudp.mudp_peer;
import emetcode.net.mudp.req_conn;
import emetcode.net.netmix.bad_netmix;
import emetcode.net.netmix.nx_conn_id;
import emetcode.net.netmix.nx_connection;
import emetcode.net.netmix.nx_peer;
import emetcode.net.netmix.nx_responder;
import emetcode.net.netmix.locale.L;
import emetcode.util.devel.dbg_slow;
import emetcode.util.devel.logger;

public class nx_mudp_peer extends nx_peer {

	static final boolean IN_DEBUG_1 = false;
	static final boolean IN_DEBUG_2 = false;
	static final boolean IN_DEBUG_3 = false; // kill_accept
	static final boolean IN_DEBUG_4 = false; // slow respond

	mudp_connection udp_srv_conn;
	mudp_peer peer;
	long sub_port;

	nx_responder the_responder;

	// use context.make_peer insted
	protected nx_mudp_peer() {
		init_nx_peer();

		udp_srv_conn = null;
		peer = null;
		sub_port = 0;

		the_responder = null;
	}

	public static InetSocketAddress get_address(String local_nm) {
		int col_pos1 = local_nm.indexOf(PORT_SEP);
		int col_pos2 = local_nm.indexOf(PORT_SEP, col_pos1 + 1);

		String host = null;
		int udp_port = 0;

		try {
			String ss = "";
			if (col_pos1 != -1) {
				ss = local_nm.substring(0, col_pos1);
			} else {
				ss = local_nm;
			}
			if (!ss.isEmpty()) {
				host = ss;
			}
			if (col_pos2 != -1) {
				ss = local_nm.substring(col_pos1 + 1, col_pos2);
			} else {
				ss = local_nm.substring(col_pos1 + 1);
			}
			if (!ss.isEmpty()) {
				udp_port = Integer.parseInt(ss);
			}
		} catch (Exception ex) {
			logger.debug("nx_BAD_MUDP_DESCRIPTION=" + local_nm);
		}
		InetSocketAddress addr = null;
		if ((host != null) && (udp_port != 0)) {
			addr = new InetSocketAddress(host, udp_port);
		}
		return addr;
	}

	public void init_peer(String local_nm, boolean is_local, boolean is_srv) {
		is_server = is_srv;
		if (local_nm == null) {
			if (is_local) {
				throw new bad_netmix(2, String.format(
						L.invalid_mudp_local_peer, "null"));
			} else {
				throw new bad_netmix(2, String.format(
						L.invalid_mudp_remote_peer, "null"));
			}
		}

		local_name = new String(local_nm);
		str_description = new String(local_nm);

		int col_pos = str_description.lastIndexOf(PORT_SEP);
		int col_pos2 = str_description.lastIndexOf(PORT_SEP, col_pos - 1);

		mudp_manager mgr = get_conn_mgr();
		InetSocketAddress mgr_addr = mgr.get_socket_address();
		if (is_local) {
			InetSocketAddress descr_addr = get_address(local_name);
			if ((descr_addr != null) && !mgr_addr.equals(descr_addr)) {
				throw new bad_netmix(2, String.format(
						L.invalid_mudp_local_peer, str_description)
						+ "\n"
						+ mgr_addr + "\n" + descr_addr);
			}
		}

		String host = mgr_addr.getHostName();
		int udp_port = mgr_addr.getPort();
		sub_port = 0;

		try {
			String ss = str_description.substring(0, col_pos2);
			if (!ss.isEmpty()) {
				host = ss;
			}
			ss = str_description.substring(col_pos2 + 1, col_pos);
			if (!ss.isEmpty()) {
				udp_port = Integer.parseInt(ss);
			}
			ss = str_description.substring(col_pos + 1);
			if (!ss.isEmpty()) {
				sub_port = Long.parseLong(ss);
			}
		} catch (Exception ex) {
			logger.debug("nx_BAD_UDP_DESCRIPTION=" + str_description);
		}

		if (is_srv && (sub_port == 0)) {
			return;
		}
		if (udp_port == 0) {
			return;
		}

		// logger.debug("UDP_HOST=" + host + " UDP_PORT=" + udp_port);
		InetSocketAddress addr = new InetSocketAddress(host, udp_port);
		// logger.debug("UDP_ADDR_ADDR=" + addr.getAddress() + " UDP_ADDR_PORT="
		// + addr.getPort());

		peer = new mudp_peer(addr);

		int MAX_TRY_MAKE_SERVER = 10;

		udp_srv_conn = null;
		if (is_local && is_srv) {
			for (int aa = 0; aa < MAX_TRY_MAKE_SERVER; aa++) {
				udp_srv_conn = get_conn_mgr().make_server_connection(sub_port);
				if (udp_srv_conn != null) {
					break;
				}
				Thread.yield();
			}
			if (udp_srv_conn == null) {
				logger.debug("CANNOT_CREATE_MUDP_SERVER_PEER="
						+ str_description);
			}
		}

		the_responder = null;

		if (!is_valid()) {
			if (is_local) {
				throw new bad_netmix(2, String.format(
						L.invalid_mudp_local_peer, str_description));
			} else {
				throw new bad_netmix(2, String.format(
						L.invalid_mudp_remote_peer, str_description));
			}
		}
	}

	mudp_manager get_conn_mgr() {
		nx_mudp_context ctx = (nx_mudp_context) get_context();
		return ctx.udp_mgr;
	}

	public boolean is_valid() {
		boolean c1 = (context != null);
		boolean c2 = (peer != null);
		boolean c3 = (sub_port != 0);
		boolean c4 = (str_description != null);
		return (c1 && c2 && c3 && c4);
	}

	public void init_local_peer(String local_nm, key_owner owr,
			boolean can_accept) {
		init_peer(local_nm, true, can_accept);
		set_owner(owr);
		is_remote = false;

		if (IN_DEBUG_2) {
			logger.debug("nx_Inited_LOCAL_mudp destination=");
			logger.debug(str_description);
			logger.debug("_________________________________________________________");
		}
	}

	public void init_remote_peer(String local_nm, String descrip) {
		init_peer(local_nm, false, false);
		is_remote = true;

		if (IN_DEBUG_2) {
			logger.debug("nx_Inited_REMOTE_mudp destination=");
			logger.debug(str_description);
			logger.debug("_________________________________________________________");
		}
	}

	/*
	 * private void init_remote_peer(InetSocketAddress sok_addr, long
	 * the_conn_id) { peer = new mudp_peer(sok_addr); conn_id = the_conn_id;
	 * is_remote = true; }
	 */

	public nx_connection connect_to(nx_peer dest) {
		nx_mudp_peer pp = (nx_mudp_peer) (dest);
		if (!pp.is_remote_peer()) {
			throw new bad_netmix(2, String.format(
					L.cannot_connect_to_same_mudp_peer, dest.get_local_name()));
		}
		if (IN_DEBUG_2) {
			logger.debug("nx_mudp_Connecting to.");
			logger.debug(pp.get_description());
			logger.debug("__________________________________");
		}

		nx_mudp_connection connec = new nx_mudp_connection();
		connec.udp_conn = get_conn_mgr().connect(pp.peer, pp.sub_port);
		connec.local_peer = this;
		connec.remote_peer = pp;
		connec.init_streams();
		return connec;
	}

	public nx_connection connect_to(String dest_descrip) {
		nx_mudp_peer dest = new nx_mudp_peer();
		dest.set_context(context);
		dest.init_remote_peer(dest_descrip, null);
		return connect_to(dest);
	}

	public void kill_accept() {
		if (udp_srv_conn == null) {
			if (IN_DEBUG_3) {
				// String stk = logger.get_stack_str();
				logger.debug("kill_accept. udp_srv_conn == null.");
			}
			return;
		}
		udp_srv_conn.close();
		udp_srv_conn = null;
	}

	public nx_connection accept() {
		if (udp_srv_conn == null) {
			throw new bad_netmix(2, L.null_mudp_server_socket);
		}
		if (IN_DEBUG_2) {
			logger.debug("nx_ACCEPTING CALLS MUDP_ADDR");
			logger.debug(str_description);
			logger.debug("__________________________________");
		}
	
		set_server_started();

		nx_mudp_connection conn = new nx_mudp_connection();
		conn.udp_conn = udp_srv_conn.accept();
		if (conn.udp_conn == null) {
			return null;
		}

		conn.local_peer = this;

		nx_mudp_peer dest = new nx_mudp_peer();
		dest.set_context(context);

		mudp_peer rem_peer = conn.udp_conn.get_remote_peer();

		String rem_descr = rem_peer.get_ip_descr() + PORT_SEP
				+ conn.udp_conn.get_conn_id();
		if (IN_DEBUG_1) {
			logger.debug("nx_accepted_MUDP_conn from=" + rem_descr);
		}

		dest.init_remote_peer(rem_descr, null);

		conn.remote_peer = dest;
		conn.init_streams();

		return conn;
	}

	public boolean can_accept() {
		return ((udp_srv_conn != null) && !udp_srv_conn.is_closed());
	}

	public void init_responder(nx_responder rr) {
		if (udp_srv_conn != null) {
			throw new bad_netmix(2);
		}
		if (the_responder != null) {
			throw new bad_netmix(2);
		}
		if (rr == null) {
			throw new bad_netmix(2);
		}
		if (IN_DEBUG_1) {
			logger.debug("nx_initing_MUDP_responder conn_id=" + sub_port);
		}
		udp_srv_conn = get_conn_mgr().make_responder_connection(sub_port);
		if (udp_srv_conn != null) {
			if (!udp_srv_conn.is_server()) {
				throw new bad_netmix(2);
			}
		}
		the_responder = rr;
	}

	public boolean can_respond() {
		return can_accept();
	}

	public void kill_responder() {
		kill_accept();
	}

	public void respond() {
		if (!can_respond()) {
			throw new bad_netmix(2);
		}
		nx_mudp_connection cnn = (nx_mudp_connection) accept();
		if (cnn != null) {
			dbg_slow sl1 = null;
			if (IN_DEBUG_4) {
				sl1 = new dbg_slow();
			}
			
			req_conn rco = (req_conn) cnn.udp_conn;
			long cokey_id = rco.get_cokey_id();
			byte[] arr_req = cnn.udp_conn.get_request();
			nx_conn_id curr_coid = null;
			if (cokey_id != 0) {
				curr_coid = new nx_conn_id(cokey_id);
			}
			byte[] resp = the_responder.get_response(cnn.get_remote_peer(),
					curr_coid, arr_req);
			cnn.udp_conn.respond(resp);
			
			if (IN_DEBUG_4) {
				sl1.log_if_slow("SLOW_RESPOND !!!!!");
			}
		}
	}

	public nx_connection get_requester(nx_peer dest, nx_conn_id req_conn) {
		nx_mudp_peer pp = (nx_mudp_peer) (dest);
		if (!pp.is_remote_peer()) {
			throw new bad_netmix(2, String.format(
					L.cannot_connect_to_same_mudp_peer, dest.get_local_name()));
		}
		long cokey_id = 0;
		if (req_conn != null) {
			cokey_id = req_conn.as_long();
		}
		mudp_manager mgr = pp.get_conn_mgr();
		mudp_connection cli = mgr.make_requester_connection(pp.peer,
				pp.sub_port, cokey_id);

		nx_mudp_connection connec = new nx_mudp_connection();
		connec.udp_conn = cli;
		connec.local_peer = this;
		connec.remote_peer = pp;
		connec.init_streams();
		return connec;
	}
}
