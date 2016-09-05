package emetcode.net.mudp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import emetcode.util.devel.logger;

public class conns_local_peer {
	static final boolean IN_DEBUG_1 = false;
	static final boolean IN_DEBUG_2 = false;
	static final boolean IN_DEBUG_3 = true; // failed to get run lock

	private static final int SECS_TIMEOUT_TRYLOCK = 30;

	mudp_manager mgr;
	mudp_peer local_peer;

	private final Map<mudp_peer, conns_remote_peer> all_remote;
	private final ReentrantReadWriteLock rw_lk;
	private final Lock r_lk;
	private final Lock w_lk;

	conns_local_peer(mudp_manager the_mgr) {
		mgr = the_mgr;
		mudp_peer loc_peer = new mudp_peer(mgr.get_socket_address());
		local_peer = loc_peer;
		all_remote = new TreeMap<mudp_peer, conns_remote_peer>();
		rw_lk = new ReentrantReadWriteLock();
		r_lk = rw_lk.readLock();
		w_lk = rw_lk.writeLock();
	}

	private conns_remote_peer get_remote_peer(mudp_peer peer, Lock[] to_unlock) {
		if (to_unlock.length != 1) {
			throw new bad_udp(2);
		}

		conns_remote_peer peer_dest = null;

		to_unlock[0] = null;
		r_lk.lock();
		to_unlock[0] = r_lk;
		if (all_remote.containsKey(peer)) {
			peer_dest = all_remote.get(peer);
			return peer_dest;
		}
		to_unlock[0] = null;
		r_lk.unlock();

		w_lk.lock();
		to_unlock[0] = w_lk;
		if (all_remote.containsKey(peer)) {
			peer_dest = all_remote.get(peer);
		} else {
			peer_dest = new conns_remote_peer(this, peer);
			all_remote.put(peer, peer_dest);
			if (IN_DEBUG_1) {
				logger.debug("ADDED_REMOTE " + peer + " in " + local_peer);
			}
		}
		return peer_dest;
	}

	int all_remote_size() {
		r_lk.lock();
		try {
			return all_remote.size();
		} finally {
			r_lk.unlock();
		}
	}

	void remove_remote(mudp_peer rem_peer) {
		boolean got_lk = w_lk.tryLock();
		if (got_lk) {
			try {
				all_remote.remove(rem_peer);
				if (IN_DEBUG_1) {
					logger.debug("removed remote peer=" + rem_peer);
				}
				if (all_remote.isEmpty()) {
					if (IN_DEBUG_2) {
						logger.debug("OFFERING_ALL_REMOTE_FINISHED");
					}
					mgr.all_finished.offer(true);
				}
			} finally {
				w_lk.unlock();
			}
		}
	}

	msg_conn make_new_client_conn(mudp_peer peer, long server_id) {
		if (!mudp_connection.is_server(server_id)) {
			throw new bad_udp(2);
		}
		Lock[] to_unlock = new Lock[1];
		to_unlock[0] = null;

		msg_conn conn = null;
		try {
			conns_remote_peer peer_dest = get_remote_peer(peer, to_unlock);
			if (peer_dest != null) {
				conn = peer_dest.make_new_client_data_conn(server_id);
			}
		} finally {
			if (to_unlock[0] != null) {
				to_unlock[0].unlock();
			}
		}
		return conn;
	}

	msg_conn make_new_server_conn(long server_id) {
		if (!mudp_connection.is_server(server_id)) {
			throw new bad_udp(2);
		}
		Lock[] to_unlock = new Lock[1];
		to_unlock[0] = null;

		msg_conn conn = null;
		try {
			conns_remote_peer peer_dest = get_remote_peer(local_peer, to_unlock);
			if (peer_dest != null) {
				conn = peer_dest.make_new_server_data_conn(server_id);
			}
		} finally {
			if (to_unlock[0] != null) {
				to_unlock[0].unlock();
			}
		}
		return conn;
	}

	msg_conn make_new_server_conn(mudp_peer rem_peer, long n_cli_id) {
		if (mudp_connection.is_server(n_cli_id)) {
			throw new bad_udp(2);
		}
		Lock[] to_unlock = new Lock[1];
		to_unlock[0] = null;

		msg_conn conn = null;
		try {
			conns_remote_peer peer_dest = get_remote_peer(rem_peer, to_unlock);
			if (peer_dest != null) {
				conn = peer_dest.make_new_server_data_conn(n_cli_id);
			}
		} finally {
			if (to_unlock[0] != null) {
				to_unlock[0].unlock();
			}
		}
		return conn;
	}

	req_conn make_new_requester_conn(mudp_peer peer, long server_id,
			long cokey_id) {
		if (!mudp_connection.is_server(server_id)) {
			throw new bad_udp(2);
		}
		if ((cokey_id != 0) && mudp_connection.is_server(cokey_id)) {
			throw new bad_udp(2);
		}
		Lock[] to_unlock = new Lock[1];
		to_unlock[0] = null;

		req_conn conn = null;
		try {
			conns_remote_peer peer_dest = get_remote_peer(peer, to_unlock);
			if (peer_dest != null) {
				conn = peer_dest.make_new_requester_conn(server_id, cokey_id);
			}
		} finally {
			if (to_unlock[0] != null) {
				to_unlock[0].unlock();
			}
		}
		return conn;
	}

	req_conn make_new_responder_conn(long server_id) {
		if (!mudp_connection.is_server(server_id)) {
			throw new bad_udp(2);
		}
		Lock[] to_unlock = new Lock[1];
		to_unlock[0] = null;

		req_conn conn = null;
		try {
			conns_remote_peer peer_dest = get_remote_peer(local_peer, to_unlock);
			if (peer_dest != null) {
				conn = peer_dest.make_new_responder_conn(server_id);
			}
		} finally {
			if (to_unlock[0] != null) {
				to_unlock[0].unlock();
			}
		}
		return conn;
	}

	boolean has_conn(mudp_peer peer, long id) {
		boolean cc = false;
		r_lk.lock();
		try {
			if (all_remote.containsKey(peer)) {
				conns_remote_peer peer_dest = all_remote.get(peer);
				cc = peer_dest.has_conn(id);
			}
		} finally {
			r_lk.unlock();
		}
		return cc;
	}

	mudp_connection get_conn(mudp_peer rem_peer, long id) {
		mudp_connection conn = mudp_connection.FALSE_CONN;
		boolean got_lk = r_lk.tryLock();
		if (got_lk) {
			try {
				if (mudp_connection.is_server(id)) {
					rem_peer = local_peer;
				}
				conns_remote_peer peer_dest = all_remote.get(rem_peer);
				if (peer_dest != null) {
					conn = peer_dest.get_conn(id);
				} else {
					conn = null;
				}
			} finally {
				r_lk.unlock();
			}
		}
		return conn;
	}

	long run_all_data_conn() {
		List<mudp_connection> all_closed = new ArrayList<mudp_connection>();
		List<conns_remote_peer> all_to_remove = new ArrayList<conns_remote_peer>();

		all_closed.clear();
		long max_tm = -1;
		boolean got_lok = false;
		try {
			got_lok = r_lk.tryLock(SECS_TIMEOUT_TRYLOCK, TimeUnit.SECONDS);
		} catch (InterruptedException ex1) {
			if (IN_DEBUG_3) {
				logger.error(ex1, "CANNOT_GET_INTERNAL_MUDP_LOCK.");
			}
		}
		if (!got_lok) {
			return max_tm;
		}
		try {
			for (Map.Entry<mudp_peer, conns_remote_peer> pend : all_remote
					.entrySet()) {
				conns_remote_peer peer_dest = pend.getValue();
				long ss = peer_dest
						.run_all_data_conn(all_closed, all_to_remove);
				if (ss > max_tm) {
					max_tm = ss;
				}
			}
		} finally {
			r_lk.unlock();
			for (mudp_connection conn : all_closed) {
				conn.remove_me();
			}
			if (IN_DEBUG_2) {
				if (!all_to_remove.isEmpty()) {
					logger.debug("REMOVING_PEERS_sz=" + all_to_remove.size());
				}
			}
			for (conns_remote_peer to_remov : all_to_remove) {
				to_remov.remove_conn(null);
			}
		}
		return max_tm;
	}

	void stop_all_conn() {
		List<mudp_connection> all_conn = new ArrayList<mudp_connection>();
		List<conns_remote_peer> all_to_remove = new ArrayList<conns_remote_peer>();

		all_conn.clear();
		boolean got_lok = false;
		try {
			got_lok = r_lk.tryLock(SECS_TIMEOUT_TRYLOCK, TimeUnit.SECONDS);
		} catch (InterruptedException ex1) {
			if (IN_DEBUG_3) {
				logger.error(ex1, "CANNOT_GET_INTERNAL_MUDP_LOCK.");
			}
		}
		if (!got_lok) {
			return;
		}
		try {
			for (Map.Entry<mudp_peer, conns_remote_peer> pend : all_remote
					.entrySet()) {
				conns_remote_peer peer_dest = pend.getValue();
				peer_dest.add_all_conn(all_conn, all_to_remove);
			}
		} finally {
			r_lk.unlock();
			for (mudp_connection conn : all_conn) {
				conn.stop_conn();
				conn.remove_me();
			}
			for (conns_remote_peer to_remov : all_to_remove) {
				to_remov.remove_conn(null);
			}
		}
	}
}
