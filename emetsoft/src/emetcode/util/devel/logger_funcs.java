package emetcode.util.devel;

public interface logger_funcs {

	public abstract void d(String tag, String msg);

	public abstract void d(String tag, String msg, Throwable tr);

	public abstract void e(String tag, String msg);

	public abstract void e(String tag, String msg, Throwable tr);

	public abstract void i(String tag, String msg);

	public abstract void i(String tag, String msg, Throwable tr);

	public abstract void v(String tag, String msg);

	public abstract void v(String tag, String msg, Throwable tr);

	public abstract void w(String tag, String msg);

	public abstract void w(String tag, String msg, Throwable tr);

}
