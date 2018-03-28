package kuhnserver;


import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author xexio
 */
public class ServerThread extends Thread {
    private int modo;
    private Socket socket;
    private ProtocolServer protocolo;
    private int numDealer;
    public ServerThread(Socket socket, int modo){
        //try {
            this.socket = socket;
            //socket.setSoTimeout(3000);
            this.modo = modo;
            Random rand = new Random();
            this.numDealer = rand.nextInt(2);
        //} //catch (SocketException ex) {
            //Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        //}
        
    }
    
    public void run(){
        try {
            this.protocolo = new ProtocolServer(socket);
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            protocolo.setModo(modo);
            do{
                
                protocolo.readJuego();
                protocolo.stakes();
                protocolo.dealer(this.numDealer);
                protocolo.card();
                if (this.numDealer==0){
                    this.numDealer=1;
                }else{
                    this.numDealer=0;
                }
                protocolo.resetTurno();
                
            }while(socket != null);
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            try {
                socket.close();
            } catch (IOException ex) {
                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
}
