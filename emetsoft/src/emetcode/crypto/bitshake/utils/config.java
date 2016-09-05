package emetcode.crypto.bitshake.utils;

import java.nio.charset.Charset;

public class config {

	public static final boolean DEBUG = false;

	public static final int KB_1 = 1000;
	public static final int MB_1 = 1000 * KB_1;

	public static final int AVERAGE_BUFF_SZ = 100 * KB_1;

	public static final String SIGNER_ID = "sner";
	public static final String SIGNATURE_ID = "snat";
	public static final String SHA_ID = "sha2";

	public static final String PRE_TMP = "tmp_"; // file name prefix

	public static final String RAND_STR = "rand"; // frame for randomizer state

	public static final String UTF_8_NAM = "UTF-8";
	public static final Charset UTF_8 = Charset.forName(UTF_8_NAM);

	public static final String UTF_16_NAM = "UTF-16";
	public static final Charset UTF_16 = Charset.forName(UTF_16_NAM);

	public static final long MIN_KEY_INIT_CHANGES = 1000;
	public static final long MAX_KEY_INIT_CHANGES = 2000;

	public static final byte DOT_BYTE = (byte) ('.');

	public static final char PIPE_CHAR = '|';
	public static final char EOL_CHAR = '\n';
	public static final char USC_CHAR = '_';
	public static final char SPC_CHAR = ' ';

	public static int NUM_BYTES_RANDOM_KEY_SZ = 100;
	public static int NUM_BYTES_SEED = 100;

	public static final String INVALID_IP = "invalid_ip";
	public static final String INVALID_GPS = "invalid_gps";
	public static final String INVALID_SHA = "invalid_sha";

	public static String CURRENT_IP_DESCRP = INVALID_IP;
	public static String CURRENT_GPS_DESCRP = INVALID_GPS;

	public static final String SHA_256_STR = "SHA-256";
	public static final String SHA_512_STR = "SHA-512";

	public static boolean has_valid_ip_descrip() {
		return (CURRENT_IP_DESCRP != INVALID_IP);
	}

	public static boolean has_valid_gps_descrip() {
		return (CURRENT_GPS_DESCRP != INVALID_GPS);
	}

}
