package emetcode.util.devel;

public class bad_emetcode extends Error {

	private static final long serialVersionUID = 1L;

	public static final String NO_DESCRIPTION = "No description for error";

	public String msg;
	public int code;

	public bad_emetcode(int cod) {
		code = cod;
		msg = NO_DESCRIPTION;
	}

	public bad_emetcode(int cod, String msg2) {
		code = cod;
		msg = new String(msg2);
	}

	public String toString() {
		String msg2 = super.toString() + ".\n" + msg;
		return msg2;
	}

}
