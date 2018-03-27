/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kuhnclient;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Random;


/**
 *
 * @author levanna
 */
public class Menu {
    private Protocol protocolo;
    String nomMaquina;
    int numPort, modo;
    /**
     * Constructor
     */
    public Menu(String nomMaquina, int numPort, int modo){
        this.nomMaquina = nomMaquina;
        this.numPort = numPort;
        this.modo = modo;
        menu();
    }
    
    private void menu(){
        Scanner sc = new Scanner(System.in);
        boolean salir = false;
        int id = -1;

        do{
            imprimirMenu();
            int opcion = sc.nextInt();
            switch(opcion){
                case 1: 
                    //System.out.println("Introduce el nombre del servidor");
                    //String address = sc.next();
                    //System.out.println("Introduce el puerto de conexion");
                    //int port = sc.nextInt();
                    
                    try {
                        protocolo = new Protocol(nomMaquina, numPort);
                        protocolo.setModo(this.modo);
                    } catch (IOException ex) {
                        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    break;
                case 2:
                    protocolo.resetTurno();
                    if(protocolo != null){
                        if(id == -1){
                            System.out.println("Introduce una ID:");
                            id = sc.nextInt();
                        }
                        try {
                            protocolo.start(id);
                            if(this.modo == 1 || this.modo == 2){
                                protocolo.ante();
                            
                            }
                            if(this.modo == 0){
                                System.out.println("Aceptas la apuesta inicial?\n   1- Si\n   2- No");
                                int op = sc.nextInt();
                                if(op == 1){
                                    protocolo.ante();
                                    do{
                                        String accion = protocolo.getAccionTurno();
                                        int turno = protocolo.getTurno();

                                        if(protocolo.isDealer()){

                                            switch(turno){
                                                case 2:
                                                    if(accion.equals("P")){
                                                        System.out.println("Accion:\n   1- Pasar\n   2- Apostar\n   3- Salir");
                                                        op = sc.nextInt();
                                                        if(op == 1){
                                                            protocolo.check();
                                                        }else if(op == 2){
                                                            protocolo.bet();
                                                        }else{
                                                            protocolo.quit();
                                                        }
                                                    }else if(accion.equals("A")){
                                                        System.out.println("Accion:\n   1- Ir\n   2- Retirarse\n   3- Salir");
                                                        op = sc.nextInt();
                                                        if(op == 1){
                                                            protocolo.call();
                                                        }else if(op == 2){
                                                            protocolo.fold();
                                                        }else{
                                                            protocolo.quit();
                                                        }
                                                    }
                                                    break;
                                            }
                                        }
                                        else{
                                            switch(turno){
                                                case 1:
                                                    System.out.println("Accion:\n   1- Pasar\n   2- Apostar\n   3- Salir");
                                                    op = sc.nextInt();
                                                    if(op == 1){
                                                        protocolo.check();
                                                    }else if(op == 2){
                                                        protocolo.bet();
                                                    }else{
                                                        protocolo.quit();
                                                    }
                                                    break;
                                                case 3:
                                                    System.out.println("Accion:\n   1- Ir\n   2- Retirarse\n   3- Salir");
                                                    op = sc.nextInt();
                                                    if(op == 1){
                                                        protocolo.call();
                                                    }else if(op == 2){
                                                        protocolo.fold();
                                                    }else{
                                                        protocolo.quit();
                                                    }
                                                    break;
                                                default:
                                                    protocolo.readJuego();
                                            }
                                        }
                                    }while(protocolo.getTurno() < 4);
                                }else{
                                    protocolo.quit();
                                    protocolo = null;
                                }
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }else{
                        System.out.println("No estas conectado a ningun servidor");
                    }
                    break;
                case 3:
                    
                    salir = true;
                    break;
            }
        
        }while(!salir);
    }
    
    /**
     * Imprime el menu
     */
    public static void imprimirMenu(){
        System.out.println("Menú");
        System.out.println("=======================");
        System.out.println("   1- Conectar-se a un servidor");
        System.out.println("   2- Jugar");
        System.out.println("   3- Salir");
    }
}

