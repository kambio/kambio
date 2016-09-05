package emetcode.net.netmix.mudp_adapter;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import emetcode.net.mudp.mudp_connection;
import emetcode.net.mudp.req_conn;
import emetcode.net.netmix.bad_netmix;
import emetcode.net.netmix.nx_connection;
import emetcode.net.netmix.nx_peer;
import emetcode.net.netmix.locale.L;
import emetcode.util.devel.logger;

public class nx_mudp_connection extends nx_connection {

	static final boolean IN_DEBUG_1 = false; // stop_net_conn
	static final boolean IN_DEBUG_2 = false; // no coid requests

	mudp_connection udp_conn;
	nx_peer local_peer;
	nx_peer remote_peer;

	public nx_mudp_connection() {
		udp_conn = null;
		local_peer = null;
		remote_peer = null;
	}

	public nx_peer get_local_peer() {
		if (local_peer == null) {
			throw new bad_netmix(2, L.mudp_local_peer_is_null);
		}
		return local_peer;
	}

	public nx_peer get_remote_peer() {
		if (remote_peer == null) {
			throw new bad_netmix(2, L.mudp_remote_peer_is_null);
		}
		return remote_peer;
	}

	public InputStream get_InputStream() {
		return null;
	}

	public OutputStream get_OutputStream() {
		return null;
	}

	public void stop_net_connection() {
		if (IN_DEBUG_1) {
			logger.debug("STOPPING_CONN=" + this);
		}
		udp_conn.stop_conn();
		if (IN_DEBUG_1) {
			logger.debug("STOPPED_CONN=" + this);
		}
	}

	public void close_net_connection() {
		udp_conn.close();
	}

	public boolean is_closed() {
		return udp_conn.is_closed();
	}

	public void send_byte_array(byte[] msg) {
		if (!is_valid()) {
			throw new bad_netmix(2, L.cannot_send_bytes);
		}
		udp_conn.send(msg);
	}

	public byte[] recv_byte_array() {
		if (!is_valid()) {
			throw new bad_netmix(2, L.cannot_recv_bytes);
		}
		return udp_conn.receive();
	}

	public boolean is_valid() {
		boolean c1 = (udp_conn != null);
		boolean c2 = (local_peer != null);
		boolean c3 = (remote_peer != null);
		return (c1 && c2 && c3);
	}

	public String toString() {
		return udp_conn.toString();
	}

	public byte[] request(byte[] req_msg) {
		if(IN_DEBUG_2){
			if(udp_conn instanceof req_conn){
				req_conn cnn = (req_conn)udp_conn;
				if(cnn.get_cokey_id() == 0){
					logger.info("NO_COID_REQUEST conn=" + cnn);
				}
			}
		}
		return udp_conn.request(req_msg);
	}

	public void set_req_timeout(long tm_out, TimeUnit uu) {
		udp_conn.set_req_timeout(tm_out, uu);
	}
}
