/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kuhnserver;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author levanna
 */
public class ProtocolServer {
    private Socket socket;
    private ComUtils utils;
    private int estado;
    private boolean dealer = false;
    private static final int PETICION = 1;
    private static final int INICIAR = 2;
    private static final int FIN = 3;
    private int turno = 1;
    private String accionTurno = null;
    private ArrayList cartas;
    private char cartaServidor;
    private char cartaCliente;
    private char cartaMesa;
    private int id;
    private int fichasServidor;
    private int fichasCliente;
    private int bote = 0;
    private ArrayList tablaJugadores;
    private int modo = 3;
    private Logger logger = Logger.getLogger("Server"+Thread.currentThread().getName()+".log");  
    private FileHandler fh;
    public ProtocolServer(Socket socket) throws UnknownHostException, IOException{
        this.socket = socket;
        this.utils = new ComUtils(this.socket);
        cartas = new ArrayList();
        cartas.add('J');
        cartas.add('Q');
        cartas.add('K');
        tablaJugadores = new ArrayList(); //Tabla para guardar a los jugadores
        Collections.shuffle(cartas);
        estado = INICIAR;
        
        //LOGGER
        fh = new FileHandler("Server"+Thread.currentThread().getName()+".log");  
        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();  
        fh.setFormatter(formatter);  
        logger.setUseParentHandlers(false);

    }
    
    public void start(int id) throws IOException{
        this.utils.write_command("STRT");
        this.utils.write_space();
        this.utils.write_int32(id);
        this.fichasServidor--;
        this.bote++;
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
        this.turno = 5;
    }
    
    //Apostar
    public void bet() throws IOException{
        
        this.utils.write_command("BET_");
        logger.info("BET_");
        this.accionTurno = "A";
        this.fichasServidor--;
        this.bote++;
        this.turno++;
        this.readJuego();
        
    }
    
    //Pasar
    public void check() throws IOException{
        
        this.utils.write_command("CHCK");
        logger.info("CHCK");
        this.accionTurno = "P";
        
        //Si el seridor es el dealer i pasa hay showdown
        if(dealer && this.turno == 2){
            showdown();
        }else{
            this.turno++;
            this.readJuego();
        }
        
    }
    
    //Ir
    public void call() throws IOException{
        
        this.accionTurno = "I";
        this.utils.write_command("CALL");
        this.bote++;
        this.fichasServidor--;
        if(this.turno == 2 || this.turno == 3){
            showdown();
        }else{
            this.turno++;
            this.readJuego();
        }
        
    }
    
    //Retirarse
    public void fold() throws IOException{
        this.accionTurno = "R";
        this.utils.write_command("FOLD");
        logger.info("FOLD");
        System.out.println("Te has retirado, el cliente gana");
        stakes(this.fichasCliente+this.bote, this.fichasServidor);
        this.turno = 5;
        
    }
    
    public void stakes(int fichasCliente, int fichasServidor) throws IOException{
        this.fichasCliente = fichasCliente;
        this.fichasServidor = fichasServidor;
        this.utils.write_command("STKS");
        this.utils.write_space();
        this.utils.write_int32(fichasCliente);
        this.utils.write_space();
        this.utils.write_int32(fichasServidor);
        logger.info("STKS"+' '+fichasCliente+' '+fichasServidor);
        System.out.println("Tienes "+fichasServidor+" fichas");
        
        if(this.turno < 4){
            boolean salir = false;
            String cmd = this.utils.read_command();
            do{
               
                if(estado == INICIAR && cmd.equals("ANOK")){
                    System.out.println("El cliente ha aceptado la apuesta inicial");
                    this.fichasCliente--;
                    this.bote++;
                    salir = true;
                }
            }while(!salir);
        }else{
            estado = FIN;
        }
    }
    
    public void dealer(int jugador) throws IOException{
        char jug;
        if(jugador == 0){
            jug = '0';
        }else{
            jug = '1';
        }
        this.utils.write_command("DEAL");
        this.utils.write_space();
        this.utils.writeChar(jug);
        logger.info("DEAL"+' '+jug);
        if(jugador == 0){
            dealer = true;
            System.out.println("Eres el dealer");
        }else{
            dealer = false;
            System.out.println("El cliente es el dealer");
        }
    }
    
    public void card() throws IOException{
        this.cartaCliente = (char) cartas.get(0);
        this.cartaServidor = (char) cartas.get(1);
        this.cartaMesa = (char) cartas.get(2);
        
        this.utils.write_command("CARD");
        this.utils.write_space();
        this.utils.writeChar(this.cartaCliente);
        logger.info("CARD"+' '+this.cartaCliente);
        System.out.println("Tu carta es: "+ this.cartaServidor);
        if(dealer){
            readJuego();
        }else if(!dealer && this.modo == 1){
            accionAleatoria();
            readJuego();
        }
        else if(!dealer && this.modo == 2){
            accionOptima();
            readJuego();
        }
    }
    
    public void showdown() throws IOException{
        this.utils.write_command("SHOW");
        this.utils.write_space();
        this.utils.writeChar(this.cartaServidor);
        System.out.println("Tu carta es: "+this.cartaServidor);
        System.out.println("La carta del cliente es: "+this.cartaCliente);
        logger.info("SHOW"+' '+this.cartaServidor);
        this.turno = 5;
        if(valorCarta(this.cartaCliente) > valorCarta(this.cartaServidor)){
            System.out.println("Gana el cliente");
            stakes(this.fichasCliente+this.bote, this.fichasServidor);
        }
        else{
            System.out.println("Gana el servidor");
            stakes(this.fichasCliente, this.fichasServidor+this.bote);
        }
        
    }
    
    
    public void read() throws IOException{
        boolean salir = false;
        String cmd;
        do{
            cmd = utils.read_command();
            if(cmd.equals("STRT")){
                utils.read_space();
                id = utils.read_int32();
                System.out.println("El jugador "+id+" ha iniciado la partida");
                logger.info(cmd+' '+id);
                salir = true;
            }
        }while(!salir); 
    }
    
    public void readJuego() throws IOException{
        boolean salir = false;
        String cmd;
        do{
            cmd = this.utils.read_command();
            System.out.println(cmd);
            if(cmd.equals("STRT")){
                utils.read_space();
                id = utils.read_int32();
                System.out.println("El jugador "+id+" ha iniciado la partida");
                salir = true;
                logger.info(cmd+' '+id);
            }
            else if(dealer){
                if(cmd.equals("CHCK")){
                    this.accionTurno = "P";
                    System.out.println("El cliente ha pasado");
                    logger.info(cmd);
                    if(this.modo == 1){
                        salir = accionAleatoria();
                    }else if(modo == 2){
                        salir = accionOptima();
                    }
                    else if(modo == 3){
                        salir = true;
                    }
                    
                }
                else if(cmd.equals("BET_")){
                    this.fichasCliente--;
                    this.bote++;
                    this.accionTurno = "A";
                    logger.info(cmd);
                    System.out.println("El cliente ha apostado");
                    if(this.modo == 1){
                        salir = accionAleatoria();
                    }else if(modo == 2){
                        salir = accionOptima();
                    }
                    else if(modo == 3){
                        salir = true;
                    }
                }
                else if(cmd.equals("CALL")){
                    this.accionTurno = "C";
                    System.out.println("El cliente ha ido");
                    logger.info(cmd);
                    this.bote++;
                    this.fichasCliente--;
                    if(this.turno == 3){
                        showdown();
                    }
                    salir = true;
                }
                else if(cmd.equals("FOLD")){
                    this.accionTurno = "F";
                    System.out.println("El cliente se ha retirado, el servidor gana");
                    logger.info(cmd);
                    stakes(this.fichasCliente, this.fichasServidor+this.bote);
                    this.turno = 5;
                    salir = true;
                }
            }else if(!dealer){
                if(cmd.equals("CHCK")){
                    this.accionTurno = "P";
                    logger.info(cmd);
                    System.out.println("El cliente ha pasado");
                    if(this.turno == 2){
                        showdown();
                    }
                    salir = true;
                }
                else if(cmd.equals("BET_")){
                    this.fichasCliente--;
                    this.bote++;
                    this.accionTurno = "A";
                    logger.info(cmd);
                    System.out.println("El cliente ha apostado");
                    if(this.modo == 1){
                        salir = accionAleatoria();
                    }
                    else if(modo == 2){
                        salir = accionOptima();
                    }
                    else if(modo == 3){
                        salir = true;
                    }
                }
                else if(cmd.equals("CALL")){
                    this.accionTurno = "C";
                    logger.info(cmd);
                    System.out.println("El cliente ha ido");
                    this.bote++;
                    this.fichasCliente--;
                    if(this.turno == 2 || this.turno == 3){
                        showdown();
                    }
                    salir = true;
                }
                else if(cmd.equals("FOLD")){
                    this.accionTurno = "F";
                    logger.info(cmd);
                    System.out.println("El cliente se ha retirado, el servidor gana");
                    stakes(this.fichasCliente, this.fichasServidor+this.bote);
                    this.turno = 5;
                    salir = true;
                }
            }
            if(this.turno > 4){
                salir = true;
            }
        }while(!salir);
        if(!cmd.equals("STRT")){
            this.turno++;
        }
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
    public void resetTurno(){
        
        this.turno = 1;
        this.bote = 0;
        cartas = new ArrayList();
        cartas.add('J');
        cartas.add('Q');
        cartas.add('K');
        tablaJugadores = new ArrayList(); //Tabla para guardar a los jugadores
        Collections.shuffle(cartas);
        estado = INICIAR;
    }
    
    public void setModo(int modo){
        this.modo = modo;
    }
    
    public boolean accionAleatoria() throws IOException{
        Random rand = new Random();
        int accion;
        if(!dealer){    
            if(this.turno == 1){
                accion = rand.nextInt(2);
                if(accion == 0){
                    this.check();
                }else{
                    this.bet();
                }
            }
            if(this.accionTurno.equals("A")){
                accion = rand.nextInt(2);
                this.turno++;
                if(accion == 0){
                    this.fold();
                }else{
                    this.call();
                }
                if(this.turno > 4){
                    return true;
                }
                return false;
            }
        }
        
        else{
            if(this.accionTurno.equals("P")){
                accion = rand.nextInt(2);
                this.turno++;
                if(accion == 0){
                    this.check();
                }else{
                    this.bet();
                }
                if(this.turno > 4){
                    return true;
                }
                return false;
            }
            else if(this.accionTurno.equals("A")){
                accion = rand.nextInt(2);
                this.turno++;
                if(accion == 0){
                    this.fold();
                }else{
                    this.call();
                }
                if(this.turno > 4){
                    return true;
                }
                return false;
            }
        }
        return false;
    }
    
    public boolean accionOptima() throws IOException{
        Random rand = new Random();
        int accion;
        if(dealer){
            if(this.cartaServidor == 'K'){
                if(this.accionTurno.equals("P")){
                    this.bet();
                }else if(this.accionTurno.equals("A")){
                    this.call();
                }
                
            }
            
            else if(this.cartaServidor == 'J'){
                if(this.accionTurno.equals("P")){
                    accion = rand.nextInt(15)+1;
                    if(accion <= 10){
                        this.check();
                    }
                    else{
                        this.bet();
                    }
                }else if(this.accionTurno.equals("A")){
                    this.call();
                }
            }
            
            else if(this.cartaServidor == 'Q'){
                if(this.accionTurno.equals("P")){
                    accion = rand.nextInt(15)+1;
                    if(accion <= 10){
                        this.check();
                    }
                    else{
                        this.bet();
                    }
                }else if(this.accionTurno.equals("A")){
                    this.fold();
                }
            }
        }
        
        else{
            if(this.cartaServidor == 'K'){
                this.bet();
            }
            
            else if(this.cartaServidor == 'Q'){
                if(this.turno == 1){
                    this.check();
                }
                if(this.accionTurno.equals("A")){
                    accion = rand.nextInt(15)+1;
                    if(accion <= 10){
                        this.call();
                    }
                    else{
                        this.fold();
                    }
                }
            }
            
            else if(this.cartaServidor == 'J'){
                if(this.turno == 1){
                    accion = rand.nextInt(15)+1;
                    if(accion <= 10){
                        this.check();
                    }
                    else{
                        this.bet();
                    }
                }
                if(this.accionTurno.equals("A")){
                    this.fold();
                }
            }
        }
        if(this.turno > 4){
            return true;
        }
        return false;
    }
    
    public int valorCarta(char carta){
        if(carta == 'J'){
            return 1;
        }
        else if(carta == 'Q'){
            return 2;
        }
        else{
            return 3;
        }
    }
    
    
    
}
