import org.mariadb.jdbc.plugin.authentication.standard.ed25519.math.ed25519.Ed25519LittleEndianEncoding;

import java.io.*;
import java.sql.*;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {

    static Connection cnxA;
    static Connection cnxB;
    final static String CSV_JUG_A = "datos/jugadorA.csv";
    final static String CSV_JUG_B = "datos/jugadorB.csv";
    final static String CSV_PRE_A = "datos/premioA.csv";
    final static String CSV_PRE_B = "datos/premioB.csv";
    final static String CSV_CLA_A = "datos/clasificacionA.csv";
    final static String CSV_CLA_B = "datos/clasificacionB.csv";

    static {
        try {
            cnxA = DriverManager.getConnection("jdbc:mariadb://localhost:3306/torneoA", "root", "root");
            cnxB = DriverManager.getConnection("jdbc:mariadb://localhost:3306/torneoB", "root", "root");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int leerInt() {
        Scanner sc = new Scanner(System.in);
        while (true){
            try {
                return sc.nextInt();
            } catch (InputMismatchException e){
                sc.next();
                System.out.println("Error: No válido");
            }
        }
    }

    public static void mostrarMenu1(){
        System.out.println(
                "Elige una opción:" + "\n" +
                "1 - Torneo A" + "\n" +
                "2 - Torneo B" + "\n" +
                "0 - Salir"
        );
    }

    public static void mostrarMenu2(){
        System.out.println(
                "Elige una opción:" + "\n" +
                "1 - Mostrar Datos" + "\n" +
                "2 - Borrar Datos" + "\n" +
                "3 - Regenerar Datos" + "\n" +
                "4 - Mostrar a que premio opta cada jugador" + "\n" +
                "0 - Atrás"
        );
    }

    public static int buscarIdTipoPremio(String aux, Connection cnx){
        try {
            ResultSet rs = cnx.createStatement().executeQuery("select * from tipoPremio");
            rs.first();
            do {
                if (aux.equals(rs.getString(2))){
                    return rs.getInt(1);
                }
            }while (rs.next());
        } catch (SQLException e){
            e.printStackTrace();
        }
        return 0;
    }

    public static String buscarTipoPremio(int n, Connection cnx){
        try {
            ResultSet rs = cnx.createStatement().executeQuery("select * from tipoPremio");
            rs.first();
            do {
                if (n == rs.getInt(1)){
                    return rs.getString(2);
                }
            }while (rs.next());
        } catch (SQLException e){
            e.printStackTrace();
        }
        return "";
    }

    public static void generarDatosPremio(Connection cnx, String csvFile){
        try {
            BufferedReader lineReader = new BufferedReader(new FileReader(csvFile));
            Statement st = cnx.createStatement();
            st.executeUpdate("delete from premio");
            String line;
            int cont = 0;
            while ((line = lineReader.readLine()) != null){
                //System.out.println(line);
                String[] data = line.split("\\|");
                String Id_Premio = data[0];
                String aux = data[1];
                String Cantidad = data[2];
                int Id_Tipo_Premio = buscarIdTipoPremio(aux, cnx);

                int res = st.executeUpdate("insert into premio values(" + Id_Premio + ", " + Id_Tipo_Premio + ", " + Cantidad + ")");
                if (res != 0) {
                    cont++;
                }
            }
            System.out.println("Se ha añadido " + cont + " filas");
        }catch (SQLException | IOException e){
            e.printStackTrace();
        }
    }

    public static void generarDatosClasificacion(Connection cnx, String csvFile){
        try {
            BufferedReader lineReader = new BufferedReader(new FileReader(csvFile));
            Statement st = cnx.createStatement();
            st.executeUpdate("delete from clasificacion");
            String line;
            int cont = 0;
            while ((line = lineReader.readLine()) != null){
                //System.out.println(line);
                String[] data = line.split("\\|");
                String Posicion = data[0];
                String Ranking = data[1];
                int res = st.executeUpdate("insert into clasificacion values(" + Posicion + ", " + Ranking + ", default )");
                if (res != 0) {
                    cont++;
                }
            }
            System.out.println("Se ha añadido " + cont + " filas");
        }catch (SQLException | IOException e){
            e.printStackTrace();
        }
    }

    public static void generarDatosJugador(Connection cnx, String csvFile){
        try {
            BufferedReader lineReader = new BufferedReader(new FileReader(csvFile));
            Statement st = cnx.createStatement();
            st.executeUpdate("delete from jugador");
            String line;
            int cont = 0;
            while ((line = lineReader.readLine()) != null){
                //System.out.println(line);
                String[] data = line.split("\\|");
                String Ranking = data[0];
                String Nombre = data[1];
                String FIDE = data[2];
                String Id_FIDE = data[3];
                String Origen = data[4];
                String Alojado = data[5];
                if (Origen.isEmpty()){
                    Origen = null;
                }
                int res = st.executeUpdate("insert into jugador values(" + Ranking + ", " + Nombre + ", " + FIDE + ", " + Id_FIDE + ", " + Origen + ", " + Alojado + ", default )");
                if (res != 0) {
                    cont++;
                }
            }
            System.out.println("Se ha añadido " + cont + " filas");
        }catch (SQLException | IOException e){
            e.printStackTrace();
        }
    }

    public static void generarDatosOpta(Connection cnx){
        try {
            Statement st = cnx.createStatement();
            st.executeUpdate("delete from opta");
            ResultSet rsJ = st.executeQuery("select * from jugador");
            ResultSet rsTP = st.executeQuery("select * from tipoPremio");
            PreparedStatement ps = cnx.prepareStatement("insert into opta values (?, ?)");
            rsJ.first();
            rsTP.first();
            do {
                ps.setInt(1, Integer.valueOf(rsJ.getString(1)));
                ps.setInt(2, rsTP.getInt(1));
                ps.executeUpdate();
                rsTP.next();
                if (rsJ.getString(5) != null && rsJ.getString(5).equals(rsTP.getString(2))){
                    ps.setInt(1, Integer.valueOf(rsJ.getString(1)));
                    ps.setInt(2, rsTP.getInt(1));
                    ps.executeUpdate();
                }
                while (rsTP.next() && rsTP.getString(2).contains("SUB")){
                    String str = rsTP.getString(2).substring(4);
                    if (Integer.valueOf(rsJ.getString(3)) < Integer.valueOf(str)){
                        //System.out.println(rsJ.getString(1) + " " + rsTP.getString(2));
                        ps.setInt(1, Integer.valueOf(rsJ.getString(1)));
                        ps.setInt(2, rsTP.getInt(1));
                        ps.executeUpdate();
                    }
                }
                if (rsJ.getString(6).equals("Si")){
                    ps.setInt(1, Integer.valueOf(rsJ.getString(1)));
                    ps.setInt(2, rsTP.getInt(1));
                    ps.executeUpdate();
                }
                rsTP.first();
            }while (rsJ.next());
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static ResultSet buscarDatosJugador(Connection cnx) throws SQLException {
        Statement st = cnx.createStatement();
        return st.executeQuery("Select * from jugador");
    }

    public static ResultSet buscarDatosOptan(Connection cnx) throws SQLException {
        Statement st = cnx.createStatement();
        return st.executeQuery("Select jugador.Ranking, Nombre, Id_Tipo_Premio from jugador inner join opta on jugador.Ranking=opta.Id_Ranking");
    }
    public static void mostrarDatos(ResultSet rs) throws SQLException{
        rs.first();
        if (rs.next()) {
            rs.first();
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                System.out.print(rs.getMetaData().getColumnName(i) + "\t");
            }
            System.out.println();
            do {
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    System.out.print(rs.getString(i) + "\t");
                }
                System.out.println();
            } while (rs.next());
        }
    }

    public static void mostrarDatosOptan(ResultSet rs, Connection cnx) throws SQLException{
        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
            System.out.print(rs.getMetaData().getColumnName(i) + "\t");
        }
        rs.first();
        int rank = 0;
        do {
            if (rank != rs.getInt(1)){
                System.out.println();
                rank = rs.getInt(1);
                for (int i = 1; i < rs.getMetaData().getColumnCount(); i++) {
                    System.out.print(rs.getString(i) + "\t");
                }
                System.out.print(buscarTipoPremio(rs.getInt(3), cnx));
            }else {
                System.out.print(", " + buscarTipoPremio(rs.getInt(3), cnx));
            }
        } while (rs.next());
        System.out.println();
    }

    public static void borrarDatos(Connection cnx){
        try {
            cnx.createStatement().execute("SET FOREIGN_KEY_CHECKS=0");
            Statement st = cnx.createStatement();
            int res = st.executeUpdate("delete from jugador");
            if (res == 0){
                System.out.println("No se ha podido eliminar la tabla");
            }else {
                System.out.println("Se ha eliminado " + res + " filas");
            }
            cnx.createStatement().execute("SET FOREIGN_KEY_CHECKS=1");
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static void generarPremios(Connection cnx){
        try {
            PreparedStatement psO = cnxA.prepareStatement("select * from opta where Id_Ranking = ?");
            PreparedStatement psActualizar = cnxA.prepareStatement("update clasificacion set Id_Premio = ? where Ranking = ?");
            Statement st = cnxA.createStatement();
            st.executeUpdate("update clasificacion set Id_Premio = null");
            ResultSet rsC = st.executeQuery("select * from clasificacion");
            ResultSet rsP = st.executeQuery("select * from premio");
            rsC.first();
            rsP.first();

            do {
                psO.setInt(1, rsC.getInt(1));
                ResultSet rsOJ = psO.executeQuery();

            }while (rsC.next());

        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Connection cnxAux = cnxA;
        String csvFile = "";

        try {
            cnxA.createStatement().execute("SET FOREIGN_KEY_CHECKS=0");
            cnxB.createStatement().execute("SET FOREIGN_KEY_CHECKS=0");
            generarDatosJugador(cnxA, CSV_JUG_A);
            generarDatosJugador(cnxB, CSV_JUG_B);
            generarDatosClasificacion(cnxA, CSV_CLA_A);
            generarDatosClasificacion(cnxB, CSV_CLA_B);
            generarDatosPremio(cnxA, CSV_PRE_A);
            generarDatosPremio(cnxB, CSV_PRE_B);
            generarDatosOpta(cnxA);
            generarDatosOpta(cnxB);
            generarPremios(cnxA);
            generarPremios(cnxB);
            cnxA.createStatement().execute("SET FOREIGN_KEY_CHECKS=1");
            cnxB.createStatement().execute("SET FOREIGN_KEY_CHECKS=1");
        } catch (SQLException e){
            e.printStackTrace();
        }

        int opcion;
        do {
            mostrarMenu1();
            opcion = leerInt();
            if (opcion == 1 || opcion == 2){
                if (opcion == 1){
                    cnxAux = cnxA;
                    csvFile = CSV_JUG_A;
                }
                if (opcion == 2){
                    cnxAux = cnxB;
                    csvFile = CSV_JUG_B;
                }
                do {
                    mostrarMenu2();
                    opcion = leerInt();
                    if (opcion >= 0 && opcion <= 4){
                        try {
                            switch (opcion) {
                                case 1 -> mostrarDatos(buscarDatosJugador(cnxAux));
                                case 2 -> borrarDatos(cnxAux);
                                case 3 -> generarDatosJugador(cnxAux, csvFile);
                                case 4 -> mostrarDatosOptan(buscarDatosOptan(cnxAux), cnxAux);
                            }
                        }catch (SQLException e){
                            e.printStackTrace();
                        }
                    }else {
                        System.out.println("Error: Opción NO válida");
                    }
                }while (opcion != 0);
                opcion = -1;
            }else if (opcion != 0){
                System.out.println("Error: Opción NO válida");
            }
        }while (opcion != 0);
    }
}