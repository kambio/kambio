package emetcode.net.netmix;

import java.nio.charset.Charset;

public class config {

	public static final boolean DEBUG = false;
	
	public static final String UTF_8_NAM = "UTF-8";
	public static final Charset UTF_8 = Charset.forName(UTF_8_NAM);

	public static final long MIN_MILLIS = 60000L;
	public static final long DAY_MILLIS = 86400000L;
	public static final long YEAR_MILLIS = 31556952000L;

	public static final String YEAR_FAILURES_FNAM = "year_failures.dat";
	public static final String TOP_LOCATIONS_FNAM = "top_locations.dat";
	public static final String LOCAL_DOMAIN_FNAM = "local_domain.dat";
	public static final String LAST_ADDR_DESCRIP_FNAM = "last_addr.dat";

	public static final char DN_EOL = '\n';
	public static final String DN_PROT_ID = "SUSP01" + '\n';
	public static final byte[] DN_PROT_ID_BYTES = DN_PROT_ID.getBytes(UTF_8);
	public static final String DN_MSG_FIRST_DELIM = "SoM" + '\n';
	public static final byte[] DN_FIRST_BYTES = DN_MSG_FIRST_DELIM
			.getBytes(UTF_8);
	public static final String DN_MSG_LAST_DELIM = "EoM" + '\n';
	public static final byte[] DN_LAST_BYTES = DN_MSG_LAST_DELIM
			.getBytes(UTF_8);

	public static int TCP_CONNECT_TIMEOUT_MILLIS = (int) (2 * MIN_MILLIS);
	public static long I2P_CONNECT_TIMEOUT_MILLIS = (8 * MIN_MILLIS);
	public static String DN_NETMIX_DIR = "netmix";
	public static String DN_GLIDS_DIR = "nx_glids";
	public static String DN_COIDS_DIR = "nx_coids";
	public static String DN_BY_COREF_DIR = "nx_corefs";
	public static String DN_GAMAL_DIR = "gamal_sys";
	public static String DN_GAMAL_FILE_SUFIX = ".gam";
	public static String DN_REF_FILE_SUFIX = ".ref";
	public static String DN_REF_CONFIRM_FILE_SUFIX = ".ref_ok";
	public static int NUM_BYTES_RANDOM_KEY_SZ = 100;

	public final static int MIN_DAYS_TO_RECREATE_GAMAL = 7;
	public static int DAYS_TO_RECREATE_GAMAL = MIN_DAYS_TO_RECREATE_GAMAL;

}
