package tcpRecv_java;

public class Main {
	public static void main(String[] args) {

    	TCPRecv t = new TCPRecv();
        t.initializeConnection();

        while(t.sc.hasNext()) {
        System.out.println(System.currentTimeMillis() + " / " + t.sc.nextLine());
        }
    }
}
