package emetcode.util.devel;

import java.util.List;

public class logger {

	public static logger_funcs external_logger = null;

	public static String get_stack_str() {
		String stk_msg = add_stack(new StringBuilder(), new RuntimeException())
				.toString();
		return stk_msg;
	}
	
	public static void debug_trace() {
		String stk_msg = get_stack_str();
		if (external_logger != null) {
			external_logger.v("emet", stk_msg);
		} else {
			print_str(stk_msg);
		}
	}

	private static void add_reps(StringBuilder full_str, int num, String rep) {
		for (int ii = 0; ii < num; ii++) {
			full_str.append(rep);
		}
	}

	private static StringBuilder add_stack(StringBuilder full_str, Throwable ex) {
		StackTraceElement[] stk = ex.getStackTrace();
		full_str.append("STACK---\n");
		for (StackTraceElement ee : stk) {
			full_str.append(ee.toString());
			full_str.append('\n');
		}
		full_str.append("_____________________________________\n");
		return full_str;
	}

	private static String get_thd_prefix() {
		Thread thd = Thread.currentThread();
		String pref = "thd=" + thd.getId() + " nam=" + thd.getName();
		return pref;
	}

	private static void add_thd_prefix(StringBuilder full_str) {
		full_str.append(get_thd_prefix());
	}

	private static StringBuilder add_msg(StringBuilder full_str, String msg) {
		add_thd_prefix(full_str);
		full_str.append(" '" + msg + "'\n");
		return full_str;
	}

	private static StringBuilder add_error(StringBuilder full_str,
			Throwable ex1, String msg) {
		add_reps(full_str, 3, "\n");
		add_reps(full_str, 16, "ERROR.");
		full_str.append('\n');
		add_thd_prefix(full_str);
		full_str.append('\n');
		add_stack(full_str, new RuntimeException());
		if (ex1 != null) {
			full_str.append("\n");
			add_stack(full_str, ex1);
			full_str.append(" '" + ex1.getMessage() + "'\n");
			if(ex1 instanceof bad_emetcode){
				full_str.append(((bad_emetcode)ex1).msg + "\n");
			}
		}
		if (msg != null) {
			full_str.append(" '" + msg + "'\n");
		}
		full_str.append("\n\n");
		add_reps(full_str, 16, "error.");
		add_reps(full_str, 3, "\n");
		//full_str.append('\n');
		return full_str;
	}

	private static void print_str(String msg) {
		System.out.print(msg);
		System.out.flush();
	}

	public static void error(Throwable ex1, String msg) {
		String err_str = add_error(new StringBuilder(), ex1, msg).toString();
		if (external_logger != null) {
			external_logger.e("emet", err_str);
		} else {
			print_str(err_str);
		}
	}

	public static void debug(String msg) {
		String dbg_str = add_msg(new StringBuilder(), msg).toString();
		if (external_logger != null) {
			external_logger.d("emet", dbg_str);
		} else {
			print_str(dbg_str);
		}
	}

	public static void debug(String format, Object... args) {
		String str = String.format(format, args);
		debug(str);
	}

	public static void info(String format, Object... args) {
		String str = String.format(format, args);
		info(str);
	}

	public static void info(String msg) {
		String inf_str = add_msg(new StringBuilder(), msg).toString();
		if (external_logger != null) {
			external_logger.i("emet", inf_str);
		} else {
			print_str(inf_str);
		}
	}

	public static void info(List<String> msg) {
		String pref = get_thd_prefix();
		if (external_logger != null) {
			external_logger.i("emet", pref + "\n");
			for (String ss : msg) {
				external_logger.i("emet", ss + "\n");
			}
		} else {
			print_str(pref + "\n");
			int aa = 0;
			for (String ss : msg) {
				print_str("\t" + aa + ": '" + ss + "'\n");
				aa ++;
			}
		}
	}

}
