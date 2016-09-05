package emetcode.test.udp;

import java.io.*;
import java.net.*;


public class UDP_client {
	
	public static void main(String args[]) throws Exception {
		//main_client(args);
		test_msg_peer(args);
	}
	
	public static void main_client(String args[]) throws Exception {
		System.out.println("Starting UDP_client");
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(
				System.in));
		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress IPAddress = InetAddress.getByName("localhost");
		byte[] sendData = new byte[1024];
		byte[] receiveData = new byte[1024];
		System.out.println("Type message and enter:");
		String sentence = inFromUser.readLine();
		sendData = sentence.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData,
				sendData.length, IPAddress, 9876);
		clientSocket.send(sendPacket);
		DatagramPacket receivePacket = new DatagramPacket(receiveData,
				receiveData.length);
		clientSocket.receive(receivePacket);
		String modifiedSentence = new String(receivePacket.getData());
		System.out.println("FROM SERVER:" + modifiedSentence);
		clientSocket.close();
	}

	public static void test_msg_peer(String args[]){
		System.out.println("TEST start");
	}
}
