package emetcode.crypto.bitshake.utils;

import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import emetcode.crypto.bitshake.locale.L;
import emetcode.util.devel.bad_emetcode;
import emetcode.util.devel.logger;

public class file_funcs {

	static boolean IN_DEBUG_1 = false; // concurr_move_file
	static boolean IN_DEBUG_2 = true; // create_label_
	static boolean IN_DEBUG_3 = true; // has_label_

	private static final boolean LONG_PATHS = true; // false to debug
	private static final int[] DIR_SLICES_1 = { 2, 2, 3, 4 };

	public static void dbg_prt(String ln) {
		System.err.println(ln);
	}

	public static String get_running_path() {
		try {
			String pth1 = file_funcs.class.getProtectionDomain()
					.getCodeSource().getLocation().toURI().getPath();
			return pth1;
		} catch (URISyntaxException ex1) {
		}
		throw new bad_emetcode(2);
	}

	public static void print_lines(PrintStream ps, String[] lines) {
		for (String ln : lines) {
			ps.println(ln);
		}
	}

	public static void concurrent_copy_file(File src, File dest) {
		bad_emetcode ex1 = null;
		file_lock lk_src = file_lock.read_lock(src);
		file_lock lk_dest = file_lock.write_lock(dest);
		try {
			copy_file(src, dest);
		} catch (bad_emetcode ex) {
			ex1 = ex;
		}
		file_lock.read_unlock(lk_src);
		file_lock.write_unlock(lk_dest);
		if (ex1 != null) {
			throw ex1;
		}
	}

	private static void copy_file(File src, File dest) {
		if (src == null) {
			throw new bad_emetcode(2);
		}
		if (dest == null) {
			throw new bad_emetcode(2);
		}
		if (src.getPath().equals(dest.getPath())) {
			throw new bad_emetcode(2);
		}
		if (!src.exists()) {
			return;
		}

		FileInputStream f_in = null;
		FileOutputStream f_out = null;
		FileChannel source = null;
		FileChannel destination = null;

		Exception ex1 = null;
		Exception ex2 = null;
		try {
			if (!dest.exists()) {
				dest.createNewFile();
			}

			f_in = new FileInputStream(src);
			f_out = new FileOutputStream(dest);

			source = f_in.getChannel();
			destination = f_out.getChannel();
			long transfered = 0;
			long tot_bts = source.size();
			while (transfered < tot_bts) {
				transfered += destination
						.transferFrom(source, 0, source.size());
				destination.position(transfered);
			}
		} catch (IOException ex) {
			ex1 = ex;
		}
		try {
			if (source != null) {
				source.close();
			} else if (f_in != null) {
				f_in.close();
			}
			if (destination != null) {
				destination.close();
			} else if (f_out != null) {
				f_out.close();
			}
		} catch (IOException ex) {
			ex2 = ex;
		}
		String ex_msg = "";
		if (ex1 != null) {
			ex_msg += ex1.toString();
		}
		if (ex2 != null) {
			ex_msg += ex2.toString();
		}
		if ((ex1 != null) || (ex2 != null)) {
			ex_msg += "\nsrc_file='" + src.getPath() + "'\ndest_file='"
					+ dest.getPath() + "'";
			throw new bad_emetcode(2, ex_msg);
		}
	}

	public static boolean all_zero(byte[] arr) {
		if (arr == null) {
			return true;
		}
		for (byte bt1 : arr) {
			if (bt1 != 0) {
				return false;
			}
		}
		return true;
	}

	public static void copy_list_files(File[] lst_files, File dst_dir) {
		for (File src_f : lst_files) {
			File dst = new File(dst_dir, src_f.getName());
			concurrent_copy_file(src_f, dst);
		}
	}

	public static void copy_all_files(File src_dir, File dst_dir) {
		if (src_dir == null) {
			throw new bad_emetcode(2);
		}
		if (dst_dir == null) {
			throw new bad_emetcode(2);
		}

		File[] all_ff = src_dir.listFiles();
		copy_list_files(all_ff, dst_dir);
	}

	public static File[] move_list_files(File[] lst_files, File dst_dir) {
		File[] moved = new File[lst_files.length];
		int aa = 0;
		for (File src_f : lst_files) {
			File dst = new File(dst_dir, src_f.getName());
			concurrent_move_file(src_f, dst);
			moved[aa] = dst;
			aa++;
		}
		return moved;
	}

	public static File[] move_all_files(File src_dir, File dst_dir) {
		if (src_dir == null) {
			throw new bad_emetcode(2);
		}
		if (dst_dir == null) {
			throw new bad_emetcode(2);
		}
		if (src_dir.getPath().equals(dst_dir.getPath())) {
			throw new bad_emetcode(2);
		}

		File[] all_ff = src_dir.listFiles();
		File[] all_dest = move_list_files(all_ff, dst_dir);
		return all_dest;
	}

	public static void concurrent_move_file(File src, File dest) {
		bad_emetcode ex1 = null;
		if (src == null) {
			return;
		}
		if (!src.exists()) {
			return;
		}
		if (src.equals(dest)) {
			return;
		}

		file_lock lk_src = file_lock.read_lock(src);
		file_lock lk_dest = file_lock.write_lock(dest);
		try {
			move_file(src, dest);
		} catch (bad_emetcode ex) {
			ex1 = ex;
		}
		file_lock.write_unlock(lk_dest);
		file_lock.read_unlock(lk_src);
		if (ex1 != null) {
			throw ex1;
		}
		if (IN_DEBUG_1) {
			logger.debug("concurrent_move_file. \n\tsrc=" + src + "\n\tdest="
					+ dest);
		}
	}

	private static void move_file(File src, File dst) {
		if (src == null) {
			throw new bad_emetcode(2);
		}
		if (dst == null) {
			throw new bad_emetcode(2);
		}
		if (!src.exists()) {
			return;
		}
		if (src.getPath().equals(dst.getPath())) {
			throw new bad_emetcode(2);
		}
		if (!src.renameTo(dst)) {
			throw new bad_emetcode(2);
		}
	}

	public static void concurrent_delete_file(File f_nam) {
		if (f_nam == null) {
			throw new bad_emetcode(2);
		}
		if (!f_nam.exists()) {
			return;
		}
		file_lock lk_dest = file_lock.write_lock(f_nam);
		boolean del_ok = f_nam.delete();
		file_lock.write_unlock(lk_dest);
		if (!del_ok) {
			throw new bad_emetcode(2);
		}
	}

	static public void delete_all(File dir, List<String> all_files) {
		for (String nam : all_files) {
			File full_nam = new File(dir, nam);
			file_funcs.concurrent_delete_file(full_nam);
		}
	}

	public static File get_dir(String dir_nm) {
		File dir = new File(dir_nm);
		dir = get_dir(dir);
		return dir;
	}

	public static File get_dir(File fdir, String subdir) {
		File full_dir = new File(fdir, subdir);
		full_dir = get_dir(full_dir);
		return full_dir;
	}

	public static File get_dir(File fdir) {
		if (fdir == null) {
			throw new bad_emetcode(2, L.null_dir);
		}
		if (!fdir.exists()) {
			fdir.mkdirs();
		}
		if (!fdir.isDirectory()) {
			throw new bad_emetcode(2, String.format(L.not_a_dir, fdir));
		}
		return as_canonical(fdir);
	}

	public static File as_canonical(File fdir) {
		if (fdir == null) {
			throw new bad_emetcode(2, L.null_dir);
		}
		try {
			File dir = fdir.getCanonicalFile();
			return dir;
		} catch (IOException ex1) {
		}
		throw new bad_emetcode(2, String.format(L.cannot_get_canonical_form_of,
				fdir));
	}

	public static void delete_dir(File dir) {
		if (dir == null) {
			return;
		}
		if(!dir.exists()){
			return;
		}
		if (dir.isDirectory()) {
			for (File ff : dir.listFiles()) {
				delete_dir(ff);
			}
		}
		if (!dir.delete()) {
			throw new bad_emetcode(2, String.format(L.cannot_delete_dir, dir));
		}
	}

	public static File[] diff_file_arrays(File[] lst1, File[] lst2) {
		Set<File> s_lst1 = new TreeSet<File>(Arrays.asList(lst1));
		for (File ff : lst2) {
			if (s_lst1.contains(ff)) {
				s_lst1.remove(ff);
			}
		}
		File[] diff = s_lst1.toArray(new File[0]);
		return diff;
	}

	public static List<File> diff_file_lists(Collection<File> lst1,
			Collection<File> lst2) {
		Set<File> s_lst1 = new TreeSet<File>(lst1);
		for (File ff : lst2) {
			if (s_lst1.contains(ff)) {
				s_lst1.remove(ff);
			}
		}
		List<File> diff = new ArrayList<File>(s_lst1);
		return diff;
	}

	public static List<String> files_to_paths(File[] all_ff) {
		List<String> pths = new ArrayList<String>(all_ff.length);
		for (File ff : all_ff) {
			pths.add(as_canonical(ff).getPath());
		}
		return pths;
	}

	public static File[] paths_to_files(List<String> pths) {
		File[] all_ff = new File[pths.size()];
		int aa = 0;
		for (String pth : pths) {
			all_ff[aa] = new File(pth);
			aa++;
		}
		return all_ff;
	}

	public static List<String> files_to_path_list(Collection<File> all_ff) {
		List<String> pths = new ArrayList<String>(all_ff.size());
		for (File ff : all_ff) {
			pths.add(as_canonical(ff).getPath());
		}
		return pths;
	}

	public static List<File> paths_to_file_list(Collection<String> pths) {
		List<File> all_ff = new ArrayList<File>(pths.size());
		for (String pth : pths) {
			all_ff.add(new File(pth));
		}
		return all_ff;
	}

	public static void delete_files(Collection<File> all_ff) {
		for (File ff : all_ff) {
			if (!ff.exists()) {
				continue;
			}
			ff.delete();
		}
	}

	public static File[] filter_exists_files(File[] all_ff) {
		List<File> lst_ff = new ArrayList<File>(all_ff.length);
		for (File ff : all_ff) {
			if (!ff.exists()) {
				continue;
			}
			lst_ff.add(ff);
		}
		File[] all_ff2 = lst_ff.toArray(new File[0]);
		return all_ff2;
	}

	/*
	 * This code makes doesn't preserve dates and I'm not sure how it would
	 * react to stuff like symlinks. No attempt is made to add directory
	 * entries, so empty directories would not be included.
	 */

	public static void zip_dir(File base_dir, File zipfile) {
		List<File> all_dirs = new ArrayList<File>();
		all_dirs.add(base_dir);
		zip_dirs(base_dir, all_dirs, zipfile);
	}

	public static void zip_dirs(File base_dir, List<File> all_dirs, File zipfile) {
		try {
			URI base = base_dir.toURI();
			Deque<File> queue = new LinkedList<File>();
			OutputStream out = new FileOutputStream(zipfile);
			Closeable res1 = out;
			ZipOutputStream zout = new ZipOutputStream(out);
			Closeable res2 = zout;

			try {
				for (File z_dir : all_dirs) {
					queue.push(z_dir);
					while (!queue.isEmpty()) {
						File wrk_directory = queue.pop();
						for (File kid : wrk_directory.listFiles()) {
							String name = base.relativize(kid.toURI())
									.getPath();
							if (kid.isDirectory()) {
								queue.push(kid);
								name = name.endsWith("/") ? name : name + "/";
								zout.putNextEntry(new ZipEntry(name));
							} else {
								zout.putNextEntry(new ZipEntry(name));
								copy(kid, zout);
								zout.closeEntry();
							}
						}
					}
				}
			} finally {
				res2.close();
				res1.close();
			}
		} catch (IOException ex) {
			logger.error(
					ex,
					"Cannot zip dirs \n"
							+ Arrays.toString(get_file_paths(all_dirs).toArray(
									new String[0])) + "\n\t under " + base_dir);
			throw new bad_emetcode(2, ex.getMessage() + '\n'
					+ String.format(L.cannot_create_zip_file, zipfile));
		}
	}

	public static void unzip_dir(File zipfile, File base_dir) {
		try {
			ZipFile zfile = new ZipFile(zipfile);
			Enumeration<? extends ZipEntry> entries = zfile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				File file = new File(base_dir, entry.getName());
				if (entry.isDirectory()) {
					file.mkdirs();
				} else {
					file.getParentFile().mkdirs();
					InputStream in = zfile.getInputStream(entry);
					try {
						copy(in, file);
					} finally {
						in.close();
					}
				}
			}
		} catch (IOException ex) {
			throw new bad_emetcode(2);
		}
	}

	private static void copy(InputStream in, OutputStream out)
			throws IOException {
		byte[] buffer = new byte[1024];
		while (true) {
			int readCount = in.read(buffer);
			if (readCount < 0) {
				break;
			}
			out.write(buffer, 0, readCount);
		}
	}

	private static void copy(File file, OutputStream out) throws IOException {
		InputStream in = new FileInputStream(file);
		try {
			copy(in, out);
		} finally {
			in.close();
		}
	}

	private static void copy(InputStream in, File file) throws IOException {
		OutputStream out = new FileOutputStream(file);
		try {
			copy(in, out);
		} finally {
			out.close();
		}
	}

	public static File get_temp_file(File full_name) {
		return get_temp_file(full_name, 0);
	}

	public static File get_temp_file(File full_name, int num_tmp) {
		char sep = File.separatorChar;
		File cano_ff = as_canonical(full_name);
		String tmp_pth = cano_ff.getParent() + sep + config.PRE_TMP
				+ cano_ff.getName();
		if (num_tmp > 0) {
			tmp_pth = cano_ff.getParent() + sep + config.PRE_TMP + num_tmp
					+ cano_ff.getName();
		}
		File tmp_ff = new File(tmp_pth);
		return tmp_ff;
	}

	public static boolean is_sub_file(File pnt_dir, File sub_ff) {
		File cn_pnt_dir = as_canonical(pnt_dir);
		File cn_sub_ff = as_canonical(sub_ff);
		String pth_pnt = cn_pnt_dir.getPath();
		String pth_sub = cn_sub_ff.getPath();
		return pth_sub.startsWith(pth_pnt);
	}

	public static void path_delete(File parent_dir, File to_del_ff) {
		File pnt_dir = as_canonical(parent_dir);
		File to_del = as_canonical(to_del_ff);
		String pth1 = pnt_dir.getPath();
		String pth2 = to_del.getPath();

		if (!pth2.startsWith(pth1)) {
			return;
		}
		if (pth2.length() == pth1.length()) {
			return;
		}

		while (to_del.exists()) {
			try {
				concurrent_delete_file(to_del);
			} catch (bad_emetcode ex) {
				// ONLY DELETE EMPTY BRANCHES
				break;
			}

			File pnt = to_del.getParentFile();
			if (!pnt.isDirectory()) {
				break;
			}
			if (pnt.equals(pnt_dir)) {
				break;
			}

			to_del = pnt;
		}
	}

	public static Iterator<File> get_pre_iter(File directory) {
		return new dir_iterator(directory, null, true);
	}

	public static Iterator<File> get_pos_iter(File directory) {
		return new dir_iterator(directory, null, false);
	}

	public static Iterator<File> get_file_iterator(File directory,
			FileFilter fltr, boolean in_pre_order) {
		return new dir_iterator(directory, fltr, in_pre_order);
	}

	public static void walk_dir(File directory, file_visitor vv) {
		URI base = directory.toURI();
		Deque<File> queue = new LinkedList<File>();
		queue.push(directory);
		try {
			while (!queue.isEmpty()) {
				directory = queue.pop();
				for (File kid : directory.listFiles()) {
					String name = base.relativize(kid.toURI()).getPath();
					if (kid.isDirectory()) {
						queue.push(kid);
						name = name.endsWith("/") ? name : name + "/";
					}
					vv.visit(kid, name);
				}
			}
		} finally {
		}
	}

	public static void copy_dir(File src_dir, File dst_dir) {
		File dest_dir = get_dir(dst_dir);
		URI base = src_dir.toURI();
		Deque<File> queue = new LinkedList<File>();
		queue.push(src_dir);

		while (!queue.isEmpty()) {
			src_dir = queue.pop();
			for (File ff1 : src_dir.listFiles()) {
				String name = base.relativize(ff1.toURI()).getPath();
				boolean is_dir = ff1.isDirectory();
				if (is_dir) {
					queue.push(ff1);
					name = name.endsWith("/") ? name : name + "/";
				}
				File ff2 = new File(dest_dir, name);
				if (is_dir) {
					ff2.mkdirs();
				} else {
					ff2.getParentFile().mkdirs();
					concurrent_copy_file(ff1, ff2);
				}
			}
		}
	}

	public static void move_dir(File src_dir, File dst_dir) {
		File base_dir = as_canonical(src_dir);
		File dst_dir1 = as_canonical(dst_dir);
		if (base_dir.equals(dst_dir1)) {
			return;
		}

		File dest_dir = get_dir(dst_dir);

		URI base = src_dir.toURI();
		Deque<File> queue = new LinkedList<File>();
		queue.push(src_dir);

		while (!queue.isEmpty()) {
			src_dir = queue.pop();
			for (File ff1 : src_dir.listFiles()) {
				String name = base.relativize(ff1.toURI()).getPath();
				boolean is_dir = ff1.isDirectory();
				if (is_dir) {
					queue.push(ff1);
					name = name.endsWith("/") ? name : name + "/";
				}
				File ff2 = new File(dest_dir, name);
				if (is_dir) {
					ff2.mkdirs();
				} else {
					ff2.getParentFile().mkdirs();
					concurrent_move_file(ff1, ff2);
				}
			}
		}

		delete_dir(base_dir);
	}

	public static List<File> list_dir(File src_dir, FileFilter is_leaf_dir,
			FileFilter is_selected) {
		Deque<File> queue = new LinkedList<File>();
		queue.push(src_dir);

		List<File> all_ff = new ArrayList<File>();
		while (!queue.isEmpty()) {
			src_dir = queue.pop();
			for (File ff1 : src_dir.listFiles()) {
				if (ff1.isDirectory()) {
					if ((is_leaf_dir == null) || !is_leaf_dir.accept(ff1)) {
						queue.push(ff1);
					}
				}
				if ((is_selected == null) || is_selected.accept(ff1)) {
					all_ff.add(ff1);
				}
			}
		}
		return all_ff;
	}

	public static List<File> list_dir_old(File src_dir, FileFilter fl_filter,
			boolean only_leafs) {
		Deque<File> queue = new LinkedList<File>();
		queue.push(src_dir);

		List<File> all_ff = new ArrayList<File>();
		while (!queue.isEmpty()) {
			src_dir = queue.pop();
			for (File ff1 : src_dir.listFiles(fl_filter)) {
				if (ff1.isDirectory()) {
					queue.push(ff1);
					if (only_leafs) {
						continue;
					}
				}
				all_ff.add(ff1);
			}
		}
		return all_ff;
	}

	public static List<String> get_file_names(File[] all_ff) {
		List<String> all_nm = new ArrayList<String>();
		for (File ff : all_ff) {
			all_nm.add(ff.getName());
		}
		return all_nm;
	}

	public static List<String> get_file_paths(List<File> all_ff) {
		List<String> all_nm = new ArrayList<String>();
		for (File ff : all_ff) {
			all_nm.add(ff.getPath());
		}
		return all_nm;
	}

	public static boolean is_same_file(File ff1, File ff2) {
		ff1 = as_canonical(ff1);
		ff2 = as_canonical(ff2);
		return ff1.equals(ff2);
	}

	public static String get_rel_path(File base_dir, File ff) {
		if (!is_sub_file(base_dir, ff)) {
			throw new bad_emetcode(2, "NOT_SUB_FILE. \nbase_dir=" + base_dir + "\nff=" + ff);
		}
		URI base = base_dir.toURI();
		String name = base.relativize(ff.toURI()).getPath();
		return name;
	}

	public static File get_rel_file(File base_dir, File ff) {
		String name = get_rel_path(base_dir, ff);
		File rel_ff = new File(name);
		return rel_ff;
	}

	public static List<String> files_to_rel_paths(File base_dir, List<File> all_ff) {
		List<String> all_pth = new ArrayList<String>(all_ff.size());
		URI base = base_dir.toURI();
		for (File ff : all_ff) {
			if (!is_sub_file(base_dir, ff)) {
				throw new bad_emetcode(2);
			}
			String name = base.relativize(ff.toURI()).getPath();
			all_pth.add(name);
		}
		return all_pth;
	}

	public static List<File> rel_paths_to_files(File base_dir, List<String> all_rel_pth) {
		List<File> all_ff = new ArrayList<File>(all_rel_pth.size());
		for (String rel_pth : all_rel_pth) {
			File ff = new File(base_dir, rel_pth);
			all_ff.add(ff);
		}
		return all_ff;
	}

	public static String as_sliced_file_path(String full_nm) {
		return as_sliced_file_path(full_nm, DIR_SLICES_1);
	}

	public static String as_sliced_file_path(String full_nm, int[] sections) {
		if (full_nm == null) {
			throw new bad_emetcode(2);
		}
		String dd_nm = "";
		if (LONG_PATHS) {
			// ends withpath sep ('/' for linux)
			dd_nm = file_funcs.as_sliced_dir_path(full_nm, sections);
		}
		String f_nm = dd_nm + full_nm;
		return f_nm;
	}

	public static String as_sliced_dir_path(String nam, int[] sections) {
		if (!LONG_PATHS) {
			return nam;
		}

		char sep = File.separatorChar;
		StringBuilder frm = new StringBuilder();
		int min_sec_sz = 1;
		int pos1 = 0;
		int pos2 = 0;
		List<CharSequence> seqs = new ArrayList<CharSequence>(
				sections.length + 1);
		for (int vv : sections) {
			if (vv < min_sec_sz) {
				vv = min_sec_sz;
			}
			if (pos1 >= nam.length()) {
				break;
			}
			pos2 = pos1 + vv;
			if (pos2 > nam.length()) {
				pos2 = nam.length();
			}

			CharSequence sec1 = nam.subSequence(pos1, pos2);
			pos1 = pos2;

			seqs.add(sec1);
			frm.append(sec1);
			frm.append(sep);

		}
		if (pos2 < nam.length()) {
			CharSequence sec2 = nam.subSequence(pos2, nam.length());
			frm.append(sec2);
			frm.append(sep);
		}
		return frm.toString();
	}

	public static boolean mk_parent_dir(File ff) {
		File pnt_dir = ff.getParentFile();
		boolean created = false;
		if (!pnt_dir.exists()) {
			pnt_dir.mkdirs();
			created = true;
		}
		return created;
	}

	public static List<String> read_list_file(File ltor_ff, key_owner owr) {
		try {
			byte[] all_bts = mem_file.concurrent_read_encrypted_bytes(ltor_ff,
					owr);
			List<String> all_str = convert.bytes_to_string_list(all_bts);
			if (all_str == null) {
				all_str = new ArrayList<String>();
			}
			return all_str;
		} catch (Exception ex1) {
			logger.info(String.format(L.cannot_read_list_file,
					ltor_ff.toString()));
		}
		return new ArrayList<String>();
	}

	public static void write_list_file(File ltor_ff, key_owner owr,
			List<String> all_str) {
		byte[] all_bts = convert.string_list_to_bytes(all_str);
		mem_file.concurrent_write_encrypted_bytes(ltor_ff, owr, all_bts);
	}

	public static File get_label_file(File ff, String lbl_nm) {
		File ff2 = new File(ff.getPath() + lbl_nm);
		return ff2;
	}

	public static boolean is_label_file(File ff, String lbl_nm) {
		boolean nm_ok = ff.getName().endsWith(lbl_nm);
		boolean ff_ok = ff.exists();
		boolean is_tt = nm_ok && ff_ok;
		return is_tt;
	}

	public static File create_label_file(File ff, key_owner owr, String lbl_nm) {
		File lb_ff = get_label_file(ff, lbl_nm);
		byte[] sha_val = mem_file.calc_sha_bytes(ff, null);
		byte[] nm = ff.getName().getBytes(config.UTF_8);

		byte[] pk_1 = data_packer.pack_two(nm, sha_val);

		mem_file.concurrent_write_encrypted_bytes(lb_ff, owr, pk_1);
		
		if(IN_DEBUG_2){
			//String full_stk = "\nduring\n" + logger.get_stack_str();
			String full_stk = "";
			logger.debug("created_label="+ lbl_nm + " for=\n\t" + ff + full_stk);
		}
		return lb_ff;
	}

	public static boolean has_label_file(File ff, key_owner owr, String lbl_nm) {
		if (ff == null) {
			return false;
		}
		if (!ff.exists()) {
			return false;
		}
		File tg_ff = get_label_file(ff, lbl_nm);
		if (!is_label_file(tg_ff, lbl_nm)) {
			return false;
		}

		try {
			byte[] pk_1 = mem_file.concurrent_read_encrypted_bytes(tg_ff, owr);
			List<byte[]> arr_val = data_packer.unpack_list(pk_1);

			if (arr_val.size() != 2) {
				return false;
			}

			byte[] sha_val = mem_file.calc_sha_bytes(ff, null);
			byte[] nm = ff.getName().getBytes(config.UTF_8);

			boolean ck_1 = Arrays.equals(nm, arr_val.get(0));
			boolean ck_2 = Arrays.equals(sha_val, arr_val.get(1));

			boolean can_tt = (ck_1 && ck_2);

			if(IN_DEBUG_3){
				if(can_tt){
					logger.debug("HAS_label="+ lbl_nm + " ff=\n\t" + ff);
				}
			}
			return can_tt;
		} catch (bad_emetcode ex) {
		}
		return false;
	}

	public static String fix_file_name(String nm){
		char[] all_ch = nm.toCharArray();
		for(int aa = 0; aa < all_ch.length; aa++){
			if(! Character.isLetterOrDigit(all_ch[aa])){
				all_ch[aa] = '_';
			}
		}
		String f_nm = new String(all_ch);
		return f_nm;
	}
	
	public static boolean equal_sha(File ff1, key_owner owr1, File ff2,
			key_owner owr2) {
		try {
			byte[] sha_1 = mem_file.calc_sha_bytes(ff1, owr1);
			byte[] sha_2 = mem_file.calc_sha_bytes(ff2, owr2);
			boolean eq_s = Arrays.equals(sha_1, sha_2);
			return eq_s;
		} catch (bad_emetcode ex1) {
		}
		return false;
	}

	public static boolean is_badge_file(File lb_ff, key_owner owr, String ck_str) {
		if (!lb_ff.exists()) {
			return false;
		}
		List<String> all_lines = read_list_file(lb_ff, owr);
		if (all_lines.size() != 2) {
			return false;
		}
		if (!all_lines.get(0).equals(ck_str)) {
			return false;
		}

		return true;
	}

	public static void create_badge_file(File lb_ff, key_owner owr,
			String ck_str) {
		if (owr == null) {
			throw new bad_emetcode(2);
		}
		String rnd_str = "" + owr.new_random_long();
		
		List<String> all_lines = new ArrayList<String>();
		all_lines.add(ck_str);
		all_lines.add(rnd_str);
		write_list_file(lb_ff, owr, all_lines);
	}

}
