package emetcode.net.mudp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import emetcode.crypto.bitshake.utils.convert;
import emetcode.crypto.bitshake.utils.mer_twist;
import emetcode.util.devel.logger;

public class message_section {
	public static boolean IN_DEBUG_1 = false;
	static final boolean IN_DEBUG_2 = false;
	static final boolean IN_DEBUG_3 = true;

	static final byte DAT_KIND = 66;
	static final byte ACK_KIND = 77;
	static final byte SYN_KIND = 88;

	static final byte REQ_KIND = 11;
	static final byte ANS_KIND = 22;

	static final String DAT_STR = "DAT";
	static final String ACK_STR = "ACK";
	static final String SYN_STR = "SYN";

	static final String REQ_STR = "REQ";
	static final String ANS_STR = "ANS";

	static final int MAX_SEC_DATA_NUM_BY = 1000;
	static final int MAX_SEC_NUM_BY = 1100;
	static final int MIKID_NUM_BITS = emetcode.crypto.bitshake.utils.key_owner.NUM_BITS_MINI_SHA;

	static final mer_twist dbg_rnd = new mer_twist();
	static final List<message_section> dbg_all_sec_sent = new LinkedList<message_section>();
	static boolean dbg_doing_send = false;

	public static final int MIKID_NUM_BY = MIKID_NUM_BITS / Byte.SIZE;

	mudp_peer msg_src;
	mudp_peer msg_dest;

	byte sec_kind;

	long msg_conn_id;
	long msg_consec;
	int msg_num_by;
	int msg_mini_sha;

	private long sec_time;
	int sec_mini_sha;
	int sec_consec;

	long req_key_id;

	byte[] sec_data;

	AtomicInteger sec_num_ok;
	AtomicInteger sec_num_times_sent;
	long sec_recv_time;

	message_section() {
		init_msg_section();
	}

	message_section(mudp_peer loc_peer, DatagramPacket pak) {
		init_msg_section();

		msg_src = new mudp_peer(pak.getAddress(), pak.getPort());
		msg_dest = loc_peer;

		byte[] buff = pak.getData();
		ByteBuffer dat = ByteBuffer.wrap(buff);

		sec_kind = dat.get();

		msg_conn_id = dat.getLong();
		msg_consec = dat.getLong();
		msg_num_by = dat.getInt();
		msg_mini_sha = dat.getInt();

		sec_time = dat.getLong();
		sec_mini_sha = dat.getInt();
		sec_consec = dat.getInt();

		if (is_req()) {
			req_key_id = dat.getLong();
		}

		sec_data = null;
		int dat_sz = dat.getInt();
		if (dat_sz > MAX_SEC_DATA_NUM_BY) {
			throw new bad_udp(2);
		}
		sec_data = new byte[dat_sz];
		dat.get(sec_data);
	}

	void init_msg_section() {
		msg_src = null;
		msg_dest = null;

		sec_kind = 0;

		msg_conn_id = 0;
		msg_consec = 0;
		msg_num_by = 0;
		msg_mini_sha = 0;

		sec_time = 0;
		sec_mini_sha = 0;
		sec_consec = 0;

		req_key_id = 0;

		sec_data = null;

		sec_num_ok = new AtomicInteger(0);
		sec_num_times_sent = new AtomicInteger(0);
		sec_recv_time = 0;
	}

	boolean from_self() {
		return msg_src.equals(msg_dest);
	}

	boolean ck_msg_section() {
		boolean c1 = (msg_src != null);
		boolean c2 = (msg_dest != null);

		boolean c3 = (sec_kind != 0);

		boolean c4 = (msg_conn_id != 0);
		boolean c5 = (msg_consec != 0);
		boolean c6 = (msg_num_by != 0);

		boolean c7 = (sec_time != 0);
		boolean c8 = (sec_consec != 0);
		boolean c9 = (sec_data != null);

		boolean all_co = (c1 && c2 && c3 && c4 && c5 && c6 && c7 && c8 && c9);
		return all_co;
	}

	public DatagramPacket as_datagram() {
		ck_msg_section();

		byte[] buff = new byte[MAX_SEC_NUM_BY];
		ByteBuffer dat = ByteBuffer.wrap(buff);

		dat.put(sec_kind);

		dat.putLong(msg_conn_id);
		dat.putLong(msg_consec);
		dat.putInt(msg_num_by);
		dat.putInt(msg_mini_sha);

		dat.putLong(sec_time);
		dat.putInt(sec_mini_sha);
		dat.putInt(sec_consec);

		if (is_req()) {
			dat.putLong(req_key_id);
		}

		if (sec_data.length > MAX_SEC_DATA_NUM_BY) {
			throw new bad_udp(2);
		}
		dat.putInt(sec_data.length);
		dat.put(sec_data);

		DatagramPacket pak = null;
		//try {
			pak = new DatagramPacket(buff, dat.position(), msg_dest.sok_addr);
		//} catch (SocketException e) {
		//	throw new bad_udp(2);
		//}
		return pak;
	}

	public int tot_sec_in_msg() {
		int rema = (msg_num_by % MAX_SEC_DATA_NUM_BY);
		int nm_sec = (msg_num_by / MAX_SEC_DATA_NUM_BY);
		if (rema > 0) {
			nm_sec++;
		}
		if (sec_consec > nm_sec) {
			throw new bad_udp(2);
		}
		return nm_sec;
	}

	boolean ck_mini_sha() {
		int misha = convert.calc_minisha_int(sec_data);
		boolean eq1 = (misha == sec_mini_sha);
		return eq1;
	}

	public void set_mini_sha() {
		sec_mini_sha = convert.calc_minisha_int(sec_data);
	}

	private message_section get_ack_section() {
		message_section ack_sec = new message_section();
		ack_sec.msg_src = msg_dest;
		ack_sec.msg_dest = msg_src;

		ack_sec.sec_kind = ACK_KIND;

		ack_sec.msg_conn_id = msg_conn_id;
		ack_sec.msg_consec = msg_consec;
		ack_sec.msg_num_by = msg_num_by;

		ack_sec.sec_time = sec_time;
		ack_sec.sec_consec = sec_consec;
		byte[] dat = convert.to_byte_array(ack_sec.msg_conn_id);
		ack_sec.sec_data = dat;
		ack_sec.set_mini_sha();
		return ack_sec;
	}

	boolean send_ack_section(DatagramSocket sok) {
		message_section ack = get_ack_section();
		if (!sok.isClosed()) {
			ack.send_section(sok);
			return true;
		}
		return false;
	}

	message_section get_resp_section(long conn_id, byte[] dat) {
		message_section resp_sec = new message_section();
		resp_sec.msg_src = msg_dest;
		resp_sec.msg_dest = msg_src;

		resp_sec.sec_kind = message_section.ANS_KIND;

		resp_sec.msg_conn_id = conn_id;
		resp_sec.msg_num_by = dat.length;

		resp_sec.sec_data = dat;
		resp_sec.set_mini_sha();
		resp_sec.sec_time = sec_time;
		return resp_sec;
	}

	void send_section(DatagramSocket dsok) {
		if (IN_DEBUG_2) {
			if (!dbg_doing_send && (sec_kind == DAT_KIND)) {
				dbg_add_sent(this);
				return;
			}
		}
		if (IN_DEBUG_1) {
			logger.debug("SENDING--" + toString());
		}
		if (from_self()) {
			if (IN_DEBUG_3) {
				String stk = logger.get_stack_str();
				logger.debug("SKIPPED_SELF_SENDING--" + toString() + " src="
						+ msg_src + " dest=" + msg_dest + " stk=\n" + stk);
			}
			return;
		}
		if (!is_ack() && !is_ans()) {
			sec_time = System.currentTimeMillis();
		}
		sec_num_times_sent.incrementAndGet();
		DatagramPacket pak = as_datagram();
		try {
			dsok.send(pak);
		} catch (IOException ex) {
			logger.error(ex, "during udp send_section");
			// throw new bad_udp(2);
		}
	}

	static message_section receive_section(mudp_peer loc_pp, DatagramSocket dsok) {
		byte[] buff = new byte[MAX_SEC_NUM_BY];
		DatagramPacket pak = new DatagramPacket(buff, buff.length);
		message_section sec = null;
		try {
			dsok.receive(pak);
			sec = new message_section(loc_pp, pak);
		} catch (IOException ex) {
			// logger.error(ex, "during udp receive_section");
			// throw new bad_udp(2);
		}
		if (sec == null) {
			return null;
		}
		if (sec.is_ack() || sec.is_ans()) {
			sec.sec_recv_time = System.currentTimeMillis();
		}
		if (IN_DEBUG_1) {
			logger.debug("RECEIVING--" + sec.toString());
		}
		return sec;
	}

	long get_round_trip_time() {
		if (!is_ack() && !is_ans()) {
			throw new bad_udp(2);
		}
		return (sec_recv_time - sec_time);
	}

	public static String kind_as_str(int kk) {
		String str = "INVALID_KIND";
		switch (kk) {
		case DAT_KIND:
			str = DAT_STR;
			break;
		case ACK_KIND:
			str = ACK_STR;
			break;
		case SYN_KIND:
			str = SYN_STR;
			break;
		case REQ_KIND:
			str = REQ_STR;
			break;
		case ANS_KIND:
			str = ANS_STR;
			break;
		}
		return str;
	}

	public String toString() {
		// int ssz = 0;
		// if (sec_data != null) {
		// ssz = sec_data.length;
		// }
		String mm = kind_as_str(sec_kind);
		if (is_close_sec()) {
			mm += ".CLOSE_SEC." + msg_conn_id;
		} else if (is_abort_sec()) {
			mm += ".ABORT_SEC." + msg_conn_id;
		} else if (is_syn() || is_req() || is_ans()) {
			mm += "." + msg_conn_id;
		} else {
			mm += "." + msg_consec + "." + sec_consec;
		}
		// String id = Integer.toHexString(System.identityHashCode(this));
		// String sstr1 = "(" + id + ")" + kk + ".conn:" + msg_conn_id + ".ii:"
		// + msg_consec + ".isz:" + msg_num_by + ".kk:" + sec_consec
		// + ".ksz:" + ssz + ".src:" + msg_src.toString() + ".dest:"
		// + msg_dest.toString();
		return mm;
	}

	public boolean is_syn() {
		return (sec_kind == SYN_KIND);
	}

	public boolean is_dat() {
		return (sec_kind == DAT_KIND);
	}

	public boolean is_ack() {
		return (sec_kind == ACK_KIND);
	}

	public boolean is_req() {
		return (sec_kind == REQ_KIND);
	}

	public boolean is_ans() {
		return (sec_kind == ANS_KIND);
	}

	public boolean is_close_sec() {
		boolean is_clo = (is_dat()
				&& (sec_data.length == msg_conn.CLOSE_MSG.length) && Arrays
				.equals(sec_data, msg_conn.CLOSE_MSG));
		return is_clo;
	}

	public boolean is_abort_sec() {
		boolean is_abo = (is_dat()
				&& (sec_data.length == msg_conn.ABORT_MSG.length) && Arrays
				.equals(sec_data, msg_conn.ABORT_MSG));
		return is_abo;
	}

	void ck_recv_with(message_section sec_ck, int consec) {
		if (msg_num_by != sec_ck.msg_num_by) {
			throw new bad_udp(2);
		}
		if (msg_conn_id != sec_ck.msg_conn_id) {
			throw new bad_udp(2);
		}
		if (sec_kind != sec_ck.sec_kind) {
			throw new bad_udp(2);
		}
		if (sec_consec != consec) {
			throw new bad_udp(2);
		}
		if (msg_consec != sec_ck.msg_consec) {
			throw new bad_udp(2);
		}
		if (!msg_dest.equals(sec_ck.msg_dest)) {
			throw new bad_udp(2);
		}
		if (!msg_src.equals(sec_ck.msg_src)) {
			throw new bad_udp(2);
		}
	}

	private static void dbg_add_sent(message_section sec) {
		int vv = dbg_rnd.nextInt();
		int idx = (int) (convert.to_interval(vv, 0, dbg_all_sec_sent.size()));
		dbg_all_sec_sent.add(idx, sec);
	}

	static void dbg_send_all(DatagramSocket dsok) {
		dbg_doing_send = true;
		while (!dbg_all_sec_sent.isEmpty()) {
			message_section sec = dbg_all_sec_sent.remove(0);
			sec.send_section(dsok);
		}
		dbg_doing_send = false;
	}

	static boolean dbg_drop_sec() {
		int vv = dbg_rnd.nextInt();
		long bool_1 = convert.to_interval(vv, 0, 2);
		return (bool_1 == 1);
	}

}
