package emetcode.util.devel;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class concurrent_average {
	private double avg;
	private long sz;
	private long slot_sz;

	private final ReentrantReadWriteLock rw_lk = new ReentrantReadWriteLock();
	private final Lock r_lk = rw_lk.readLock();
	private final Lock w_lk = rw_lk.writeLock();

	public concurrent_average() {
		avg = 0;
		sz = 0;
		slot_sz = -1;
	}

	public void add_val(double val) {
		w_lk.lock();
		try {
			if (sz == 0) {
				sz = 1;
				avg = val;
				return;
			}
			double nxt_sz = (sz + 1);
			avg = (avg * (((double) sz) / nxt_sz)) + (val / nxt_sz);
			if ((slot_sz < 0) || ((slot_sz > 0) && (sz < slot_sz))) {
				sz++;
			}
		} finally {
			w_lk.unlock();
		}
	}

	public void remove_val(double val) {
		w_lk.lock();
		try {
			if (sz <= 1) {
				sz = 0;
				avg = 0;
				return;
			}
			double prv_sz = (sz - 1);
			avg = (avg * (((double) sz) / prv_sz)) - (val / prv_sz);
			sz--;
		} finally {
			w_lk.unlock();
		}
	}

	public double get_avg() {
		r_lk.lock();
		try {
			return avg;
		} finally {
			r_lk.unlock();
		}

	}

	public long get_sz() {
		r_lk.lock();
		try {
			return sz;
		} finally {
			r_lk.unlock();
		}

	}

	public long get_slot_sz() {
		r_lk.lock();
		try {
			return slot_sz;
		} finally {
			r_lk.unlock();
		}

	}

	public void reset() {
		reset(slot_sz);
	}
	
	public void reset(long n_slot_sz) {
		w_lk.lock();
		try {
			avg = 0;
			sz = 0;
			slot_sz = n_slot_sz;
		} finally {
			w_lk.unlock();
		}
	}
}
