package emetcode.net.mudp;

import java.nio.charset.Charset;

public class config {

	public static final boolean DEBUG = false;

	public static final String UTF_8_NAM = "UTF-8";
	public static final Charset UTF_8 = Charset.forName(UTF_8_NAM);

	public static final long MIN_MILLIS = 60000L;
	public static final long DAY_MILLIS = 86400000L;
	public static final long YEAR_MILLIS = 31556952000L;

	public static final String FLD_SEP = ":";

}
