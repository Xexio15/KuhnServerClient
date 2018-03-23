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
        int modo = 0;
        String nomMaquina = "localhost";
        int numPort = 1212;
        for(int i = 0; i < args.length; i++){
            if(args[i].equals("-s")){
                nomMaquina = args[i+1];
            }
            else if(args[i].equals("-p")){
                numPort = Integer.parseInt(args[i+1]);
            }else if(args[i].equals("-i")){
                modo = Integer.parseInt(args[i+1]);
            }
        }

        Menu menu = new Menu(nomMaquina, numPort, modo);
    } // fi del main
 
} // fi de la classe

