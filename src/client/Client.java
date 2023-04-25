package client;

import static client.ClientInputHelper.fetchItineraryInput;
import static client.ClientInputHelper.fetchLoginInput;
import static client.ClientInputHelper.fetchSignUpInput;
import static client.ClientInputHelper.fetchUserOperationInput;
import static client.ClientInputHelper.fetchSignupOrLoginInput;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.SimpleDateFormat;
import java.util.Date;
import logs.Logger;
import server.Server;
import server.itinerary.Itinerary;
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
  private boolean prompt = true;

  private Server userDbServer, keyValueStoreServer;

  private User user;
  // TODO - Use any fields from below to keep a track of whether the client is signed in or not
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
      userDbServer = (Server) registry.lookup("UserDB");
      logger.debug(false, "Found the User Database server stub!");

      // Server asks for Log in Sign up

      String signupOrLoginInput = fetchSignupOrLoginInput();
      logger.debug(false, "Received Session request from user: ", signupOrLoginInput);

      String response = null;
      String emailId = null;

      if (signupOrLoginInput.equalsIgnoreCase("Signup")) {
        String  signUpInput = fetchSignUpInput();

        assert signUpInput != null;
        String[] tokens = parseMessage(signUpInput);
        emailId = tokens[1];

        logger.debug(false, "Sending sign up request to the server: ", signUpInput);
        response = userDbServer.signUp(signUpInput);
      } else if (signupOrLoginInput.equalsIgnoreCase("Login")) {
        String loginInput = fetchLoginInput();

        assert loginInput != null;
        String[] tokens = parseMessage(loginInput);
        emailId = tokens[0];

        logger.debug(false, "Sending login request to the server: ", loginInput);
        response = userDbServer.login(loginInput);
      }

      if (response != null) {
        if (response.contains("User Created")) {
          this.setSignedIn(true);
          this.user = userDbServer.getUser(emailId);
          this.user.setLoggedIn(true);
        } else if (response.contains("User Logged in")) {
          this.setSignedIn(true);
          this.user = userDbServer.getUser(emailId);
          this.user.setLoggedIn(true);
        }
      }

      logger.debug(false, "Response from server: ", response);
      System.out.println("Response from server: " + response);

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
      keyValueStoreServer = (Server) registry.lookup("KVS" + serverId);
      logger.debug(false, "Found the Itinerary KeyValueStore server stub!");
    } catch (Exception e) {
      logger.error(false, "Error connecting to KeyValueStore RMI registry and " +
          "while fetching the server stub.");
      System.out.println("Error! The RMI registry is not reachable!\nMake sure that it has been started and " +
          "that the hostname and port number are correct.");
      e.printStackTrace();
    }

    // After the client is signed in, it can execute operations
    logger.debug(false, "Client connected to the server");
    System.out.println("Client is connected to the server");

    try {
      while (true) {

        //System.out.println("Current User: " + this.user.getName());
        // For taking input after logging in
        // Request contains the input entered by the client for any operation
        String request = fetchUserOperationInput(prompt);
        logger.debug(false, "Received request from user: ", request);

        if (request != null) {
          if (request.equalsIgnoreCase("x")) {
            logger.debug(false, "Quitting the application.");
            this.setSignedIn(false);

            // TODO - A Case where same user signs in different terminal.
            // Sol: Do not allow to sign in a different terminal
            this.user.setLoggedIn(false);
            break;
          }

          logger.debug(false, "Sending request to the server: ", request);
          // Server executes the user inputs and sends the response
          String response = keyValueStoreServer.executeOperation(request, this.user);
          logger.debug(false, "Response from server: ", response);
          System.out.println("Response from server: " + response);

          if (response != null) {
            // Execute Operation from server sent this response because of PUT method
            if (response.equalsIgnoreCase("Enter Itinerary Details")) {
              sendItineraryToServer();
            }
          }
        }
      }
    } catch (Exception e) {
      logger.error(true, "Error connecting client with server!");
      e.printStackTrace();
    }
  }

  // Helper method to send Itinerary Inputs from the client to server
  private void sendItineraryToServer() throws RemoteException {
    // Ask for user input for itinerary details
    Itinerary itinerary = fetchItineraryInput(user);

    // To carry on getting continuous input from user for the 5 operations
    this.prompt = true;

    if (itinerary != null) {
      // Add the itinerary in the KeyValueStore
      logger.debug(false, "Sending Itinerary request to the server: ",
          itinerary.getName());
      String itineraryResponse = keyValueStoreServer.putItinerary(itinerary, this.user);
      logger.debug(false, "Response from server: ", itineraryResponse);
      //System.out.println("Response from server: " + itineraryResponse);


      if (itineraryResponse.startsWith("Error")) {
        System.out.println("Response from server: " + itineraryResponse);
        logger.error(true, "Couldn't add your created Itinerary: ",
            itinerary.getName());
      } else {
        System.out.println("Itinerary Added with Name: '"+ itinerary.getName() +
            "'  Your Unique ID for Accessing it is: " + itineraryResponse);
        logger.debug(false, "Itinerary Added with Name: '", itinerary.getName(),
            "'  and you can access it using Unique ID: ", itineraryResponse);
      }
    }
  }

  public boolean isSignedIn() {
    return isSignedIn;
  }

  void setSignedIn(boolean signedIn) {
    isSignedIn = signedIn;
  }

  /**
   * Parse the tokens from the input message which is pipe separated.
   *
   * @param message - input message
   * @return - array containing tokens
   */
  String[] parseMessage(String message) {
    return message.split("\\|");
  }
}
