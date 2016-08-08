package UDPrecv_java;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;

import javax.swing.JPanel;
import javax.swing.Timer;

public class move extends JPanel implements ActionListener
{
	Timer t = new Timer(7, this);
	int x = 10, y = 10, velX = 7, velY = 7;

	public void paintComponent(Graphics g, Graphics h)
	{
		super.paintComponent(h);
		super.paintComponent(g);
		System.out.println(g);
		Graphics2D g2 = (Graphics2D) g;
		Ellipse2D circle = new Ellipse2D.Double(x, y, 40, 40);
		g2.fill(circle);
		t.start();
	}

	public void actionPerformed(ActionEvent e) {
		if(x<0 || x > getWidth())
		{
			velX = -velX;
		}
		if(y < 0 || y > getHeight())
		{
			velY = -velY;
		}
		x += velX;
		y += velY;
		repaint();
	}   
}