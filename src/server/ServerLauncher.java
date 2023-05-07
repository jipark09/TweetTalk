package server;

import util.IOUtil;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerLauncher {

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(IOUtil.PORT);
            InetAddress myIP = InetAddress.getLocalHost();
            System.out.println("내 IP 주소: " + myIP.getHostAddress());
            
            while(true) {
                Socket socket = serverSocket.accept();
                ServerReceiveSender sender = new ServerReceiveSender(socket);
                sender.start();
            }
        } catch (IOException e) {
            System.out.println("server main: " + e);
        }
    }
}
