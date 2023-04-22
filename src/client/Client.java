package client;

import static client.ClientInputHelper.fetchUserInput;
import static client.ClientInputHelper.fetchUserLoginInput;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.SimpleDateFormat;
import java.util.Date;
import logs.Logger;
import server.Server;

/**
 * Client class that is used to look up the server stub from the RMI registry and invoke remote
 * methods from the server stub.
 */
public class Client {

  private final String hostName;
  private final int port;
  private final int serverId;
  private final Logger logger;

  private Server server;

  /**
   * Constructor for client and initializes the client object.
   *
   * @param hostName - IP address for RMI registry
   * @param port - port number for RMI registry
   * @param serverId - server ID
   */
  public Client(String hostName, int port, int serverId) {
    this.hostName = hostName;
    this.port = port;
    this.serverId = serverId;

    String currentTime = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
    this.logger = new Logger("src/logs/client_" + currentTime + ".log", "ClientMain");
  }

  void signIn() {
    logger.debug(false, "Starting the client:");

    try {
      logger.debug(false, "Looking for RMI registry...");
      Registry registry = LocateRegistry.getRegistry(hostName, port);
      logger.debug(false, "Found RMI registry!");

      logger.debug(false, "Looking for server stub...");
      this.server = (Server) registry.lookup("KVS" + serverId);
      logger.debug(false, "Found the server stub!");

      // Server asks for Log in Sign up

      boolean flag = true;
      while (true) {
        String sessionRequest = fetchUserLoginInput(flag);
        logger.debug(false, "Received Session request from user: ", sessionRequest);

        if (flag) {
          flag = false;
        }

        if (sessionRequest != null) {
          logger.debug(false, "Sending sign in request to the server: ", sessionRequest);
          String response = server.signIn(sessionRequest);
          logger.debug(false, "Response from server: ", response);
          System.out.println("Response from server: " + response);
        }
      }

    } catch (RemoteException | NotBoundException e) {
      logger.error(false, "Error connecting to RMI registry and while fetching the" +
          " server stub.");
      System.out.println("Error! The RMI registry is not reachable!\nMake sure that it has been started and " +
          "that the hostname and port number are correct.");
    }
  }

  /**
   * This method starts the Client. It tries to locate the RMI registry and fetch server stub
   * from it. After that, it invokes remote method of the server stub in order to interact with it.
   */
  void run() {

    // After the client is signed in, it can execute operations
    logger.debug(true, "Client connected to server");

    try {

      boolean prompt = true;
      while (true) {

        // For taking input after logging in
        String request = fetchUserInput(prompt);
        logger.debug(false, "Received request from user: ", request);

        if (prompt) {
          prompt = false;
        }

        if (request != null) {
          if (request.equalsIgnoreCase("x")) {
            logger.debug(false, "Quitting the application.");
            break;
          }

          logger.debug(false, "Sending request to the server: ", request);
          String response = server.executeOperation(request);
          logger.debug(false, "Response from server: ", response);
          System.out.println("Response from server: " + response);
        }
      }

    } catch (Exception e) {
      logger.error(true, "Error connecting client with server! Unable to Sign In!");
    }
  }
}
