package emetcode.net.netmix.tcp_adapter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.net.netmix.bad_netmix;
import emetcode.net.netmix.config;
import emetcode.net.netmix.nx_connection;
import emetcode.net.netmix.nx_peer;
import emetcode.net.netmix.locale.L;
import emetcode.util.devel.logger;

public class nx_tcp_peer extends nx_peer {

	ServerSocket srv_socket;

	InetSocketAddress tcp_destination;

	// use context.make_peer insted
	protected nx_tcp_peer() {
		init_nx_peer();

		srv_socket = null;
		tcp_destination = null;
	}

	public void init_peer(String local_nm, boolean is_local, boolean is_srv) {
		is_server = is_srv;
		if (local_nm == null) {
			if (is_local) {
				throw new bad_netmix(2, L.invalid_tcp_local_peer);
			} else {
				throw new bad_netmix(2, L.invalid_tcp_remote_peer);
			}
		}

		int col_pos1 = local_nm.indexOf(PORT_SEP);
		int col_pos2 = local_nm.indexOf(PORT_SEP, col_pos1 + 1);

		local_name = local_nm;
		if (col_pos2 != -1) {
			// local_name = local_nm.substring(0, col_pos2);
		}
		str_description = local_name;

		String host = str_description.substring(0, col_pos1);
		int port = 0;

		if (col_pos2 == -1) {
			port = Integer.parseInt(str_description.substring(col_pos1 + 1));
		} else {
			port = Integer.parseInt(str_description.substring(col_pos1 + 1,
					col_pos2));
		}

		srv_socket = null;
		if (is_local && is_srv) {
			try {
				srv_socket = new ServerSocket(port);
			} catch (IOException ex) {
				logger.error(ex, "Could not create ServerSocket port=" + port);
			}
		}

		tcp_destination = new InetSocketAddress(host, port);
		// tcp_destination = InetSocketAddress.createUnresolved(host, port);

		if (!is_valid()) {
			if (is_local) {
				throw new bad_netmix(2, L.invalid_tcp_local_peer);
			} else {
				throw new bad_netmix(2, L.invalid_tcp_remote_peer);
			}
		}
	}

	public boolean is_valid() {
		boolean c1 = (context != null);
		boolean c2 = (tcp_destination != null);
		boolean c3 = (str_description != null);
		return (c1 && c2 && c3);
	}

	public void init_local_peer(String local_nm, key_owner owr,
			boolean can_accept) {
		init_peer(local_nm, true, can_accept);
		set_owner(owr);
		is_remote = false;

		if (config.DEBUG) {
			logger.debug("Inited LOCAL tcp destination=");
			logger.debug(str_description);
			logger.debug("_________________________________________________________");
		}
	}

	public void init_remote_peer(String local_nm, String descrip) {
		init_peer(local_nm, false, false);
		is_remote = true;

		if (config.DEBUG) {
			logger.debug("Inited REMOTE tcp destination=");
			logger.debug(str_description);
			logger.debug("_________________________________________________________");
		}
	}

	InetSocketAddress get_local_destination() {
		return null;
	}

	public nx_connection connect_to(nx_peer dest) {
		nx_tcp_peer pp = (nx_tcp_peer) (dest);
		if (!pp.is_remote_peer()) {
			throw new bad_netmix(2, String.format(
					L.cannot_connect_to_same_tcp_peer, dest.get_local_name()));
		}
		try {
			if (config.DEBUG) {
				logger.debug("Connecting to.");
				logger.debug(pp.tcp_destination.toString());
				logger.debug("__________________________________");
			}

			nx_tcp_connection conn = new nx_tcp_connection();
			conn.tcp_socket = new Socket();
			conn.tcp_socket.connect(pp.tcp_destination,
					config.TCP_CONNECT_TIMEOUT_MILLIS);
			conn.local_peer = this;
			conn.remote_peer = pp;
			conn.init_streams();
			return conn;
		} catch (IOException ex) {
			if (config.DEBUG) {
				logger.debug("General read/write-exception!");
			}
		}
		return null;
	}

	public nx_connection connect_to(String dest_descrip) {
		nx_tcp_peer dest = new nx_tcp_peer();
		dest.set_context(context);
		dest.init_remote_peer(dest_descrip, null);
		return connect_to(dest);
	}

	public void kill_accept() {
		if (srv_socket == null) {
			return;
		}
		try {
			srv_socket.close();
			srv_socket = null;
		} catch (IOException ee) {
			throw new bad_netmix(2, L.cannot_close_tcp_server_socket);
		}
	}

	public nx_connection accept() {
		if (srv_socket == null) {
			return null;
		}
		try {
			if (config.DEBUG) {
				logger.debug("ACCEPTING CALLS TCP_ADDR");
				logger.debug(tcp_destination.toString());
				logger.debug("__________________________________");
			}

			set_server_started();
			
			nx_tcp_connection conn = new nx_tcp_connection();
			conn.tcp_socket = srv_socket.accept();
			conn.local_peer = this;

			nx_tcp_peer dest = new nx_tcp_peer();
			dest.set_context(context);
			dest.init_remote_peer(conn.tcp_socket.getRemoteSocketAddress()
					.toString(), null);

			conn.remote_peer = dest;
			conn.init_streams();
			return conn;
		} catch (IOException ex) {
			if (config.DEBUG) {
				logger.debug("General read/write-exception!");
			}
		}
		return null;
	}

	public boolean can_accept() {
		return ((srv_socket != null) && !srv_socket.isClosed());
	}

}
