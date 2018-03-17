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
    private static final int PETICION = 1;
    private static final int INICIAR = 2;
    private static final int ESPERA_ACCION_SERVIDOR = 3;
    private static final int CONECTADO = 0;
    private int turno = 1;
    private String accionTurno = null;
    
    public Protocol(String nomMaquina, int port) throws UnknownHostException, IOException{
        this.address = InetAddress.getByName(nomMaquina);
        this.port = port;
        socket = new Socket(this.address, this.port);
        this.utils = new ComUtils(this.socket);
    }
    
    public void start(int id) throws IOException{
        this.utils.write_command("STRT");
        this.utils.write_space();
        this.utils.write_int32(id);
        estado = PETICION;
        read();
    }
    
    public void ante() throws IOException{
        this.utils.write_command("ANOK");
        estado = INICIAR;
        read();
    }
    
    public void quit() throws IOException{
        this.utils.write_command("QUIT");
        
    }
    
    //Apostar
    public void bet() throws IOException{
        this.utils.write_command("BET_");
        this.accionTurno = "A";
        this.readJuego();
    }
    
    //Pasar
    public void check() throws IOException{
        this.utils.write_command("CHCK");
        this.accionTurno = "P";
        this.readJuego();
    }
    
    //Ir
    public void call() throws IOException{
        this.accionTurno = "I";
        this.utils.write_command("CALL");
        this.readJuego();
    }
    
    //Retirarse
    public void fold() throws IOException{
        this.accionTurno = "R";
        this.utils.write_command("FOLD");
        this.readJuego();
    }
    
    public void stakes(int fichasCliente, int fichasServidor) throws IOException{
        this.utils.write_command("STKS");
        this.utils.write_space();
        this.utils.write_int32(fichasCliente);
        this.utils.write_space();
        this.utils.write_int32(fichasServidor);
    }
    
    public void dealer(int jugador) throws IOException{
        this.utils.write_command("DEAL");
        this.utils.write_space();
        this.utils.write_int32(jugador);
    }

    public void read() throws IOException{
        boolean salir = false;
        String cmd;
        int arg1;
        int arg2;
        char carta;
        do{
            cmd = this.utils.read_command();
            
            if(estado == PETICION && cmd.equals("STKS")){
                this.utils.read_space();
                arg1 = this.utils.read_int32();
                System.out.println("Tienes "+arg1+" fichas");
                this.utils.read_space();
                this.utils.read_int32();
                salir = true;
            }
            else if(estado == INICIAR && cmd.equals("DEAL")){
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
            else if (estado == INICIAR && cmd.equals("CARD")){
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
        
        }while(!salir);
    }
    
    public void readJuego() throws IOException{
        this.turno++;
        boolean salir = false;
        do{
            String cmd = this.utils.read_command();
            
            if(cmd.equals("SHOW")){
                utils.read_space();
                System.out.println("El servidor tiene la carta: "+utils.readChar());
                salir = true;
            }else if(cmd.equals("STKS")){
                this.utils.read_space();
                int arg1 = this.utils.read_int32();
                System.out.println("Tienes "+arg1+" fichas");
                this.utils.read_space();
                this.utils.read_int32();
                salir = true;
            }else if(dealer){
                if(cmd.equals("CHCK")){
                    this.accionTurno = "P";
                    System.out.println("El servidor ha pasado");
                    salir = true;
                }else if(cmd.equals("BET_")){
                    this.accionTurno = "A";
                    System.out.println("El servidor ha apostado");
                    salir = true;
                }else if(cmd.equals("CALL")){
                    this.accionTurno = "C";
                    System.out.println("El servidor ha ido");
                    salir = true;
                }else if(cmd.equals("FOLD")){
                    this.accionTurno = "F";
                    System.out.println("El servidor se ha retirado");
                    salir = true;
                }
            }else{
                if(cmd.equals("CHCK")){
                    this.accionTurno = "P";
                    System.out.println("El servidor ha pasado");
                    salir = true;
                }else if(cmd.equals("BET_")){
                    this.accionTurno = "A";
                    System.out.println("El servidor ha apostado");
                    salir = true;
                }
            }

            
        }while(!salir);
    }
    
    public boolean isDealer(){
        return dealer;
    }
    
    public int getTurno(){
        return turno;
    }
    
    public String getAccionTurno(){
        return accionTurno;
    }
    
}
