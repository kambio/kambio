

package io.github.kambio.kambio;

import android.util.Log;
import emetcode.util.devel.logger_funcs;

public class andro_logger implements logger_funcs {
	
	
	public void d(String tag, String msg){
		Log.d(tag, msg);
	}
	public void d(String tag, String msg, Throwable tr){
		Log.d(tag, msg, tr);		
	}
	public void e(String tag, String msg){
		Log.e(tag, msg);				
	}
	public void e(String tag, String msg, Throwable tr){
		Log.e(tag, msg, tr);		
	}
	public void i(String tag, String msg){
		Log.i(tag, msg);		
	}
	public void i(String tag, String msg, Throwable tr){
		Log.i(tag, msg, tr);		
	}
	public void v(String tag, String msg){
		Log.v(tag, msg);		
	}
	public void v(String tag, String msg, Throwable tr){ 
		Log.v(tag, msg, tr);
	}
	public void w(String tag, String msg){
		Log.w(tag, msg);		
	}
	public void w(String tag, String msg, Throwable tr){
		Log.w(tag, msg, tr);		
	}
	
	private static void init_emet_logger(){
		emetcode.util.devel.logger.external_logger = new andro_logger();
	}
	
	static{
		init_emet_logger();
	}
}
