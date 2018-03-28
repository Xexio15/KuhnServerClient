/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kuhnserver;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
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
    private int fichasServidor = 15;
    private int fichasCliente = 20;
    private int bote = 0;
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
        Collections.shuffle(cartas);
        estado = INICIAR;
        
        //LOGGER
        fh = new FileHandler("Server"+Thread.currentThread().getName()+".log");  
        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();  
        fh.setFormatter(formatter);  
        logger.setUseParentHandlers(false);

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
        if(this.modo == 3){
            System.out.println("Te has retirado, el cliente gana");
        }
        this.fichasCliente = this.fichasCliente + this.bote;
        this.turno = 5;
        stakes();
        
    }
    
    public void stakes() throws IOException{
       
        this.utils.write_command("STKS");
        this.utils.write_space();
        this.utils.write_int32(this.fichasCliente);
        this.utils.write_space();
        this.utils.write_int32(this.fichasServidor);
        logger.info("STKS"+' '+fichasCliente+' '+fichasServidor);
        if(this.modo == 3){
            System.out.println("Tienes "+fichasServidor+" fichas");
        }
        if(this.turno < 4){
            boolean salir = false;
            String cmd = this.utils.read_command();
            do{    
                if(estado == INICIAR && cmd.equals("ANOK")){
                    if(this.modo == 3){
                        System.out.println("El cliente ha aceptado la apuesta inicial");
                    }
                    this.fichasCliente--;
                    this.fichasServidor--;
                    this.bote++;
                    this.bote++;
                    logger.info(cmd);
                    salir = true;
                }else if(estado == INICIAR && cmd.equals("QUIT")){
                    if(this.modo == 3){
                        System.out.println("El cliente no ha aceptado la apuesta inicial");
                    }
                    logger.info(cmd);
                    salir = true;
                    try{
                        socket.close();
                    }catch(SocketException ex){
                        System.out.println("Socket tancat");
                    }
                }
            }while(!salir && turno < 4 );
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
            if(this.modo == 3){
                System.out.println("Eres el dealer");
            }
        }else{
            dealer = false;
            if(this.modo == 3){
                System.out.println("El cliente es el dealer");
            }
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
        if(this.modo == 3){
            System.out.println("Tu carta es: "+ this.cartaServidor);
        }
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
        if(this.modo == 3){
            System.out.println("Tu carta es: "+this.cartaServidor);
            System.out.println("La carta del cliente es: "+this.cartaCliente);
        }
        logger.info("SHOW"+' '+this.cartaServidor);
        this.turno = 5;
        if(valorCarta(this.cartaCliente) > valorCarta(this.cartaServidor)){
            if(this.modo == 3){
                System.out.println("Gana el cliente");
            }
            this.fichasCliente = this.fichasCliente + this.bote;
            stakes();
        }
        else{
            if(this.modo == 3){
                System.out.println("Gana el servidor");
            }
            this.fichasServidor = this.fichasServidor + this.bote;
            stakes();
        }
        
    }
    
    
    
    public void readJuego() throws IOException{
        boolean salir = false;
        String cmd = "";
        
        while(!salir && this.turno < 4){
            cmd = this.utils.read_command();
            
            if(cmd.equals("STRT")){
                utils.read_space();
                id = utils.read_int32();
                if(this.modo == 3){
                    System.out.println("El jugador "+id+" ha iniciado la partida "+ this.turno);
                }
                salir = true;
                logger.info(cmd+' '+id);
            }
            else if(cmd.equals("QUIT")){
                if(this.modo == 3){
                    System.out.println("El cliente se ha ido");
                }
                logger.info(cmd);
                salir = true;
                this.turno = 5;
                try{
                    socket.close();
                    socket = null;
                }catch(SocketException ex){
                    System.out.println("Socket tancat");
                }
            }
            else if(dealer){
                if(cmd.equals("CHCK")){
                    this.accionTurno = "P";
                    if(this.modo == 3){
                        System.out.println("El cliente ha pasado");
                    }
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
                    if(this.modo == 3){
                        System.out.println("El cliente ha apostado");
                    }
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
                    if(this.modo == 3){
                        System.out.println("El cliente ha ido");
                    }
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
                    if(this.modo == 3){
                        System.out.println("El cliente se ha retirado, el servidor gana");
                    }
                    logger.info(cmd);
                    this.fichasServidor = this.fichasServidor + this.bote;
                    this.turno = 5;
                    stakes();
                    salir = true;
                }
            }else if(!dealer){
                if(cmd.equals("CHCK")){
                    this.accionTurno = "P";
                    logger.info(cmd);
                    if(this.modo == 3){
                        System.out.println("El cliente ha pasado");
                    }
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
                    if(this.modo == 3){
                        System.out.println("El cliente ha apostado");
                    }
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
                    if(this.modo == 3){
                        System.out.println("El cliente ha ido");
                    }
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
                    if(this.modo == 3){
                        System.out.println("El cliente se ha retirado, el servidor gana");
                    }
                    this.fichasServidor = this.fichasServidor + this.bote;
                    this.turno = 5;
                    stakes();
                    salir = true;
                }
            }
            if(this.turno > 4){
                salir = true;
            }
        }
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
        Collections.shuffle(cartas);
        estado = INICIAR;
        accionTurno = null;
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
                this.turno++;
                if(this.accionTurno.equals("P")){
                    this.bet();
                }else if(this.accionTurno.equals("A")){
                    this.call();
                }
                
            }
            else if(this.cartaServidor == 'J'){
                this.turno++;
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
                this.turno++;
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
                this.turno++;
                this.bet();
            }
            
            else if(this.cartaServidor == 'Q'){
                
                if(this.turno == 1){
                    this.check();
                }
                this.turno++;
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
                this.turno++;
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
