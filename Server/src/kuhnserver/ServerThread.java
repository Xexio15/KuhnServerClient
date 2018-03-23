package kuhnserver;


import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
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
        this.socket = socket;
        this.modo = modo;
        Random rand = new Random();
        this.numDealer = rand.nextInt(2);
        try {
            this.protocolo = new ProtocolServer(socket);
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void run(){
        try {
            protocolo.setModo(modo);
            do{
                protocolo.resetTurno();
                protocolo.readJuego();
                protocolo.stakes(20, 15);//Habria que quitar este parametro i que el servidor guardase las fichas en una tabla por ID's, si el jugador es nuevo darle 20 fichas por ejemplo
                protocolo.dealer(this.numDealer);
                protocolo.card();
                if (this.numDealer==0){
                    this.numDealer=1;
                }else{
                    this.numDealer=0;
                }
                
            }while(true);
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
