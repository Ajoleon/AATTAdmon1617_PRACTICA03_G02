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
 * Este programa debe ller el nombre y NIF de un usuario del DNIe, formar el identificador de usuario y autenticarse con un servidor remoto a través de HTTP 
 * @author Juan Carlos Cuevas Martínez
 */
public class Main {
    public final static String  direccion = "10.82.202.204:8081";
    public final static String[] resultados = {"OK","Error"};
    public final static String[] mensajes = {"Autenticación Correcta.","Error en la autenticación, usuario inválido.",
                                            "Error de conexión.", "Error en la url."};
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception{
        ByteArrayInputStream bais=null;
        
        //TAREA 2. Conseguir que el método LeerNIF de ObtenerDatos devuelva el 
        //         correctamente los datos de usuario 
        ObtenerDatos od = new ObtenerDatos();
        Usuario user = od.LeerNIF();
        if(user!=null)
            System.out.println("usuario: "+user.toString());
            String usuario = user.generaruser();
            System.out.println(usuario);
            String resultado = peticion(direccion,usuario,user.getNif());
            if(resultado.equals(resultados[0])){
                System.out.println(mensajes[0]);
            }else if(resultado.equals(resultados[1])){
                System.out.println(mensajes[1]);
            }else{
                System.out.println(resultado);
            }
        //TAREA 3. AUTENTICAR EL CLIENTE CON EL SERVIDOR
        
    }
    public static String peticion(String ip,String user, String clave) throws MalformedURLException, ProtocolException, IOException{
        String inputline= "";
        String [] salida = null;
        String urlParameters  = "usuario="+user+"&clave="+clave;
        byte[] datos = urlParameters.getBytes( StandardCharsets.UTF_8 );
        int longitud = datos.length;
        String direccion = "http://"+ip+"/servidor/login";
        try{
            URL url = new URL(direccion);
            try{
                HttpURLConnection conn= (HttpURLConnection) url.openConnection();           
                conn.setDoOutput(true);
                conn.setConnectTimeout(2000);
                conn.setInstanceFollowRedirects( false );
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
                conn.setRequestProperty("charset", "utf-8");
                conn.setRequestProperty("Content-Length", Integer.toString(longitud));
                conn.setUseCaches(false);
                try( DataOutputStream wr = new DataOutputStream( conn.getOutputStream())) {
                    wr.write(datos);
                }
                Reader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                BufferedReader in = new BufferedReader(br);
                while ((inputline = in.readLine()) != null) {
                        if(inputline.startsWith("Resultado=")){
                            salida = inputline.split("=");
                        }
                    }
                return salida[1];
            }catch(IOException e){
            return mensajes[2];
        }
        }catch(MalformedURLException u){
            return mensajes[3];
        }

    }     
    
}
