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
public class Protocolo {
    private InetAddress address;
    private int port;
    private Socket socket;
    private ComUtils utils;
    public Protocolo(String nomMaquina, int port) throws UnknownHostException, IOException{
        this.address = InetAddress.getByName(nomMaquina);
        this.port = port;
        socket = new Socket(this.address, this.port);
        this.utils = new ComUtils(this.socket);
    }
    
    public void start(int id) throws IOException{
        this.utils.write_string("STRT ");
        this.utils.write_int32(id);
    }
    
    public void ante() throws IOException{
        this.utils.write_string("ANOK");
    }
    
    public void quit() throws IOException{
        this.utils.write_string("QUIT");
    }
    
    public void bet() throws IOException{
        this.utils.write_string("BET_");
    }
    
    public void check() throws IOException{
        this.utils.write_string("CHCK");
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
}
