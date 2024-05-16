import java.io.*;
import java.net.*;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class BattagliaNavaleClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final char[][] grigliaLocale = new char[10][10]; // Matrice locale per tracciare gli attacchi

    public BattagliaNavaleClient() {
        // Inizializza la griglia locale con punti, indicando che nessuna cella è stata attaccata
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                grigliaLocale[i][j] = '.';
            }
        }
    }

    public void startConnection(String ip, int port) {
        try {
            socket = new Socket(ip, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void gioca() {
        Scanner scanner = new Scanner(System.in);
        String difficoltaInput;
        int difficolta = 0;
        while (true) {
            System.out.println("Scegli la difficoltà (1 - Facile, 2 - Media, 3 - Difficile):");
            difficoltaInput = scanner.nextLine();
            if (difficoltaInput.equals("1") || difficoltaInput.equals("2") || difficoltaInput.equals("3")) {
                difficolta = Integer.parseInt(difficoltaInput);
                break;
            } else {
                System.out.println("La difficoltà inserita non è valida. Riprova.");
            }
        }

        int roundTotali = difficolta == 1 ? 16 : difficolta == 2 ? 12 : 8;

        try {
            // Comunica al server il numero di round totali
            out.println(roundTotali);

            for (int round = 0; round < roundTotali; round++) {
                System.out.println("\nRound " + (round + 1) + " di " + roundTotali);
                Set<String> coordinateInserite = new HashSet<>();

                while (coordinateInserite.size() < 5) {
                    System.out.println("Inserisci la coordinata di attacco #" + (coordinateInserite.size() + 1) + " (es. 00, 04, 23). la prima cifra individua la riga, la seconda individua la colonna : ");
                    String coord = scanner.nextLine();

                    // Controllo se la stringa inserita è valida
                    if (coord.length() != 2 || !coord.matches("[0-9][0-9]")) {
                        System.out.println("Coordinate non valide. Riprova.");
                        continue;
                    }

                    // Controllo se la coordinata è già stata attaccata
                    int riga = Character.getNumericValue(coord.charAt(0));
                    int colonna = Character.getNumericValue(coord.charAt(1));
                    if (grigliaLocale[riga][colonna] == 'X' || grigliaLocale[riga][colonna] == '/') {
                        System.out.println("Coordinata già attaccata. Riprova.");
                        continue;
                    }

                    coordinateInserite.add(coord);
                    grigliaLocale[riga][colonna] = 'X'; // Segna la casella come attaccata nella matrice locale
                }

                // Invia tutte le coordinate in una volta, separate da virgole
                String attacchi = String.join(",", coordinateInserite);
                out.println(attacchi);

                // Ricevi e stampa l'intera matrice aggiornata dal server
                String grigliaAggiornata;
                while ((grigliaAggiornata = in.readLine()) != null && !grigliaAggiornata.isEmpty()) {
                    System.out.println(grigliaAggiornata);
                }

                // Ricezione del conteggio delle navi colpite dal server
                String conteggioNavi = in.readLine();
                if (conteggioNavi != null) {
                    int naviColpite = Integer.parseInt(conteggioNavi);
                    if (naviColpite == 15) {
                        //  System.out.println("Hai colpito tutte le navi. Fine della partita.");
                        break;
                    }
                }
            }

            // Ricezione del messaggio finale di vittoria o sconfitta
            String messaggioFinale = in.readLine();
            if (messaggioFinale != null) {
                System.out.println(messaggioFinale);
            }

        } catch (IOException e) {
            System.out.println("Errore nella comunicazione con il server.");
            e.printStackTrace();
        } finally {
            scanner.close();
            stopConnection(); // Chiude la connessione al termine del gioco
        }
    }

    public void stopConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        BattagliaNavaleClient client = new BattagliaNavaleClient();
        client.startConnection("127.0.0.1", 6666);
        client.gioca();
        client.stopConnection();
    }
}