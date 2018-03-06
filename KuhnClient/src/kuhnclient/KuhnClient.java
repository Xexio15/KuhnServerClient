/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kuhnclient;

import java.io.*;
import java.rmi.*;

/**
 *
 * @author xexio
 */
public class KuhnClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            int RMIPort;         
            String hostName;
            String portNum;
            InputStreamReader is = new InputStreamReader(System.in);
            BufferedReader br = new BufferedReader(is);
            
            System.out.println("Enter the RMIRegistry host namer:");
            hostName = br.readLine();
            //Demanem el port a connectarse
            System.out.println("Enter the RMIRegistry port number:");
            portNum = br.readLine();
            RMIPort = Integer.parseInt(portNum);
            
            // Look up the remote object and cast its reference 
            // to the remote interface class -- replace "localhost"
            // with the appropriate host name of the remote object.
            String registryURL = "rmi://localhost:" + portNum + "/some";  
            KuhnInterface h = (KuhnInterface)Naming.lookup(registryURL);
            // invoke the remote method(s)
            //String message = h.method1();
            //System.out.println(message);
            // method2 can be invoked similarly
        } 
        catch (Exception ex) {
            ex.printStackTrace( );
        } 
    }
}
