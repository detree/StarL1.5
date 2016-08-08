package UDPrecv_java;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class mainClass {

	DatagramSocket dataIn;
	DatagramPacket receivePacket;
	byte[] recvBuf = new byte[200];  //buffer for recieving the data
	int corner[][] = {{-2000,2000},{2000,2000},{-2000,-2000},{2000,-2000}};
	double fieldW = corner[1][0]-corner[0][0], fieldH = corner[0][1]- corner[2][1];
	double screenW=1200, screenH=800;
	

	public static void main(String[] args) {
		new mainClass();
	}

	public mainClass() {
		try {
			receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
			dataIn = new DatagramSocket(9876);
		} catch (SocketException e) {
			e.printStackTrace();
		}

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
				}

				JFrame frame = new JFrame("Testing");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setLayout(new BorderLayout());
				frame.add(new TestPane());
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
	}

	public class TestPane extends JPanel {

		int x = 150, y = 50, r = 15; // Position and radius of the circle
		double yaw = 0;				// in RAD!!
		int destx = 0, desty = 0, destr=5;

		public TestPane() {
			Timer timer = new Timer(50, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						dataIn.receive(receivePacket);
						String sentence = new String( receivePacket.getData() );
						System.out.println(sentence);
						String[] parts = sentence.replace(",", "").split("\\|");
						x = Integer.parseInt(parts[1]);
						y = Integer.parseInt(parts[2]);
						yaw = Math.toRadians(Integer.parseInt(parts[3]));
						destx = Integer.parseInt(parts[4]);
						desty = Integer.parseInt(parts[5]);
						
						repaint();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			});
			timer.start();
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension((int)screenW, (int)screenH);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setColor(Color.red);
			int relaX = x - corner[0][0], relaY = y - corner[2][1];
			relaX /= (fieldW/screenW);
			relaY /= (fieldH/screenH);
			relaY = (int) (screenH - relaY);
			g.fillOval(relaX - r, relaY - r, r * 2, r * 2);
			g.setColor(Color.black);
			g.drawLine(relaX - (int)(r*Math.sin(yaw)), relaY - (int)(r*Math.cos(yaw)), relaX, relaY);

			g.setColor(Color.blue);
			relaX = destx - corner[0][0];
			relaY = desty - corner[2][1];
			relaX /= (double)((double)fieldW/(double)screenW);
			relaY /= (fieldH/screenH);
			relaY = (int) (screenH - relaY);
			g.fillOval(relaX - destr, relaY - destr, destr * 2, destr * 2);
		}
	}

}