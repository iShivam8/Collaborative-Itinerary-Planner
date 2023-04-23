package client;

import static client.ClientInputHelper.fetchLoginInput;
import static client.ClientInputHelper.fetchSignUpInput;
import static client.ClientInputHelper.fetchUserOperationInput;
import static client.ClientInputHelper.fetchSignupOrLoginInput;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.SimpleDateFormat;
import java.util.Date;
import logs.Logger;
import server.Server;
import server.user.User;

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

  // TODO - Use any fields from below to keep a track of whether the client is signed in or not
  private User user;
  private boolean isSignedIn;

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

      logger.debug(false, "Looking for the User Database server stub...");
      this.server = (Server) registry.lookup("UserDB");
      logger.debug(false, "Found the User Database server stub!");

      // Server asks for Log in Sign up

      String signupOrLoginInput = fetchSignupOrLoginInput();
      logger.debug(false, "Received Session request from user: ", signupOrLoginInput);

      if (signupOrLoginInput != null) {
        String response = null;

        if (signupOrLoginInput.equalsIgnoreCase("Signup")) {
          String signUpInput = fetchSignUpInput();
          logger.debug(false, "Sending sign up request to the server: ", signUpInput);
          response = server.signUp(signUpInput);
        } else if (signupOrLoginInput.equalsIgnoreCase("Login")) {
          String loginInput = fetchLoginInput();
          logger.debug(false, "Sending login request to the server: ", loginInput);
          response = server.login(loginInput);
        }

        if (response != null) {
          if (response.contains("User Created") || response.contains("User Logged in")) {
            this.setSignedIn(true);
          }
        }

        logger.debug(false, "Response from server: ", response);
        System.out.println("Response from server: " + response);
      }

    } catch (Exception e) {
      logger.error(false, "Error connecting to RMI registry and while fetching the" +
          " server stub.");
      System.out.println("Error! The RMI registry is not reachable!\nMake sure that it has been started and " +
          "that the hostname and port number are correct.");
      e.printStackTrace();
    }
  }

  /**
   * This method starts the Client. It tries to locate the RMI registry and fetch server stub
   * from it. After that, it invokes remote method of the server stub in order to interact with it.
   */
  void run() {

    try {
      logger.debug(false, "Looking for RMI registry...");
      Registry registry = LocateRegistry.getRegistry(hostName, port);
      logger.debug(false, "Found RMI registry!");

      logger.debug(false, "Looking for the Itinerary KeyValueStore server stub...");
      this.server = (Server) registry.lookup("KVS" + serverId);
      logger.debug(false, "Found the Itinerary KeyValueStore server stub!");
    } catch (Exception e) {
      logger.error(false, "Error connecting to KeyValueStore RMI registry and " +
          "while fetching the server stub.");
      System.out.println("Error! The RMI registry is not reachable!\nMake sure that it has been started and " +
          "that the hostname and port number are correct.");
      e.printStackTrace();
    }

    // After the client is signed in, it can execute operations
    logger.debug(true, "Client connected to the server");

    try {

      boolean prompt = true;
      while (true) {

        // For taking input after logging in
        // Request contains the input entered by the client for any operation
        String request = fetchUserOperationInput(prompt);
        logger.debug(false, "Received request from user: ", request);

        if (prompt) {
          prompt = false;
        }

        if (request != null) {
          if (request.equalsIgnoreCase("x")) {
            logger.debug(false, "Quitting the application.");
            // TODO - Make isSignedIn boolean of the user as false. Send false to server for User
            this.setSignedIn(false);
            break;
          }

          logger.debug(false, "Sending request to the server: ", request);
          // Server executes the user inputs and sends the response
          String response = server.executeOperation(request);
          logger.debug(false, "Response from server: ", response);
          System.out.println("Response from server: " + response);
        }
      }

    } catch (Exception e) {
      logger.error(true, "Error connecting client with server! Unable to Sign In!");
    }
  }

  public boolean isSignedIn() {
    return isSignedIn;
  }

  void setSignedIn(boolean signedIn) {
    isSignedIn = signedIn;
  }
}
