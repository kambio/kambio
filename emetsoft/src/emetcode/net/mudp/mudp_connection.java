package emetcode.net.mudp;

import java.net.DatagramSocket;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import emetcode.crypto.bitshake.utils.convert;
import emetcode.util.devel.logger;

public class mudp_connection {
	private static final int MAX_SERVER_CONN_ID = 1000000;

	static final mudp_connection FALSE_CONN = new mudp_connection(null, 0);
	static final mudp_connection STOP_CONN = new mudp_connection(null, 0);

	public static final String KIND_mudp_connection = "KIND_mudp_connection";
	
	protected conns_remote_peer remote_conns_dir;
	protected volatile long conn_id;

	volatile long server_conn_id;
	AtomicLong time_last_activ;

	private AtomicBoolean is_removed;

	dbg_conn_stats dbg_stats;
	
	mudp_connection(conns_remote_peer pp) {
		init_mudp_connection(pp, System.currentTimeMillis());
	}

	mudp_connection(conns_remote_peer pp, long id) {
		init_mudp_connection(pp, id);
	}

	private void init_mudp_connection(conns_remote_peer pp, long id) {
		remote_conns_dir = pp;
		conn_id = id;

		server_conn_id = 0;
		time_last_activ = new AtomicLong(0);
		is_removed = new AtomicBoolean(false);

		dbg_stats = new dbg_conn_stats();
	}

	static boolean is_server(long id) {
		return ((id > 0) && (id <= MAX_SERVER_CONN_ID));
	}

	boolean is_client_data_conn() {
		return (server_conn_id != 0);
	}

	void remove_me() {
		if (is_removed.compareAndSet(false, true)) {
			remote_conns_dir.remove_conn(this);
		}
	}

	void stablish() {
		throw new bad_udp(2);
	}

	boolean recv_ack_section(message_section sec) {
		throw new bad_udp(2);
	}

	boolean recv_dat_section(message_section sec) {
		throw new bad_udp(2);
	}

	void recv_ans_section(message_section sec) {
		throw new bad_udp(2);
	}

	List<message_section> recv_all_full() {
		throw new bad_udp(2);
	}

	msg_conn recv_syn(message_section sec) {
		throw new bad_udp(2);
	}

	void recv_req(message_section sec) {
		throw new bad_udp(2);
	}

	long sender_main() {
		throw new bad_udp(2);
	}

	protected mudp_manager get_manager() {
		return remote_conns_dir.local_conns_dir.mgr;
	}

	protected DatagramSocket get_sok() {
		return get_manager().sok;
	}

	protected message_section get_send_message_header(byte[] data) {
		mudp_peer loc_peer = get_local_peer();
		mudp_peer rem_peer = get_remote_peer();

		message_section hd = new message_section();
		hd.msg_src = loc_peer;
		hd.msg_dest = rem_peer;
		hd.msg_conn_id = conn_id;
		hd.msg_num_by = data.length;
		hd.msg_mini_sha = convert.calc_minisha_int(data);
		return hd;
	}

	public String get_pt_id() {
		String id = Integer.toHexString(System.identityHashCode(this));
		return id;
	}

	public String toString() {
		String id = get_pt_id();
		String resp = "(" + id + ")" + conn_id + ".src:" + get_local_peer();
		if (!(this instanceof req_conn) || is_client_data_conn()) {
			resp += ".dest:" + get_remote_peer();
		}
		return resp;
	}

	public mudp_peer get_local_peer() {
		if(remote_conns_dir == null){
			throw new bad_udp(2);
		}
		if(remote_conns_dir.local_conns_dir == null){
			throw new bad_udp(2);
		}
		mudp_peer loc_peer = remote_conns_dir.local_conns_dir.local_peer;
		return loc_peer;
	}

	public mudp_peer get_remote_peer() {
		if(remote_conns_dir == null){
			throw new bad_udp(2);
		}
		mudp_peer rem_peer = remote_conns_dir.remote_peer;
		return rem_peer;
	}

	public long get_conn_id() {
		return conn_id;
	}

	public boolean is_server() {
		return is_server(conn_id);
	}

	public mudp_connection accept() {
		throw new bad_udp(2);
	}

	public byte[] receive() {
		throw new bad_udp(2);
	}

	public void send(byte[] dat) {
		throw new bad_udp(2);
	}

	public void stop_conn() {
		throw new bad_udp(2);
	}
	
	public void close() {
		throw new bad_udp(2);
	}

	public boolean is_closed() {
		logger.info("CALLING_is_closed_of_an_mudp_connection !!!!!");
		return true;
		//throw new bad_udp(2);
	}

	public byte[] request(byte[] dat) {
		throw new bad_udp(2);
	}

	public byte[] get_request() {
		throw new bad_udp(2);
	}

	public void respond(byte[] dat) {
		throw new bad_udp(2);
	}

	public void set_req_timeout(long tm_out, TimeUnit uu) {
		throw new bad_udp(2);
	}

	public String get_conn_kind() {
		return KIND_mudp_connection;
	}
}
