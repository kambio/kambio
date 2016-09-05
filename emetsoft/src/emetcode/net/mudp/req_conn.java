package emetcode.net.mudp;

import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import emetcode.util.devel.logger;

public class req_conn extends mudp_connection {
	private static final boolean IN_DEBUG_1 = false;
	private static final boolean IN_DEBUG_2 = false;
	private static final boolean IN_DEBUG_3 = false; // accept
	private static final boolean IN_DEBUG_4 = false; // null_request

	public static final String KIND_req_conn = "KIND_req_conn";

	private static final int MAX_NUM_REQ = 100;

	private static final int MAX_NUM_USR_SEND = 1;
	private static final int MAX_NUM_USR_RECV = 1;

	private static final byte[] NULL_REQ_MSG = "MUDP_NULL_REQ_MESSAGE"
			.getBytes(config.UTF_8);

	private long start_time;
	private long wait_time;
	private long last_send_tm;

	private long req_tm_out;
	private TimeUnit req_tmo_unit;

	private long cokey_id;

	private message_section req_sec;
	private message_section accept_sec;
	private DatagramSocket resp_sok;

	private mudp_peer requester_peer;

	private BlockingQueue<byte[]> usr_send;
	private BlockingQueue<byte[]> usr_recv;

	private BlockingQueue<message_section> all_req;

	private AtomicBoolean got_ans;

	private static final int SECS_TIMEOUT_REQ = 15;
	private static final int SECS_TIMEOUT_OFFER_STOP = 30;
	private static final byte[] ABORT_MSG = "MUDP_ABORT_MESSAGE"
			.getBytes(config.UTF_8);
	private static final message_section STOP_SEC = new message_section();

	req_conn(conns_remote_peer pp, long cnn_id) {
		super(pp, cnn_id);
		init_req_conn();
	}

	req_conn(conns_remote_peer pp) {
		super(pp);
		init_req_conn();
	}

	void init_req_conn() {
		start_time = 0;
		wait_time = 0;
		last_send_tm = 0;

		req_tm_out = SECS_TIMEOUT_REQ;
		req_tmo_unit = TimeUnit.SECONDS;

		cokey_id = 0;

		req_sec = null;
		accept_sec = null;
		resp_sok = null;

		requester_peer = null;

		usr_send = new LinkedBlockingQueue<byte[]>(MAX_NUM_USR_SEND);
		usr_recv = new LinkedBlockingQueue<byte[]>(MAX_NUM_USR_RECV);

		all_req = new LinkedBlockingQueue<message_section>(MAX_NUM_REQ);

		got_ans = new AtomicBoolean(false);
	}

	void set_key_id(long kk_id) {
		cokey_id = kk_id;
	}

	public void set_req_timeout(long tm_out, TimeUnit uu) {
		req_tm_out = tm_out;
		req_tmo_unit = uu;
	}

	private message_section get_req_section(byte[] req_dat) {
		if (req_dat.length > message_section.MAX_SEC_DATA_NUM_BY) {
			throw new bad_udp(2);
		}
		if (server_conn_id == 0) {
			throw new bad_udp(2);
		}

		byte[] data = Arrays.copyOf(req_dat, req_dat.length);
		message_section sec = get_send_message_header(data);
		sec.sec_kind = message_section.REQ_KIND;
		sec.msg_conn_id = server_conn_id;
		sec.msg_consec = conn_id;
		sec.req_key_id = cokey_id;
		sec.sec_data = data;
		sec.set_mini_sha();

		return sec;
	}

	private long get_wait_time() {
		long curr_tm = get_manager().current_send_date;
		if (start_time == 0) {
			start_time = curr_tm;
		}
		long tot_tm = curr_tm - start_time;
		wait_time = 10;
		if (tot_tm > wait_time) {
			wait_time = tot_tm;
		}
		if (tot_tm > 1000) {
			wait_time = 1000;
		}
		return wait_time;
	}

	boolean is_send_tm(long curr_tm) {
		if (last_send_tm == 0) {
			last_send_tm = curr_tm;
		}
		long diff = curr_tm - last_send_tm;
		boolean is_stm = (diff > wait_time);
		return is_stm;
	}

	long timeout_as_millis() {
		long in_millis = TimeUnit.MILLISECONDS
				.convert(req_tm_out, req_tmo_unit);
		return in_millis;
	}

	private boolean has_timeout() {
		if (is_server()) {
			return false;
		}
		long curr_tm = get_manager().current_send_date;
		time_last_activ.compareAndSet(0, curr_tm);
		long inactiv_tm = curr_tm - time_last_activ.get();
		long tmo_millis = timeout_as_millis();
		if (inactiv_tm > tmo_millis) {
			if (IN_DEBUG_2) {
				logger.info("INACTIVE_TIMEOUT_REQ=" + this
						+ " time_last_activ=" + time_last_activ.get()
						+ " curr_tm=" + curr_tm + " inactiv_tm=" + inactiv_tm);
			}
			return true;
		}
		return false;
	}

	long sender_main() {
		if (got_ans.get()) {
			return -1;
		}
		if (has_timeout()) {
			close();
			return -1;
		}
		if (req_sec == null) {
			byte[] dat = usr_send.poll();
			if (dat == null) {
				return -1;
			}
			req_sec = get_req_section(dat);
			req_sec.send_section(get_sok());
		} else {
			long curr_tm = System.currentTimeMillis();
			if (is_send_tm(curr_tm)) {
				last_send_tm = curr_tm;
				req_sec.send_section(get_sok());
			}
		}
		return get_wait_time();
	}

	void recv_ans_section(message_section sec) {
		if (got_ans.get()) {
			return;
		}
		if (sec.sec_kind != message_section.ANS_KIND) {
			if (IN_DEBUG_1) {
				logger.debug("Not an ANS section in req_conn");
			}
			return;
		}
		if (!sec.ck_mini_sha()) {
			if (IN_DEBUG_1) {
				logger.debug("Bad mini sha. Corrupted message section");
			}
			return;
		}
		boolean ok = usr_recv.offer(sec.sec_data);
		if (ok) {
			got_ans.set(true);
		}
	}

	private byte[] recv_ans() {
		byte[] dat = null;
		try {
			dat = usr_recv.poll(req_tm_out, req_tmo_unit);
			if (IN_DEBUG_4) {
				if(dat == null){
					logger.debug("NULL_POLL_(timeout)_mudp_recv_ans");
				}
			}
		} catch (InterruptedException e) {
			logger.debug("Interrupted receive");
			throw new bad_udp(2);
		}
		if ((dat != null) && (dat.length == NULL_REQ_MSG.length)
				&& Arrays.equals(dat, NULL_REQ_MSG)) {
			dat = null;
			if (IN_DEBUG_4) {
				logger.debug("NULL_MSG_mudp_recv_ans");
			}
		}
		if (dat == ABORT_MSG) {
			dat = null;
			if (IN_DEBUG_4) {
				logger.debug("ABORT_mudp_recv_ans");
			}
		}
		return dat;
	}

	private void send_req(byte[] dat) {
		if (dat == null) {
			dat = NULL_REQ_MSG;
		}
		try {
			usr_send.put(dat);
			get_manager().has_work.offer(true);
		} catch (InterruptedException e) {
			logger.debug("Interrupted send");
			throw new bad_udp(2);
		}
	}

	public byte[] request(byte[] dat) {
		if (is_server()) {
			throw new bad_udp(2);
		}
		send_req(dat);
		byte[] resp = recv_ans();
		if (IN_DEBUG_4) {
			logger.debug("GOT_NULL_mudp_request");
		}
		return resp;
	}

	private static message_section wait_for_section(
			BlockingQueue<message_section> qq) {
		message_section sec = null;
		try {
			// logger.info("waiting for TAKE");
			sec = qq.take();
			// logger.info("got TAKE");
		} catch (InterruptedException ex) {
			logger.error(ex, "during udp process_syn");
		}
		if (sec == null) {
			return null;
		}
		if (sec == STOP_SEC) {
			return STOP_SEC;
		}
		if (!sec.is_syn() && !sec.is_req()) {
			throw new bad_udp(2);
		}
		if (!sec.ck_mini_sha()) {
			logger.debug("Bad mini sha. Corrupted message section");
			return null;
		}
		return sec;
	}

	public mudp_connection accept() {
		if (!is_server()) {
			throw new bad_udp(2);
		}
		if (is_closed()) {
			return null;
		}
		if (IN_DEBUG_3) {
			logger.info("waiting for ACCEPT srv_conn=" + this);
		}
		message_section sec = wait_for_section(all_req);
		if (IN_DEBUG_3) {
			logger.info("waiting for ACCEPT srv_conn=" + this);
		}
		if (sec == null) {
			return null;
		}
		if (sec == STOP_SEC) {
			stop_conn();
			return null;
		}

		long n_conn_id = sec.msg_consec;
		if (is_server(n_conn_id)) {
			logger.debug("n_conn_id is server !!!!!");
			return null;
		}

		req_conn n_conn = new req_conn(null, n_conn_id);

		n_conn.requester_peer = sec.msg_src;

		n_conn.accept_sec = sec;
		n_conn.cokey_id = sec.req_key_id;
		if (n_conn.accept_sec == null) {
			throw new bad_udp(2);
		}
		n_conn.resp_sok = get_sok();

		if (IN_DEBUG_1) {
			logger.debug("ACCEPTED_REQ_CONN	 (" + sec.msg_conn_id + ") in "
					+ conn_id + " conn_id=" + n_conn_id + " requester="
					+ n_conn.requester_peer + " cokey_id=" + n_conn.cokey_id);
		}

		get_manager().has_work.offer(true);
		return n_conn;
	}

	public mudp_peer get_remote_peer() {
		if (is_client_data_conn()) {
			return super.get_remote_peer();
		}
		if (requester_peer == null) {
			throw new bad_udp(2);
		}
		return requester_peer;
	}

	void recv_req(message_section sec) {
		if (!is_server()) {
			throw new bad_udp(2);
		}
		if (sec == null) {
			throw new bad_udp(2);
		}
		boolean ok = all_req.offer(sec);
		if (ok) {
			if (IN_DEBUG_1) {
				logger.debug("GOT REQ FOR ACCEPT (" + sec.msg_conn_id + ") in "
						+ conn_id);
			}
		}
	}

	public byte[] get_request() {
		if (is_server()) {
			throw new bad_udp(2);
		}
		if (accept_sec == null) {
			throw new bad_udp(2);
		}
		byte[] dat = accept_sec.sec_data;
		if ((dat != null) && (dat.length == NULL_REQ_MSG.length)
				&& Arrays.equals(dat, NULL_REQ_MSG)) {
			dat = null;
		}
		return dat;
	}

	public void respond(byte[] dat) {
		if (is_server()) {
			throw new bad_udp(2);
		}
		if (accept_sec == null) {
			throw new bad_udp(2);
		}
		if (dat == null) {
			dat = NULL_REQ_MSG;
		}

		message_section sec = accept_sec.get_resp_section(conn_id, dat);
		sec.send_section(resp_sok);

		accept_sec = null;
	}

	public void close() {
		stop_conn();
	}

	public void stop_conn() {
		if (got_ans.compareAndSet(false, true)) {
			if (IN_DEBUG_1) {
				logger.debug("stopping_1=" + this);
			}
			while (usr_send.poll() != null)
				;
			try {
				boolean ok1 = usr_recv.offer(ABORT_MSG,
						SECS_TIMEOUT_OFFER_STOP, TimeUnit.SECONDS);
				boolean ok2 = all_req.offer(STOP_SEC, SECS_TIMEOUT_OFFER_STOP,
						TimeUnit.SECONDS);
				if (IN_DEBUG_1) {
					logger.debug("stopping_2=" + this);
				}
				if (!ok1) {
					throw new bad_udp(2, "TIMEOUT WHILE STOPPING MUDP CONN="
							+ this);
				}
				if (!ok2) {
					throw new bad_udp(2, "TIMEOUT WHILE STOPPING MUDP CONN="
							+ this);
				}
			} catch (Exception ee1) {
				logger.info("GOT EXCEPTION \n" + ee1
						+ "\n while stopping mudp conn=" + this);
			}
			if (IN_DEBUG_1) {
				logger.debug("MUDP_REQS_STOPED=" + this);
			}
		}
		get_manager().has_work.offer(true);
	}

	public boolean is_closed() {
		return got_ans.get();
	}

	public long get_cokey_id() {
		return cokey_id;
	}

	public String get_conn_kind() {
		return KIND_req_conn;
	}

}
