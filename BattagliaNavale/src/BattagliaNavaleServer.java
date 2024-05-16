import java.io.*;
import java.net.*;
import java.util.Random;

public class BattagliaNavaleServer {
    private ServerSocket serverSocket;
    private int[][] griglia = new int[10][10];
    private int[][] grigliaAttacchi = new int[10][10]; // 0 = non attaccato, 1 = colpito, -1 = mancato

    public BattagliaNavaleServer() {
        posizionaNavi();
    }

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server avviato e in ascolto sulla porta " + port);
            Socket clientSocket = serverSocket.accept();
            gestisciClient(clientSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void posizionaNavi() {
        Random random = new Random();
        int naviDaPosizionare = 5;

        while (naviDaPosizionare > 0) {
            int riga = random.nextInt(10);
            int colonna = random.nextInt(10);
            boolean orizzontale = random.nextBoolean();

            if (puoPosizionareNave(riga, colonna, orizzontale)) {
                for (int i = 0; i < 3; i++) {
                    if (orizzontale) {
                        griglia[riga][colonna + i] = 1;
                    } else {
                        griglia[riga + i][colonna] = 1;
                    }
                }
                naviDaPosizionare--;
            }
        }
    }

    private boolean puoPosizionareNave(int riga, int colonna, boolean orizzontale) {
        for (int i = 0; i < 3; i++) {
            if (orizzontale) {
                if (colonna + i >= 10 || griglia[riga][colonna + i] == 1) {
                    return false;
                }
            } else {
                if (riga + i >= 10 || griglia[riga + i][colonna] == 1) {
                    return false;
                }
            }
        }
        return true;
    }

    private void gestisciClient(Socket clientSocket) {
        try {
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // Legge il numero di round totali inviati dal client
            String roundTotaliStr = in.readLine();
            int roundTotali = Integer.parseInt(roundTotaliStr);
            int roundAttuali = 0;

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                roundAttuali++; // Incrementa il contatore di round attuali ad ogni ciclo

                // Processa gli attacchi
                String[] attacchi = inputLine.split(",");
                for (String attacco : attacchi) {
                    int riga = Character.getNumericValue(attacco.charAt(0));
                    int colonna = Character.getNumericValue(attacco.charAt(1));

                    if (griglia[riga][colonna] == 1) {
                        grigliaAttacchi[riga][colonna] = 1;
                    } else {
                        grigliaAttacchi[riga][colonna] = -1;
                    }
                }

                // Invia la griglia aggiornata al client
                out.println(costruisciGrigliaAttacchi());

                // Invia il conteggio delle navi colpite al client
                out.println(contaColpi());

                if (contaColpi() == 15 || roundAttuali >= roundTotali) { // Se tutte le navi sono state colpite o sono terminati i round
                    String messaggio = contaColpi() == 15 ? "Vittoria! Hai colpito tutte le navi." : "Sconfitta! Non hai colpito tutte le navi nei round disponibili.";
                    out.println(messaggio + " La partita è terminata"); // Invia un messaggio speciale per indicare la fine della partita
                    break; // Termina il ciclo
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close(); // Chiude la connessione solo dopo aver gestito tutte le condizioni
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int contaColpi() {
        int colpi = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (grigliaAttacchi[i][j] == 1) colpi++;
            }
        }
        return colpi;
    }

    private String costruisciGrigliaAttacchi() {
        StringBuilder sb = new StringBuilder();

        // Aggiungi intestazione con numeri delle colonne
        sb.append("  "); // Spazio per l'allineamento della riga
        for (int col = 0; col < 10; col++) {
            sb.append(col + " ");
        }
        sb.append("\n");

        for (int i = 0; i < 10; i++) {
            // Aggiungi numerazione all'inizio della riga
            sb.append(i + " ");
            for (int j = 0; j < 10; j++) {
                if (grigliaAttacchi[i][j] == 1) {
                    sb.append("X ");
                } else if (grigliaAttacchi[i][j] == -1) {
                    sb.append("/ ");
                } else {
                    sb.append(". ");
                }
            }
            // Aggiungi numerazione alla fine della riga
            sb.append(i + "\n");
        }

        // Aggiungi piede con numeri delle colonne
        sb.append("  "); // Spazio per l'allineamento della riga
        for (int col = 0; col < 10; col++) {
            sb.append(col + " ");
        }
        sb.append("\n");

        return sb.toString();
    }

    public static void main(String[] args) {
        BattagliaNavaleServer server = new BattagliaNavaleServer();
        server.start(6666); // Esempio di porta.
    }
}