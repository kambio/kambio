package emetcode.crypto.bitshake.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.util.Arrays;

import emetcode.crypto.bitshake.bad_bitshake;
import emetcode.crypto.bitshake.bitshaker;
import emetcode.crypto.bitshake.locale.L;
import emetcode.util.devel.bad_emetcode;
import emetcode.util.devel.logger;

public class mem_file {

	public static final boolean IN_DEBUG_1 = false; // locks
	public static final boolean IN_DEBUG_2 = false; // create file
	public static final boolean IN_DEBUG_3 = false; // concurr write file

	public static final long MILLIS_TIMEOUT_READ = 8000;

	public static final String UTF_8_NAM = "UTF-8";
	public static final Charset UTF_8 = Charset.forName(UTF_8_NAM);

	public static final int AVERAGE_FILE_SZ = 10000;

	public static void main(String args[]) {
		System.out.println("pru_sha");
	}

	public static byte[] alloc_bytes(int arr_sz) {
		Runtime rt = Runtime.getRuntime();
		try {
			byte[] arr = new byte[arr_sz];
			return arr;
		} catch (OutOfMemoryError ex) {
			rt.gc();
			throw new bad_bitshake(2, ex.toString());
		}
	}

	public static int read_stream(InputStream istm, byte[] full_dat, int off0)
			throws IOException {
		if (istm == null) {
			return -1;
		}
		if (full_dat == null) {
			return -1;
		}
		if (full_dat.length == 0) {
			return -1;
		}
		if (off0 < 0) {
			return -1;
		}

		int br = 0;
		int off = off0;
		int len = full_dat.length - off;

		if (len < 0) {
			return -1;
		}

		while ((br = istm.read(full_dat, off, len)) > 0) {
			off += br;
			len -= br;
		}
		if ((off == off0) && (br == -1)) {
			off = -1;
		}
		return off;
	}

	public static byte[] read_stream(InputStream istm) {
		try {
			byte[] full_dat = alloc_bytes(AVERAGE_FILE_SZ);
			int d_sz = 0;
			int off = 0;
			while ((d_sz = read_stream(istm, full_dat, off)) != -1) {
				if (d_sz < full_dat.length) {
					full_dat = Arrays.copyOf(full_dat, d_sz);
					break;
				}
				off = d_sz;
				byte[] tmp_arr = alloc_bytes(full_dat.length * 2);
				ByteBuffer buff = ByteBuffer.wrap(tmp_arr);
				buff.put(full_dat);
				full_dat = tmp_arr;
			}
			if ((off == 0) && (d_sz == -1)) {
				return null;
			}
			return full_dat;
		} catch (IOException ex) {
			logger.error(ex, "During read_stream");
		}
		return null;
	}

	private static byte[] read_bytes(File ff) {
		// bb[0] = false;
		if (!ff.canRead() && !ff.setReadable(true)) {
			// bb[0] = true;
			return null;
		}
		if (!ff.isFile()) {
			// bb[0] = true;
			return null;
		}

		// WARNING !!!!!! WARNING !!!!!! WARNING !!!!!! WARNING !!!!!!
		// interprocess shared locking is taken care in file_lock because
		// class FileLock is NOT thread safe as documented.

		Exception ex1 = null;
		FileInputStream fin = null;
		byte[] data = null;
		try {
			fin = new FileInputStream(ff);
			FileChannel fch = fin.getChannel();
			if (IN_DEBUG_1) {
				System.out.println("return to RELEASE read lock.");
				String ll = System.console().readLine();
				System.out.println("read=" + ll);
			}

			long fsz = fch.size();
			if (fsz > Integer.MAX_VALUE) {
				throw new IOException();
			}

			int dat_sz = (int) fsz;
			data = alloc_bytes(dat_sz);
			ByteBuffer buff = ByteBuffer.wrap(data);
			@SuppressWarnings("unused")
			int num_rr = 0;
			while (buff.hasRemaining()) {
				int rr = fch.read(buff);
				if (rr == -1) {
					break;
				}
				if (IN_DEBUG_1) {
					System.out.println("rr=" + rr + " pos=" + buff.position());
				}
				num_rr += rr;
			}
			// if(num_rr == dat_sz){
			// bb[0] = true;
			// }
			// byte[] data = read_stream(fin, dat_sz);
			// data = read_stream(fin);
			if (IN_DEBUG_1) {
				System.out.println("dat_sz=" + dat_sz + " fsz=" + fsz
						+ " num_read=" + num_rr);
			}
		} catch (FileNotFoundException ex) {
			ex1 = ex;
		} catch (IOException ex) {
			ex1 = ex;
		}

		try {
			if (fin != null) {
				fin.close();
			}
		} catch (IOException ee) {
			ex1 = ee;
		}
		if (ex1 != null) {
			throw new bad_bitshake(2, ex1.toString());
		}

		return data;
	}

	private static boolean write_bytes(File ff, byte[] data) {
		int len = 0;
		if (data != null) {
			len = data.length;
		}
		return write_bytes(ff, data, 0, len, false);
	}

	private static boolean append_bytes(File ff, byte[] data) {
		int len = 0;
		if (data != null) {
			len = data.length;
		}
		return write_bytes(ff, data, 0, len, true);
	}

	private static boolean write_bytes(File ff, byte[] data, int off, int len,
			boolean append) {
		Exception ex1 = null;
		FileOutputStream fout = null;
		FileLock fl = null;
		try {
			fout = new FileOutputStream(ff, append);
			FileChannel fch = fout.getChannel();
			fl = fch.lock();
			if (fl != null) {
				if (IN_DEBUG_1) {
					System.out.println("return to RELEASE write lock");
					System.console().readLine();
				}
				if (!append) {
					fch.truncate(0);
				}
				if (data != null) {
					ByteBuffer buff = ByteBuffer.wrap(data, off, len);
					while (buff.hasRemaining()) {
						fch.write(buff);
					}
					// fout.write(data, off, len);
				}
				fout.flush();
			}

		} catch (IOException ex) {
			ex1 = ex;
		}

		try {
			if (fl != null) {
				fl.release();
			}
			if (fout != null) {
				fout.close();
			}
		} catch (IOException ee) {
			ex1 = ee;
		}
		if (ex1 != null) {
			throw new bad_bitshake(2, ex1.toString());
		}

		return true;
	}

	private static void write_encrypted_bytes(File ff, key_owner owr,
			byte[] data) {
		if (data == null) {
			return;
		}
		write_encrypted_bytes(ff, owr, data, 0, data.length);
	}

	private static void write_encrypted_bytes(File ff, key_owner owr,
			byte[] data, int off, int len) {
		if (data == null) {
			throw new bad_bitshake(2, L.cannot_write_null_data);
		}
		if (data.length == 0) {
			boolean ok1 = mem_file.write_bytes(ff, data);
			if (!ok1) {
				throw new bad_bitshake(2, L.cannot_write_empty_data);
			}
			return;
		}
		byte[] cp_data = Arrays.copyOfRange(data, off, off + len);
		byte[] enc_data = bitshaker.encrypt_bytes_with_sha(cp_data, owr);
		if (enc_data == null) {
			throw new bad_bitshake(2, L.cannot_encrypt_data);
		}

		boolean ok1 = mem_file.write_bytes(ff, enc_data);
		if (!ok1) {
			throw new bad_bitshake(2, L.cannot_write_data);
		}
	}

	private static byte[] read_encrypted_bytes(File ff, key_owner owr) {
		byte[] enc_data = read_bytes(ff);
		if (enc_data == null) {
			return null;
		}
		if (enc_data.length == 0) {
			return null;
		}
		byte[] data = bitshaker.decrypt_bytes_with_sha(enc_data, owr);
		if (data == null) {
			throw new bad_bitshake(2, L.cannot_decrypt_data);
		}
		return data;
	}

	public static String read_encrypted_string(File ff, key_owner owr) {
		Runtime rt = Runtime.getRuntime();
		try {
			byte[] dt = concurrent_read_encrypted_bytes(ff, owr);
			if (dt == null) {
				return null;
			}
			String str = new String(dt, UTF_8);
			return str;
		} catch (OutOfMemoryError ex) {
			rt.gc();
		}
		return null;
	}

	public static String read_string(File ff) {
		return read_encrypted_string(ff, null);
	}

	public static boolean write_encrypted_string(File ff, key_owner owr, String str) {
		if (ff == null) {
			return false;
		}
		if (str == null) {
			return false;
		}
		Runtime rt = Runtime.getRuntime();
		try {
			byte[] dt = str.getBytes(UTF_8);
			concurrent_write_encrypted_bytes(ff, owr, dt);
			return true;
		} catch (OutOfMemoryError ex) {
			rt.gc();
		}
		return false;
	}

	public static boolean write_string(File ff, String str) {
		return write_encrypted_string(ff, null, str);
	}

	public static byte[] calc_sha_bytes(File ff, key_owner owr) {
		byte[] data = concurrent_read_encrypted_bytes(ff, owr);
		if (data == null) {
			throw new bad_bitshake(2);
		}
		return convert.calc_sha_bytes(data);
	}

	public static String calc_sha_str(File ff, key_owner owr) {
		String sha_str = convert.bytes_to_hex_string(calc_sha_bytes(ff, owr));
		return sha_str;
	}

	public static String try_calc_content_sha_str(File ff, key_owner owr) {
		try {
			String sha_str = convert
					.bytes_to_hex_string(calc_sha_bytes(ff, owr));
			return sha_str;
		} catch (bad_emetcode ex) {
		}
		return config.INVALID_SHA;
	}

	public static String calc_sha_text(File ff, key_owner owr) {
		byte[] sha_bts = calc_sha_bytes(ff, owr);
		String sha_tgt = convert
				.bytes_to_hex_frm_string(sha_bts, config.SHA_ID);
		return sha_tgt;
	}

	public static void debug_msg(String msg, boolean ask_enter) {
		System.out.println(msg);
		if (ask_enter) {
			System.console().readLine();
		}
	}

	public static byte[] concurrent_read_encrypted_bytes(File ff, key_owner owr) {
		if (!ff.exists()) {
			return null;
		}
		bad_emetcode ex1 = null;
		file_lock rl = file_lock.read_lock(ff);
		byte[] data = null;
		try {
			if (owr != null) {
				data = read_encrypted_bytes(ff, owr);
			} else {
				data = read_bytes(ff);
			}
		} catch (bad_emetcode ex) {
			ex1 = ex;
		}
		file_lock.read_unlock(rl);
		if (ex1 != null) {
			throw ex1;
		}
		return data;
	}

	public static void concurrent_write_encrypted_bytes(File ff, key_owner owr,
			byte[] data) {
		bad_emetcode ex1 = null;
		file_lock rl = file_lock.write_lock(ff);
		try {
			if (owr != null) {
				write_encrypted_bytes(ff, owr, data);
			} else {
				write_bytes(ff, data);
			}
		} catch (bad_emetcode ex) {
			ex1 = ex;
		}
		if (IN_DEBUG_3) {
			logger.debug("Writed file " + ff);
		}
		file_lock.write_unlock(rl);
		if (ex1 != null) {
			throw ex1;
		}
	}

	public static void concurrent_create_file(File ff) {
		bad_emetcode ex1 = null;
		file_lock rl = file_lock.write_lock(ff);
		try {
			write_bytes(ff, null);
		} catch (bad_emetcode ex) {
			ex1 = ex;
		}
		file_lock.write_unlock(rl);
		if (ex1 != null) {
			throw ex1;
		}
		if (IN_DEBUG_2) {
			if (ff.exists()) {
				logger.debug("Created file " + ff);
			}
		}
	}

	public static void concurrent_append_bytes(File ff, byte[] data) {
		bad_emetcode ex1 = null;
		file_lock rl = file_lock.write_lock(ff);
		try {
			append_bytes(ff, data);
		} catch (bad_emetcode ex) {
			ex1 = ex;
		}
		file_lock.write_unlock(rl);
		if (ex1 != null) {
			throw ex1;
		}
	}

	public static byte[] load_resource(URL url) {
		try {
			InputStream istm = url.openStream();
			byte[] url_data = mem_file.read_stream(istm);
			return url_data;
		} catch (IOException ex1) {
			throw new bad_bitshake(2, ex1.toString());
		}
	}

}
