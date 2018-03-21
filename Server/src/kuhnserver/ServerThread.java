package kuhnserver;


import java.io.IOException;
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
    public ServerThread(Socket socket, int modo){
        this.socket = socket;
        this.modo = modo;
    }
    
    public void run(){
        try {
            ProtocolServer protocolo = new ProtocolServer(socket);
            protocolo.setModo(modo);
            protocolo.resetTurno();
            protocolo.read();
            protocolo.stakes(20, 15);//Habria que quitar este parametro i que el servidor guardase las fichas en una tabla por ID's, si el jugador es nuevo darle 20 fichas por ejemplo
            Random rand = new Random();
            protocolo.dealer(rand.nextInt(2));
            protocolo.card();
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
