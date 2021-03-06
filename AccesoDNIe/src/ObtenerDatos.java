import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.*;

/**
 * La clase ObtenerDatos implementa cuatro métodos públicos que permiten obtener
 * determinados datos de los certificados de tarjetas DNIe, Izenpe y Ona.
 * 
 * @author Juan Carlos Cuevas Martínez, Pablo Castillo Segura y Antonio José 
 * León Sánchez
 */
public class ObtenerDatos {

    private static final byte[] dnie_v_1_0_Atr = {
        (byte) 0x3B, (byte) 0x7F, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x6A, (byte) 0x44,
        (byte) 0x4E, (byte) 0x49, (byte) 0x65, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x90, (byte) 0x00};
    private static final byte[] dnie_v_1_0_Mask = {
        (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
        (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0xFF};

    public ObtenerDatos() {
    }

    /**
     * Método para leer DNIe
     * 
     * @return Usuario leído del DNIe
     */
    public Usuario LeerNIF() {

        Usuario user = null;
        byte[] datos=null;
        
        try {
            Card c = ConexionTarjeta();
            //Si existe la tarjeta
            if (c == null) {
                throw new Exception("ACCESO DNIe: No se ha encontrado ninguna tarjeta");
            }
            byte[] atr = c.getATR().getBytes();
            CardChannel ch = c.getBasicChannel();

            if (esDNIe(atr)) {
                datos = leerCertificado(ch);
                //Si existen datos del certificado
                if(datos!=null)
                    //Método que devuelve el objeto usuario relleno con los datos leídos
                    user = leerDatosUsuario(datos);
            }
            c.disconnect(false);

        } catch (Exception ex) {
            Logger.getLogger(ObtenerDatos.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return user;
    }

    /**
     * Método para leer el certificado del Dnie
     * 
     * @param ch Indica el canal de envío de información hacia la tarjeta
     * @return bytes de datos a transmitir
     * @throws CardException Excepción al transmitir por el canal los comandos 
     * hacia el DNIe
     * @throws CertificateException Excepción que impide el uso del certificado 
     * del DNIe
     */
    public byte[] leerCertificado(CardChannel ch) throws CardException, CertificateException {


        int offset = 0;
        String completName = null;
        
        //[1] PRÁCTICA 3. Punto 1.a
        //Comando SELECT, selecciona fichero dedicado. Longitud datos: 0b(. Datos: 11 bytes.
        byte[] command = new byte[]{(byte) 0x00, (byte) 0xa4, (byte) 0x04, (byte) 0x00, (byte) 0x0b, (byte) 0x4D, (byte) 0x61, (byte) 0x73, (byte) 0x74, (byte) 0x65, (byte) 0x72, (byte) 0x2E, (byte) 0x46, (byte) 0x69, (byte) 0x6C, (byte) 0x65};
        ResponseAPDU r = ch.transmit(new CommandAPDU(command));
        if ((byte) r.getSW() != (byte) 0x9000) {
            System.out.println("ACCESO DNIe: SW incorrecto");
            return null;
        }

        //[2] PRÁCTICA 3. Punto 1.a
        // Comando SELECT, selecciona fichero por  id, en concreto el 5015. Longitud 2 bytes. 
        command = new byte[]{(byte) 0x00, (byte) 0xA4, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x50, (byte) 0x15};
        r = ch.transmit(new CommandAPDU(command));

        if ((byte) r.getSW() != (byte) 0x9000) {
            System.out.println("ACCESO DNIe: SW incorrecto");
            return null;
        }

        //[3] PRÁCTICA 3. Punto 1.a
        // Igual que el anterior, seleccionando archivo 6004. 
        command = new byte[]{(byte) 0x00, (byte) 0xA4, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x60, (byte) 0x04};
        r = ch.transmit(new CommandAPDU(command));

        byte[] responseData = null;
        if ((byte) r.getSW() != (byte) 0x9000) {
            System.out.println("ACCESO DNIe: SW incorrecto");
            return null;
        } else {
            responseData = r.getData();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] r2 = null;
        int bloque = 0;

        do {
             //[4] PRÁCTICA 3. Punto 1.b
            final byte CLA = (byte) 0x00;//Indica la clase. Este valor hace que no afecte a las APDUS cifradas.
            final byte INS = (byte) 0xB0;// Establece el fichero para autenticacion.
            final byte LE = (byte) 0xFF;// Numero de bytes a leer

            //[4] PRÁCTICA 3. Punto 1.b
            //Comando READ BINARY.
            command = new byte[]{CLA, INS, (byte) bloque/*P1*/, (byte) 0x00/*P2*/, LE};//Identificar qué hacen P1 y P2
            r = ch.transmit(new CommandAPDU(command));
            //P1 y P2 indican el offset del primer byte a leer desde el principio del fichero
            //System.out.println("ACCESO DNIe: Response SW1=" + String.format("%X", r.getSW1()) + " SW2=" + String.format("%X", r.getSW2()));

            if ((byte) r.getSW() == (byte) 0x9000) {
                r2 = r.getData();

                baos.write(r2, 0, r2.length);

                for (int i = 0; i < r2.length; i++) {
                    byte[] t = new byte[1];
                    t[0] = r2[i];
                    System.out.println(i + (0xff * bloque) + String.format(" %2X", r2[i]) + " " + String.format(" %d", r2[i])+" "+new String(t));
                }
                bloque++;
            } else {
                return null;
            }

        } while (r2.length >= 0xfe);


         ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

      

        
        return baos.toByteArray();
    }

    
    
    
    /**
     * Este método establece la conexión con la tarjeta. La función busca el
     * Terminal que contenga una tarjeta, independientemente del tipo de tarjeta
     * que sea.
     *
     * @return objeto Card con conexión establecida
     * @throws Exception
     */
    private Card ConexionTarjeta() throws Exception {

        Card card = null;
        TerminalFactory factory = TerminalFactory.getDefault();
        List<CardTerminal> terminals = factory.terminals().list();
        //System.out.println("Terminals: " + terminals);

        for (int i = 0; i < terminals.size(); i++) {

            // get terminal
            CardTerminal terminal = terminals.get(i);

            try {
                if (terminal.isCardPresent()) {
                    card = terminal.connect("*"); //T=0, T=1 or T=CL(not needed)
                }
            } catch (Exception e) {

                System.out.println("Exception catched: " + e.getMessage());
                card = null;
            }
        }
        return card;
    }

    /**
     * Este método nos permite saber el tipo de tarjeta que estamos leyendo del
     * Terminal, a partir del ATR de ésta.
     *
     * @param atrCard ATR de la tarjeta que estamos leyendo
     * @return tipo de la tarjeta. 1 si es DNIe, 2 si es Starcos y 0 para los
     * demás tipos
     */
    private boolean esDNIe(byte[] atrCard) {
        int j = 0;
        boolean found = false;

        //Es una tarjeta DNIe?
        if (atrCard.length == dnie_v_1_0_Atr.length) {
            found = true;
            while (j < dnie_v_1_0_Atr.length && found) {
                if ((atrCard[j] & dnie_v_1_0_Mask[j]) != (dnie_v_1_0_Atr[j] & dnie_v_1_0_Mask[j])) {
                    found = false; //No es una tarjeta DNIe
                }
                j++;
            }
        }

        if (found == true) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * Analizar los datos leídos del DNIe para obtener
     *   - nombre
     *   - apellidos
     *   - NIF
     * @param datos
     * @return Objeto de la clase Usuario
     */
    private Usuario leerDatosUsuario(byte[] datos) {
        int  i = 0;
        String dni = "";
        String nombre = "";
        String apellidos ="";
        String[] separados = {"","",};
        byte[] t = new byte[3];
        boolean flag = false;
        
        // DNI: empieza tras un byte de valor  de OID  = 85 4 5.
        //nombre: OID 85 4 42
        //apellidos y nombre : 85 4 3 
        do {
            t[0] = datos[i];
            t[1] = datos[i+1];
            t[2] = datos[i+2];
            
            //Voy avanzando en los datos hasta encontrar OID = 85 4 5 (DNI)
            if(t[0]==85&&t[1]==4&&t[2]==5){

                for(int j=1 ; j<=9;j++){
                    byte[] s = new byte[1];
                    s[0] = datos[i+j+4];
                    //Voy concatenando las cifras del DNI
                    dni = dni + new String(s);
                }
                flag = true;
                
            }else{
                i++;
            }
                      
        }while(flag==false);
        flag = false;
        
        do {
            t[0] = datos[i];
            t[1] = datos[i+1];
            t[2] = datos[i+2];
            
            //Voy avanzando en los datos hasta encontrar OID = 85 4 42 (nombre)
            if(t[0]==85&&t[1]==4&&t[2]==42){
                int j= 1;
                byte[] s = new byte[1];
                do{
                    s[0] = datos[i+j+4];
                    if(s[0]!=49){
                        //Concateno en la variable nombre
                        nombre = nombre + new String(s);
                    }
                    i++;
                }while(s[0]!=49);
                flag = true;
            }else{
                i++;
            }
                      
        }while(flag==false);
        flag = false;
        
        do {
                t[0] = datos[i];
                t[1] = datos[i+1];
                t[2] = datos[i+2];
                
                //Voy avanzando en los datos hasta encontrar OID = 85 4 3 (apellidos)
                if(t[0]==85&&t[1]==4&&t[2]==3){
                        int j= 1;
                        byte[] s = new byte[1];
                        do{
                            s[0] = datos[i+j+4];
                            if(s[0]!=44){
                                //Concateno en la variable apellidos
                                apellidos = apellidos + new String(s);
                            }
                            i++;
                        }while(s[0]!=44);
                        flag = true;
                    }else{
                         i++;
                     }
                      
        }while(flag==false);
        
        
        System.out.println(dni);
        System.out.println(nombre);
        System.out.println(apellidos);
        
        //Separo los apellidos para introducirlos por separado en el objeto Usuario
        separados = apellidos.split(" ");
        System.out.println(separados[0]);
        System.out.println(separados[1]);
        Usuario user = new Usuario(nombre,separados[0],separados[1],dni);
       return user;
    }
    /**
     * Método para leer datos de ASN1
     * 
     * @param datos bytes de datos
     * @return Usuario del 
     */
    private Usuario leerDatosUsuarioASN1(byte[] datos){
        return null;
    }
   
}
