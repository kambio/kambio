package emetcode.crypto.bitshake.utils;

import java.io.File;
import java.io.FileFilter;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import emetcode.crypto.bitshake.bad_bitshake;

public class dir_iterator implements Iterator<File> {

	File base_dir;
	Deque<File[]> dir_ctr;
	Deque<Integer> idx_ctr;
	File curr_file;
	FileFilter filter;
	boolean pre_order;

	public dir_iterator(File dir, FileFilter fltr, boolean in_pre_order) {
		if (dir == null) {
			throw new bad_bitshake(2);
		}
		if (!dir.exists()) {
			throw new bad_bitshake(2);
		}
		if (!dir.isDirectory()) {
			throw new bad_bitshake(2);
		}

		base_dir = dir;
		dir_ctr = new LinkedList<File[]>();
		idx_ctr = new LinkedList<Integer>();
		curr_file = base_dir;
		filter = fltr;

		pre_order = in_pre_order;

		if (!pre_order) {
			calc_next_pos();
		}

	}

	public boolean hasNext() {
		return (curr_file != null);
	}

	public File next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		File nxt = curr_file;
		// curr_file = calc_next_pre();
		curr_file = calc_next();
		return nxt;
	}

	public void remove() {
	}

	private File[] dir_files(File dir) {
		if (!dir.isDirectory()) {
			return null;
		}
		File[] all_files = dir.listFiles(filter);
		if (all_files.length == 0) {
			return null;
		}
		return all_files;
	}

	private File calc_next_pre() {
		File[] nxt_lst = dir_files(curr_file);
		if (nxt_lst != null) {
			dir_ctr.push(nxt_lst);
			idx_ctr.push(0);
			curr_file = nxt_lst[0];
			return curr_file;
		}

		curr_file = null;
		while (curr_file == null) {
			if (dir_ctr.isEmpty()) {
				break;
			}

			File[] arr = dir_ctr.pop();
			int idx = idx_ctr.pop();
			idx++;
			if (idx < arr.length) {
				dir_ctr.push(arr);
				idx_ctr.push(idx);
				curr_file = arr[idx];
			}
		}

		return curr_file;
	}

	private File calc_next_pos() {
		while (true) {
			if (dir_ctr.isEmpty()) {
				break;
			}

			File[] arr = dir_ctr.pop();
			int idx = idx_ctr.pop();
			idx++;
			if (idx < arr.length) {
				dir_ctr.push(arr);
				idx_ctr.push(idx);
				curr_file = arr[idx];
				break;
			}

			if (dir_ctr.isEmpty()) {
				return null;
			}

			File[] arr2 = dir_ctr.peekFirst();
			int idx2 = idx_ctr.peekFirst();
			curr_file = arr2[idx2];
			return curr_file;
		}

		while (true) {
			File[] nxt_lst = dir_files(curr_file);
			if (nxt_lst == null) {
				break;
			}

			dir_ctr.push(nxt_lst);
			idx_ctr.push(0);
			curr_file = nxt_lst[0];
		}
		return curr_file;
	}

	private File calc_next() {
		if (curr_file == null) {
			throw new NoSuchElementException();
		}
		if (pre_order) {
			return calc_next_pre();
		}
		return calc_next_pos();
	}
}
