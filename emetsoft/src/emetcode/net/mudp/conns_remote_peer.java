package emetcode.net.mudp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import emetcode.util.devel.logger;

public class conns_remote_peer {
	static final boolean IN_DEBUG_1 = false;
	static final boolean IN_DEBUG_7 = false;	// abort on non expected cases
	static final boolean IN_DEBUG_8 = false;	// print try close

	private static final int SECS_TIMEOUT_TRYLOCK = 30;
	static final int NUM_SEC_AVG = 500;

	static int MAX_NUM_CONN = 20;

	conns_local_peer local_conns_dir;
	mudp_peer remote_peer;

	private final Map<Long, mudp_connection> all_conn = new TreeMap<Long, mudp_connection>();
	private final ReentrantReadWriteLock rw_lk = new ReentrantReadWriteLock();
	private final Lock r_lk = rw_lk.readLock();
	private final Lock w_lk = rw_lk.writeLock();

	conns_remote_peer(conns_local_peer dd, mudp_peer pp) {
		local_conns_dir = dd;
		remote_peer = pp;
	}

	msg_conn make_new_client_data_conn(long server_id) {
		if (!mudp_connection.is_server(server_id)) {
			throw new bad_udp(2);
		}
		msg_conn conn = null;
		w_lk.lock();
		try {
			if (all_conn.size() >= MAX_NUM_CONN) {
				int num_remv = remove_all_closed();
				logger.debug("num_removed_CLOSED_CONNS=" + num_remv);
			}
			if (all_conn.size() <= MAX_NUM_CONN) {
				conn = new msg_conn(this);
				conn.server_conn_id = server_id;
				while (all_conn.containsKey(conn.get_conn_id())
						|| (conn.is_server())) {
					conn = new msg_conn(this);
					conn.server_conn_id = server_id;
				}
				all_conn.put(conn.get_conn_id(), conn);
			} else {
				logger.debug("SERVER_WITH_MAX_NUM_CONN!!!!\n remome_peer=" + remote_peer);
				if(IN_DEBUG_7){
					throw new bad_udp(2);
				}
			}
		} finally {
			w_lk.unlock();
		}
		return conn;
	}

	msg_conn make_new_server_data_conn(long conn_id) {
		msg_conn conn = null;
		w_lk.lock();
		try {
			if (all_conn.size() >= MAX_NUM_CONN) {
				int num_remv = remove_all_closed();
				logger.debug("num_removed_CLOSED_CONNS=" + num_remv);
			}
			if (all_conn.size() <= MAX_NUM_CONN) {
				if (!all_conn.containsKey(conn_id)) {
					conn = new msg_conn(this, conn_id);
					all_conn.put(conn.get_conn_id(), conn);
				}
			} else {
				logger.debug("SERVER_WITH_MAX_NUM_CONN!!!!\n remome_peer=" + remote_peer);
				if(IN_DEBUG_7){
					throw new bad_udp(2);
				}
			}
		} finally {
			w_lk.unlock();
		}
		return conn;
	}

	req_conn make_new_requester_conn(long server_id, long cokey_id) {
		if (!mudp_connection.is_server(server_id)) {
			throw new bad_udp(2);
		}
		req_conn conn = null;
		w_lk.lock();
		try {
			if (all_conn.size() >= MAX_NUM_CONN) {
				int num_remv = remove_all_closed();
				logger.debug("num_removed_CLOSED_CONNS=" + num_remv);
			}
			if (all_conn.size() <= MAX_NUM_CONN) {
				conn = new req_conn(this);
				conn.set_key_id(cokey_id);
				conn.server_conn_id = server_id;
				while (all_conn.containsKey(conn.get_conn_id())
						|| (conn.is_server())) {
					conn = new req_conn(this);
					conn.set_key_id(cokey_id);
					conn.server_conn_id = server_id;
				}
				all_conn.put(conn.get_conn_id(), conn);
				if (IN_DEBUG_1) {
					logger.debug("ADDED_REQUESTER " + conn.get_conn_id()
							+ " in " + remote_peer);
				}
			} else {
				logger.debug("SERVER_WITH_MAX_NUM_CONN!!!!\n remome_peer=" + remote_peer);
				if(IN_DEBUG_7){
					throw new bad_udp(2);
				}
			}
		} finally {
			w_lk.unlock();
		}
		return conn;
	}

	req_conn make_new_responder_conn(long conn_id) {
		req_conn conn = null;
		w_lk.lock();
		try {
			if (all_conn.size() >= MAX_NUM_CONN) {
				int num_remv = remove_all_closed();
				logger.debug("num_removed_CLOSED_CONNS=" + num_remv);
			}
			if (all_conn.size() <= MAX_NUM_CONN) {
				if (!all_conn.containsKey(conn_id)) {
					conn = new req_conn(this, conn_id);
					all_conn.put(conn.get_conn_id(), conn);
				}
			} else {
				logger.debug("SERVER_WITH_MAX_NUM_CONN!!!!\n remome_peer=" + remote_peer);
				if(IN_DEBUG_7){
					throw new bad_udp(2);
				}
			}
		} finally {
			w_lk.unlock();
		}
		return conn;
	}

	private void no_lock_remove_conn(mudp_connection conn) {
		// MUST BE WRITE LOCKED TO CALL THIS FUNC
		if (conn != null) {
			all_conn.remove(conn.get_conn_id());
			if (IN_DEBUG_1) {
				logger.debug("REMOVED_CONN=" + conn);
			}
		}
	}
	
	void remove_conn(mudp_connection conn) {
		boolean got_lk = w_lk.tryLock();
		if (got_lk) {
			try {
				no_lock_remove_conn(conn);
				if (all_conn.isEmpty()) {
					local_conns_dir.remove_remote(remote_peer);
				}
			} finally {
				w_lk.unlock();
			}
		}
	}

	boolean has_conn(long id) {
		boolean cc = false;
		r_lk.lock();
		try {
			mudp_connection conn = all_conn.get(id);
			cc = (conn != null);
		} finally {
			r_lk.unlock();
		}
		return cc;
	}

	mudp_connection get_conn(long id) {
		mudp_connection conn = mudp_connection.FALSE_CONN;
		boolean got_lk = r_lk.tryLock();
		if (got_lk) {
			try {
				conn = all_conn.get(id);
			} finally {
				r_lk.unlock();
			}
		}
		return conn;
	}

	long run_all_data_conn(List<mudp_connection> all_closed,
			List<conns_remote_peer> all_to_remove) {
		long max_tm = -1;
		boolean got_lok = false;
		try {
			got_lok = r_lk.tryLock(SECS_TIMEOUT_TRYLOCK, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
		}
		if (!got_lok) {
			return max_tm;
		}
		try {
			if (all_conn.isEmpty()) {
				all_to_remove.add(this);
			} else {
				for (Map.Entry<Long, mudp_connection> e_cnn : all_conn
						.entrySet()) {
					mudp_connection conn = e_cnn.getValue();
					if (conn.is_closed()) {
						all_closed.add(conn);
						continue;
					}
					if (!conn.is_server()) {
						long ss = conn.sender_main();
						if (ss > max_tm) {
							max_tm = ss;
						}
					}
				}
			}
		} finally {
			r_lk.unlock();
		}
		return max_tm;
	}

	private int remove_all_closed(){
		// MUST BE WRITE LOCKED TO CALL THIS FUNC
		List<mudp_connection> all_closed = new ArrayList<mudp_connection>();
		fill_all_closed(all_closed);
		int num_remv = all_closed.size();
		for (mudp_connection conn : all_closed) {
			no_lock_remove_conn(conn);
		}
		return num_remv;
	}
	
	private void fill_all_closed(List<mudp_connection> all_closed){
		// MUST BE LOCKED TO CALL THIS FUNC
		for (Map.Entry<Long, mudp_connection> e_cnn : all_conn
				.entrySet()) {
			mudp_connection conn = e_cnn.getValue();
			if (conn.is_closed()) {
				all_closed.add(conn);
			}
			if(IN_DEBUG_8){
				if(conn instanceof msg_conn){
					msg_conn cnn = (msg_conn)conn;
					logger.debug("TRY_CONN=" + cnn + cnn.get_info(true));
				}
			}
		}
	}

	void add_all_conn(List<mudp_connection> all_conn_lst,
			List<conns_remote_peer> all_to_remove) {
		boolean got_lok = false;
		try {
			got_lok = r_lk.tryLock(SECS_TIMEOUT_TRYLOCK, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
		}
		if (!got_lok) {
			return;
		}
		try {
			if (all_conn.isEmpty()) {
				all_to_remove.add(this);
			} else {
				for (Map.Entry<Long, mudp_connection> e_cnn : all_conn
						.entrySet()) {
					mudp_connection conn = e_cnn.getValue();
					all_conn_lst.add(conn);
				}
			}
		} finally {
			r_lk.unlock();
		}
	}

}
