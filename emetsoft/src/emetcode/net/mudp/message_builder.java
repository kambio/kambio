package emetcode.net.mudp;

import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import emetcode.crypto.bitshake.utils.convert;
import emetcode.util.devel.logger;

public class message_builder {
	private static final boolean IN_DEBUG_1 = false;
	private static final boolean IN_DEBUG_2 = false;
	private static final boolean IN_DEBUG_10 = false; // / dbg_stats

	long msg_consec;
	int tot_bld_by;
	int tot_bld_mini_sha;
	int tot_recv_mini_sha;

	AtomicInteger num_acks;

	private int num_recv_by;

	private byte[] recv_dat;

	private List<message_section> all_sections;
	private final ReentrantReadWriteLock rw_lk = new ReentrantReadWriteLock();
	private final Lock r_lk = rw_lk.readLock();
	private final Lock w_lk = rw_lk.writeLock();

	private message_section last_ack_sec;
	private boolean is_fin_msg_bld;

	long dbg_send_mini_sha;

	public message_builder(long n_consec) {
		init_msg_builder();
		msg_consec = n_consec;
	}

	void init_msg_builder() {
		msg_consec = 0;
		tot_bld_by = 0;
		tot_bld_mini_sha = 0;
		tot_recv_mini_sha = 0;

		num_acks = new AtomicInteger(0);
		num_recv_by = 0;

		all_sections = null;
		recv_dat = null;

		last_ack_sec = null;
		is_fin_msg_bld = false;

		dbg_send_mini_sha = 0;
	}

	void ck_all_acks() {
		// must be called inside a lock
		for (message_section sec : all_sections) {
			if (sec.sec_num_ok.get() < 1) {
				throw new bad_udp(2);
			}
		}
	}

	boolean has_all_acks() {
		r_lk.lock();
		try {
			return (num_acks.get() == all_sections.size());
		} finally {
			r_lk.unlock();
		}
	}

	boolean has_all_recv_by() {
		return (num_recv_by == tot_bld_by);
	}

	public String toString() {
		return get_info();
	}

	void ck_recv_dat() {
		if (recv_dat != null) {
			if (tot_recv_mini_sha == 0) {
				tot_recv_mini_sha = convert.calc_minisha_int(recv_dat);
			}
			if (tot_recv_mini_sha != tot_bld_mini_sha) {
				throw new bad_udp(2, "Invalid mini_sha for message");
			}
			if (recv_dat.length != num_recv_by) {
				throw new bad_udp(2);
			}
			if (tot_bld_by != num_recv_by) {
				throw new bad_udp(2);
			}
		}
	}

	String get_info() {
		ck_recv_dat();
		String inf = "." + msg_consec + "(" + num_acks + "<="
				+ all_sections.size() + ")(" + tot_bld_by + "==" + num_recv_by
				+ ")";
		return inf;
	}

	private static List<message_section> split_send_msg(byte[] data) {
		int m_num_by = data.length;
		int num_sec = (m_num_by / message_section.MAX_SEC_DATA_NUM_BY) + 1;
		List<message_section> all_sec = new ArrayList<message_section>(num_sec);
		ByteBuffer buff = ByteBuffer.wrap(data);
		while (buff.hasRemaining()) {
			int rr = buff.remaining();
			int sec_sz = 0;
			if (rr > message_section.MAX_SEC_DATA_NUM_BY) {
				sec_sz = message_section.MAX_SEC_DATA_NUM_BY;
			} else {
				sec_sz = rr;
			}
			message_section sec = new message_section();
			sec.sec_data = new byte[sec_sz];
			buff.get(sec.sec_data);
			all_sec.add(sec);
		}
		return all_sec;
	}

	private static void init_all_send_sec(List<message_section> sec_lst,
			message_section hder) {
		int s_consec = 0;
		for (message_section sec : sec_lst) {
			if (sec.sec_data == null) {
				throw new bad_udp(2);
			}

			sec.msg_src = hder.msg_src;
			sec.msg_dest = hder.msg_dest;

			sec.msg_conn_id = hder.msg_conn_id;
			sec.msg_consec = hder.msg_consec;
			sec.msg_num_by = hder.msg_num_by;
			sec.msg_mini_sha = hder.msg_mini_sha;

			sec.sec_kind = message_section.DAT_KIND;
			sec.set_mini_sha();
			sec.sec_consec = s_consec;

			s_consec++;
		}
	}

	void set_sections(byte[] data) {
		w_lk.lock();
		try {
			if (IN_DEBUG_2) {
				dbg_send_mini_sha = convert.calc_minisha_long(data);
			}
			tot_bld_by = data.length;
			all_sections = split_send_msg(data);
			if (data == msg_conn.CLOSE_MSG) {
				is_fin_msg_bld = true;
			}
		} finally {
			w_lk.unlock();
		}
	}

	boolean is_fin_msg() {
		return is_fin_msg_bld;
	}

	void init_sections(message_section hd) {
		w_lk.lock();
		try {
			init_all_send_sec(all_sections, hd);
			int tot_sec = hd.tot_sec_in_msg();
			if (all_sections.size() != tot_sec) {
				throw new bad_udp(2);
			}
		} finally {
			w_lk.unlock();
		}
	}

	void send_sections(DatagramSocket sok, msg_conn conn) {
		send_stats stats = conn.stats;
		dbg_conn_stats dbg_stats = conn.dbg_stats;
		if (stats.sent_max()) {
			return;
		}
		if (IN_DEBUG_1) {
			if (is_fin_msg()) {
				dbg_stats.tot_send_close_msg.incrementAndGet();
				logger.debug("SENDING_CLOSE_MSG " + conn);
				//+ conn.get_info(true));
				// + conn.get_manager().mg_stats.get_info());
			}
		}
		r_lk.lock();
		try {
			for (message_section sec : all_sections) {
				if (sec.sec_num_ok.get() < 1) {
					sec.send_section(sok);
					stats.inc_num_sent();
					if (IN_DEBUG_10) {
						dbg_stats.tot_dat_sent.incrementAndGet();
					}
					if (stats.sent_max()) {
						return;
					}
				}
			}
		} finally {
			r_lk.unlock();
		}
	}

	message_section get_recv_ck_sec(mudp_connection conn) {
		message_section sec = new message_section();
		sec.msg_num_by = tot_bld_by;
		sec.msg_conn_id = conn.get_conn_id();
		sec.sec_kind = message_section.DAT_KIND;
		sec.msg_consec = msg_consec;
		sec.msg_dest = conn.get_local_peer();
		sec.msg_src = conn.get_remote_peer();
		return sec;
	}

	byte[] get_recv_data(mudp_connection conn) {
		if (recv_dat != null) {
			ck_recv_dat();
			return recv_dat;
		}
		message_section sec_ck = get_recv_ck_sec(conn);
		byte[] arr_data = new byte[tot_bld_by];
		Arrays.fill(arr_data, (byte) 0);
		ByteBuffer buff = ByteBuffer.wrap(arr_data);
		int consec = 0;
		for (message_section sec : all_sections) {
			sec.ck_recv_with(sec_ck, consec);
			buff.put(sec.sec_data);
			consec++;
		}
		if (buff.hasRemaining()) {
			throw new bad_udp(2);
		}
		recv_dat = arr_data;
		ck_recv_dat();
		return recv_dat;
	}

	message_section get_section(int idx_sec) {
		message_section orig_sec = null;
		r_lk.lock();
		try {
			if ((idx_sec >= 0) || (idx_sec < all_sections.size())) {
				orig_sec = all_sections.get(idx_sec);
			}
		} finally {
			r_lk.unlock();
		}
		return orig_sec;
	}

	void add_section(message_section sec) {
		w_lk.lock();
		try {
			if (all_sections == null) {
				int tot_sec = sec.tot_sec_in_msg();
				if (tot_sec > msg_conn.MAX_RECV_NUM_SEC) {
					throw new bad_udp(2);
				}
				all_sections = new ArrayList<message_section>(tot_sec);
				while (all_sections.size() < tot_sec) {
					all_sections.add(null);
				}
			}
			int idx_sec = sec.sec_consec;
			if ((idx_sec < 0) || (idx_sec >= all_sections.size())) {
				throw new bad_udp(2);
			}
			if (sec.msg_mini_sha != tot_bld_mini_sha) {
				throw new bad_udp(2, "Invalid section message total mini_sha");
			}
			if (all_sections.get(idx_sec) == null) {
				num_recv_by += sec.sec_data.length;
				if (has_all_recv_by()) {
					last_ack_sec = sec;
				}
			}
			all_sections.set(idx_sec, sec);
		} finally {
			w_lk.unlock();
		}
	}

	message_section get_last_ack_sec() {
		if (last_ack_sec == null) {
			throw new bad_udp(2);
		}
		return last_ack_sec;
	}

	boolean is_last_ack_sec(message_section sec) {
		if (last_ack_sec == null) {
			return false;
		}
		boolean same = ((last_ack_sec.msg_consec == sec.msg_consec) && (last_ack_sec.sec_consec == sec.sec_consec));
		if (same && (last_ack_sec.sec_mini_sha != sec.sec_mini_sha)) {
			throw new bad_udp(2);
		}
		return same;
	}
}
