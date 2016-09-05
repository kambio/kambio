package emetcode.net.mudp;

import java.util.concurrent.atomic.AtomicLong;

import emetcode.util.devel.concurrent_average;
import emetcode.util.devel.logger;

public class send_stats {
	static final boolean IN_DEBUG_3 = false;

	static final int NUM_ACK_FOR_AVG = 10000;
	static final int NUM_VALS_FOR_VEL_AVG = 100;
	static final int NUM_VALS_FOR_SND_AVG = 1000;

	private long last_send_tm;
	private long curr_num_send;

	private double last_velo;
	private long mult;
	private boolean is_growing;

	concurrent_average curr_avg_ack_time;
	concurrent_average avg_velocity;

	AtomicLong num_ack_by;

	private long num_send;
	private long wait_tm;

	private long max_mult;
	private long max_num_send;
	private long max_wait_tm;

	send_stats() {
		init_send_stats();
	}

	void init_send_stats() {
		last_send_tm = System.currentTimeMillis();
		curr_num_send = 0;

		last_velo = 0;
		mult = 1;
		is_growing = true;

		curr_avg_ack_time = new concurrent_average();
		curr_avg_ack_time.reset(NUM_ACK_FOR_AVG);

		avg_velocity = new concurrent_average();
		avg_velocity.reset(NUM_VALS_FOR_VEL_AVG);

		num_ack_by = new AtomicLong(0);

		num_send = 1;
		wait_tm = 1;

		max_mult = mult;
		max_num_send = num_send;
		max_wait_tm = wait_tm;
	}

	private void calc_mult(double last_velo, double curr_velo) {
		int top_grow = 7;
		if (curr_velo >= last_velo) {
			if (is_growing) {
				if (mult < top_grow) {
					mult = mult * 2;
				}
			} else {
				if (mult > 1) {
					mult = mult / 2;
				}
			}
		} else {
			if (is_growing) {
				if (mult > 1) {
					mult = mult / 2;
				}
			} else {
				if (mult < top_grow) {
					mult = mult * 2;
				}
			}
			is_growing = !is_growing;
		}
	}

	boolean calc_next() {
		long curr_send_tm = System.currentTimeMillis();
		long diff_tm = curr_send_tm - last_send_tm;
		if (diff_tm < wait_tm) {
			return false;
		}

		curr_num_send = 0;
		double nm_ack_by = (double) num_ack_by.get();

		if (nm_ack_by < message_section.MAX_SEC_DATA_NUM_BY) {
			// do not change
			return true;
		}
		if (diff_tm < 1) {
			// do not change
			return true;
		}

		double ack_tm = (double) diff_tm;
		double curr_velo = nm_ack_by / ack_tm;
		avg_velocity.add_val(curr_velo);

		calc_mult(last_velo, curr_velo);
		last_velo = curr_velo;
		if (mult > max_mult) {
			max_mult = mult;
		}

		double w_tm = curr_avg_ack_time.get_avg();
		double n_by_send = curr_velo * w_tm;
		double n_sec_send = n_by_send
				/ ((double) message_section.MAX_SEC_DATA_NUM_BY);

		long n_send = 1;
		if (n_sec_send > 1) {
			n_send = (long) n_sec_send;
		}

		wait_tm = (long) w_tm;
		num_send = n_send;

		num_send = mult * num_send;

		if (num_send > max_num_send) {
			max_num_send = num_send;
		}
		if (wait_tm > max_wait_tm) {
			max_wait_tm = wait_tm;
		}

		if (IN_DEBUG_3) {
			logger.debug("V=" + curr_velo + "(" + nm_ack_by + "/" + ack_tm
					+ ")" + " aV=" + avg_velocity.get_avg() + " aK=" + w_tm
					+ " mult=" + mult + " max_mult=" + max_mult
					+ " max_num_send=" + max_num_send + " max_wait_tm="
					+ max_wait_tm);
			logger.debug("#S=" + num_send + " WT=" + wait_tm);
			// System.console().readLine();
		}

		last_send_tm = curr_send_tm;
		num_ack_by.set(0);

		return true;
	}

	public String toString() {
		String ss = "stats[#S=" + num_send + " wtm=" + wait_tm + " aV="
				+ avg_velocity.get_avg() + " #s=" + curr_num_send + " mult="
				+ mult + "]";
		return ss;
	}

	public String get_info() {
		String inf = "" + "\n last_send_tm=" + last_send_tm
				+ "\n curr_num_send=" + curr_num_send + "\n last_velo="
				+ last_velo + "\n mult=" + mult + "\n is_growing=" + is_growing
				+ "\n curr_avg_ack_time=" + curr_avg_ack_time.get_avg()
				+ "\n avg_velocity=" + avg_velocity.get_avg()
				+ "\n avg_velocity=" + num_ack_by.get() + "\n num_send="
				+ num_send + "\n wait_tm=" + wait_tm + "\n max_mult="
				+ max_mult + "\n max_num_send=" + max_num_send
				+ "\n max_wait_tm=" + max_wait_tm;
		return inf;
	}

	void inc_num_sent() {
		curr_num_send++;
	}

	void set_max_num_sent() {
		curr_num_send = num_send;
	}

	boolean sent_max() {
		return (curr_num_send >= num_send);
	}

	long get_wait_tm() {
		return wait_tm;
	}
}
