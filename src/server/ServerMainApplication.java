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

    List<PaxosServer> listOfServers = new ArrayList<>();

    try {
      logger.debug(true, "Creating RMI Registry at port: ", String.valueOf(port));
      Registry registry = LocateRegistry.createRegistry(port);

      Server userDb = new UserDBServer("UserDB");
      Server userDbServerStub = (Server) UnicastRemoteObject.exportObject(userDb, port);
      registry.rebind("UserDB", userDbServerStub);

      logger.debug(true, "Server bounded with stub UserDB to RMI Registry.");


      for (int i = 0; i < numberOfServers; i++) {
        Server server = new KeyValueStoreServer("KVS" + i, userDb);
        listOfServers.add((PaxosServer) server);

        Server serverStub = (Server) UnicastRemoteObject.exportObject(server, i);
        registry.rebind("KVS" + i, serverStub);

        logger.debug(true, "Server bounded with stub KVS" + i + ", to RMI Registry.");
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
