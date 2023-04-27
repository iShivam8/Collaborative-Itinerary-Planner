package server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import logs.Logger;
import server.keyvaluestore.KeyValueStoreServer;
import server.user.UserDBServer;

/**
 * Entry Gateway for Server.
 */
public class ServerMainApplication {

  public static void main(String[] args) {

    if (args.length != 2) {
      System.out.println("Incorrect Arguments! Need 2 arguments: 1] Port number for registry, and "
          + "2] Number of server instances (Replicas)");
      return;
    }

    int port = -1, numberOfServers = -1;

    try {
      port = Integer.parseInt(args[0]);
      numberOfServers = Integer.parseInt(args[1]);

      if (port < 0 || port > 65536) {
        throw new NumberFormatException();
      }

      if (numberOfServers < 1 || numberOfServers > 100) {
        throw new NumberFormatException();
      }
    } catch (NumberFormatException numberFormatException) {
      System.out.println("Port number should be a integer. Number of server instances should be " +
          "between 1 to 100");
      return;
    }

    Logger logger = new Logger("src/logs/server.log", "ServerMain");
    logger.debug(true, "Starting the servers...");


    // For 2PC
    List<TwoPCServer> twoPCServerList = new ArrayList<>();

    // For PAXOS
    List<PaxosServer> listOfServers = new ArrayList<>();

    try {
      logger.debug(true, "Creating RMI registry on port: " + port);
      Registry registry = LocateRegistry.createRegistry(port);

      for (int i = 0; i < numberOfServers; i++) {
        // For UserDB Server
        Server userDb = new UserDBServer("UserDB2PC" + i);
        twoPCServerList.add((TwoPCServer) userDb);
        Server userDbServerStub = (Server) UnicastRemoteObject.exportObject(userDb, i);
        registry.rebind("UserDB2PC" + i, userDbServerStub);
        logger.debug(true, "Server bounded with stub UserDB2PC " + i + " to RMI Registry.");

        // For KeyValueStore Server
        Server server = new KeyValueStoreServer("KVS" + i, userDb);
        listOfServers.add((PaxosServer) server);
        Server serverStubKVS = (Server) UnicastRemoteObject.exportObject(server, i);
        registry.rebind("KVS" + i, serverStubKVS);
        logger.debug(true, "Server bounded with stub KVS" + i + ", to RMI Registry.");
      }

      for (TwoPCServer twoPCServer: twoPCServerList) {
        twoPCServer.connectWithAllServers(numberOfServers, port);
      }

      for (PaxosServer paxosServer: listOfServers) {
        paxosServer.connectWithAllServers(numberOfServers, port);
      }


    } catch (RemoteException remoteException) {
      logger.error(true, "Cannot create RMI Registry! Please try again.");
      remoteException.printStackTrace();
    }
  }
}