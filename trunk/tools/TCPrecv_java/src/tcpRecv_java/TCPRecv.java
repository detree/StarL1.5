package tcpRecv_java;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class TCPRecv {

    private Socket socket;
    private PrintWriter out;
    public Scanner sc;

    /**
     * Initialize connection to the phone
     *
     */
    public void initializeConnection(){
        //Create socket connection
        try{
            socket = new Socket("localhost", 38300);
            out = new PrintWriter(socket.getOutputStream(), true);
            //in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            sc = new Scanner(socket.getInputStream());

            // add a shutdown hook to close the socket if system crashes or exists unexpectedly
            Thread closeSocketOnShutdown = new Thread() {
                public void run() {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };

            Runtime.getRuntime().addShutdownHook(closeSocketOnShutdown);

        } catch (UnknownHostException e) {
            System.err.println("Socket connection problem (Unknown host)" + e.getStackTrace());
        } catch (IOException e) {
            System.err.println("Could not initialize I/O on socket " + e.getStackTrace());
        }
    }
}
