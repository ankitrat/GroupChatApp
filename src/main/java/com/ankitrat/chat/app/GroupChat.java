package com.ankitrat.chat.app;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Scanner;

public class GroupChat {
    static boolean isFinished = false;
    static String name;

    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("2 parameters are required...");
        } else {
            try {
                InetAddress group = InetAddress.getByName(args[0]);
                int port = Integer.parseInt(args[1]);
                Scanner sc = new Scanner(System.in);
                name = sc.nextLine();
                MulticastSocket socket = new MulticastSocket(port);
                socket.setTimeToLive(0);
                socket.joinGroup(group);
                Thread t = new Thread(new ReadThread(socket, group, port));
                t.start();
                System.out.println("Start typing messages...\n");
                while (true) {
                    String message;
                    message = sc.nextLine();
                    if (message.equalsIgnoreCase("Exit")) {
                        isFinished = true;
                        socket.leaveGroup(group);
                        socket.close();
                        break;
                    }
                    message = name + " : " + message;
                    byte[] buffer = message.getBytes();
                    DatagramPacket datagram = new
                            DatagramPacket(buffer, buffer.length, group, port);
                    socket.send(datagram);


                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

class ReadThread implements Runnable {

    private MulticastSocket socket;
    private InetAddress group;
    private int port;
    private static final int MAX_LEN = 1000;

    ReadThread(MulticastSocket socket, InetAddress group, int port) {
        this.socket = socket;
        this.group = group;
        this.port = port;
    }

    public void run() {
        while (!GroupChat.isFinished) {
            byte[] buffer = new byte[ReadThread.MAX_LEN];
            DatagramPacket datagram = new DatagramPacket(buffer, buffer.length, group, port);
            String message;
            try {
                socket.receive(datagram);
                message = new String(buffer, 0, datagram.getLength(), "UTF-8");
                if (!message.startsWith(GroupChat.name)) {
                    System.out.println(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
