package UDPrecv_java;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class mainClass {

	public static void main(String args[]) throws Exception
	{
		DatagramSocket serverSocket = new DatagramSocket(9876);

		// add a shutdown hook to close the socket if system crashes or exists unexpectedly
        Thread closeSocketOnShutdown = new Thread() {
            public void run() {
                serverSocket.close();
            }
        };
        Runtime.getRuntime().addShutdownHook(closeSocketOnShutdown);


		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024];
		while(true)
		{
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receivePacket);
			String sentence = new String( receivePacket.getData() );
			//long sendt = Long.parseUnsignedLong(sentence);
			System.out.println("RECEIVED: " + sentence + " \\ " + System.currentTimeMillis());
		}
	}
}



//import java.awt.Color;
//import javax.swing.JFrame;
//
//public class mainClass {
//	public static void main(String[] args)
//	{
//		move s = new move();
//		JFrame f = new JFrame("move");
//		BouncingCircle b = new BouncingCircle();
//		f.add(b);
//		f.setVisible(true);
//		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		f.setSize(1000, 1000);
//		f.setTitle("Moving Circle");
//		b.start();
//		f.setBackground(Color.GREEN);
//	}
//}
