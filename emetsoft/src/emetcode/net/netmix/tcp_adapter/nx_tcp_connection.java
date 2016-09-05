package emetcode.net.netmix.tcp_adapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import emetcode.net.netmix.bad_netmix;
import emetcode.net.netmix.config;
import emetcode.net.netmix.nx_connection;
import emetcode.net.netmix.nx_peer;
import emetcode.net.netmix.locale.L;
import emetcode.util.devel.logger;

public class nx_tcp_connection extends nx_connection {

	Socket tcp_socket;
	nx_peer local_peer;
	nx_peer remote_peer;

	public nx_tcp_connection() {
		tcp_socket = null;
		local_peer = null;
		remote_peer = null;
	}

	public nx_peer get_local_peer() {
		if (local_peer == null) {
			throw new bad_netmix(2, L.tcp_local_peer_is_null);
		}
		return local_peer;
	}

	public nx_peer get_remote_peer() {
		if (remote_peer == null) {
			throw new bad_netmix(2, L.tcp_remote_peer_is_null);
		}
		return remote_peer;
	}

	public InputStream get_InputStream() {
		try {
			return tcp_socket.getInputStream();
		} catch (IOException ex1) {
			throw new bad_netmix(2, L.cannot_get_tcp_input_stream);
		}
	}

	public OutputStream get_OutputStream() {
		try {
			return tcp_socket.getOutputStream();
		} catch (IOException ex1) {
			throw new bad_netmix(2, L.cannot_get_tcp_output_stream);
		}
	}

	public void close_net_connection() {
		stop_net_connection();
	}
	
	public void stop_net_connection() {
		try {
			tcp_socket.close();
		} catch (IOException ex1) {
			if (config.DEBUG) {
				logger.debug("IOException " + ex1.toString()
						+ ". Cannot tcp_socket.close().");
			}
		}
	}

	public boolean is_closed() {
		return tcp_socket.isClosed();
	}

}
