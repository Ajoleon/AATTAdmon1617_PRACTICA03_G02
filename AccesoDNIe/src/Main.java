import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;


/**
 * Aplicaciones Telemáticas para la Administración
 * 
 * Este programa debe leer el nombre y NIF de un usuario del DNIe, formar el identificador de usuario y autenticarse con un servidor remoto a través de HTTP 
 * @author Juan Carlos Cuevas Martínez
 */
public class Main {
    
    public final static String[] resultados = {"OK","Error"};
    public final static String[] mensajes = {"Autenticación Correcta.","Error en la autenticación, usuario inválido.",
                                            "Error de conexión.", "Error en la url."};
    
    /**
     * Método principal, que recoge los métodos de leer DNI, realizar petición al servidor, y mostrar resultado
     * 
     * @param direccion Dirección IP y puerto del servidor donde autenticarse
     * @return Cadena en función del resultado del servidor
     * @throws Exception Recoge la excepción de la conexión al servidor
     */
    public static String main(String direccion) throws Exception{
        ByteArrayInputStream bais=null;
        
        //TAREA 2. Conseguir que el método LeerNIF de ObtenerDatos devuelva correctamente los datos de usuario 
        
        //Instancia de la clase ObtenerDatos
        ObtenerDatos od = new ObtenerDatos();
        //Método para leer datos del DNIe
        Usuario user = od.LeerNIF();
        
        //Si el usuario existe
        if(user!=null)
            //Imprimo por pantalla el usuario
            System.out.println("usuario: "+user.toString());
        
        
        //Cadena con el usuario para autenticarse en el servidor
        String usuario = user.generaruser();
        System.out.println(usuario);
        
        //Petición, con usuario y DNI, al servidor con la dirección especificada
        String resultado = peticion(direccion,usuario,user.getNif());

        //Si el resultado del servidor es OK
        if(resultado.equals(resultados[0])){
            System.out.println(mensajes[0]);
            return mensajes[0];
            
        //Si es Error
        }else if(resultado.equals(resultados[1])){
            System.out.println(mensajes[1]);
            return mensajes[1];
            
        //Si devuelve otra cosa
        }else{
            System.out.println(resultado);
            return resultado;
        }
        //TAREA 3. AUTENTICAR EL CLIENTE CON EL SERVIDOR
        
    }
    
    /**
     * Método para realizar la petición al servidor donde autenticarse
     * 
     * @param ip IP:puerto del servidor
     * @param user Nombre de usuario con la cadena en el formato definido para los usuarios
     * @param clave DNI
     * 
     * @return Mensaje en función de si hay fallos o no en la conexión bien
     * 
     * @throws MalformedURLException Recoge la excepción de montar mal la URL
     * @throws ProtocolException Recoge la excepción de algún error en el protocolo de comunicación
     * @throws IOException Recoge excepción de fallos al escribir o leer de la conexión
     */
    public static String peticion(String ip,String user, String clave) throws MalformedURLException, ProtocolException, IOException{
        String inputline= "";
        String [] salida = null;
        String urlParameters  = "usuario="+user+"&clave="+clave;
        
        byte[] datos = urlParameters.getBytes( StandardCharsets.UTF_8 );
        int longitud = datos.length;
        
        //Cadena con la URL
        String direccion = "http://"+ip+"/servidor/login";
        
        try{
            //Monto la URL
            URL url = new URL(direccion);
            
            try{
                //Establezco conexión y parámetros
                HttpURLConnection conn= (HttpURLConnection) url.openConnection();         
                
                conn.setDoOutput(true);
                conn.setConnectTimeout(2000);//Tiempo de intento de conexión al servidor
                conn.setInstanceFollowRedirects( false );
                conn.setRequestMethod("POST");//Método POST
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
                conn.setRequestProperty("charset", "utf-8");
                conn.setRequestProperty("Content-Length", Integer.toString(longitud));
                conn.setUseCaches(false);
                
                //Escribe en la conexión
                try( DataOutputStream wr = new DataOutputStream( conn.getOutputStream())) {
                    wr.write(datos);
                }
                
                //Métodos de lectura
                Reader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                BufferedReader in = new BufferedReader(br);
                
                //Mientras lea líneas
                while ((inputline = in.readLine()) != null) {
                    //Si la línea empieza por Resultado=
                    if(inputline.startsWith("Resultado=")){
                        //Separo las palabras por el =
                        salida = inputline.split("=");
                    }
                }
                //Devuelvo la cadena que había detrás del =
                return salida[1];
            
            //Si no puedo leer o escribir
            }catch(IOException e){
                //Error de conexión
                return mensajes[2];
            }
        
        //Si la URL no está bien montada
        }catch(MalformedURLException u){
            //Error de URL
            return mensajes[3];
        }

    }     
    
}
