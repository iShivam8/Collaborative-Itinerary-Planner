package client;

import static client.ClientInputHelper.fetchItineraryInput;
import static client.ClientInputHelper.fetchLoginInput;
import static client.ClientInputHelper.fetchSignUpInput;
import static client.ClientInputHelper.fetchUserOperationInput;
import static client.ClientInputHelper.fetchSignupOrLoginInput;
import java.io.IOException;
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
  private User user;
  private boolean isSignedIn;
  private Server userDbServer, keyValueStoreServer;

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
      this.userDbServer = (Server) registry.lookup("UserDB");
      logger.debug(false, "Found the User Database server stub!");

      // Server asks for Log in Sign up

      String signupOrLoginInput = fetchSignupOrLoginInput();
      logger.debug(false, "Received Session request from user: ", signupOrLoginInput);

      String response = null;
      String emailId = null;

      if (signupOrLoginInput.equalsIgnoreCase("Signup")) {
        String  signUpInput = fetchSignUpInput();

        String[] tokens = parseMessage(signUpInput);
        emailId = tokens[1];

        logger.debug(false, "Sending sign up request to the server: ", signUpInput);
        response = this.userDbServer.signUp(signUpInput);
      } else if (signupOrLoginInput.equalsIgnoreCase("Login")) {
        String loginInput = fetchLoginInput();

        String[] tokens = parseMessage(loginInput);
        emailId = tokens[0];

        logger.debug(false, "Sending login request to the server: ", loginInput);
        response = this.userDbServer.login(loginInput);
      }

      if (response != null) {
        if (response.equals("User already Logged-in")) {
          logger.debug(false, "User is already logged-in on "
              + "a different terminal. First logout then try to sign-in again!");
          System.out.println("User is already logged-in on "
              + "a different terminal. First logout then try to sign-in again!");
          this.user = this.userDbServer.getUser(emailId);
          this.setSignedIn(false);
          return;
        }

        if (response.equals("User Created")) {
          this.user = this.userDbServer.getUser(emailId);
          this.user.setLoggedIn(true);
          this.setSignedIn(true);
        } else if (response.equals("User Logged in")) {
          this.user = this.userDbServer.getUser(emailId);

          if (this.userDbServer.getSetOfLoggedInUsers()
              .contains(this.user.getEmailId())) {
            logger.debug(false, "User is already logged-in on "
                + "a different terminal. First logout then try to sign-in again!");
            System.out.println("User is already logged-in on "
                + "a different terminal. First logout then try to sign-in again!");
            return;
          }

          this.user.setLoggedIn(true);
          this.setSignedIn(true);
        }
      }

      logger.debug(false, "Response from server: ", response);
      System.out.println("Response from server: " + response);

      if (this.isSignedIn && this.user.isLoggedIn()) {
        run();
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
  private void run() {

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

        // For taking input after logging in
        // Request contains the input entered by the client for any operation
        String request = fetchUserOperationInput(prompt);
        logger.debug(false, "Received request from user: ", request);

        if (request != null) {
          if (request.equalsIgnoreCase("x")) {
            logger.debug(false, "Quitting the application.");
            this.setSignedIn(false);
            this.user.setLoggedIn(false);
            String logoutResponse = this.userDbServer.logout(this.user.getEmailId());
            logger.debug(true, "Logout response received from Server: ", logoutResponse);
            break;
          }

          // TODO - If user is null i.e. a server instance where the user is not logged in, handle that

          logger.debug(false, "Sending request to the server: ", request);
          // Server executes the user inputs and sends the response
          String response = this.keyValueStoreServer.executeOperation(request);
          logger.debug(false, "Response from server: ", response);
          System.out.println("Response from server: " + response);

          if (response != null) {
            // Execute Operation from server sent this response because of PUT method
            if (response.equalsIgnoreCase("Enter Itinerary Details")) {
              sendItineraryToServer("ENTER", null);
            } else if (response.contains("Update Itinerary Details")) {
              String[] updateResponse = parseMessage(response);
              // updateResponse[1] = Previous Itinerary id
              sendItineraryToServer("UPDATE", updateResponse[1]);
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
  private void sendItineraryToServer(String enterOrUpdate, String previousItineraryId)
      throws IOException, ClassNotFoundException {
    // Ask for user input for itinerary details
    Itinerary itinerary = fetchItineraryInput(this.user, enterOrUpdate, previousItineraryId);

    // To carry on getting continuous input from user for the 5 operations
    this.prompt = true;

    if (itinerary != null) {
      // Add the itinerary in the KeyValueStore
      logger.debug(false, "Sending Itinerary request to the server: ",
          itinerary.getName());
      String itineraryResponse = this.keyValueStoreServer.putItinerary(itinerary);
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

  boolean isSignedIn() {
    return this.isSignedIn;
  }

  void setSignedIn(boolean signedIn) {
    this.isSignedIn = signedIn;
  }

  void logout() throws RemoteException {

    if (this.user == null || this.userDbServer == null) {
      return;
    }

    logger.debug(false, "Quitting the application.");
    System.out.println("Quitting the application...");
    this.setSignedIn(false);
    this.user.setLoggedIn(false);
    String logoutResponse = this.userDbServer.logout(this.user.getEmailId());
    logger.debug(true, "Logout response received from Server: ", logoutResponse);
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
