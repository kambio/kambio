package emetcode.net.netmix;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import emetcode.crypto.bitshake.bitshaker;
import emetcode.crypto.bitshake.cryper;
import emetcode.crypto.bitshake.utils.convert;
import emetcode.crypto.bitshake.utils.mem_file;
import emetcode.net.mudp.bad_udp;
import emetcode.net.netmix.locale.L;
import emetcode.util.devel.bad_emetcode;
import emetcode.util.devel.logger;

public abstract class nx_connection {

	public static final boolean IN_DEBUG_1 = false; // check encryp

	private static final String UTF_8_NAM = "UTF-8";
	private static final Charset UTF_8 = Charset.forName(UTF_8_NAM);

	private static final int MAX_BYTES_INT_STR = 100;
	private static final byte[] INT_BUFF = new byte[MAX_BYTES_INT_STR];

	protected abstract InputStream get_InputStream();

	protected abstract OutputStream get_OutputStream();

	public abstract nx_peer get_local_peer();

	public abstract nx_peer get_remote_peer();

	public abstract void close_net_connection();

	public abstract void stop_net_connection();
	
	public abstract boolean is_closed();

	private int max_msg_num_bytes;
	private BufferedInputStream in_stm;
	private BufferedOutputStream out_stm;
	private cryper session_cryper;

	private long dbg_num_recv;
	private long dbg_num_send;

	public nx_connection() {
		init_nx_connection();
	}

	void init_nx_connection() {
		max_msg_num_bytes = 0;
		in_stm = null;
		out_stm = null;
		session_cryper = null;

		dbg_num_recv = 0;
		dbg_num_send = 0;
	}

	public void init_streams() {
		in_stm = new BufferedInputStream(get_InputStream());
		out_stm = new BufferedOutputStream(get_OutputStream());
	}

	void set_cryper(byte[] kk) {
		if(session_cryper != null){
			throw new bad_netmix(2);
		}
		session_cryper = new cryper(kk);
	}

	void reset_cryper() {
		if(session_cryper == null){
			throw new bad_netmix(2);
		}
		session_cryper = null;
	}

	boolean has_cryper() {
		return (session_cryper != null);
	}

	cryper get_cryper() {
		return session_cryper;
	}

	public int get_msg_max_num_bytes() {
		return max_msg_num_bytes;
	}

	private void recv_exact_bytes(byte[] bts) throws IOException {
		byte[] rcv_arr = new byte[bts.length];
		int d_sz = mem_file.read_stream(in_stm, rcv_arr, 0);
		if (d_sz < rcv_arr.length) {
			throw new bad_netmix(2, String.format(
					L.invalid_exact_bytes_bad_len, "" + d_sz, ""
							+ rcv_arr.length));
		}
		if (!Arrays.equals(rcv_arr, bts)) {
			throw new bad_netmix(2, L.invalid_exact_bytes);
		}
	}

	private int recv_int_string() throws IOException {
		int nxt = -1;
		byte[] bts = new byte[INT_BUFF.length];
		//byte[] bts = INT_BUFF;
		int off = 0;
		while ((nxt = in_stm.read()) != -1) {
			if (off >= bts.length) {
				throw new bad_netmix(2, L.cannot_recv_int_str);
			}
			bts[off] = (byte) nxt;
			if (nxt == config.DN_EOL) {
				break;
			}
			off++;
		}
		String val_str = new String(bts, 0, off, UTF_8);
		return convert.parse_int(val_str);
	}

	public byte[] recv_byte_array() {
		byte[] msg = null;
		try {
			if (has_secure_conn()) {
				recv_exact_bytes(config.DN_FIRST_BYTES);
			} else {
				recv_exact_bytes(config.DN_PROT_ID_BYTES);
			}
			int msg_sz = recv_int_string();
			if (msg_sz > 0) {
				msg = new byte[msg_sz];
				int d_sz = mem_file.read_stream(in_stm, msg, 0);
				if (d_sz < msg.length) {
					throw new bad_netmix(2, String.format(L.bad_msg_szs, ""
							+ d_sz, "" + msg.length));
				}
			}
			recv_exact_bytes(config.DN_LAST_BYTES);
		} catch (IOException ex1) {
			throw new bad_netmix(2);
		}
		return msg;
	}

	static String get_prt_dbg_str(long num_nt_op, boolean is_send, boolean is_enc, byte[] msg) {
		long msg_id = 0;
		if (msg != null) {
			msg_id = convert.calc_minisha_long(msg);
		}
		// long key_id = session_cryper.calc_key_id();
		String pref = num_nt_op + "_RECV_";
		if (is_send) {
			pref = num_nt_op + "_SEND_";
		}
		String enc = "";
		if (is_enc) {
			enc = "ENC_";
		}
		pref += enc;
		String dbg_str = pref + " m_len=" + msg.length + " m_id=" + msg_id;
		// + "  kk_id=" + key_id;
		return dbg_str;
		// System.out.println(dbg_str);
	}

	private void prt_dbg_ids(boolean is_send, boolean is_enc, byte[] msg) {
		if (!has_secure_conn()) {
			return;
		}
		long num_nt_op = dbg_num_recv;
		if (is_send) {
			num_nt_op = dbg_num_send;
		}
		String dbg_str = get_prt_dbg_str(num_nt_op, is_send, is_enc, msg);
		if (dbg_str != null) {
			logger.debug(dbg_str);
		}
	}

	byte[] secure_recv_bytes() {
		if (!is_valid()) {
			throw new bad_netmix(2, L.cannot_recv_bytes);
		}
		byte[] msg = recv_byte_array();
		if (IN_DEBUG_1) {
			dbg_num_recv++;
			prt_dbg_ids(false, true, msg);
		}
		if (has_secure_conn() && (msg != null)) {
			try {
				byte[] dec_msg = bitshaker.decrypt_bytes_with_sha(msg,
						session_cryper);
				msg = dec_msg;
			} catch (bad_emetcode ex) {
				logger.error(ex, get_prt_dbg_str(dbg_num_recv, false, false, msg));
				throw new bad_netmix(2, get_prt_dbg_str(dbg_num_recv, false, false, msg));
			}
			if (IN_DEBUG_1) {
				prt_dbg_ids(false, false, msg);
			}
		}
		if ((msg != null) && (msg.length > max_msg_num_bytes)) {
			max_msg_num_bytes = msg.length;
		}
		return msg;
	}

	public void send_byte_array(byte[] msg) {
		try {
			if (has_secure_conn()) {
				out_stm.write(config.DN_FIRST_BYTES);
			} else {
				out_stm.write(config.DN_PROT_ID_BYTES);
			}

			int m_sz = 0;
			if (msg != null) {
				m_sz = msg.length;
			}

			String sz_str = "" + m_sz + config.DN_EOL;
			out_stm.write(sz_str.getBytes(UTF_8));

			if (msg != null) {
				out_stm.write(msg);
			}

			out_stm.write(config.DN_LAST_BYTES);
			out_stm.flush();
		} catch (IOException ex1) {
			throw new bad_netmix(2, ex1.toString());
		}
	}

	public String get_pt_id() {
		String id = Integer.toHexString(System.identityHashCode(this));
		return id;
	}

	public String toString() {
		String id = get_pt_id();
		return "[CONN." + id + "]" + ".src:" + get_local_peer() + ".dest:"
				+ get_remote_peer();
	}

	void secure_send_bytes(byte[] msg) {
		if (IN_DEBUG_1) {
			dbg_num_send++;
			prt_dbg_ids(true, false, msg);
		}
		if ((msg != null) && (msg.length > max_msg_num_bytes)) {
			max_msg_num_bytes = msg.length;
		}
		if (!is_valid()) {
			throw new bad_netmix(2, L.cannot_send_bytes);
		}
		if (has_secure_conn() && (msg != null)) {
			try {
				byte[] enc_msg = bitshaker.encrypt_bytes_with_sha(msg,
						session_cryper);
				msg = enc_msg;
			} catch (bad_emetcode ex) {
				logger.error(ex, get_prt_dbg_str(dbg_num_send, true, true, msg));
				throw new bad_netmix(2, get_prt_dbg_str(dbg_num_send, true, true, msg));
			}
			if (IN_DEBUG_1) {
				prt_dbg_ids(true, true, msg);
			}
		}
		send_byte_array(msg);
	}

	public byte[] request(byte[] req_msg) {
		throw new bad_netmix(2);
	}

	public void set_req_timeout(long tm_out, TimeUnit uu) {
		throw new bad_udp(2);
	}
	
	public boolean has_secure_conn() {
		return (session_cryper != null);
	}

	public boolean is_valid() {
		boolean c1 = (in_stm != null);
		boolean c2 = (out_stm != null);
		return (c1 && c2);
	}

	public int get_net_type() {
		return get_local_peer().get_net_type();
	}

}
