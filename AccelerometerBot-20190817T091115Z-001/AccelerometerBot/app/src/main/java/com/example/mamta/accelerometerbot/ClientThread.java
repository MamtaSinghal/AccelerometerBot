package com.example.mamta.accelerometerbot;

import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientThread extends Thread {

    String dstAddress;
    int dstPort;
    private boolean running;
    MainActivity.ClientHandler handler;

    Socket socket;
    PrintWriter printWriter;
    BufferedReader bufferedReader;

    public ClientThread(String addr, int port, MainActivity.ClientHandler handler) {
        super();
        dstAddress = addr;
        dstPort = port;
        this.handler = handler;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    private void sendState(String state) {
        handler.sendMessage(
                Message.obtain(handler,
                        MainActivity.ClientHandler.UPDATE_STATE, state));
    }

    public void txMsg(String msgToSend) {
        if (printWriter != null) {
            printWriter.println(msgToSend);
            printWriter.flush();
        }
    }

    @Override
    public void run() {
//        sendState("Connecting...");

        running = true;

        try {

            socket = new Socket(dstAddress, dstPort);// NICEEEEEEEE

//            sendState("Connected");
            OutputStream outputStream = socket.getOutputStream();
            printWriter = new PrintWriter(outputStream, true);

            InputStream inputStream = socket.getInputStream();
            InputStreamReader inputStreamReader =
                    new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(inputStreamReader);

            while (running) {

                //bufferedReader block the code
//                String line = bufferedReader.readLine();
//                if (line != null) {
//                    handler.sendMessage(
//                            Message.obtain(handler,
//                                    MainActivity.ClientHandler.UPDATE_MSG, line));
//                }

            }
        }catch(UnknownHostException e){
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        } finally{
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            if (printWriter != null) {
                printWriter.close();
//                sendState("writer disconnected");
            }

            if (socket != null) {
                try {
                    socket.close();
                    Log.e("Client Thread", "Socket Closed");
//                    sendState("socket disconnected");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        handler.sendEmptyMessage(MainActivity.ClientHandler.UPDATE_END);
    }


    public void closeConnection() {

//        sendState("Disconnected");
        try {
            socket.close();
        } catch (IOException ex) {
            System.out.println("Error closing the socket and streams");
        }catch (NullPointerException e){
            Log.e("CONNECTION END", "closeConnection: connection closed");
        }finally {
            handler.sendEmptyMessage(MainActivity.ClientHandler.UPDATE_END);
        }

    }
}
