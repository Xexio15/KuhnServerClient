/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kuhnclient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 *
 * @author levanna
 */
public class Protocol {
    private InetAddress address;
    private int port;
    private Socket socket;
    private ComUtils utils;
    private int estado;
    private boolean dealer = false;
    private static final int CONECTADO = 1;
    private static final int CONECTADO = 1;
    private static final int CONECTADO = 1;
    private static final int CONECTADO = 1;
    
    public Protocol(String nomMaquina, int port) throws UnknownHostException, IOException{
        this.address = InetAddress.getByName(nomMaquina);
        this.port = port;
        socket = new Socket(this.address, this.port);
        this.utils = new ComUtils(this.socket);
    }
    
    public void start(int id) throws IOException{
        this.utils.write_string("STRT ");
        this.utils.write_int32(id);
        estado = PETICION;
        read();
    }
    
    public void ante() throws IOException{
        this.utils.write_string("ANOK");
        estado = INICIAR;
        read();
    }
    
    public void quit() throws IOException{
        this.utils.write_string("QUIT");
        
    }
    
    public void bet() throws IOException{
        this.utils.write_string("BET_");
        estado = ;
    }
    
    public void check() throws IOException{
        this.utils.write_string("CHCK");
        estado = ;
    }
    
    public void call() throws IOException{
        this.utils.write_string("CALL");
    }
    
    public void fold() throws IOException{
        this.utils.write_string("FOLD");
    }
    
    public void stakes(int fichasCliente, int fichasServidor) throws IOException{
        this.utils.write_string("STKS ");
        this.utils.write_int32(fichasCliente);
        this.utils.write_string(" ");
        this.utils.write_int32(fichasServidor);
    }
    
    public void dealer(int jugador) throws IOException{
        this.utils.write_string("DEAL ");
        this.utils.write_int32(jugador);
    }
    
    public void card(String carta) throws IOException{
        this.utils.write_string("CARD "+carta);
    }
    
    public void showdown(){
        //PROTOCOO MAL DEFINIDO I NUESTROS NOMBRES TAMBIEN
    }
    
    
    public void read() throws IOException{
        boolean salir = false;
        String cmd;
        int arg1;
        int arg2;
        char carta;
        do{
            cmd = this.utils.read_command();
            
            if(estado == PETICION && cmd == "STKS"){
                this.utils.read_space();
                arg1 = this.utils.read_int32();
                System.out.println("Tienes "+arg1+" fichas");
                this.utils.read_space();
                this.utils.read_int32();
                salir = true;
            }
            else if(estado == INICIAR && cmd == "DEAL"){
                this.utils.read_space();
                arg1 = this.utils.read_int32();
                if(arg1 == 1){
                    System.out.println("Eres el dealer");
                    dealer = true;
                }else{
                    System.out.println("El servidor es el dealer");
                    dealer = false;
                }
            }
            else if (estado == INICIAR && cmd == "CARD"){
                this.utils.read_space();
                carta = this.utils.readChar();
                System.out.println("Tu carta es: "+carta);
                if(dealer){//Empieza el servidor
                    salir = false;
                    estado = ESPERA_ACCION_SERVIDOR;
                }else{//Empiezas tú, el cliente
                    salir = true;
                }
            }
        
        }while(!salir)
    }
    
    public void isDealer(){
        return dealer;
    }
    
}
