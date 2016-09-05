package emetcode.net.mudp;

import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import emetcode.crypto.bitshake.utils.convert;
import emetcode.util.devel.logger;

public class msg_conn extends mudp_connection {

	private static final boolean IN_DEBUG_1 = false; // sending
	private static final boolean IN_DEBUG_2 = false;
	private static boolean IN_DEBUG_3 = true; // maxs and limits
	public static boolean IN_DEBUG_4 = false; // open/close conn
	private static boolean IN_DEBUG_5 = false; // START send/recv
	private static boolean IN_DEBUG_6 = false; // END send/recv
	private static boolean IN_DEBUG_7 = false; // accept
	private static boolean IN_DEBUG_8 = false; // bad dat sec
	private static boolean IN_DEBUG_9 = false; // debug take
	private static boolean IN_DEBUG_10 = false; // debug stats
	private static boolean IN_DEBUG_11 = false; // bad make conn

	private static final long NUM_BY_TOCAL_RC = 1000000;

	private static final long MILLISEC_TIMEOUT_SEND = 3 * config.MIN_MILLIS;
	private static final long MILLISEC_TIMEOUT_CONN = 3 * config.MIN_MILLIS;
	private static final long MILLISEC_TIMEOUT_CLOSE = MILLISEC_TIMEOUT_CONN
			+ config.MIN_MILLIS;

	// private static final long MILLISEC_TIMEOUT_SEND = 10000;
	// private static final long MILLISEC_TIMEOUT_CONN = 10000;
	// private static final long MILLISEC_TIMEOUT_CLOSE = 15000;

	private static final int SECS_TIMEOUT_OFFER_STOP = 30;

	private static final int MAX_NUM_USR_SEND = 2;
	private static final int MAX_NUM_USR_RECV = 4;

	private static final int MAX_NUM_CNN = 20;

	private static final int MAX_NUM_SENDING = 50;
	private static final long MAX_NUM_BY_IN_SENDING = 40000000;

	private static final int MAX_NUM_RECEIVING = 100;
	private static final long MAX_NUM_BY_IN_RECEIVING = 45000000;

	private static final byte[] NULL_MSG = "MUDP_NULL_MESSAGE"
			.getBytes(config.UTF_8);

	static final byte[] ABORT_MSG = "MUDP_ABORT_MESSAGE".getBytes(config.UTF_8);

	private static final int NUM_ABORT = 10;

	static final long MAX_RECV_NUM_SEC = ((MAX_NUM_BY_IN_RECEIVING / message_section.MAX_SEC_DATA_NUM_BY) + 1);

	static final byte[] CLOSE_MSG = "MUDP_CLOSE_MESSAGE".getBytes(config.UTF_8);

	public static final String KIND_msg_conn = "KIND_msg_conn";

	private long start_time;
	private long end_time;
	private long last_tot_work;
	private long last_wait_time;

	send_stats stats;

	private AtomicBoolean connec_established;
	private AtomicBoolean stoped_conn;
	private AtomicBoolean recv_something;
	private AtomicBoolean is_closing;
	private AtomicBoolean recv_close_msg;
	private AtomicBoolean sent_close_msg;
	private AtomicBoolean recv_abort_msg;
	private boolean recv_close;
	private boolean sending_last_msg;

	private long msg_send_consec;
	private long msg_recv_consec;

	private long num_by_in_sending;
	private long num_by_in_receiving;

	private List<message_builder> all_sending;
	private List<message_builder> all_receiving;

	private final ReentrantReadWriteLock send_rw_lk = new ReentrantReadWriteLock();
	private final Lock send_r_lk = send_rw_lk.readLock();
	private final Lock send_w_lk = send_rw_lk.writeLock();

	private BlockingQueue<byte[]> usr_send;
	private BlockingQueue<byte[]> usr_recv;

	private BlockingQueue<Boolean> close_waiter;
	private BlockingQueue<mudp_connection> all_accepting;

	msg_conn(conns_remote_peer pp) {
		super(pp);
		init_net_connection();
	}

	msg_conn(conns_remote_peer pp, long id) {
		super(pp, id);
		init_net_connection();
		if (!is_server()) {
			connec_established.set(true);
		}
	}

	private void init_net_connection() {

		connec_established = new AtomicBoolean(false);
		stoped_conn = new AtomicBoolean(false);
		recv_something = new AtomicBoolean(false);
		is_closing = new AtomicBoolean(false);
		recv_close_msg = new AtomicBoolean(false);
		sent_close_msg = new AtomicBoolean(false);
		recv_abort_msg = new AtomicBoolean(false);
		recv_close = false;
		sending_last_msg = false;

		start_time = 0;
		end_time = 0;
		last_tot_work = 0;
		last_wait_time = 0;

		stats = new send_stats();

		msg_send_consec = 0;
		msg_recv_consec = 0;

		num_by_in_sending = 0;
		num_by_in_receiving = 0;

		all_sending = new ArrayList<message_builder>();
		all_receiving = new ArrayList<message_builder>();

		usr_send = new LinkedBlockingQueue<byte[]>(MAX_NUM_USR_SEND);
		usr_recv = new LinkedBlockingQueue<byte[]>(MAX_NUM_USR_RECV);

		close_waiter = new LinkedBlockingQueue<Boolean>(1);
		all_accepting = new LinkedBlockingQueue<mudp_connection>(MAX_NUM_CNN);
	}

	private long first_send_consec() {
		long fst_consec = (msg_send_consec + 1);
		return fst_consec;
	}

	private long next_send_consec() {
		// must be inside a write lock
		return (msg_send_consec + all_sending.size() + 1);
	}

	private long get_send_consec(int bb_idx) {
		// must be inside a read lock
		if (bb_idx < 0) {
			throw new bad_udp(2);
		}
		if (bb_idx >= all_sending.size()) {
			throw new bad_udp(2);
		}
		return (msg_send_consec + bb_idx + 1);
	}

	private message_builder get_next_dat_builder_for(byte[] data) {
		message_builder bb = new message_builder(next_send_consec());
		bb.set_sections(data);
		message_section hd = get_send_message_header(data);
		hd.msg_consec = bb.msg_consec;
		bb.init_sections(hd);
		return bb;
	}

	private message_builder add_builder_for(byte[] data) {
		message_builder bb = null;
		send_w_lk.lock();
		try {
			bb = get_next_dat_builder_for(data);
			if (bb.tot_bld_by != data.length) {
				throw new bad_udp(2);
			}
			num_by_in_sending += bb.tot_bld_by;
			all_sending.add(bb);
		} finally {
			send_w_lk.unlock();
		}
		return bb;
	}

	private void send_all_dat_sections_for(message_builder bb,
			message_builder first_bb) {
		if (first_bb == null) {
			throw new bad_udp(2);
		}
		if (bb.is_fin_msg() && (bb != first_bb)) {
			stats.set_max_num_sent();
			if (!stats.sent_max()) {
				throw new bad_udp(2);
			}
		}
		if (!sending_last_msg) {
			sending_last_msg = (bb.is_fin_msg() && (bb == first_bb));
		}
		if (sending_last_msg && stats.sent_max()) {
			throw new bad_udp(2, "CANNOT_SEND_CLOSE_MSG");
		}
		bb.send_sections(get_sok(), this);
	}

	private message_builder get_builder_for(message_section sec) {
		send_r_lk.lock();
		try {
			if (all_sending.isEmpty()) {
				if (IN_DEBUG_1) {
					logger.debug("ack with empty sending !!" + sec.toString());
				}
				return null;
			}
			long l_idx = (sec.msg_consec - first_send_consec());
			if ((l_idx < 0) || (l_idx >= all_sending.size())) {
				if (IN_DEBUG_1) {
					logger.debug("ack with invalid msg_consec in sending!!"
							+ sec.toString());
				}
				return null;
			}
			int idx = (int) l_idx;
			message_builder bb = all_sending.get(idx);
			return bb;
		} finally {
			send_r_lk.unlock();
		}
	}

	private long first_recv_consec() {
		long fst_consec = (msg_recv_consec + 1);
		return fst_consec;
	}

	private long next_recv_consec() {
		return (msg_recv_consec + all_receiving.size() + 1);
	}

	private long get_recv_consec(int bb_idx) {
		if (bb_idx < 0) {
			throw new bad_udp(2);
		}
		if (bb_idx >= all_receiving.size()) {
			throw new bad_udp(2);
		}
		return (msg_recv_consec + bb_idx + 1);
	}

	private void ck_recv_consec(int idx) {
		long consec = get_recv_consec(idx);
		message_builder bld = all_receiving.get(idx);
		if (bld.msg_consec != consec) {
			throw new bad_udp(2);
		}
	}

	private boolean is_sending_full() {
		boolean is_full = (num_by_in_sending > MAX_NUM_BY_IN_SENDING);
		if (IN_DEBUG_3) {
			if (is_full) {
				throw new bad_udp(2, "is_sending_full. num_by_in_sending="
						+ num_by_in_sending);
			}
		}
		return is_full;
	}

	private boolean is_sending_at_max_cap() {
		int num_sending = 0;
		boolean at_max = true;
		send_r_lk.lock();
		try {
			num_sending = all_sending.size();
			at_max = (num_sending >= MAX_NUM_SENDING);
		} finally {
			send_r_lk.unlock();
		}
		if (IN_DEBUG_3) {
			if (at_max) {
				throw new bad_udp(2, "is_sending_at_max_cap. num_sending="
						+ num_sending);
			}
		}
		return at_max;
	}

	private message_builder get_first_msg() {
		send_r_lk.lock();
		try {
			if (all_sending.isEmpty()) {
				return null;
			}
			return all_sending.get(0);
		} finally {
			send_r_lk.unlock();
		}
	}

	private void send_pending_dat() {
		if (stats.sent_max()) {
			return;
		}
		// long current_date = System.currentTimeMillis();
		int ini_sz = usr_send.size();
		for (int aa = 0; aa < ini_sz; aa++) {
			if (is_sending_full() || is_sending_at_max_cap()) {
				break;
			}
			byte[] dat = usr_send.poll();
			if (dat == null) {
				if (IN_DEBUG_3) {
					logger.debug("\n\n\nCANNOT POLL\n\n\n");
				}
				break;
			}
			message_builder bb = add_builder_for(dat);
			if (bb != null) {
				send_all_dat_sections_for(bb, get_first_msg());
				if (IN_DEBUG_5) {
					logger.debug("STARTED SEND OF MSG=" + bb.msg_consec
							+ " msg_sz=" + bb.tot_bld_by + " msg_msha="
							+ bb.dbg_send_mini_sha + " all_send_sz="
							+ all_sending.size() + " conn=" + this);
				}
			}
			if (stats.sent_max()) {
				return;
			}
		}
	}

	private boolean is_receiving_full() {
		boolean is_full = (num_by_in_receiving > MAX_NUM_BY_IN_RECEIVING);
		if (IN_DEBUG_3) {
			if (is_full) {
				throw new bad_udp(2, "is_receiving_full. num_by_in_receiving="
						+ num_by_in_receiving);
			}
		}
		return is_full;
	}

	private boolean is_receiving_at_max_cap() {
		int num_recv = all_receiving.size();
		boolean is_at_max = (num_recv >= MAX_NUM_RECEIVING);
		if (IN_DEBUG_3) {
			if (is_at_max) {
				throw new bad_udp(2, "is_receiving_at_max_cap. num_recv="
						+ num_recv);
			}
		}
		return is_at_max;
	}

	private message_section get_syn_section() {
		long my_cnn_id = conn_id;
		if (is_server(my_cnn_id)) {
			throw new bad_udp(2);
		}

		long the_dest_id = my_cnn_id;
		if (!connec_established.get() && is_client_data_conn()) {
			the_dest_id = server_conn_id;
		}

		message_section syn_sec = new message_section();
		syn_sec.msg_src = get_local_peer();
		syn_sec.msg_dest = get_remote_peer();
		syn_sec.msg_conn_id = the_dest_id;
		syn_sec.sec_kind = message_section.SYN_KIND;
		byte[] dat = convert.to_byte_array(my_cnn_id);
		syn_sec.sec_data = dat;
		syn_sec.set_mini_sha();
		return syn_sec;
	}

	private message_section get_abort_section() {
		long my_cnn_id = conn_id;

		message_section syn_sec = new message_section();
		syn_sec.msg_src = get_local_peer();
		syn_sec.msg_dest = get_remote_peer();
		syn_sec.msg_conn_id = my_cnn_id;
		syn_sec.sec_kind = message_section.DAT_KIND;
		syn_sec.sec_data = Arrays.copyOf(ABORT_MSG, ABORT_MSG.length);
		syn_sec.set_mini_sha();
		return syn_sec;
	}

	private void resend_pending_dat() {
		if (stats.sent_max()) {
			return;
		}

		send_r_lk.lock();
		try {
			message_builder first_bb = null;
			int bb_idx = 0;
			for (message_builder bb : all_sending) {
				if (bb_idx == 0) {
					first_bb = bb;
				}
				if (bb.msg_consec != get_send_consec(bb_idx)) {
					throw new bad_udp(2);
				}
				send_all_dat_sections_for(bb, first_bb);
				bb_idx++;
			}
		} finally {
			send_r_lk.unlock();
		}
	}

	private void send_syn() {
		message_section syn = get_syn_section();
		DatagramSocket sok = get_sok();
		if (!sok.isClosed()) {
			syn.send_section(sok);
		}
	}

	private void send_abort() {
		if (is_server()) {
			return;
		}
		message_section syn = get_abort_section();
		DatagramSocket sok = get_sok();
		for (int aa = 0; aa < NUM_ABORT; aa++) {
			if (!sok.isClosed()) {
				syn.send_section(sok);
				Thread.yield();
			}
		}
	}

	private int get_num_sending() {
		send_r_lk.lock();
		try {
			return all_sending.size();
		} finally {
			send_r_lk.unlock();
		}
	}

	private String dbg_get_tot_msg() {
		String msg = "";
		int sz = 0;
		sz = get_num_sending();
		if (sz != 0) {
			msg += ".snd_sz=" + sz;
		}
		sz = usr_send.size();
		if (sz != 0) {
			msg += ".usr_sz=" + sz;
		}
		sz = all_accepting.size();
		if (sz != 0) {
			msg += ".acc_sz=" + sz;
		}
		if (!is_server()) {
			if (!connec_established.get()) {
				msg += ".cnn=1";
			}
			if (!recv_something.get()) {
				msg += ".som=1";
			}
			if (is_closing.get()) {
				msg += ".clo=1";
			}
		}
		return msg;
	}

	private long tot_pending_work() {
		long tot = 0;
		tot += get_num_sending();

		tot += usr_send.size();

		tot += all_accepting.size();

		if (!is_server()) {
			if (!connec_established.get()) {
				tot++;
			}
			if (!recv_something.get()) {
				tot++;
			}
			if (is_closing.get()) {
				tot++;
			}
		}

		if (IN_DEBUG_2) {
			if (tot != 0) {
				logger.debug(dbg_get_tot_msg());
			} else {
				logger.debug("Zero send pending " + this);
			}
		}
		if (IN_DEBUG_2) {
			if (tot != 0) {
				logger.debug(dbg_get_tot_msg());
			} else {
				logger.debug("Zero send pending " + this);
			}
		}

		return tot;
	}

	private void remove_all_complete() {
		boolean stop_it = false;
		boolean call_rc = false;
		send_w_lk.lock();
		try {
			while (!all_sending.isEmpty() && all_sending.get(0).has_all_acks()) {
				message_builder rr = all_sending.remove(0);
				msg_send_consec++;
				num_by_in_sending -= rr.tot_bld_by;
				call_rc = (rr.tot_bld_by > NUM_BY_TOCAL_RC);
				if (IN_DEBUG_1) {
					rr.ck_all_acks();
				}
				if (IN_DEBUG_6) {
					logger.debug("FINISHED_SEND_OF_MSG=" + rr.msg_consec
							+ " msg_sz=" + rr.tot_bld_by + " msg_msha="
							+ rr.dbg_send_mini_sha + " all_send_sz="
							+ all_sending.size() + " conn=" + this);
				}
				boolean is_fin = rr.is_fin_msg();
				rr = null;
				if (call_rc) {
					Runtime.getRuntime().gc();
				}
				if (!is_server() && is_fin) {
					stop_it = true;
					break;
				}
			}
		} finally {
			send_w_lk.unlock();
			if (stop_it) {
				sent_close_msg.set(true);
			}
			if (call_rc) {
				Runtime.getRuntime().gc();
			}
		}
	}

	private long get_wait_time() {
		last_tot_work = tot_pending_work();
		if (last_tot_work == 0) {
			last_wait_time = -1;
			return -1;
		}
		long w_tm = stats.get_wait_tm();
		if (!connec_established.get() && !is_closing.get()) {
			long curr_tm = get_manager().current_send_date;
			if (start_time == 0) {
				start_time = curr_tm;
			}
			long tot_tm = curr_tm - start_time;
			w_tm = 1;
			if (tot_tm > 1) {
				w_tm = tot_tm;
			}
		}
		if (sending_last_msg) {
			long curr_tm = get_manager().current_send_date;
			if (end_time == 0) {
				end_time = curr_tm;
			}
			long tot_tm = curr_tm - end_time;
			w_tm = 1;
			if (tot_tm > 1) {
				w_tm = tot_tm;
			}
			if (w_tm > 1000) {
				w_tm = 1000;
			}
		}
		if (w_tm == -1) {
			throw new bad_udp(2);
		}
		last_wait_time = w_tm;
		return w_tm;
	}

	void stablish() {
		if (connec_established.compareAndSet(false, true)) {
			if (IN_DEBUG_1) {
				logger.debug("CONN_STRABLISHED=" + this);
			}
		}
	}

	boolean recv_ack_section(message_section sec) {
		if (!sec.ck_mini_sha()) {
			if (IN_DEBUG_3) {
				logger.debug("Bad mini sha. Corrupted message section. sec="
						+ sec);
			}
			if (IN_DEBUG_10) {
				dbg_stats.tot_recv_ack_bad_minisha.incrementAndGet();
			}
			return false;
		}
		if (!sec.is_ack()) {
			throw new bad_udp(2);
		}
		long rt_tm = sec.get_round_trip_time();
		if (rt_tm < 0) {
			if (IN_DEBUG_3) {
				logger.debug("ack with invalid ack round trip time (" + rt_tm
						+ ") sec=" + sec);
			}
			if (IN_DEBUG_10) {
				dbg_stats.tot_recv_ack_bad_round_tm.incrementAndGet();
			}
			return false;
		}

		message_builder bb = get_builder_for(sec);
		if (bb == null) {
			if (IN_DEBUG_1) {
				logger.debug("ack with invalid msg_consec in sending!!"
						+ sec.toString());
			}
			if (IN_DEBUG_10) {
				dbg_stats.tot_recv_ack_bad_builder.incrementAndGet();
			}
			return false;
		}
		message_section orig_sec = bb.get_section(sec.sec_consec);
		if (orig_sec == null) {
			if (IN_DEBUG_1) {
				logger.debug("ack with invalid sec_consec in sending!!"
						+ sec.toString());
			}
			if (IN_DEBUG_10) {
				dbg_stats.tot_recv_ack_bad_orig_sec.incrementAndGet();
			}
			return false;
		}

		boolean all_ack = false;
		int num_ok = orig_sec.sec_num_ok.incrementAndGet();
		if (num_ok == 1) {
			stats.curr_avg_ack_time.add_val(sec.get_round_trip_time());
			stats.num_ack_by.addAndGet(orig_sec.sec_data.length);
			// stats.num_sec_send_avg.add_val(orig_sec.sec_num_times_sent.get());
			get_manager().num_sec_send_avg.add_val(orig_sec.sec_num_times_sent
					.get());

			bb.num_acks.incrementAndGet();
			if (bb.has_all_acks()) {
				all_ack = true;
			}
		}

		recv_something.set(true);
		time_last_activ.set(sec.sec_recv_time);
		if (IN_DEBUG_10) {
			dbg_stats.tot_recv_ack_ok.incrementAndGet();
		}
		return all_ack;
	}

	private boolean has_timeout() {
		long curr_tm = get_manager().current_send_date;
		time_last_activ.compareAndSet(0, curr_tm);
		long inactiv_tm = curr_tm - time_last_activ.get();
		if (inactiv_tm > MILLISEC_TIMEOUT_CONN) {
			logger.info("INACTIVE TIMEOUT CONN=" + this + " time_last_activ="
					+ time_last_activ.get() + " curr_tm=" + curr_tm
					+ " inactiv_tm=" + inactiv_tm + get_info(false));
			return true;
		}
		return false;
	}

	boolean recv_dat_section(message_section sec) {
		if (is_server()) {
			throw new bad_udp(2);
		}
		if (sec.sec_kind != message_section.DAT_KIND) {
			throw new bad_udp(2);
		}
		if (sec.is_abort_sec()) {
			recv_abort_msg.set(true);
			recv_close_msg.set(true);
			recv_close = true;
			return true;
		}
		if (sec.is_close_sec()) {
			if (IN_DEBUG_4) {
				logger.debug("got CLOSE_MSG mudp conn=" + this);
			}
			recv_close_msg.set(true); // to be checked in sender thread too
			recv_close = true; // use only in receiver thread
			if (IN_DEBUG_10) {
				dbg_stats.tot_recv_dat_close.incrementAndGet();
			}
			if (!sent_close_msg.get() && !is_client_data_conn()) {
				return false;
			}
			return true;
		}
		if (recv_close) {
			if (IN_DEBUG_10) {
				dbg_stats.tot_recv_dat_prev_close.incrementAndGet();
			}
			return true;
		}
		if (is_receiving_full()) {
			if (IN_DEBUG_10) {
				dbg_stats.tot_recv_dat_is_full.incrementAndGet();
			}
			return false;
		}
		if (!sec.ck_mini_sha()) {
			if (IN_DEBUG_8) {
				logger.debug("Bad_section mini sha. Corrupted message section");
			}
			if (IN_DEBUG_10) {
				dbg_stats.tot_recv_dat_bad_minisha.incrementAndGet();
			}
			return false;
		}
		int tot_sec = sec.tot_sec_in_msg();
		if (tot_sec > MAX_RECV_NUM_SEC) {
			if (IN_DEBUG_8) {
				logger.debug("Bad_section. Invalid size");
			}
			if (IN_DEBUG_10) {
				dbg_stats.tot_recv_dat_max_sec.incrementAndGet();
			}
			return false;
		}

		if (is_receiving_at_max_cap()) {
			if (IN_DEBUG_8) {
				logger.debug("Bad_section. Reciving at MAX CAPACITY");
			}
			if (IN_DEBUG_10) {
				dbg_stats.tot_recv_dat_max_cap.incrementAndGet();
			}
			return false;
		}

		while ((next_recv_consec() <= sec.msg_consec)
				&& !is_receiving_at_max_cap()) {
			all_receiving.add(null);
		}

		if (all_receiving.isEmpty()) {
			if (IN_DEBUG_1) {
				logger.debug("already received sec " + sec.toString());
			}
			if (IN_DEBUG_10) {
				dbg_stats.tot_recv_dat_empty_recving.incrementAndGet();
			}
			return true;
		}

		if (first_recv_consec() > sec.msg_consec) {
			if (IN_DEBUG_1) {
				logger.debug("already received sec " + sec.toString());
			}
			if (IN_DEBUG_10) {
				dbg_stats.tot_recv_dat_fst_gt_recv.incrementAndGet();
			}
			return true;
		}

		long l_idx = (sec.msg_consec - first_recv_consec());
		if ((l_idx < 0) || (l_idx >= all_receiving.size())) {
			throw new bad_udp(2, "idx=" + l_idx + " f_cosc="
					+ first_recv_consec() + " m_cosc=" + sec.msg_consec
					+ " rcv_sz=" + all_receiving.size());
		}
		int bb_idx = (int) l_idx;

		message_builder bb = all_receiving.get(bb_idx);
		if (bb == null) {
			long consec = get_recv_consec(bb_idx);
			bb = new message_builder(consec);
			bb.tot_bld_by = sec.msg_num_by;
			bb.tot_bld_mini_sha = sec.msg_mini_sha;

			all_receiving.set(bb_idx, bb);
			num_by_in_receiving += bb.tot_bld_by;

			if (IN_DEBUG_5) {
				logger.debug("STARTED RECV OF MSG=" + bb.msg_consec
						+ " msg_sz=" + sec.msg_num_by + " msg_sz2="
						+ bb.tot_bld_by + " all_send_sz="
						+ all_receiving.size() + " conn=" + this);
			}
		}

		bb.add_section(sec);
		ck_recv_consec(bb_idx);

		recv_something.set(true);

		if (IN_DEBUG_10) {
			dbg_stats.tot_recv_dat_added_sec.incrementAndGet();
		}

		boolean is_last = bb.is_last_ack_sec(sec);
		return !is_last;
	}

	List<message_section> recv_all_full() {
		if (all_receiving.isEmpty()) {
			if (IN_DEBUG_10) {
				dbg_stats.tot_recv_full_empty_recving.incrementAndGet();
			}
			return null;
		}

		List<message_section> all_last = new ArrayList<message_section>();
		message_builder bb = all_receiving.get(0);
		while ((bb != null) && bb.has_all_recv_by()) {
			long msha = 0;
			int msz = 0;
			byte[] dat_arr = bb.get_recv_data(this);
			if (IN_DEBUG_6) {
				msha = convert.calc_minisha_long(dat_arr);
				msz = dat_arr.length;
			}
			if (dat_arr == null) {
				throw new bad_udp(2);
			}

			boolean ok = usr_recv.offer(dat_arr);
			if (!ok) {
				break;
			}
			if (IN_DEBUG_2) {
				logger.debug("RECV_" + bb.get_info());
			}
			message_builder bb2 = all_receiving.remove(0);
			num_by_in_receiving -= bb2.tot_bld_by;

			boolean call_rc = (bb2.tot_bld_by > NUM_BY_TOCAL_RC);
			if (bb != bb2) {
				throw new bad_udp(2);
			}

			all_last.add(bb2.get_last_ack_sec());
			msg_recv_consec++;

			if (IN_DEBUG_10) {
				dbg_stats.tot_recv_full_offer_ok.incrementAndGet();
			}
			if (IN_DEBUG_6) {
				logger.debug("FINISHED_RECV_OF_MSG=" + bb2.msg_consec
						+ " msg_len=" + msz + " msg_sz=" + bb2.tot_bld_by
						+ " msg_msha=" + msha + " all_recv_sz="
						+ all_receiving.size() + " in conn=" + this);
			}
			bb.init_msg_builder();
			bb = null;
			bb2 = null;
			if (!all_receiving.isEmpty()) {
				bb = all_receiving.get(0);
			}
			if (IN_DEBUG_2) {
				logger.debug("FST_RECV_CONSEC=" + first_recv_consec());
				logger.debug("ALL_RECV=\n" + all_receiving.toString());
			}
			if (call_rc) {
				Runtime.getRuntime().gc();
			}
		}
		return all_last;
	}

	private boolean sent_and_recv_close_msg() {
		return (sent_close_msg.get() && recv_close_msg.get());
	}

	long sender_main() {
		if (is_server()) {
			throw new bad_udp(2);
		}
		if (stoped_conn.get()) {
			throw new bad_udp(2);
		}
		if (sent_and_recv_close_msg() || has_timeout() || recv_abort_msg.get()) {
			stop_msg_conn();
			return -1;
		}
		if (IN_DEBUG_1) {
			logger.debug("CONN_MAIN_MUDP_conn=" + this + " <<<<<<<");
		}

		try {
			boolean in_clo = is_closing.get();
			if (connec_established.get() || in_clo) {
				boolean snd = stats.calc_next();
				if (snd || in_clo) {
					remove_all_complete();
					resend_pending_dat();
					send_pending_dat();
				}
			} else {
				send_syn();
			}
		} catch (bad_udp ex1) {
			logger.error(ex1, "connection " + conn_id + " GOT BAD_UDP !!!!");
		}
		if (IN_DEBUG_1) {
			logger.debug(">>>>_con= " + conn_id);
		}

		return get_wait_time();
	}

	private mudp_connection get_conn(mudp_peer rem_peer, long conn_id) {
		mudp_connection old_conn = remote_conns_dir.local_conns_dir.get_conn(
				rem_peer, conn_id);
		return old_conn;
	}

	msg_conn recv_syn(message_section sec) {
		if (!is_server()) {
			throw new bad_udp(2);
		}
		if (stoped_conn.get()) {
			if (IN_DEBUG_7) {
				logger.debug("accept_warning. STOPPED SERVER !!!!");
			}
			return null;
		}
		long n_conn_id = convert.to_long(sec.sec_data);
		if (is_server(n_conn_id)) {
			if (IN_DEBUG_7) {
				logger.debug("accept_warning. TRYING TO OPEN SERVER CONN AS CLIENT !!!!!");
			}
			return null;
		}

		msg_conn n_conn = remote_conns_dir.local_conns_dir
				.make_new_server_conn(sec.msg_src, n_conn_id);
		if (n_conn != null) {
			if (IN_DEBUG_11) {
				mudp_connection added_cnn = get_conn(sec.msg_src, n_conn_id);
				if ((added_cnn != mudp_connection.FALSE_CONN)
						&& (added_cnn != n_conn)) {
					logger.debug("COULD_NOT_ADD_CLIENT_CONN!!!!\n n_conn_id="
							+ n_conn_id);
					logger.debug("\n conn1=" + n_conn + "\n conn2=" + added_cnn
							+ "\n IS_FALSE_CONN="
							+ (n_conn == mudp_connection.FALSE_CONN));
					throw new bad_udp(2);
				}
			}
			boolean ok = all_accepting.offer(n_conn);
			if (!ok) {
				n_conn.remove_me();
				n_conn = null;
				if (IN_DEBUG_7) {
					logger.debug("accept_warning. could_not_OFFER_NEW_CLIENT_CONN!!!!\n n_conn_id="
							+ n_conn_id + " all_acc_sz=" + all_accepting.size());
				}
			}
		} else {
			mudp_connection old_conn = get_conn(sec.msg_src, n_conn_id);
			if ((old_conn != null) && (old_conn != mudp_connection.FALSE_CONN)
					&& (!old_conn.is_client_data_conn())
					&& (old_conn instanceof msg_conn)) {
				n_conn = (msg_conn) old_conn;
			}
			if (IN_DEBUG_7) {
				if (old_conn == null) {
					logger.debug("accept_warning. could_not_FIND_CREATING_CLIENT_CONN!!!!");
				}
				if (old_conn == mudp_connection.FALSE_CONN) {
					logger.debug("accept_warning. could_not_GET_LOCKS_DURING_CREATING_CLIENT_CONN!!!!");
				}
			}
		}
		if (n_conn != null) {
			n_conn.send_syn();
			if (IN_DEBUG_4) {
				logger.debug("OPENED_SRV_MUDP_CONN=" + n_conn);
			}
		}
		get_manager().has_work.offer(true);
		return n_conn;
	}

	public mudp_connection accept() {
		if (stoped_conn.get()) {
			return null;
		}
		mudp_connection n_conn = null;
		try {
			if (IN_DEBUG_7) {
				logger.info("waiting for ACCEPT srv_conn=" + this);
			}
			n_conn = all_accepting.take();
			if (IN_DEBUG_7) {
				logger.info("GOT ACCEPT new_cli_conn=" + n_conn);
			}
		} catch (InterruptedException ex) {
			logger.error(ex, "during udp process_syn");
		}
		if (n_conn == STOP_CONN) {
			if (IN_DEBUG_4) {
				logger.debug("STOPPING_MUDP_ACCEPT CONN=" + this);
			}
			stop_msg_conn();
			return null;
		}
		return n_conn;
	}

	public byte[] receive() {
		if (is_closing.get() || stoped_conn.get()) {
			throw new bad_udp(2, "Stopped mudp connection conn=" + this);
		}
		byte[] dat = take_dat();
		if ((dat.length == NULL_MSG.length) && Arrays.equals(dat, NULL_MSG)) {
			return null;
		}
		if (dat == ABORT_MSG) {
			throw new bad_udp(2, "ABORT_MUDP_CONNECTION CONN=" + this);
		}
		return dat;
	}

	private byte[] dbg_poll_dat() {
		byte[] dat = null;
		try {
			dat = usr_recv.poll(2, TimeUnit.MINUTES);
			if (dat == null) {
				logger.info("TIMEOUT ON POLL_DAT CONN=" + this
						+ get_info(false));
				throw new bad_udp(2);
			}
		} catch (InterruptedException e) {
			logger.debug("Interrupted mudp poll_dat");
			throw new bad_udp(2);
		}
		return dat;
	}

	private byte[] take_dat() {
		if (IN_DEBUG_9) {
			return dbg_poll_dat();
		}
		byte[] dat = null;
		try {
			dat = usr_recv.take();
		} catch (InterruptedException e) {
			logger.debug("Interrupted mudp take_dat");
			throw new bad_udp(2);
		}
		return dat;
	}

	public void send(byte[] dat) {
		if ((dat != CLOSE_MSG) && (is_closing.get() || stoped_conn.get())) {
			throw new bad_udp(2, "Stopped mudp connection conn=" + this);
		}
		if (dat == null) {
			dat = NULL_MSG;
		}
		try {
			boolean ok = usr_send.offer(dat, MILLISEC_TIMEOUT_SEND,
					TimeUnit.MILLISECONDS);
			if (!ok) {
				throw new bad_udp(2, "TIMEDOUT_SEND conn=" + this);
			}
			get_manager().has_work.offer(true);
		} catch (InterruptedException e) {
			logger.debug("Interrupted mudp send");
			throw new bad_udp(2);
		}
	}

	public void stop_conn() {
		stop_msg_conn();
	}

	private void stop_msg_conn() {
		if (stoped_conn.compareAndSet(false, true)) {
			is_closing.set(true);
			send_abort();
			while (usr_send.poll() != null)
				;
			try {
				boolean ok1 = usr_recv.offer(ABORT_MSG,
						SECS_TIMEOUT_OFFER_STOP, TimeUnit.SECONDS);
				boolean ok2 = close_waiter.offer(true, SECS_TIMEOUT_OFFER_STOP,
						TimeUnit.SECONDS);
				boolean ok3 = all_accepting.offer(STOP_CONN,
						SECS_TIMEOUT_OFFER_STOP, TimeUnit.SECONDS);
				if (!ok1) {
					throw new bad_udp(2, "TIMEOUT WHILE STOPPING MUDP CONN="
							+ this);
				}
				if (!ok2) {
					throw new bad_udp(2, "TIMEOUT WHILE STOPPING MUDP CONN="
							+ this);
				}
				if (!ok3) {
					throw new bad_udp(2, "TIMEOUT WHILE STOPPING MUDP CONN="
							+ this);
				}
			} catch (Exception ee1) {
				logger.info("GOT EXCEPTION \n" + ee1
						+ "\n while stopping mudp conn=" + this);
			}
			if (IN_DEBUG_4) {
				logger.debug("MUDP_CONN_STOPED=" + this);
			}
		}
		get_manager().has_work.offer(true);
	}

	public void close() {
		if (IN_DEBUG_4) {
			logger.debug("CLOSING_MUDP_CONN=" + this);
		}
		if (is_server()) {
			stop_msg_conn();
		} else {
			is_closing.set(true); // has to be before the send so wait time is
									// correct
			send(CLOSE_MSG);
			Boolean got_closed = null;
			try {
				got_closed = close_waiter.poll(MILLISEC_TIMEOUT_CLOSE,
						TimeUnit.MILLISECONDS);
			} catch (InterruptedException ee1) {
				logger.debug("Interrupted while closing MUDP_CONN=" + this);
				throw new bad_udp(2, "Interrupted while close");
			}
			if (got_closed == null) {
				logger.info("TIMEOUT_waiting_for_close!!!! in close MUDP_CONN="
						+ this + "  " + get_info(false));
				throw new bad_udp(2, "CLOSE_TIMEDOUT");
			} else if (got_closed != true) {
				logger.info("BAD_CLOSE in close MUDP_CONN=" + this);
				throw new bad_udp(2, "BAD_CLOSE");
			}
		}
		if (IN_DEBUG_4) {
			logger.debug("FINISHED_CLOSING_MUDP_CONN=" + this);
		}
		get_manager().has_work.offer(true);
	}

	public boolean is_closed() {
		return stoped_conn.get();
	}

	public String toString() {
		return super.toString() + " is_stablished=" + connec_established.get();
	}

	public String get_conn_kind() {
		return KIND_msg_conn;
	}

	public String get_info(boolean thd_safe) {
		String inf = "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< CONN_INFO"
				+ "\n conn_id="
				+ conn_id
				+ "\n start_time="
				+ start_time
				+ "\n end_time="
				+ end_time
				+ "\n stats="
				+ stats
				+ "\n connec_established="
				+ connec_established.get()
				+ "\n stoped_conn="
				+ stoped_conn.get()
				+ "\n is_closed="
				+ is_closed()
				+ "\n recv_something="
				+ recv_something.get()
				+ "\n usr_send_sz="
				+ usr_send.size()
				+ "\n usr_recv="
				+ usr_recv.size()
				+ "\n time_last_activ="
				+ time_last_activ.get()
				+ "\n is_closing="
				+ is_closing
				+ "\n recv_close_msg="
				+ recv_close_msg.get()
				+ "\n sent_close_msg="
				+ sent_close_msg.get()
				+ "\n ----------------------------- " + dbg_stats.get_info();
		if (!thd_safe) {
			String inf2 = ""
					+ "\n ----------------------------- THREAD UNSAFE INFO "
					+ "\n last_wait_time="
					+ last_wait_time
					+ "\n last_tot_work="
					+ last_tot_work
					+ "\n sent_and_recv_close_msg="
					+ sent_and_recv_close_msg()
					+ "\n sending_last_msg="
					+ sending_last_msg
					+ "\n recv_close="
					+ recv_close
					+ "\n msg_send_consec="
					+ msg_send_consec
					+ "\n msg_recv_consec="
					+ msg_recv_consec
					+ "\n num_by_in_sending="
					+ num_by_in_sending
					+ "\n num_by_in_receiving="
					+ num_by_in_receiving
					+ "\n all_sending_sz="
					+ all_sending.size()
					+ "\n all_receiving_sz="
					+ all_receiving.size()
					+ "\n dbg_get_tot_msg="
					+ dbg_get_tot_msg()
					+ "\n tot_pending_work="
					+ tot_pending_work()
					+ "\n ---------------------------- STATS"
					+ stats.get_info()
					+ "\n stats.calc_next="
					+ stats.calc_next()
					+ "\n ----------------------------- MANAGER "
					+ get_manager().mg_stats.get_info();

			inf += inf2;
		}
		inf += "\n >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>";
		return inf;
	}
}
