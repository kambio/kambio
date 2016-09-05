package emetcode.crypto.bitshake.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import emetcode.crypto.bitshake.bad_bitshake;
import emetcode.util.devel.logger;

public class file_lock {
	public static boolean IN_DEBUG = false;

	private static final int READ_LOCK = 500;
	private static final int WRITE_LOCK = 501;

	private static final ReentrantLock code_lock = new ReentrantLock();

	private static final ConcurrentHashMap<String, file_lock> all_locks = new ConcurrentHashMap<String, file_lock>();

	String name;
	ReentrantReadWriteLock my_lock;

	File shared_file;
	FileInputStream shared_stm;
	FileLock shared_lock;

	private file_lock(File ff) {
		name = get_path(ff);
		my_lock = new ReentrantReadWriteLock(true);

		shared_file = ff;
		init_shared();
	}

	private void init_shared() {
		shared_stm = null;
		shared_lock = null;
	}

	private static String get_path(File ff) {
		try {
			String can = ff.getCanonicalPath();
			return can;
		} catch (IOException ex) {
		}
		throw new bad_bitshake(2);
	}

	void lock_sys_shared_lock() {
		if(shared_stm != null){
			return;
		}
		if(shared_lock != null){
			return;
		}
		try {
			shared_stm = new FileInputStream(shared_file);
			FileChannel fch = shared_stm.getChannel();
			shared_lock = fch.lock(0, Long.MAX_VALUE, true);
			if (shared_lock != null) {
			}
		} catch (FileNotFoundException ee1) {
			logger.debug(ee1.toString());
			init_shared();
		} catch (IOException ee2) {
			logger.debug(ee2.toString());
			init_shared();
		}
	}

	void unlock_sys_shared_lock() {
		try {
			if (shared_lock != null) {
				shared_lock.release();
				shared_lock = null;
			}
			if (shared_stm != null) {
				shared_stm.close();
				shared_stm = null;
			}
		} catch (IOException ee) {
			logger.debug(ee.toString());
		}
		init_shared();
	}

	private static String kind_to_str(int kk) {
		if (kk == READ_LOCK) {
			return "READ_LOCK";
		}
		return "WRITE_LOCK";
	}

	private static file_lock get_name_lock(File ff, int kind) {
		file_lock lk = null;
		code_lock.lock();
		try {
			// debug_thread_dialog.msg("GETTING_LOCK");
			if (IN_DEBUG) {
				logger.debug("Getting " + kind_to_str(kind) + " FOR '" + ff
						+ "'");
			}
			lk = new file_lock(ff);
			file_lock o_lk = all_locks.putIfAbsent(lk.name, lk);
			if (o_lk != null) {
				lk = o_lk;
			}
			if (kind == READ_LOCK) {
				lk.my_lock.readLock().lock();
				if (lk.num_read_locks() == 1) {
					lk.lock_sys_shared_lock();
				}
			} else {
				lk.my_lock.writeLock().lock();
				lk.unlock_sys_shared_lock();
			}
			if (IN_DEBUG) {
				logger.debug("GOT " + kind_to_str(kind) + " FOR '" + ff + "'");
			}
		} finally {
			code_lock.unlock();
		}
		return lk;
	}

	private int num_read_locks() {
		return my_lock.getReadLockCount();
	}

	private boolean can_remove() {
		boolean c1 = !my_lock.hasQueuedThreads();
		boolean c2 = (!my_lock.isWriteLocked());
		boolean c3 = (my_lock.getReadLockCount() == 0);
		boolean all_ok = (c1 && c2 && c3);
		return all_ok;
	}

	private static void drop_name_lock(file_lock lk, int kind) {
		if (IN_DEBUG) {
			file_lock lk0 = all_locks.putIfAbsent(lk.name, lk);
			if (lk != lk0) {
				throw new bad_bitshake(2);
			}
		}

		if (kind == READ_LOCK) {
			lk.my_lock.readLock().unlock();
		} else {
			lk.my_lock.writeLock().unlock();
		}

		code_lock.lock();
		try {
			if (lk.num_read_locks() == 0) {
				lk.unlock_sys_shared_lock();
			}
			if (lk.can_remove()) {
				// debug_thread_dialog.msg("CAN_REMOVE");
				boolean rm_ok = all_locks.remove(lk.name, lk);
				if (IN_DEBUG) {
					if (!rm_ok) {
						logger.info("CANNOT_REMOVE_YET");
					}
				}
			}
		} finally {
			code_lock.unlock();
		}
	}

	public static file_lock read_lock(File ff) {
		file_lock rl = get_name_lock(ff, READ_LOCK);
		return rl;
	}

	public static void read_unlock(file_lock lk0) {
		drop_name_lock(lk0, READ_LOCK);
	}

	public static file_lock write_lock(File ff) {
		file_lock wl = get_name_lock(ff, WRITE_LOCK);
		return wl;
	}

	public static void write_unlock(file_lock lk0) {
		drop_name_lock(lk0, WRITE_LOCK);
	}

	public static void print_all_locks() {
	}

}
