package emetcode.test.udp;

import java.net.*;

import emetcode.util.devel.logger;
import emetcode.util.devel.thread_funcs;

public class UDP_multi_client {

	public static void main(String args[]) throws Exception {
		main_client(args);
		// test_msg_peer(args);
	}

	public static void main_client(String args[]) throws Exception {
		System.out.println("Starting UDP_clients");
		DatagramSocket clientSocket = new DatagramSocket();

		for (int aa = 0; aa < 10; aa++) {
			String thd_nm = Thread.currentThread().getName() + "-thd-" + aa;
			thread_funcs.start_thread(thd_nm, send_some(clientSocket), false);
		}
	}

	static Runnable send_some(final DatagramSocket sok) {
		logger.debug("send_some");
		Runnable rr1 = new Runnable() {
			public void run() {
				try {
					byte[] sendData = new byte[1024];
					byte[] receiveData = new byte[1024];
					InetAddress IPAddress = InetAddress.getByName("localhost");
					String msg = Thread.currentThread().getName();
					long num_msg = 0;
					while (true) {
						num_msg++;
						String sentence = msg + "-" + num_msg;
						sendData = sentence.getBytes();
						DatagramPacket sendPacket = new DatagramPacket(
								sendData, sendData.length, IPAddress, 9876);
						sok.send(sendPacket);
						DatagramPacket receivePacket = new DatagramPacket(
								receiveData, receiveData.length);
						sok.receive(receivePacket);
						String modifiedSentence = new String(
								receivePacket.getData());
						System.out.println("FROM SERVER:" + modifiedSentence);
					}
					// sok.close();
				} catch (Exception ex1) {
					logger.info(ex1.toString());
				}
			}
		};
		return rr1;
	}

	public static void test_msg_peer(String args[]) {
		System.out.println("TEST start");
	}
}
