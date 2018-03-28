/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kuhnclient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author levanna
 */
public class Protocol {
    private InetAddress address;
    private int modo;
    private int port;
    private Socket socket;
    private ComUtils utils;
    private int estado;
    private boolean dealer = false;
    private static final int PETICION = 1;
    private static final int INICIAR = 2;
    private static final int FIN = 3;
    private static final int CON_CARTA = 4;
    private static final int STKS_LEIDO = 5;
    private int turno = 1;
    private String accionTurno = null;
    private char carta;
    private int fichas;
    
    /**
     *
     * @param String nomMaquina
     * @param int port
     * @throws UnknownHostException
     * @throws IOException
     */
    public Protocol(Socket socket) throws UnknownHostException, IOException{
        //this.address = InetAddress.getByName(nomMaquina);
        //this.port = port;
        
        //socket = new Socket(this.address, this.port);
        this.socket = socket;
        this.utils = new ComUtils(this.socket);
    }
    
    /**
     * Envia el comando STRT ID
     * @param int id
     * @throws IOException
     */
    public void start(int id) throws IOException{
        //Enviamos el comando START
        this.utils.write_command("STRT");
        this.utils.write_space();
        this.utils.write_int32(id);
        
        //Cambiamos de estado
        estado = PETICION;
        
        //Entramos en el bucle de lectura del socket
        read();
    }
    
    /**
     * Envia el comando ANOK
     * @throws IOException
     */
    public void ante() throws IOException{
        //Enviamos el comanod ANTE_OK
        if(this.fichas > 0){
            this.utils.write_command("ANOK");
            //Cambiamos el estado
            estado = INICIAR;

            //Entramos en el bucle de lectura del socket
            read();
        }else{
            System.out.println("No te quedan fichas");
            quit();
        }
    }
    
    /**
     * Envia el comando QUIT
     * @throws IOException
     */
    public void quit() throws IOException{
        //Enviamos el comando QUIT
        this.utils.write_command("QUIT");
        
        //Modificamos el valor de turno para dar por finalizada la partida
        this.turno = 5;
        
    }
    

    /**
     * Envia el comando BET_
     * @throws IOException
     */
    public void bet() throws IOException{
        //Enviamos el comando BET
        this.utils.write_command("BET_");
        
        //Guardamos la ultima accion (Apostar)
        this.accionTurno = "A";
        
        //Incrementamos el turno
        this.turno++;
        
        //Entramos en el bucle de lectura del socket
        this.readJuego();
    }
    

    /**
     * Envia el comando CHCK
     * @throws IOException
     */
    public void check() throws IOException{
        //Enviamos el comando CHECK
        this.utils.write_command("CHCK");
        
        //Guardamos la ultima accion (Pasar)
        this.accionTurno = "P";
        
        //Incrementamos el turno
        this.turno++;
        
        //Entramos en el bucle de lectura del socket
        this.readJuego();
    }
    

    /**
     * Envia el comando CALL
     * @throws IOException
     */
    public void call() throws IOException{
        //Enviamos el comando CALL
        this.utils.write_command("CALL");
        
        //Guardamos la ultima accion (Ir)
        this.accionTurno = "I";
        
        //Incrementamos el turno
        this.turno++;
        
        //Entramos en el bucle de lectura del socket
        this.readJuego();
    }
    

    /**
     * Envia el comando FOLD
     * @throws IOException
     */
    public void fold() throws IOException{
        //Enviamos el comando FOLD
        this.utils.write_command("FOLD");
        
        //Guardamos la ultima accion (Retirarse)
        this.accionTurno = "R";
        
        //Informamos que el cliente se ha retirado
        System.out.println("Te has retirado, el servidor gana");
        
        //Modificamos el turno para dar la partida por finalizada
        this.turno = 5;
        
        //Entramos al bucle de lectura del socket para recibir los comandos del servidor
        readJuego();
    }
    
    
    /**
     * Lectura del socket
     * @throws IOException
     */
    public void read() throws IOException{
        boolean salir = false;
        String cmd;
        int arg1;
        int arg2;
        char arg_dealer;
        
        do{
            //Leemos el comando del socket
            cmd = this.utils.read_command();
            System.out.println(cmd);
            //Si el comando es STKS
            if(estado == PETICION && cmd.equals("STKS")){
                //Quitamos el espacio
                this.utils.read_space();
                
                //Leemos el primer argumento
                arg1 = this.utils.read_int32();
                this.fichas = arg1;
                //Informamos de las fichas que tiene el cliente
                System.out.println("Tienes "+arg1+" fichas");
                
                //Quitamos el espacio
                this.utils.read_space();
                
                //Leemos el segundo argumento
                this.utils.read_int32();
                
                //Podemos salir del bucle de lectura
                salir = true;
            }
            
            //Si el comando es DEAL
            else if(estado == INICIAR && cmd.equals("DEAL")){
                //Quitamos el espacio
                this.utils.read_space();
                
                //Leemos el primer argumento
                arg_dealer = this.utils.readChar();
                
                //Si recibimos un 1 somos el dealer
                if(arg_dealer == '1'){
                    System.out.println("Eres el dealer");
                    dealer = true;
                }
                //Sino lo es el servidor
                else{
                    System.out.println("El servidor es el dealer");
                    dealer = false;
                }
            }
            
            //Si el comando es CARD
            else if (estado == INICIAR && cmd.equals("CARD")){
                //Quitamos el espacio
                this.utils.read_space();
                
                //Leemos el argumento i guardamos que carta tenemos
                carta = this.utils.readChar();
                System.out.println("Tu carta es: "+carta);
                salir = true;
                estado = CON_CARTA;
                if(!dealer && this.modo == 1){
                    accionAleatoria();
                }else if(!dealer && this.modo == 2){
                    accionOptima();
                }
                
            }
        }while(!salir && this.turno < 4);
        
        if(estado == CON_CARTA && (this.modo == 1 || this.modo == 2)){
            readJuego();
        }
        //Si somos el dealer, empezamos segundos, entonces esperamos a leer que hara el servidor
        if(dealer && this.turno < 4){
            readJuego();
        }
    }
    
    /**
     * Lectura del socket
     * @throws IOException
     */
    public void readJuego() throws IOException{
        boolean salir = false;
        
        do{
            //Leemos el comando del socket
            String cmd = this.utils.read_command();
            
             if(cmd.equals("SHOW")){
                utils.read_space();
                char cartaServidor = utils.readChar();
                
                System.out.println("El servidor tiene la carta: "+cartaServidor);
                
                if(valorCarta(this.carta) > valorCarta(cartaServidor)){
                    System.out.println("Has ganado!");
                }
                else{
                    System.out.println("Gana el servidor");
                }
                estado = FIN;

            }
            
            else if(cmd.equals("STKS")){
                this.utils.read_space();
                int arg1 = this.utils.read_int32();
                
                System.out.println("Tienes "+arg1+" fichas");
                
                this.utils.read_space();
                this.utils.read_int32();
                
                salir = true;
                estado = STKS_LEIDO;
            }
            
            //Si somos el dealer
            else if(dealer){
                if(cmd.equals("CHCK")){
                    this.accionTurno = "P";
                    System.out.println("El servidor ha pasado");
                    if(this.modo == 1){
                        salir = accionAleatoria();
                    }else if(modo == 2){
                        salir = accionOptima();
                    }
                    else if(this.turno != 2 && modo == 0){
                        salir = true;
                    }
                    //No salimos porque tendra que leer el showdown/stakes
                    if(this.turno != 2){
                        salir = true;
                    }
                }
                else if(cmd.equals("BET_")){
                    this.accionTurno = "A";
                    System.out.println("El servidor ha apostado");
                    if(this.modo == 1){
                        salir = accionAleatoria();
                    }else if(modo == 2){
                        salir = accionOptima();
                    }
                    else if(modo == 0){
                        salir = true;
                    }
                }
                else if(cmd.equals("CALL")){
                    this.accionTurno = "C";
                    System.out.println("El servidor ha ido");
                    
                    //Si estamos en el tercer turno, tendremos que leer el showdown/stakes
                    if(this.turno < 3){
                        salir = true;
                    }
                }
                else if(cmd.equals("FOLD")){
                    this.accionTurno = "F";
                    System.out.println("El servidor se ha retirado, el cliente gana");
                    this.turno = 5;
                }
            }
            else if (!dealer){
                if(cmd.equals("CHCK")){
                    this.accionTurno = "P";
                    System.out.println("El servidor ha pasado");
                    if(this.turno != 2){
                        salir = true;
                    }
                }else if(cmd.equals("BET_")){
                    this.accionTurno = "A";
                    System.out.println("El servidor ha apostado");
                    if(this.modo == 1){
                        salir = accionAleatoria();
                    }
                    else if(modo == 2){
                        salir = accionOptima();
                    }
                    else if(modo == 0){
                        salir = true;
                    }
                }else if(cmd.equals("FOLD")){
                    this.accionTurno = "F";
                    System.out.println("El servidor se ha retirado, has ganado!");
                    this.turno = 5;
                }else if(cmd.equals("CALL")){
                    this.accionTurno = "C";
                    System.out.println("El servidor ha ido");
                    if(this.turno != 2){
                        salir = true;
                    }
                }
            }
            
            if(estado == STKS_LEIDO){
                salir= true;
            }
        }while(!salir);
        this.turno++;
    }
    
    /**
     * 
     * @return
     */
    public boolean isDealer(){
        return dealer;
    }
    
    /**
     *
     * @return
     */
    public int getTurno(){
        return turno;
    }
    
    /**
     *
     * @return
     */
    public String getAccionTurno(){
        return accionTurno;
    }
    
    /**
     *
     */
    public void resetTurno(){
        this.turno = 1;
       
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
    
    public boolean accionOptima() throws IOException{
        Random rand = new Random();
        int accion;
        if(dealer){
            if(this.carta == 'K'){
                this.turno++;
                if(this.accionTurno.equals("P")){
                    this.bet();
                }else if(this.accionTurno.equals("A")){
                    this.call();
                }
                
            }
            
            else if(this.carta == 'J'){
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
            
            else if(this.carta == 'Q'){
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
            if(this.carta == 'K'){
                this.turno++;
                this.bet();
            }
            
            else if(this.carta == 'Q'){
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
            
            else if(this.carta == 'J'){
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
    
    public void setModo(int modo){
        this.modo = modo;
    }
}
