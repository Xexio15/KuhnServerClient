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
    public MenuServer(){
        menu();
    }
    
    private void menu(){
        Scanner sc = new Scanner(System.in);
        boolean salir = false;
        try {
            server = new ServerSocket(1212);
        } catch (IOException ex) {
            Logger.getLogger(MenuServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        do{
            System.out.println("Esperando conexión...");
            try {
                socket = server.accept();
                System.out.println("Connexion aceptada");
                protocolo = new ProtocolServer(socket);
                protocolo.read();
                protocolo.stakes(20, 15);
                
                protocolo.dealer(0);
                protocolo.card();
                
                do{
                    String accion = protocolo.getAccionTurno();
                    int turno = protocolo.getTurno();
                    int op;
                    if(protocolo.isDealer()){
                        
                        System.out.println(accion);
                        switch(turno){
                            case 2:
                                if(accion.equals("P")){
                                    System.out.println("Accion:\n   1- Pasar\n    2- Apostar");
                                    op = sc.nextInt();
                                    if(op == 1){
                                        protocolo.check();
                                    }else{
                                        protocolo.bet();
                                    }
                                }
                                break;
                        }
                    }
                    else{
                        switch(turno){
                            case 1:
                                System.out.println("Accion:\n   1- Pasar\n    2- Apostar");
                                op = sc.nextInt();
                                if(op == 1){
                                    protocolo.check();
                                }else{
                                    protocolo.bet();
                                }
                                break;
                            case 3:
                                System.out.println("Accion:\n   1- Ir\n    2- Retirarse");
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

            } catch (IOException ex) {
                Logger.getLogger(MenuServer.class.getName()).log(Level.SEVERE, null, ex);
            }

        }while(!salir);
    }
}

