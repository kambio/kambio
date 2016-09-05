package emetcode.net.mudp;

import java.util.concurrent.atomic.AtomicLong;

public class dbg_conn_stats {
	AtomicLong tot_dat_sent;
	AtomicLong tot_ack_sent;
	AtomicLong tot_syn_sent;

	AtomicLong tot_dat_recvd;
	AtomicLong tot_ack_recvd;
	AtomicLong tot_syn_recvd;

	AtomicLong tot_send_close_msg;
	
	AtomicLong tot_recv_syn_stablish;
	AtomicLong tot_recv_ack_bad_minisha;
	AtomicLong tot_recv_ack_bad_round_tm;
	AtomicLong tot_recv_ack_bad_builder;
	AtomicLong tot_recv_ack_bad_orig_sec;
	AtomicLong tot_recv_ack_ok;

	AtomicLong tot_recv_dat_close;
	AtomicLong tot_recv_dat_prev_close;
	AtomicLong tot_recv_dat_is_closing;
	AtomicLong tot_recv_dat_is_full;
	AtomicLong tot_recv_dat_bad_minisha;
	AtomicLong tot_recv_dat_max_sec;
	AtomicLong tot_recv_dat_max_cap;
	AtomicLong tot_recv_dat_empty_recving;
	AtomicLong tot_recv_dat_fst_gt_recv;
	AtomicLong tot_recv_dat_added_sec;

	AtomicLong tot_recv_full_empty_recving;
	AtomicLong tot_recv_full_offer_ok;

	AtomicLong tot_recv_closed_conn_drops;
	
	dbg_conn_stats() {
		init_send_stats();
	}

	void init_send_stats() {
		tot_dat_sent = new AtomicLong(0);
		tot_ack_sent = new AtomicLong(0);
		tot_syn_sent = new AtomicLong(0);

		tot_dat_recvd = new AtomicLong(0);
		tot_ack_recvd = new AtomicLong(0);
		tot_syn_recvd = new AtomicLong(0);
		
		tot_send_close_msg = new AtomicLong(0);

		tot_recv_syn_stablish = new AtomicLong(0);
		tot_recv_ack_bad_minisha = new AtomicLong(0);
		tot_recv_ack_bad_round_tm = new AtomicLong(0);
		tot_recv_ack_bad_builder = new AtomicLong(0);
		tot_recv_ack_bad_orig_sec = new AtomicLong(0);
		tot_recv_ack_ok = new AtomicLong(0);

		tot_recv_dat_close = new AtomicLong(0);
		tot_recv_dat_prev_close = new AtomicLong(0);
		tot_recv_dat_is_closing = new AtomicLong(0);
		tot_recv_dat_is_full = new AtomicLong(0);
		tot_recv_dat_bad_minisha = new AtomicLong(0);
		tot_recv_dat_max_sec = new AtomicLong(0);
		tot_recv_dat_max_cap = new AtomicLong(0);
		tot_recv_dat_empty_recving = new AtomicLong(0);
		tot_recv_dat_fst_gt_recv = new AtomicLong(0);
		tot_recv_dat_added_sec = new AtomicLong(0);
		
		tot_recv_full_empty_recving = new AtomicLong(0);
		tot_recv_full_offer_ok = new AtomicLong(0);
		
		tot_recv_closed_conn_drops = new AtomicLong(0);
	}

	public String toString() {
		return get_info();
	}

	public String get_info() {
		String inf = "CONN_STATS[" +

		"\n tot_dat_sent=" + tot_dat_sent.get() +

		"\n tot_ack_sent=" + tot_ack_sent.get() +

		"\n tot_syn_sent=" + tot_syn_sent.get() +

		"\n tot_dat_recvd=" + tot_dat_recvd.get() +

		"\n tot_ack_recvd=" + tot_ack_recvd.get() +

		"\n tot_syn_recvd=" + tot_syn_recvd.get() +

		"\n tot_send_close_msg=" + tot_send_close_msg.get() +

		"\n tot_recv_syn_stablish=" + tot_recv_syn_stablish.get() +

		"\n tot_recv_ack_bad_minisha=" + tot_recv_ack_bad_minisha.get() +

		"\n tot_recv_ack_bad_round_tm=" + tot_recv_ack_bad_round_tm.get() +

		"\n tot_recv_ack_bad_builder=" + tot_recv_ack_bad_builder.get() +

		"\n tot_recv_ack_bad_orig_sec=" + tot_recv_ack_bad_orig_sec.get() +

		"\n tot_recv_ack_ok=" + tot_recv_ack_ok.get() +

		"\n tot_recv_dat_close=" + tot_recv_dat_close.get() +

		"\n tot_recv_dat_prev_close=" + tot_recv_dat_prev_close.get() +

		"\n tot_recv_dat_is_closing=" + tot_recv_dat_is_closing.get() +

		"\n tot_recv_dat_is_full=" + tot_recv_dat_is_full.get() +

		"\n tot_recv_dat_bad_minisha=" + tot_recv_dat_bad_minisha.get() +

		"\n tot_recv_dat_over_max=" + tot_recv_dat_max_sec.get() +

		"\n tot_recv_dat_max_cap=" + tot_recv_dat_max_cap.get() +

		"\n tot_recv_dat_empty_recving=" + tot_recv_dat_empty_recving.get() +

		"\n tot_recv_dat_fst_gt_recv=" + tot_recv_dat_fst_gt_recv.get() +

		"\n tot_recv_dat_added_sec=" + tot_recv_dat_added_sec.get() +

		"\n tot_recv_full_empty_recving=" + tot_recv_full_empty_recving.get() +

		"\n tot_recv_full_offer_ok=" + tot_recv_full_offer_ok.get() +

		"\n tot_recv_closed_conn_drops=" + tot_recv_closed_conn_drops.get() +

		" ]";
		return inf;
	}
}
