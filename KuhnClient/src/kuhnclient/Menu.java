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
    private Protocolo protocolo;
    
    public Menu(){
        menu();
    }
    
    private void menu(){
        Scanner sc = new Scanner(System.in);
        boolean salir = false;

        do{
            imprimirMenu();
            int opcion = sc.nextInt();
            switch(opcion){
                case 1: 
                    System.out.println("Introduce el nombre del servidor");
                    String address = sc.next();
                    System.out.println("Introduce el puerto de conexion");
                    int port = sc.nextInt();
                    try {
                        protocolo = new Protocolo(address, port);
                    } catch (IOException ex) {
                        Logger.getLogger(KuhnClient.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    break;
                case 2:
                    if(protocolo != null){
                        Random rand = null;
                        int randomNum = rand.nextInt((9999 - 1000) + 1) + 1000;
                        try {
                            protocolo.start(randomNum);
                            
                        } catch (IOException ex) {
                            Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }else{
                        System.out.println("No estas conectado a ningun servidor");
                    }
                    break;
                case 3:
                    System.out.println("Opcion3");
                    salir = true;
                    break;
            }

        }while(!salir);
    }
    
    public static void imprimirMenu(){
        System.out.println("Menú");
        System.out.println("=======================");
        System.out.println("   1- Conectar-se a un servidor");
        System.out.println("   2- Jugar");
        System.out.println("   3- Salir");
    }
}

