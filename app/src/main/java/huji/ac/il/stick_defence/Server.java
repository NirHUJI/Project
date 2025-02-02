package huji.ac.il.stick_defence;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * This class represents a server that manages all the league stuff
 * It uses the singleton pattern so that the current instance of the
 * server doesn't depend on the calling activity and his life cycle is during
 * the whole league.
 * The person who starts the server (by creating a new league) is just hosting
 * the server on his machine, but from the server point of view this person
 * his just a regular client like evryone else.
 * In other words the server doesn't know on witch machine it is being hosted.
 */
public class Server {
    private int leagueParticipants;
    public static final int PORT = 6666; //arbitrary port
    private static Server server;
    private boolean acceptingNewClients = false;
    //we keep tracking all the connected peers todo: change to hashmap based on id
    private ArrayList<Peer> peers = new ArrayList<>();
    private ServerSocket serverSocket;
    private LeagueManager leagueManager;
    private int gameOverCounter = 0;
    private int approvedPeersCounter =0;
    private int duplicateNameCounter = 0;
    private ArrayList<String> peersIds = new ArrayList<>();
    private ArrayList<String> names = new ArrayList<>();



    /**
     * private constructor, for the singleton pattern.
     *
     * @param participants the number of participants
     */
    private Server(int participants) {
        this.leagueParticipants = participants;
    }

    /**
     * Creates a new server if the current static server is null
     *
     * @return an instance of the server
     */
    public static Server createServer(int participants) {
        if (server == null) {
            server = new Server(participants);
            server.start();
        }
        else{
            server.stopServer();
            server = new Server(participants);
            server.start();
        }
        return server;
    }

    /**
     * Returns an instance of the server.
     * Could return null if no one called "createServer" before
     *
     * @return an instance of the server.
     */
    public static Server getServerInstance() {
        return server;
    }

    /**
     * Start the server.
     */
    private void start() {
        this.acceptingNewClients = true;
        //we create a new socket and listen to it on a different thread:
     new Thread(new Runnable() {
         @Override
         public void run() {
             Log.w("custom", "starting server");
             try {
                 serverSocket = new ServerSocket(PORT);
                 while (acceptingNewClients) {
                     Socket socket = serverSocket.accept(); //the accept method is blocking.
                     Log.w("custom", "client accepted!"); //if we reach this line only when a new client is connected.
                     Peer peer = new Peer(socket);
                     peers.add(peer); //save the new client in the peers list

                 }

             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }).start();

    }

    public void stopServer(){
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void sendLeagueInfo(String info) {
        for (Peer peer : this.peers) {
            peer.send(Protocol.stringify(Protocol.Action.LEAGUE_INFO, info));
        }
    }


    /**
     * This method decide what to do on each data received from a client.
     *
     * @param rawInput the input line that has been received from the client
     * @param peer     the client that send us the action.
     */
    private void doAction(String rawInput, Peer peer) {
        Protocol.Action action = Protocol.getAction(rawInput);
        if (null == action){
            return;
        }
        switch (action) {
            case NAME:
                JSONObject data;
                String name = null;
                String id = null;
                try {
                    data = new JSONObject(Protocol.getData(rawInput));
                    name = data.getString("name");
                    id= data.getString("id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(!peersIds.contains(id)) {
                    peersIds.add(id);
                    peer.approved = true;
                    approvedPeersCounter++;

                    if(names.contains(name)){
                        name=name+duplicateNameCounter;
                        duplicateNameCounter++;
                    }
                peer.setName(name);
                    names.add(name);
                    peer.setId(id);
                peer.send(Protocol.stringify(Protocol.Action.NAME_CONFIRMED));

                    if (approvedPeersCounter == leagueParticipants) {
                        //todo: sleep some time to see that no one is disconnecting
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        acceptingNewClients = false;
                        //remove unapproved peers:
                        for (int i=0; i<peers.size(); i++){
                            if (!peers.get(i).approved){
                                peers.remove(i);
                            }
                        }
                        leagueManager = new LeagueManager(peers);
                        JSONObject info = leagueManager.getLeagueInfo();
                        sendLeagueInfo(info.toString());
                        Log.w("custom", "league info sent!");
                    }

                }
                break;

            case READY_TO_PLAY:
                peer.setReadyToPlay();
                //both peers can ready to play
                if (peer.canStartPlay && peer.partner.canStartPlay) {
                    sendStartGame(peer);
                }
                break;

            case PARTNER_INFO:
                peer.setReceivedPartnerInfo();
                //both peers can ready to play
                if (peer.canStartPlay && peer.partner.canStartPlay) {
                    sendStartGame(peer);
                }
                break;

            case GAME_OVER:
                String isPeerWin = Protocol.getData(rawInput);
                if (isPeerWin.equals("true")) {
                    peer.wins++;
                }
                this.gameOverCounter++;
                Log.w("custom",""+gameOverCounter+ "partis is "+ leagueParticipants );
                if (gameOverCounter == this.leagueParticipants) {
                    this.gameOverCounter = 0;
                    leagueManager.updateLeagueStage();
                    JSONObject leagueInfo = leagueManager.getLeagueInfo();
                    sendLeagueInfo(leagueInfo.toString());
                }
                break;

        }

    }

    public void makePair(Peer peer1, Peer peer2) {
        peer1.setPartner(peer2);
        peer2.setPartner(peer1);
    }

    /*
    private void destroyPair(Peer peer1, Peer peer2) {
        peer1.setPartner(null);
        peer2.setPartner(null);
    }*/

    private void sendStartGame(Peer peer){
        try {
            //sleep for a few seconds, just to give the
            // player some time to finish loading his game
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String currentTime = Long.toString(System.currentTimeMillis());
        peer.send(Protocol.stringify(Protocol.Action.START_GAME, currentTime));
        peer.partner.send(
                Protocol.stringify(Protocol.Action.START_GAME, currentTime));
        //clear the readyToPlayFlag for the next time:
        peer.clearReadyToPlayAndReceivedInfo();
        peer.partner.clearReadyToPlayAndReceivedInfo();
    }

    /**
     * This class represents a socket listener on a client node.
     */

    private class ClientSocketListener implements Runnable {
private Peer peer;
        public ClientSocketListener(Peer peer){
            this.peer=peer;
        }
        @Override
        public void run() {
            Socket socket = peer.socket;
            Log.w("custom", "start socket listener");
            String inputLine;
            try {
                BufferedReader in =
                        new BufferedReader(
                                new InputStreamReader(socket.getInputStream()));
                //the readLine is a blocking method.
                while ((inputLine = in.readLine()) != null) {
                    Log.w("custom", "client syas: "+ inputLine);
                    doAction(inputLine, peer);
                    if (peer.partner != null) {
                        //add time stamp to the action;
                        inputLine = Protocol.addTimeStampToRawInput(inputLine);
                        peer.partner.send(inputLine);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.w("custom", "finish socket listener");
        }
    }

    /**
     * A socket wrapper.
     * Instead of keeping track on raw sockets, we wrap them as peers with name, id, and usfull methods.
     */
    public class Peer {
        private boolean approved = false; //check if the peer is an approved
        private String id; //unique id for each peer
        private String name; //name of the client. don't have to be unique.
        private PrintWriter out;
        private Socket socket;
        private Peer partner = null;
        private int wins = 0;
        private Boolean readyToPlay = false;
        private Boolean receivedPartnerInfo = true;//we start with true
        // because in the first round we don't need to wait for partner info
        private Boolean canStartPlay = false; //if both readyToPlay and
        // receivedPartnerInfo are true;

        /**
         * Constructs a new peer.
         *
         * @param socket the socket to wrap
         */
        public Peer(Socket socket) {
            this.socket = socket;
            new Thread(new ClientSocketListener(this)).start();
            try {
                this.out = new PrintWriter(socket.getOutputStream(), true);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        /**
         * Sets the name of the client this peer belongs too.
         *
         * @param name the name of the client.
         */
        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        /**
         * Send data to the socket
         *
         * @param out the data to be sent.
         */
        public void send(String out) {
            this.out.println(out);
            this.out.flush();
        }

        public void setPartner(Peer peer) {
            this.partner = peer;
        }

        public int getWins() {
            return this.wins;
        }
        public void setId(String id){
            this.id=id;
        }
        public void setReadyToPlay(){
            this.readyToPlay=true;
            if(this.receivedPartnerInfo){
                this.canStartPlay=true;
            }
        }

        public void setReceivedPartnerInfo(){
            this.receivedPartnerInfo=true;
            if(this.readyToPlay){
                this.canStartPlay=true;
            }
        }

        public void clearReadyToPlayAndReceivedInfo(){
            this.readyToPlay=false;
            this.canStartPlay=false;
            this.receivedPartnerInfo=false;
        }

        public void resetWins(){
            this.wins=0;
        }
    }

}
