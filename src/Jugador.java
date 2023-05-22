public class Jugador {
    private static int Ranking;
    private static String Nombre;
    private static int Fide;
    private static int Id_Fide;
    private static String Origen;
    private static String Alojado;
    private static boolean Participa;

    public Jugador(int Ranking, String Nombre, int Fide, int Id_Fide, String Origen, String Alojado, boolean Participa){
        this.Ranking = Ranking;
        this.Nombre = Nombre;
        this.Fide = Fide;
        this.Id_Fide = Id_Fide;
        this.Origen = Origen;
        this.Alojado = Alojado;
        this.Participa = Participa;
    }

    public static int getRanking() {
        return Ranking;
    }

    public static String getNombre() {
        return Nombre;
    }

    public static int getFide() {
        return Fide;
    }

    public static int getId_Fide() {
        return Id_Fide;
    }

    public static String getOrigen() {
        return Origen;
    }

    public static String getAlojado() {
        return Alojado;
    }

    public static boolean isParticipa() {
        return Participa;
    }
}
