package emetcode.economics.netpasser;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import emetcode.crypto.bitshake.utils.file_funcs;
import emetcode.crypto.bitshake.utils.key_owner;
import emetcode.economics.netpasser.locale.L;
import emetcode.economics.passet.bad_passet;
import emetcode.economics.passet.paccount;
import emetcode.economics.passet.tag_transfer;
import emetcode.net.netmix.nx_peer;
import emetcode.util.devel.bad_emetcode;
import emetcode.util.devel.logger;

public class file_operator implements Runnable, trans_operator {

	static final boolean IN_DEBUG_1 = true;
	static final boolean IN_DEBUG_2 = true; // show output files of change

	public static int QUEUE_SIZE = 5;

	private nx_peer l_peer;
	transaction working_trans;
	BlockingQueue<transaction> to_file_operate;

	public file_operator(nx_peer loc_peer) {
		if (loc_peer == null) {
			throw new bad_netpasser(2);
		}
		l_peer = loc_peer;
		working_trans = null;
		to_file_operate = new LinkedBlockingQueue<transaction>(QUEUE_SIZE);
	}

	public void tell_finish_cli() {
		queue_transaction(new transaction());
	}

	public transaction wait_for_transaction(transaction trans) {
		if (working_trans != null) {
			throw new bad_netpasser(2);
		}

		try {
			working_trans = to_file_operate.take();
			if (working_trans == null) {
				throw new bad_netpasser(2);
			}
			if (working_trans.state_oper == transaction.INVALID_OPER) {
				throw new bad_netpasser(2);
			}
		} catch (InterruptedException e) {
			throw new bad_netpasser(2);
		}
		return working_trans;
	}

	public void run() {
		logger.info("Starting file operator");

		while (true) {
			working_trans = null;
			wait_for_transaction(null);
			String ret_oper = null;
			String oper = working_trans.state_oper;
			try {
				if (oper == transaction.FILE_MAKE_REPECTACLES_OPER) {
					make_receptacles();
				} else if (oper == transaction.FILE_SING_REPECTACLES_OPER) {
					sign_receptacles();
				} else if (oper == transaction.FILE_CK_ARE_MINE_OPER) {
					ck_are_mine();
				} else if (oper == transaction.FILE_IMPORT_OPER) {
					import_passets();
				} else if (oper == transaction.FILE_VERIFY_PASSETS_OPER) {
					verify_passets();
				} else if (oper == transaction.FILE_ADD_DIFF_OPER) {
					verify_passets();
				} else if (oper == transaction.FILE_CALC_SPLIT_CHANGE_OPER) {
					do_change(oper);
				} else if (oper == transaction.FILE_CALC_JOIN_CHANGE_OPER) {
					do_change(oper);
				} else if (oper == transaction.FILE_END_CHOICE_OPER) {
					end_choice();
				} else if (oper == transaction.FINISH_OPER) {
					break;
				} else {
					throw new bad_netpasser(2, L.invalid_file_oper + "\n\t"
							+ oper);
				}
				ret_oper = transaction.NET_CONTINUE_OPER;
			} catch (bad_emetcode err1) {
				working_trans.all_working_iss = null;
				ret_oper = transaction.NET_CANCEL_OPER;
				logger.error(err1, "Operation " + oper
						+ " failed. CANCELING !!!");
			}
			working_trans.answer_oper(ret_oper);
		}
		logger.info("FINISHED");
	}

	public void queue_transaction(transaction working_trans) {
		if (working_trans == null) {
			throw new bad_netpasser(2);
		}
		try {
			to_file_operate.put(working_trans);
		} catch (InterruptedException ex) {
			throw new bad_netpasser(2, ex.toString());
		}
	}

	boolean is_working() {
		boolean c1 = (working_trans != null);
		return c1;
	}

	private paccount get_local_paccount() {
		if (!is_working()) {
			throw new bad_netpasser(2);
		}
		if (working_trans.local_pcc == null) {
			throw new bad_netpasser(2);
		}
		return working_trans.local_pcc;
	}

	private paccount get_remote_paccount() {
		if (!is_working()) {
			throw new bad_netpasser(2);
		}
		if (working_trans.remote_pcc == null) {
			throw new bad_netpasser(2);
		}
		return working_trans.remote_pcc;
	}

	key_owner get_owner() {
		if (l_peer == null) {
			throw new bad_netpasser(2);
		}
		return l_peer.get_owner();
	}

	File[] get_files() {
		if (!is_working()) {
			throw new bad_netpasser(2);
		}
		return working_trans.get_files();
	}

	private void make_receptacles() {
		paccount recving_pcc = get_local_paccount();
		paccount remote_pcc = get_remote_paccount();
		List<tag_transfer> all_iss = new ArrayList<tag_transfer>();
		List<File> all_pss = Arrays.asList(get_files());
		List<File> all_recep = new ArrayList<File>();
		remote_pcc.make_receptacles_for(all_pss, get_owner(), all_iss,
				all_recep, recving_pcc);
		set_out_files(all_recep);
		set_all_working_iss(all_iss);
	}

	private void sign_receptacles() {
		paccount remote_pcc = get_remote_paccount();
		List<tag_transfer> all_in_tra_signed = new ArrayList<tag_transfer>();
		List<File> all_pss = Arrays.asList(get_files());
		List<File> all_nxt_pss = remote_pcc.try_sign_receptacles_for(all_pss,
				get_owner(), all_in_tra_signed);
		set_all_working_iss(all_in_tra_signed); // used to end_transaction
		set_out_files(all_nxt_pss);
	}

	private List<tag_transfer> ck_are_mine() {
		paccount recving_pcc = get_local_paccount();
		paccount remote_pcc = get_remote_paccount();
		List<tag_transfer> all_iss = new ArrayList<tag_transfer>();
		List<File> all_pss = Arrays.asList(get_files());
		remote_pcc.try_check_mine_for(all_pss, get_owner(), all_iss, recving_pcc);
		set_all_working_iss(all_iss);
		return all_iss;
	}

	private void import_passets() {
		ck_are_mine();
	}
	
	private void verify_passets() {
		paccount remote_pcc = get_remote_paccount();
		List<tag_transfer> all_iss = new ArrayList<tag_transfer>();
		List<File> all_pss = Arrays.asList(get_files());

		List<bad_emetcode> all_err = remote_pcc.try_verif_all_passets(all_pss,
				all_iss, get_owner());

		if (IN_DEBUG_1) {
			for (tag_transfer iss_dat : all_iss) {
				logger.info("Verified=" + iss_dat);
			}
		}

		set_all_working_iss(all_iss);

		if (!all_err.isEmpty()) {
			for (bad_emetcode err : all_err) {
				logger.info("VERIFICATION FAILED.\n" + err);
			}
			throw new bad_passet(2, L.verification_failed);
		}
	}

	private void set_all_working_iss(List<tag_transfer> all_iss) {
		if (working_trans == null) {
			throw new bad_netpasser(2);
		}
		working_trans.all_working_iss = all_iss;
	}

	private List<tag_transfer> get_all_working_iss() {
		if (working_trans == null) {
			throw new bad_netpasser(2);
		}
		return working_trans.all_working_iss;
	}

	private void set_out_files(List<File> all_out) {
		if (working_trans == null) {
			throw new bad_netpasser(2);
		}
		working_trans.set_out_files(all_out);
	}

	private void do_change(String chg_kind) {
		if (!is_working()) {
			throw new bad_passet(2);
		}

		paccount local_pcc = get_local_paccount();
		key_owner owr = get_owner();
		
		List<tag_transfer> all_in_iss = get_all_working_iss();
		List<File> all_in_ff = local_pcc.get_all_passet_files(all_in_iss);

		List<tag_transfer> out_tras = null;
		
		if (chg_kind == transaction.FILE_CALC_JOIN_CHANGE_OPER) {
			out_tras = local_pcc.join_passets(all_in_ff, owr, false);
		} else {
			out_tras = local_pcc.split_passets(all_in_ff, owr, false);
		}
		
		if (IN_DEBUG_2) {
			List<File> all_chg = local_pcc.get_all_passet_files(out_tras);
			List<String> all_chg_pth = file_funcs.files_to_path_list(all_chg);
			logger.info("ALL_ISSUED_FILES_DURING_CHANGE");
			logger.info(all_chg_pth);
		}
		
		set_all_working_iss(out_tras);
		
		paccount remote_pcc = get_remote_paccount();
		local_pcc.choose_passets(remote_pcc, get_owner(), out_tras);

		String tra_nm = remote_pcc.start_choice(owr);
		working_trans.set_choice_name(tra_nm);
	}

	private void end_choice(){
		paccount loc_pcc = get_local_paccount();
		paccount rem_pcc = get_remote_paccount();
		String tra_nm = working_trans.get_choice_name();
		List<tag_transfer> all_in_tra_signed = working_trans.all_working_iss;
		//List<tag_transfer> all_in_tra_signed = null;
		key_owner owr = get_owner();
		loc_pcc.end_choice(rem_pcc, tra_nm, owr, all_in_tra_signed);
	}

}
