package kuhnserver;
import java.net.*;
import java.io.*;
import java.util.Locale;

public class ComUtils
{
  /* Mida d'una cadena de caracters */
  private final int STRSIZE = 40;
  /* Objectes per escriure i llegir dades */
  private DataInputStream dis;
  private DataOutputStream dos;

  public ComUtils(Socket socket) throws IOException
  {
    dis = new DataInputStream(socket.getInputStream());
    dos = new DataOutputStream(socket.getOutputStream());
  }
  
  public ComUtils(File file) throws IOException{
    dis = new DataInputStream(new FileInputStream(file));
    dos = new DataOutputStream(new FileOutputStream(file));
  }

    /* Llegir un enter de 32 bits */
  public int read_int32() throws IOException
  {
    byte bytes[] = new byte[4];
    bytes  = read_bytes(4);

    return bytesToInt32(bytes,"be");
  }

  /* Escriure un enter de 32 bits */
  public void write_int32(int number) throws IOException
  {
    byte bytes[]=new byte[4];

    int32ToBytes(number,bytes,"be");
    dos.write(bytes, 0, 4);
  }

  /* Llegir un string de mida STRSIZE */
  public String read_string() throws IOException
  {
    String str;
    byte bStr[] = new byte[STRSIZE];
    char cStr[] = new char[STRSIZE];

    bStr = read_bytes(STRSIZE);

    for(int i = 0; i < STRSIZE;i++)
      cStr[i]= (char) bStr[i];

    str = String.valueOf(cStr);

    return str.trim(); 
  }
  
  public String read_space() throws IOException
  {
    String str;
    byte bStr[] = new byte[1];
    char cStr[] = new char[1];

    bStr = read_bytes(1);

    for(int i = 0; i < 1;i++)
      cStr[i]= (char) bStr[i];

    str = String.valueOf(cStr);

    return str.trim(); 
  }
  
  public String read_command() throws IOException
  {
    String str;
    byte bStr[] = new byte[4];
    char cStr[] = new char[4];

    bStr = read_bytes(4);

    for(int i = 0; i < 4;i++)
      cStr[i]= (char) bStr[i];

    str = String.valueOf(cStr);

    return str.trim(); 
  }

  /* Escriure un string */
  public void write_string(String str) throws IOException
  { 
    int numBytes, lenStr; 
    byte bStr[] = new byte[STRSIZE];

    lenStr = str.length();

    if (lenStr > STRSIZE)
      numBytes = STRSIZE;
    else
      numBytes = lenStr;

    for(int i = 0; i < numBytes; i++)
      bStr[i] = (byte) str.charAt(i);

    for(int i = numBytes; i < STRSIZE; i++)
      bStr[i] = (byte) ' ';

    dos.write(bStr, 0,STRSIZE);
  }

  /* Passar d'enters a bytes */
  private int int32ToBytes(int number,byte bytes[], String endianess)
  {
    if("be".equals(endianess.toLowerCase()))
    {
      bytes[0] = (byte)((number >> 24) & 0xFF);
      bytes[1] = (byte)((number >> 16) & 0xFF);
      bytes[2] = (byte)((number >> 8) & 0xFF);
      bytes[3] = (byte)(number & 0xFF);
    }
    else
    {
      bytes[0] = (byte)(number & 0xFF);
      bytes[1] = (byte)((number >> 8) & 0xFF);
      bytes[2] = (byte)((number >> 16) & 0xFF);
      bytes[3] = (byte)((number >> 24) & 0xFF);
    }
    return 4;
  }

  /* Passar de bytes a enters */
  private int bytesToInt32(byte bytes[], String endianess)
  {
    int number;

    if("be".equals(endianess.toLowerCase()))
    {
      number=((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16) |
        ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
    }
    else
    {
      number=(bytes[0] & 0xFF) | ((bytes[1] & 0xFF) << 8) |
        ((bytes[2] & 0xFF) << 16) | ((bytes[3] & 0xFF) << 24);
    }
    return number;
  }
	//llegir bytes.
  private byte[] read_bytes(int numBytes) throws IOException{
    int len=0 ;
    byte bStr[] = new byte[numBytes];
    int bytesread=0; 
    do {
      bytesread= dis.read(bStr, len, numBytes-len);
      if (bytesread == -1)
        throw new IOException("Broken Pipe");
      len += bytesread;
     } while (len < numBytes);
    return bStr;
  }
	
    /* Llegir un string  mida variable size = nombre de bytes especifica la longitud*/
    public  String read_string_variable(int size) throws IOException
    {
        byte bHeader[]=new byte[size];
        char cHeader[]=new char[size];
        int numBytes=0;

        // Llegim els bytes que indiquen la mida de l'string
        bHeader = read_bytes(size);
        // La mida de l'string ve en format text, per tant creem un string i el parsejem
        for(int i=0;i<size;i++){
                cHeader[i]=(char)bHeader[i]; }
        numBytes=Integer.parseInt(new String(cHeader));

        // Llegim l'string
        byte bStr[]=new byte[numBytes];
        char cStr[]=new char[numBytes];
        bStr = read_bytes(numBytes);
        for(int i=0;i<numBytes;i++)
                cStr[i]=(char)bStr[i];
        return String.valueOf(cStr);
    }

    /* Escriure un string mida variable, size = nombre de bytes especifica la longitud  */
    /* String str = string a escriure.*/
    public  void write_string_variable(int size,String str) throws IOException
    {

        // Creem una seqüència amb la mida
        byte bHeader[]=new byte[size];
        String strHeader;
        int numBytes=0; 

        // Creem la capçalera amb el nombre de bytes que codifiquen la mida
        numBytes=str.length();

        strHeader=String.valueOf(numBytes);
        int len;
        if ((len=strHeader.length()) < size){
            for (int i =len; i< size;i++){
                strHeader= "0"+strHeader;
            }
            for(int i=0;i<size;i++){
                bHeader[i]=(byte)strHeader.charAt(i);
            }
            // Enviem la capçalera
            dos.write(bHeader, 0, size);
            // Enviem l'string writeBytes de DataOutputStrem no envia el byte més alt dels chars.
            dos.writeBytes(str);
        }
    }
        
    public void writeChar(char c) throws IOException{
        int numBytes = 2; 
        //Creen un array de 2 bytes per guardar el caracter
        byte bStr[] = new byte[numBytes];
        
        //Guardem els bytes del caracter
        bStr[0] = (byte) c;
        
        //Escribim nomes el primer byte
        dos.write(bStr, 0, 1);
    }
        
    public char readChar() throws IOException{
        byte bStr[];
        char c;
        
        //Llegim un byte ja que els caracters que guardem son d'un byte
        bStr = read_bytes(1);
        
        //Fem un cast a char i retornem
        c = (char) bStr[0];
        
        return c;
    }
    public void writeTest() throws IOException{
        write_string("Adeu");
        write_int32(4);
        write_string_variable(2, "Holi");
        writeChar('a');
    }
    
    public String readTest() throws IOException{
        String string = read_string();
        int num = read_int32();
        String stringVariable = read_string_variable(2);
        char caracter = readChar();
        return string+"\n"+num+"\n"+stringVariable+"\n"+caracter;
    }
    
     /* Escriure un comandament */
    public void write_command(String str) throws IOException
    { 
      int numBytes, lenStr; 
      byte bStr[] = new byte[4];

      lenStr = str.length();

      if (lenStr > 4)
        numBytes = 4;
      else
        numBytes = lenStr;

      for(int i = 0; i < numBytes; i++)
        bStr[i] = (byte) str.charAt(i);

      for(int i = numBytes; i < 4; i++)
        bStr[i] = (byte) ' ';

      dos.write(bStr, 0, 4);
    }

    /* Escriure un espai */
    public void write_space() throws IOException
    { 
        String str = " ";
        int numBytes, lenStr; 
        byte bStr[] = new byte[1];

        lenStr = str.length();

        if (lenStr > 1)
          numBytes = 1;
        else
          numBytes = lenStr;

        for(int i = 0; i < numBytes; i++)
          bStr[i] = (byte) str.charAt(i);

        for(int i = numBytes; i < 1; i++)
          bStr[i] = (byte) ' ';

        dos.write(bStr, 0, 1);
  }
}

