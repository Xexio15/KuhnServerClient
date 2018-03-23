/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kuhnclient;

/**
 *
 * @author xexio
 */
public class Client {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args){
        
        String nomMaquina = args[0];
        int numPort =Integer.parseInt(args[1]);
        int modo =Integer.parseInt(args[2]);
        Menu menu = new Menu(nomMaquina, numPort, modo);
    } // fi del main
 
} // fi de la classe

