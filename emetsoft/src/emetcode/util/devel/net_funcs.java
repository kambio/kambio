package emetcode.util.devel;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class net_funcs {
	static final boolean IN_DEBUG_01 = false; // choose & can give
	
//	public static final String NO_NET = "NO_NET";
//	public static final String I2P_NET = "I2P_NET";
//	public static final String TCP_NET = "TCP_NET";
//	public static final String MUDP_NET = "MUDP_NET";

	public static final int NO_NET = 22;
	public static final int I2P_NET = 23;
	public static final int TCP_NET = 24;
	public static final int MUDP_NET = 25;
	
	public static final String INVALID_HOST_NAME = "INVALID_NAME";

	public static String get_hostname() {
		String fqdn = INVALID_HOST_NAME;
		try {
			InetAddress iAddress = InetAddress.getLocalHost();
			fqdn = iAddress.getCanonicalHostName();
		} catch (UnknownHostException ex) {
			if(IN_DEBUG_01){
				String stk_str = logger.get_stack_str();
				logger.info(ex +  "\n" + stk_str);
			}
		}
		return fqdn;
	}

	public static InetAddress[] get_all_ip() {
		List<InetAddress> all_ip = new ArrayList<InetAddress>();
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					all_ip.add(enumIpAddr.nextElement());
				}
			}
		} catch (SocketException ex1) {
			if(IN_DEBUG_01){
				String stk_str = logger.get_stack_str();
				logger.info(ex1 +  "\n" + stk_str);
			}
		}
		InetAddress[] arr_ip = all_ip.toArray(new InetAddress[0]);
		return arr_ip;
	}
	
	public static String get_net_kind_str(int net_kk){
		String nm = "NO_NET";
		switch(net_kk){
		case NO_NET:
			nm = "NO_NET";
			break;
		case I2P_NET:
			nm = "I2P_NET";
			break;
		case TCP_NET:
			nm = "TCP_NET";
			break;
		case MUDP_NET:
			nm = "MUDP_NET";
			break;
		}
		return nm;
	}
}
