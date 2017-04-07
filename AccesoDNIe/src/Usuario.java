/**
 * Clase para almacenar los datos de un usuario
 * 
 * @author Pablo Castillo Segura y Antonio José León Sánchez
 */
public class Usuario {
    private String nombre;
    private String apellido1;
    private String apellido2;
    private String nif;
    
    /**
     * Constructor de la clase Usuario
     * 
     * @param n Nombre
     * @param a1 Primer Apellido
     * @param a2 Segundo Apellido
     * @param ni DNI
     */
    public Usuario(String n,String a1,String a2,String ni){
        nombre=n;
        apellido1=a1;
        apellido2=a2;
        nif=ni;
    }
    
    /**
     * Método para devolver los datos de un usuario como cadena
     * 
     * @return Cadena con los datos del usuario
     */
    @Override
    public String toString(){
        return nombre+" "+apellido1+" "+apellido2+" "+nif;
    }

    /*
        Método para obtener el nombre de un objeto de la clase Usuario
    */
    public String getNombre() {
        return nombre;
    }
    /**
     * Método para establecer un nombre a un objeto de la clase usuario
     * 
     * @param nombre Nombre que se desea establecer
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /*
        Método para obtener el primer apellido de un objeto de la clase Usuario
    */
    public String getApellido1() {
        return apellido1;
    }
    /**
     * Método para establecer un nombre a un objeto de la clase usuario
     * 
     * @param apellido1 Primer apellido que se desea establecer
     */
    public void setApellido1(String apellido1) {
        this.apellido1 = apellido1;
    }

    /*
        Método para obtener el segundo apellido de un objeto de la clase Usuario
    */
    public String getApellido2() {
        return apellido2;
    }
    /**
     * Método para establecer un nombre a un objeto de la clase usuario
     * 
     * @param apellido2 Segundo apellido que se desea establecer
     */
    public void setApellido2(String apellido2) {
        this.apellido2 = apellido2;
    }

    /*
        Método para obtener el DNI de un objeto de la clase Usuario
    */
    public String getNif() {
        return nif;
    }
    /**
     * Método para establecer un nombre a un objeto de la clase usuario
     * 
     * @param nif DNI que se desea establecer
     */
    public void setNif(String nif) {
        this.nif = nif;
    }
    
    /**
     * Método para devolver la cadena: Primera Letra del Nombre + Primer apellido + Primera letra del segundo apellido,
     * que se utilizará como usuario del servidor (la clave es el DNI)
     * 
     * @return Cadena especificada
     */
    public String generaruser(){
        return this.getNombre().charAt(0)+this.getApellido1()+this.getApellido2().charAt(0);
    }      
}
