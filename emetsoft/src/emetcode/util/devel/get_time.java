package emetcode.util.devel;

public class get_time {
	public static void main(String[] args){
		long old_tm = 0;
		if(args.length > 0){
			old_tm = Long.parseLong(args[0]);
			System.out.println("DIFF=");
		}
		long diff = System.currentTimeMillis() - old_tm;
		System.out.println("" + diff);
	}
}
