package emetcode.net.mudp;

import java.util.concurrent.atomic.AtomicLong;

public class dbg_mgr_stats {

	static final boolean IN_DEBUG_1 = true; // init

	AtomicLong tot_recv_null_sec_drops;
	AtomicLong tot_recv_dbg_drops;
	AtomicLong tot_recv_self_drops;

	AtomicLong tot_recv_false_conn_drops;
	AtomicLong tot_recv_null_conn_drops;
	AtomicLong tot_orfan_acks;

	dbg_mgr_stats() {
		init_dbg_mgr_stats();
	}

	void init_dbg_mgr_stats() {
		tot_recv_null_sec_drops = new AtomicLong(0);
		tot_recv_dbg_drops = new AtomicLong(0);
		tot_recv_self_drops = new AtomicLong(0);

		tot_recv_false_conn_drops = new AtomicLong(0);
		tot_recv_null_conn_drops = new AtomicLong(0);
		tot_orfan_acks = new AtomicLong(0);
	}

	public String toString() {
		return get_info();
	}

	public String get_info() {
		String inf = "\nMGR_STATS[" +

		"\n tot_recv_null_sec_drops=" + tot_recv_null_sec_drops.get() +

		"\n tot_recv_dbg_drops=" + tot_recv_dbg_drops.get() +

		"\n tot_recv_self_drops=" + tot_recv_self_drops.get() +

		"\n tot_recv_false_conn_drops=" + tot_recv_false_conn_drops.get() +

		"\n tot_recv_null_conn_drops=" + tot_recv_null_conn_drops.get() +

		"\n tot_orfan_acks=" + tot_orfan_acks.get() +

		" ]";
		return inf;
	}
}
