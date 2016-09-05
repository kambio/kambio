package emetcode.test.udp;

import java.io.*;

public class QuoteServer {
	public static void main(String[] args) throws IOException {
		System.out.println("Starting QuoteServer");
		new QuoteServerThread().start();
	}
}
