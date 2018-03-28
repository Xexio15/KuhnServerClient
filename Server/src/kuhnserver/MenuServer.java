/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kuhnserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Random;


/**
 *
 * @author levanna
 */
public class MenuServer {
    private ProtocolServer protocolo;
    private ServerSocket server;
    private Socket socket;
    private int numPort;
    private int modo;
    
    public MenuServer(int numPort, int modo){
        this.numPort = numPort;
        this.modo = modo;
        menu();
    }
    
    private void menu(){
        Scanner sc = new Scanner(System.in);
        boolean salir = false;
        //int modoJuego;
        try {
            server = new ServerSocket(numPort);
        } catch (IOException ex) {
            Logger.getLogger(MenuServer.class.getName()).log(Level.SEVERE, null, ex);
        }

          int modoJuego = modo;
        do{
            System.out.println("Esperando conexión...");
            try {
         
                socket = server.accept();
                System.out.println("Connexion aceptada");
                if(modoJuego != 3){
                    ServerThread st = new ServerThread(socket, modoJuego);
                    st.start();
                }
                    
                //MODE NO DEMANAT A LA PRACTICA JUGADORvsJUGADOR no MULTITHREAD
                if(modoJuego == 3){
                    protocolo = new ProtocolServer(socket);
                    protocolo.setModo(modoJuego);
                    protocolo.resetTurno();
                    protocolo.readJuego();
                    protocolo.stakes();//Habria que quitar este parametro i que el servidor guardase las fichas en una tabla por ID's, si el jugador es nuevo darle 20 fichas por ejemplo
                    Random rand = new Random();
                    protocolo.dealer(rand.nextInt(2));
                    protocolo.card();
                    do{
                        String accion = protocolo.getAccionTurno();
                        int turno = protocolo.getTurno();
                        int op;

                        if(protocolo.isDealer()){
                            switch(turno){
                                case 2:
                                    if(accion.equals("P")){
                                        System.out.println("Accion:\n   1- Pasar\n   2- Apostar");
                                        op = sc.nextInt();
                                        if(op == 1){
                                            protocolo.check();
                                        }else{
                                            protocolo.bet();
                                        }
                                    }else if(accion.equals("A")){
                                        System.out.println("Accion:\n   1- Ir\n   2- Retirarse");
                                        op = sc.nextInt();
                                        if(op == 1){
                                            protocolo.call();
                                        }else{
                                            protocolo.fold();
                                        }
                                    }
                                    break;
                            }
                        }
                        else{
                            switch(turno){
                                case 1:
                                    System.out.println("Accion:\n   1- Pasar\n   2- Apostar");
                                    op = sc.nextInt();
                                    if(op == 1){
                                        protocolo.check();
                                    }else{
                                        protocolo.bet();
                                    }
                                    break;

                                case 3:
                                    System.out.println("Accion:\n   1- Ir\n   2- Retirarse");
                                    op = sc.nextInt();
                                    if(op == 1){
                                        protocolo.call();
                                    }else{
                                        protocolo.fold();
                                    }
                                    break;
                            }
                        }
                    }while(protocolo.getTurno() < 4);
                }
      
            } catch (IOException ex) {
                Logger.getLogger(MenuServer.class.getName()).log(Level.SEVERE, null, ex);
            }

        }while(!salir);
    }
}

