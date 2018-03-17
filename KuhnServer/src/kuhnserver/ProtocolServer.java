/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kuhnserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 *
 * @author levanna
 */
public class ProtocolServer {
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
    private ArrayList cartas;
    private char cartaServidor;
    private char cartaCliente;
    private char cartaMesa;
    private int id;
    
    public ProtocolServer(Socket socket) throws UnknownHostException, IOException{
        this.socket = socket;
        this.utils = new ComUtils(this.socket);
        cartas = new ArrayList();
        cartas.add('J');
        cartas.add('Q');
        cartas.add('K');
        
        Collections.shuffle(cartas);
        estado = INICIAR;
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
        
        boolean salir = false;
        do{
            String cmd = this.utils.read_command();
            if(cmd != null) {
                System.out.println(cmd);
            }
            if(estado == INICIAR && cmd.equals("ANOK")){
                System.out.println("El cliente ha aceptado la apuesta inicial");
                salir = true;
            }
        }while(!salir);
    }
    
    public void dealer(int jugador) throws IOException{
        this.utils.write_command("DEAL");
        this.utils.write_space();
        this.utils.write_int32(jugador);
        if(jugador == 0){
            dealer = true;
        }
    }
    
    public void card() throws IOException{
        this.cartaCliente = (char) cartas.get(0);
        this.cartaServidor = (char) cartas.get(1);
        this.cartaMesa = (char) cartas.get(2);
        
        this.utils.write_command("CARD");
        this.utils.write_space();
        this.utils.writeChar(this.cartaCliente);

        if(dealer){
            readJuego();
        }
    }
    
    public void showdown(){
        //DEFINIR EN SERVIDOR
    }
    
    
    public void read() throws IOException{
        boolean salir = false;
        String cmd;
        do{
            cmd = utils.read_command();
            if(cmd.equals("STRT")){
                utils.read_space();
                id = utils.read_int32();
                salir = true;
            }
        }while(!salir); 
    }
    
    public void readJuego() throws IOException{
        this.turno++;
        boolean salir = false;
        do{
            String cmd = this.utils.read_command();
            
            if(dealer){
                if(cmd.equals("CHCK")){
                    this.accionTurno = "P";
                    System.out.println("El cliente ha pasado");
                    salir = true;
                }else if(cmd.equals("BET_")){
                    this.accionTurno = "A";
                    System.out.println("El cliente ha apostado");
                    salir = true;
                }else if(cmd.equals("CALL")){
                    this.accionTurno = "C";
                    System.out.println("El cliente ha ido");
                    salir = true;
                }else if(cmd.equals("FOLD")){
                    this.accionTurno = "F";
                    System.out.println("El cliente se ha retirado");
                    salir = true;
                }
            }else{
                if(cmd.equals("CHCK")){
                    this.accionTurno = "P";
                    System.out.println("El cliente ha pasado");
                    salir = true;
                }else if(cmd.equals("BET_")){
                    this.accionTurno = "A";
                    System.out.println("El cliente ha apostado");
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
