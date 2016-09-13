package emetcode.net.mudp;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import emetcode.util.devel.concurrent_average;
import emetcode.util.devel.logger;
import emetcode.util.devel.thread_funcs;

public class mudp_manager {
	// static final boolean IN_DEBUG = true;
	static final boolean IN_DEBUG_00 = true; // begin, end global
	static final boolean IN_DEBUG_01 = false; // begin, end
	static final boolean IN_DEBUG_2 = false; // simulate bad network
	static final boolean IN_DEBUG_3 = false; // odd cases, stablish conn
	static final boolean IN_DEBUG_4 = false;
	static final boolean IN_DEBUG_5 = false; // wait for work
	static final boolean IN_DEBUG_6 = true; // abort unexpected test cases
	static final boolean IN_DEBUG_7 = false; // false conn
	static final boolean IN_DEBUG_10 = false; // dbg_stats
	static final boolean IN_DEBUG_11 = false; // particular
	static final boolean IN_DEBUG_12 = true; // start/finish server

	private static final int SECS_TIMEOUT_POLL_HAS_WORK = 30;
	private static final int SECS_TIMEOUT_ALL_FINISHED = 30;

	static final int TO_SEND_MAX_MSG = 5;
	static final int TO_CONFIRM_MAX_MSG = 5;

	InetSocketAddress sok_addr;
	DatagramSocket sok;
	long current_send_date;

	private Thread thd_sender;
	private Thread thd_receiver;

	private conns_local_peer all_working;

	private AtomicBoolean sender_working;
	private AtomicBoolean receiver_working;

	BlockingQueue<Boolean> has_work;
	BlockingQueue<Boolean> all_finished;

	public concurrent_average num_sec_send_avg;

	dbg_mgr_stats mg_stats;

	public mudp_manager(int port) {
		sok_addr = new InetSocketAddress(port);
		init_connection_manager();
	}

	public mudp_manager(InetAddress addr, int port) {
		sok_addr = new InetSocketAddress(addr, port);
		init_connection_manager();
	}

	public mudp_manager(InetSocketAddress addr) {
		sok_addr = addr;
		init_connection_manager();
	}

	private void init_connection_manager() {
		sok = null;
		try {
			sok = new DatagramSocket(sok_addr.getPort());
			//sok = new DatagramSocket(sok_addr);
		} catch (SocketException ex) {
			throw new bad_udp(2, ex.toString());
		}
		current_send_date = 0;

		thd_sender = null;
		thd_receiver = null;

		all_working = new conns_local_peer(this);

		sender_working = new AtomicBoolean(true);
		receiver_working = new AtomicBoolean(true);

		has_work = new LinkedBlockingQueue<Boolean>(1);
		all_finished = new LinkedBlockingQueue<Boolean>(1);

		num_sec_send_avg = new concurrent_average();

		mg_stats = new dbg_mgr_stats();
	}

	private void wait_for_work() {
		if (IN_DEBUG_5) {
			logger.debug("sender waiting for work sok="
					+ sok.getLocalSocketAddress());
		}
		try {
			has_work.poll(SECS_TIMEOUT_POLL_HAS_WORK, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new bad_udp(2);
		}
		if (IN_DEBUG_5) {
			logger.debug("sender WORKING sok=" + sok.getLocalSocketAddress());
		}
	}

	private void sender_main() {
		if (IN_DEBUG_12) {
			logger.debug("Starting_MUDP_sender_thread sok="
					+ get_socket_address());
		}
		if (message_section.IN_DEBUG_2) {
			logger.debug("DEBUGGING !!!!. recv DROPING and send SHUFFLE !!!");
		}
		try {
			long aa = 0;
			while (sender_working.get()) {
				current_send_date = System.currentTimeMillis();

				long w_tm = all_working.run_all_data_conn();
				if (message_section.IN_DEBUG_2) {
					message_section.dbg_send_all(sok);
				}

				if (IN_DEBUG_2) {
					logger.debug("" + get_socket_address() + "AVG_SENT_BY_SEC="
							+ num_sec_send_avg.get_avg()
							+ "\nAVG_SENT_BY_SEC_SZ="
							+ num_sec_send_avg.get_sz()
							+ "\nAVG_SENT_BY_SEC_SLOT_SZ="
							+ num_sec_send_avg.get_slot_sz());
				}
				if (w_tm == -1) {
					wait_for_work();
				} else {
					if (IN_DEBUG_01) {
						logger.debug("MUDP_sleeping " + w_tm);
					}
					Thread.sleep(w_tm);
				}
				if (IN_DEBUG_01) {
					logger.debug("MUDP_cicle sender " + aa);
				}
				aa++;
				if (IN_DEBUG_4) {
					logger.info("RETURN TO CONTINUE (sender_main)....");
					System.console().readLine();
					logger.info("GOT RETURN (sender_main)....");
				}
				Thread.yield();
			}
		} catch (bad_udp ex1) {
			logger.error(ex1, "SENDER_GOT_BAD_UDP !!!!" + get_socket_address());
		} catch (InterruptedException ex2) {
			logger.error(ex2, "Sender interrrupted" + get_socket_address());
		} finally {
			if (IN_DEBUG_00) {
				logger.debug("SENDER_ENDING_SENDER_ENDING_SENDER_ENDING_SENDER_ENDING"
						+ get_socket_address());
			}
		}
		if (IN_DEBUG_00) {
			logger.debug("Sender ending..." + get_socket_address());
		}
	}

	private void receiver_main() {
		if (IN_DEBUG_12) {
			logger.debug("Starting_MUDP_receiver_thread sok="
					+ get_socket_address());
		}
		try {
			while (receiver_working.get()) {
				message_section sec = message_section.receive_section(
						all_working.local_peer, sok);
				if (sec == null) {
					receiver_working.set(false);
					if (IN_DEBUG_10) {
						mg_stats.tot_recv_null_sec_drops.incrementAndGet();
					}
					continue;
				}
				if (message_section.IN_DEBUG_2) {
					if (message_section.dbg_drop_sec()) {
						logger.debug("DROPING  " + sec.toString());
						if (IN_DEBUG_10) {
							mg_stats.tot_recv_dbg_drops.incrementAndGet();
						}
						continue;
					}
				}
				if (sec.from_self()) {
					logger.debug("SKIPPED SELF RECEIVING--" + sec.toString());
					if (IN_DEBUG_10) {
						mg_stats.tot_recv_self_drops.incrementAndGet();
					}
					continue;
				}

				mudp_connection conn = all_working.get_conn(sec.msg_src,
						sec.msg_conn_id);

				if (conn == mudp_connection.FALSE_CONN) {
					// could not get locks. drop this packet.
					if (IN_DEBUG_7) {
						logger.debug("conn == FALSE_CONN");
					}
					if (IN_DEBUG_10) {
						mg_stats.tot_recv_false_conn_drops.incrementAndGet();
					}
					continue;
				}

				if ((conn != null)
						&& (conn.get_conn_kind() == mudp_connection.KIND_mudp_connection)) {
					if (IN_DEBUG_7) {
						logger.debug("received mudp_connection connection !!!!");
					}
					continue;
				}

				if ((conn == null) || conn.is_closed()) {
					// if (sec.is_dat()) {
					if (sec.is_close_sec()) {
						sec.send_ack_section(sok);
						if (IN_DEBUG_10) {
							mg_stats.tot_orfan_acks.incrementAndGet();
						}
						if (IN_DEBUG_3) {
							logger.debug("SENDING ORFAN ACK in conn_id="
									+ sec.msg_conn_id);
						}
					}
					if (IN_DEBUG_01) {
						logger.debug("MUDP_CONN_ID " + sec.msg_conn_id + " in "
								+ sec.msg_src + " not found."
								+ get_socket_address());
					}
					has_work.offer(true);
					if (IN_DEBUG_11) {
						if (conn == null) {
							logger.debug("NULL_CONN_conn_id=" + sec.msg_conn_id);
						}
					}
					if (IN_DEBUG_10) {
						if (conn == null) {
							mg_stats.tot_recv_null_conn_drops.incrementAndGet();
						} else if (conn.is_closed()) {
							conn.dbg_stats.tot_recv_closed_conn_drops
									.incrementAndGet();
						}
					}
					continue;
				}

				if (!conn.is_server()) {
					if (sec.is_syn()) {
						conn.stablish();
						has_work.offer(true);
						if (IN_DEBUG_3) {
							logger.debug("CLI_STABLISHING conn=" + conn);
						}
						conn.dbg_stats.tot_recv_syn_stablish.incrementAndGet();
						continue;
					}
				}

				if (IN_DEBUG_3) {
					logger.debug("calling_SWITCH_for sec=" + sec);
				}

				if (conn.get_conn_kind() == msg_conn.KIND_msg_conn) {
					switch (sec.sec_kind) {
					case message_section.ACK_KIND:
						if (!conn.is_server()) {
							boolean all_ok = conn.recv_ack_section(sec);
							if (all_ok) {
								has_work.offer(true);
							}
						} else {
							String em1 = "server receiving ACK !"
									+ get_socket_address();
							logger.debug(em1);
							if (IN_DEBUG_6) {
								throw new bad_udp(2, em1);
							}
						}
						break;
					case message_section.SYN_KIND:
						if (conn.is_server()) {
							conn.recv_syn(sec);
							if (IN_DEBUG_3) {
								logger.debug("SRV_STABLISHING conn=" + conn);
							}
						}
						break;
					case message_section.DAT_KIND:
						if (!conn.is_server()) {
							boolean s_ack = conn.recv_dat_section(sec);
							if (s_ack) {
								sec.send_ack_section(sok);
								conn.dbg_stats.tot_ack_sent.incrementAndGet();
							}

							boolean has_last = false;
							List<message_section> all_last = conn
									.recv_all_full();
							if (all_last != null) {
								has_last = !all_last.isEmpty();
								for (message_section l_sec : all_last) {
									l_sec.send_ack_section(sok);
									conn.dbg_stats.tot_ack_sent
											.incrementAndGet();
								}
							}
							if (s_ack || has_last) {
								long act_tm = System.currentTimeMillis();
								conn.time_last_activ.set(act_tm);
							}
						} else {
							String em1 = "server receiving DAT !"
									+ get_socket_address();
							logger.debug(em1);
							if (IN_DEBUG_6) {
								throw new bad_udp(2, em1);
							}
						}
						break;
					default:
						String em1 = conn.get_conn_kind() + " receiving "
								+ message_section.kind_as_str(sec.sec_kind)
								+ " !!!" + get_socket_address();
						logger.debug(em1);
						if (IN_DEBUG_6) {
							throw new bad_udp(2, em1);
						}
						break;
					}
				} else if (conn.get_conn_kind() == req_conn.KIND_req_conn) {
					switch (sec.sec_kind) {
					case message_section.REQ_KIND:
						if (conn.is_server()) {
							conn.recv_req(sec);
						} else {
							String em1 = "client receiving REQ !"
									+ get_socket_address();
							logger.debug(em1);
							if (IN_DEBUG_6) {
								throw new bad_udp(2, em1);
							}
						}
						break;
					case message_section.ANS_KIND:
						if (!conn.is_server()) {
							conn.recv_ans_section(sec);
						} else {
							String em1 = "server receiving ANS !"
									+ get_socket_address();
							logger.debug(em1);
							if (IN_DEBUG_6) {
								throw new bad_udp(2, em1);
							}
						}
						break;
					default:
						String em1 = conn.get_conn_kind() + " receiving "
								+ message_section.kind_as_str(sec.sec_kind)
								+ " !!!" + get_socket_address();
						logger.debug(em1);
						if (IN_DEBUG_6) {
							throw new bad_udp(2, em1);
						}
						break;
					}
				} else {
					String em1 = "" + conn + " of kind " + conn.get_conn_kind()
							+ " receiving "
							+ message_section.kind_as_str(sec.sec_kind)
							+ " sec=" + sec + " !!!" + get_socket_address();
					logger.debug(em1);
					if (IN_DEBUG_6) {
						throw new bad_udp(2, em1);
					}
				}

			}
		} catch (bad_udp ex1) {
			logger.error(ex1, "RECEIVER_GOT_BAD_UDP !!!!"
					+ get_socket_address());
		} finally {
			if (IN_DEBUG_00) {
				logger.debug("RECEIVER_ENDING_RECEIVER_ENDING_RECEIVER_ENDING_RECEIVER_ENDING"
						+ get_socket_address());
			}
		}
		if (IN_DEBUG_00) {
			logger.debug("Receiver ending..." + get_socket_address());
		}
	}

	private Runnable get_receiver() {
		if (IN_DEBUG_00) {
			logger.debug("get_MUDP_receiver");
		}
		Runnable rr1 = new Runnable() {
			public void run() {
				receiver_main();
			}
		};
		return rr1;
	}

	private Runnable get_sender() {
		if (IN_DEBUG_00) {
			logger.debug("get_MDUP_sender");
		}
		Runnable rr1 = new Runnable() {
			public void run() {
				try {
					sender_main();
				} catch (Throwable tt) {
					logger.debug("SENDER ABORTED !!!!" + get_socket_address()
							+ "\n" + tt.toString());
				}
			}
		};
		return rr1;
	}

	public void start_service() {
		String adr = "";
		if (sok_addr != null) {
			adr = "-" + sok_addr;
		}
		String thd_sndr = Thread.currentThread().getName() + adr
				+ "-mudp-sender";
		thd_sender = thread_funcs.start_thread(thd_sndr, get_sender(), false);
		String thd_rcvr = Thread.currentThread().getName() + adr
				+ "-mudp-receiver";
		thd_receiver = thread_funcs.start_thread(thd_rcvr, get_receiver(),
				false);
	}

	public void complete_service() {
		has_work.offer(true);
		try {
			if (IN_DEBUG_12) {
				String stk_str = logger.get_stack_str();
				logger.info("Waiting_for_all_MUDP_connections_to_finish..."
						+ get_socket_address() + "\n" + stk_str);
			}
			while (all_finished.poll(SECS_TIMEOUT_ALL_FINISHED,
					TimeUnit.SECONDS) == null) {
				has_work.offer(true);
				if (IN_DEBUG_01) {
					logger.debug("Timed out waiting for connections to finish.");
				}
				int sz = all_working.all_remote_size();
				if (IN_DEBUG_12) {
					String stk_str = logger.get_stack_str();
					logger.info("Checking_if_all_MUDP_conns_finished (" + sz
							+ " == 0)" + "\n" + stk_str);
				}
				if (sz == 0) {
					break;
				}
			}
		} catch (InterruptedException e) {
			throw new bad_udp(2);
		}
		if (IN_DEBUG_12) {
			logger.info("ALL_MUDP_connections_finished. STOPING_SERVICE. sok="
					+ get_socket_address());
		}
		stop_service();
	}

	public void stop_service() {
		if (IN_DEBUG_00) {
			logger.debug("STOPPING_MUDP_SERVICE." + get_socket_address());
		}

		sender_working.set(false);
		has_work.offer(true);
		receiver_working.set(false);
		sok.close();

		all_working.stop_all_conn();

		try {
			if (IN_DEBUG_00) {
				logger.debug("Waiting_for_MUDP_sender..."
						+ get_socket_address());
			}
			thd_sender.join();
			if (IN_DEBUG_00) {
				logger.debug("Waiting_for_MUDP_receiver..."
						+ get_socket_address());
			}
			thd_receiver.join();
		} catch (InterruptedException ee) {
			logger.debug("Interrupted_while_waiting_for_MUDP_manager threads\n"
					+ ee);
		}
		if (thd_sender.isAlive()) {
			throw new bad_udp(2);
		}
		if (thd_receiver.isAlive()) {
			throw new bad_udp(2);
		}
	}

	public msg_conn make_server_connection(long conn_id) {
		if (!mudp_connection.is_server(conn_id)) {
			throw new bad_udp(2);
		}
		msg_conn conn = all_working.make_new_server_conn(conn_id);
		if (IN_DEBUG_01) {
			logger.debug("Created_MUDP_server conn=" + conn);
		}
		return conn;
	}

	public msg_conn connect(mudp_peer pp, long srv_conn_id) {
		if (!mudp_connection.is_server(srv_conn_id)) {
			throw new bad_udp(2);
		}
		msg_conn conn = all_working.make_new_client_conn(pp, srv_conn_id);
		if (IN_DEBUG_3) {
			logger.debug("OPENED_MUDP_client CONN=" + conn);
		}
		has_work.offer(true);
		return conn;
	}

	public req_conn make_responder_connection(long conn_id) {
		if (!mudp_connection.is_server(conn_id)) {
			throw new bad_udp(2);
		}
		req_conn conn = all_working.make_new_responder_conn(conn_id);
		if (IN_DEBUG_01) {
			logger.debug("Created_MUDP_responder conn=" + conn);
		}
		return conn;
	}

	public req_conn make_requester_connection(mudp_peer pp,
			long server_conn_id, long cokey_id) {
		req_conn conn = all_working.make_new_requester_conn(pp, server_conn_id,
				cokey_id);
		return conn;
	}

	public InetSocketAddress get_socket_address() {
		return sok_addr;
	}

	// public String get_info(){
	// String inf = "";
	// return inf;
	// }
}
