package emetcode.net.mudp;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;

import emetcode.crypto.bitshake.utils.convert;
import emetcode.crypto.bitshake.utils.mer_twist;
import emetcode.util.devel.concurrent_average;
import emetcode.util.devel.logger;

public class TEST_mudp {
	public static final boolean IN_DEBUG_1 = false;

	public static final String UTF_8_NAM = "UTF-8";
	public static final Charset UTF_8 = Charset.forName(UTF_8_NAM);

	public static int MIN_MSG_SZ = 100;
	public static int MAX_MSG_SZ = 10000000;

	public static void main(String[] args) {
		// TEST_format(args);
		// TEST_gen_msg(args);
		// TEST_mudp_peer(args);
		// TEST_lst_remove(args);

		// TEST_udp_sys_1(args);
		TEST_udp_sys_2(args);
		// TEST_avg(args);
		// TEST_oper_times(args);
		// TEST_finally(args);
	}

	public static void TEST_format(String[] args) {
		System.out.println("TEST start");
		long curr_d = System.currentTimeMillis();
		if (mudp_connection.is_server(curr_d)) {
			System.out.println("TIME IS SERVER !!!! " + curr_d);
		} else {
			System.out.println("time ok as conn_id " + curr_d);
		}
	}

	public static void TEST_gen_msg(String[] args) {
		MIN_MSG_SZ = 10;
		MAX_MSG_SZ = 1000;

		mer_twist gg = new mer_twist(System.currentTimeMillis());

		for (int aa = 0; aa < 100; aa++) {
			byte[] arr = get_message(gg);
			String ss = new String(arr, UTF_8);
			System.out.println(ss);
			System.out.println("---------------------------------------------");
		}

	}

	static byte[] get_message(mer_twist gg) {
		int sz = (int) (convert.to_interval(gg.nextLong(), MIN_MSG_SZ,
				MAX_MSG_SZ));
		byte[] msg = new byte[sz];
		ByteBuffer bf = ByteBuffer.wrap(msg);
		while (bf.hasRemaining()) {
			long val = gg.nextLong();
			String vv = "" + val + ".";
			byte[] nxt = vv.getBytes(UTF_8);
			int num_rem = bf.remaining();
			if (num_rem > nxt.length) {
				bf.put(nxt);
			} else {
				bf.put(nxt, 0, num_rem);
			}
		}
		return msg;
	}

	public static void TEST_mudp_peer(String[] args) {
		InetAddress addr = null;
		try {
			// addr = InetAddress.getLocalHost();
			addr = InetAddress.getByName(null);
		} catch (UnknownHostException e) {
			System.out.println("CANNOT GET localhost");
			// throw new bad_udp(2);
		}

		try {
			addr = InetAddress.getByName("localhost");
		} catch (UnknownHostException e) {
			System.out.println("CANNOT GET localhost");
			// throw new bad_udp(2);
		}
		System.out.println("addr=" + addr);
	}

	public static void TEST_lst_remove(String[] args) {
		List<Integer> ll = new ArrayList<Integer>();
		ll.add(1);
		ll.add(2);
		ll.add(3);
		System.out.println(ll.toString());
		ll.remove(0);
		System.out.println(ll.toString());
	}

	public static void TEST_avg(String[] args) {
		if (args.length < 1) {
			System.out.println("args: <num>");
			return;
		}
		concurrent_average pp = new concurrent_average();
		// pp.reset(100);

		int vv = Integer.parseInt(args[0]);
		for (int aa = 0; aa < 10000; aa++) {
			pp.add_val(vv);
		}

		System.out.println("avg=" + pp.get_avg());
	}

	public static int get_aa() {
		try {
			int aa = 2;
			if (aa == 1) {
				System.out.println("throwing err");
				throw new bad_udp(2);
			}
			return 15;
		} catch (bad_udp ex) {
			System.out.println("cought bad_udp");
		} finally {
			System.out.println("doing finally");
		}
		return 23;
	}

	public static void TEST_finally(String[] args) {
		System.out.println("aa=" + get_aa());
	}

	public static void TEST_udp_sys_2(String[] args) {
		boolean is_serv = false;
		if (args.length < 1) {
			System.out.println("TEST_udp_sys_2. args: (-c|-s)");
			return;
		}
		if (args[0].equals("-s")) {
			is_serv = true;
		}

		// mer_twist gg = new mer_twist(System.currentTimeMillis());

		int udp_port = 5555;
		if (is_serv) {
			udp_port++;
		}

		mudp_manager mgr = new mudp_manager(udp_port);
		mgr.start_service();

		if (is_serv) {
			System.out.println("Starting server in port " + udp_port);
			mudp_connection srv = mgr.make_responder_connection(1000);
			System.out.println("Got responder " + srv);
			while (!srv.is_closed()) {
				mudp_connection cnn = srv.accept();
				if (cnn != null) {
					System.out.println("Responder got request ("
							+ cnn.get_pt_id() + ") " + cnn.get_conn_id());
					byte[] arr_req = cnn.get_request();
					String str_req = new String(arr_req, UTF_8);
					System.out.println("Request=" + str_req);
					// byte[] arr = get_message(gg);
					String mm = "HOLA_MUNDO";
					byte[] arr = mm.getBytes(UTF_8);
					System.out.println(mm);
					System.out
							.println("---------------------------------------------");
					//System.out.println("RESPOND_NULL");
					//cnn.respond(null);
					cnn.respond(arr);
				} else {
					System.out.println("Got null connection !!!!");
				}
			}
		} else {
			System.out.println("Startitng client in port " + udp_port);
			mudp_peer pp = new mudp_peer(new InetSocketAddress("localhost",
					udp_port + 1));
			String mm = "FIRST_REQUEST";
			byte[] arr = mm.getBytes(UTF_8);
			mudp_connection cli = mgr.make_requester_connection(pp, 1000, 0);
			byte[] resp = cli.request(arr);
			if (resp != null) {
				String ss = new String(resp, UTF_8);
				System.out.println(ss);
				System.out
						.println("=============================================");
			} else {
				System.out.println("GOT NULL RESPONSE");
			}
		}

		mgr.complete_service();
	}

	public static void TEST_udp_sys_1(String[] args) {
		boolean is_serv = false;
		if (args.length < 1) {
			System.out.println("args: (-c|-s)");
			return;
		}
		if (args[0].equals("-s")) {
			is_serv = true;
		}

		// mer_twist gg = new mer_twist(System.currentTimeMillis());

		int udp_port = 5555;
		if (is_serv) {
			udp_port++;
		}

		mudp_manager mgr = new mudp_manager(udp_port);
		mgr.start_service();

		if (is_serv) {
			System.out.println("Startitng server in port " + udp_port);
			mudp_connection srv = mgr.make_server_connection(1000);
			System.out.println("Got server " + srv);
			while (!srv.is_closed()) {
				mudp_connection cnn = srv.accept();
				if (cnn != null) {
					System.out.println("Server got connection "
							+ cnn.get_conn_id());
					// byte[] arr = get_message(gg);
					byte[] arr = "HOLA_MUNDO".getBytes(UTF_8);
					String ss = new String(arr, UTF_8);
					System.out.println(ss);
					System.out
							.println("---------------------------------------------");
					cnn.send(arr);

					cnn.close();
				} else {
					System.out.println("Got null connection !!!!");
				}
			}
		} else {
			System.out.println("Startitng client in port " + udp_port);
			mudp_peer pp = new mudp_peer(new InetSocketAddress("localhost",
					udp_port + 1));
			mudp_connection cnn = mgr.connect(pp, 1000);
			if (cnn != null) {
				System.out
						.println("Client got connection " + cnn.get_conn_id());
				System.out.println("to service " + cnn.server_conn_id);
				byte[] arr = cnn.receive();
				String ss = new String(arr, UTF_8);
				System.out.println(ss);
				System.out
						.println("=============================================");

				cnn.close();
			}
		}

		mgr.complete_service();
	}

	static Lock sub_pru_locks(final Lock r_lk, final Lock w_lk,
			final int thd_num) {
		long curr_tm = System.currentTimeMillis();
		Lock lk = r_lk;
		if ((curr_tm % 2) == 0) {
			lk = w_lk;
		}
		lk.lock();
		return lk;
	}

	static void pru_locks(final Lock r_lk, final Lock w_lk, final int thd_num) {
	}

	static Runnable get_locks_thd(final Lock r_lk, final Lock w_lk,
			final int thd_num) {
		logger.debug("get_locks_thd-" + thd_num);
		Runnable rr1 = new Runnable() {
			public void run() {
				pru_locks(r_lk, w_lk, thd_num);
			}
		};
		return rr1;
	}

	void TEST_locks() {
		// final ReentrantReadWriteLock rw_lk = new ReentrantReadWriteLock();
		// final Lock r_lk = rw_lk.readLock();
		// final Lock w_lk = rw_lk.writeLock();

	}
}
