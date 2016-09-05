package emetcode.util.devel;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import emetcode.net.netmix.bad_netmix;

public class thread_funcs {

	public static final boolean DEBUG_WAIT = false;
	public static final boolean DEBUG_SLOW_WAIT = false;

	private static Thread[] get_active_threads() {
		int num_act = Thread.activeCount();
		Thread[] tmp_arr = new Thread[num_act * 2];
		num_act = Thread.enumerate(tmp_arr);
		Thread[] active = new Thread[num_act];
		for (int aa = 0; aa < num_act; aa++) {
			active[aa] = tmp_arr[aa];
		}
		return active;
	}

	private static Thread[] get_active_group_threads() {
		ThreadGroup grp = Thread.currentThread().getThreadGroup();
		int num_act = grp.activeCount();
		Thread[] tmp_arr = new Thread[num_act * 2];
		num_act = grp.enumerate(tmp_arr);
		Thread[] active = new Thread[num_act];
		for (int aa = 0; aa < num_act; aa++) {
			active[aa] = tmp_arr[aa];
		}
		return active;
	}

	public static void prt_active_group_thds(String pref) {
		Thread[] all_active = get_active_group_threads();
		for (Thread thd : all_active) {
			logger.info(pref + thd.getName());
		}
	}

	public static void prt_active_thds(String pref) {
		Thread[] all_active = get_active_threads();
		for (Thread thd : all_active) {
			logger.info(pref + thd.getName());
		}
	}

	public static void wait_for_thread(Thread thd) {
		if(thd == null){
			return;
		}
		try {
			if (DEBUG_WAIT) {
				logger.debug("waiting_for_thd=" + thd.getId() + " nam="
						+ thd.getName());
			}
			if (thd.isAlive()) {
				thd.join();
			}
			if (DEBUG_WAIT) {
				logger.debug("FINISHED_thd=" + thd.getId() + " nam="
						+ thd.getName());
			}
		} catch (InterruptedException ex1) {
			throw new bad_netmix(2, ex1.toString());
		}
	}

	public static void wait_for_threads(List<Thread> all_thds) {
		try {
			for (Thread thd : all_thds) {
				dbg_slow sl1 = null;
				if (DEBUG_SLOW_WAIT) {
					sl1 = new dbg_slow();
				}
				if (DEBUG_WAIT) {
					logger.debug("waiting_for_thd=" + thd.getId() + " nam="
							+ thd.getName());
				}
				if (thd.isAlive()) {
					thd.join();
				}
				if (DEBUG_WAIT) {
					logger.debug("FINISHED_thd=" + thd.getId() + " nam="
							+ thd.getName());
				}
				if (DEBUG_SLOW_WAIT) {
					sl1.log_if_slow("SLOW_WAIT thd=" + thd.getId() + " nam="
							+ thd.getName());
				}
			}
		} catch (InterruptedException ex1) {
			throw new bad_netmix(2, ex1.toString());
		}
	}

	public static List<Thread> get_alive(List<Thread> all_thds) {
		List<Thread> all_alive = new ArrayList<Thread>();
		for (Thread thd : all_thds) {
			if (thd.isAlive()) {
				all_alive.add(thd);
			}
		}
		return all_alive;
	}

	public static long get_pid() {
		// something like '<pid>@<hostname>', at least in SUN / Oracle JVMs
		final String jvm = ManagementFactory.getRuntimeMXBean().getName();
		final int index = jvm.indexOf('@');

		if (index < 1) {
			return 0;
		}

		try {
			return Long.parseLong(jvm.substring(0, index));
		} catch (NumberFormatException ex1) {
			logger.error(ex1, "Cannot get PID");
		}
		return 0;
	}

	public static Thread start_thread(String nam, Runnable code,
			boolean as_daemon) {
		Thread thd1 = new Thread(code);
		thd1.setName(nam);
		thd1.setDaemon(as_daemon);
		thd1.start();
		return thd1;
	}

}
