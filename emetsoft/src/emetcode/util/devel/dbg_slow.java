package emetcode.util.devel;

public class dbg_slow {	
	long st_tm;
	
	public dbg_slow(){
		st_tm = System.currentTimeMillis();
	}
	
	public void log_if_slow(String msg){
		long dbg_slow_tm2 = System.currentTimeMillis();
		long diff_tm = dbg_slow_tm2 - st_tm;
		long s3 = 3000;
		if (diff_tm > s3) {
			logger.info(msg);
		}
	}
}
