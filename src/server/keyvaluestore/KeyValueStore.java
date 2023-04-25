package server.keyvaluestore;

import com.google.gson.Gson;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;
import logs.Logger;
import server.Server;
import server.itinerary.Itinerary;
import server.user.User;
import server.user.UserDB;

public class KeyValueStore {

  // KeyValueStore that stores Unique ID as Key, and Itinerary Object as value
  private final ConcurrentHashMap<String, Itinerary> keyValueStore;
  private final Logger logger;
  private final Server userDbServer;
  private UserDB userDatabase;

  /**
   * Constructor of KeyValueStore that initializes the Key-Value Store.
   *
   * @param fileName - Filename for logger
   * @param userDb
   */
  public KeyValueStore(String fileName, String serverId, Server userDb) {
    this.keyValueStore = new ConcurrentHashMap<>();
    this.userDbServer = userDb;
    this.logger = new Logger(fileName, serverId);

    try {
      this.userDatabase = this.userDbServer.getUserDB();
    } catch (RemoteException e) {
      e.printStackTrace();
    }
  }

  /**
   * This method validates all the tokens to see if they form a valid key-value store operation.
   *
   * @param tokens - tokens those need the validation
   * @return - Valid or Invalid operation response
   */
  String[] validateTokens(String[] tokens) {

    if (tokens.length == 1) {
      // PUT
      if (tokens[0].equalsIgnoreCase("PUT")) {
        return new String[] {"Valid Operation. PUT.", "PUT"};
      } else {
        logger.error(true, "Invalid operation", tokens[0]);
        return new String[] {
            "Invalid operation: " + tokens[0] + ". Only PUT is supported with Single operand."};
      }
    } else if (tokens.length == 2) {
      if (tokens[0].equalsIgnoreCase("GET")) {
        return new String[] {"Valid Operation. GET", "GET"};
      } else if (tokens[0].equalsIgnoreCase("DELETE")) {
        return new String[] {"Valid Operation. PAXOS. DELETE.", "DELETE"};
      } else if (tokens[0].equalsIgnoreCase("EDIT")) {
        return new String[] {"Valid Operation. PAXOS. EDIT.", "EDIT"};
      } else {
        logger.error(true, "Invalid operation", tokens[0]);
        return new String[] {
            "Invalid operation: " + tokens[0] + ". Only GET, DELETE, and EDIT are supported with " +
                "Two operands."};
      }
    } else if (tokens.length == 3) {
      if (tokens[0].equalsIgnoreCase("SHARE")) {
        return new String[] {"Valid Operation. PAXOS. SHARE.", "SHARE"};
      } else {
        logger.error(true, "Invalid operation", tokens[0]);
        return new String[] {"Invalid operation: " + tokens[0] + ". Only SHARE is supported with " +
            "Three operands."};
      }
    } else {
      logger.error(true, "Invalid number of operands in the request.");
      return new String[] {
          "Invalid number of operands in the request. Only PUT, GET, DELETE, EDIT, and SHARE " +
              "are supported as operations."};
    }
  }

  /**
   * This method executes the 5 operations: PUT, GET, DELETE, EDIT, SHARE
   * After validating the token message received from the client.
   *
   * @param tokens - input message token received from the client
   * @return - response of the executed operation
   */
  synchronized String executeOperation(String[] tokens, User currentUser) {

    // Tokens:  Put --> INSERT;    Get|123;    Delete|123;    EDIT|123;   Share|123|a@a.com;

    if (tokens[0].equalsIgnoreCase("PUT")) {
      // Need to update in User class, the list of Itineraries
      // USER <--> Itinerary

      return "Enter Itinerary Details";

    } else if (tokens[0].equalsIgnoreCase("INSERT")) {

      // tokens[1] contains unique Key ID
      // tokens[2] contains serialized itinerary

      // Deserializing Itinerary JSON object back to Itinerary
      Itinerary itinerary = new Gson().fromJson(tokens[2], Itinerary.class);
      logger.debug(true, "Successfully Deserialized the Itinerary: ", itinerary.getName());

      //addItinerary(itinerary);
      // TODO - What if the KVS contains key already?
      //  Can we skip that and use this method for EDIT as well?

      this.keyValueStore.put(tokens[1], itinerary);

      // Adding this itinerary in the list of created itinerary of the Current User
      currentUser.setListOfCreatedItinerary(itinerary);
      logger.debug(true, "Itinerary '", itinerary.getName(),
          "' Added in the List of Created Itineraries of User: ", currentUser.getName());

      return tokens[1];

    } else if (tokens[0].equalsIgnoreCase("GET")) {
      // The Token[1] is the Random unique ID generated. Should it be int or string?
      if (keyValueStore.containsKey(tokens[1])) {
        logger.debug(true, "Found Itinerary Key : ", tokens[1],
            "and Itinerary Name Value : ", keyValueStore.get(tokens[1]).getName());
        return keyValueStore.get(tokens[1]).toString();
      } else {
        logger.debug(true, "Itinerary Key : ", tokens[1], " not found in the store.");
        return "Itinerary Not found";
      }
      //return keyValueStore.getOrDefault(tokens[1], "Not found");

    } else if (tokens[0].equalsIgnoreCase("EDIT")) {
      // Search for specific key (It ID)
      // If found it, then ask for user input for itinerary enable PUT operation
      // If not found, return itinerary not found

      //  0       1
      // EDIT  123123213

      if (keyValueStore.containsKey(tokens[1])) {
        logger.debug(true, "Found Itinerary Key : ", tokens[1],
            "and Itinerary Name Value : ", keyValueStore.get(tokens[1]).getName());

        // TODO - Enable Put operation

        // TODO - If the itinerary is in the createdList or SharedList of the current user, then only he will be able to edit it
        return "Update Itinerary Details";

      } else {
        logger.debug(true, "Itinerary Key : ", tokens[1], " not found in the store.");
        return "Itinerary Not found";
      }

    } else if (tokens[0].equalsIgnoreCase("SHARE")) {
      //   0      1       2
      // SHARE|1223123|s@s.com
      String itineraryId = tokens[1];
      String sharedEmailId = tokens[2];

      // TODO - If current user wants to share the itinerary with himself, return cant do that

      // If no key i.e. Itinerary ID is found, return no itinerary found
      if (!keyValueStore.containsKey(itineraryId)) {
        logger.debug(true, "Itinerary Key : ", tokens[1], " not found in the store.");
        return "Itinerary Not found";
      }

      // Else, if the specified Key is found, then search for User with specified email
      try {
        //System.out.println("FIRST USER: " + userDbServer.getUserDB().getUserDatabase().get(tokens[2]).getName());
        //System.out.println("SECOND USER: " + userDatabase.getUserDatabase().get(tokens[2]).getName());

        // If there is no account of the shared user email, return no user found
        if (!this.userDatabase.getUserDatabase().containsKey(sharedEmailId)) {
          logger.debug(true, "No user found with Email: ", tokens[2]);
          return "No User Found";
        }

        // Else, if user is found, send itinerary id to that user to listOfSharedIts
        // If user is found in db, then share the key with that user
        logger.debug(true, "User found with Email: ", tokens[2],
            " Sharing the Itinerary: ",
            userDbServer.getUserDB().getUserDatabase().get(tokens[2]).getName());

        Itinerary itinerary = this.keyValueStore.get(itineraryId);

        // TODO - Current user gets null
        if (!currentUser.getEmailId().equals(itinerary.getCreatedBy().getEmailId())) {
          logger.debug(true, "You're not the Owner of this itinerary, " +
              "so you can't share with other users");
          return "You're not the Owner of this itinerary, so you can't share it with other users!";
        }

        User sharedUser = null;

        sharedUser = this.userDatabase.getUserDatabase().get(sharedEmailId);
        //sharedUser = userDbServer.getUserDB().getUserDatabase().get(tokens[2]);


        logger.debug(true, "Current User: ", currentUser.getName(),
            " Shared User: ", sharedUser.getName());
        System.out.println("Current User: " + currentUser.getName() +
            ", Shared User: " + sharedUser.getName());

        // Setting list of shared user for current user, and shared itinerary for the shared users
        currentUser.addSharedUserToMap(itinerary, sharedUser);
        //itinerary.setCreatedBy(currentUser);

        // Updates Shared Users list of itineraries
        if (itinerary.getListOfSharedWithUsers().contains(sharedUser)) {
          logger.debug(true, "This Itinerary is already share with User: ",
              sharedUser.getName(), " Email: ", sharedUser.getEmailId());

          return "Itinerary is Already Shared";
        } else {

          // Adding the shared user to the itinerary created by Current user (owner)
          itinerary.setListOfSharedWithUsers(sharedUser);

          // Adding the current itinerary as the Shared itinerary of the shared user
          // So that the shared user can know to which itinerary he has access to
          sharedUser.setListOfSharedItinerary(itinerary);

          logger.debug(true, "Itinerary '", itinerary.getName(),
              "' successfully shared with User: ", sharedUser.getName());
          return "Itinerary Successfully Shared";
        }

        // TODO - Although the Itinerary is shared, its not visible using get method

        // TODO - First time it does not found the user, so no user found
        /*
        if (userDbServer.getUserDB().getUserDatabase().containsKey(tokens[2])) {

        }
         */

      } catch (Exception e) {
        logger.error(true, "Exception occurred! Can't share with the specified User!");
        e.printStackTrace();
        return "Can't share with specified User";
      }

    } else {
      if (keyValueStore.containsKey(tokens[1])) {
        logger.debug(true, "Deleted key :", tokens[1], " from the store.");
        keyValueStore.remove(tokens[1]);
        return "Deleted";
      } else {
        logger.debug(true, "Key : ", tokens[1], " not found in the store. " +
            "Not deleting anything.");

        return "Key Not Found";
      }
    }
  }


  public String addItinerary(String uniqueKeyId, Itinerary itinerary, User currentUser) {
    if (itinerary != null) {
      //String uniqueKeyId = generateId();

      this.keyValueStore.put(uniqueKeyId, itinerary);

      // Adding this itinerary in the list of created itinerary of the Current User
      currentUser.setListOfCreatedItinerary(itinerary);
      logger.debug(true, "Itinerary '", itinerary.getName(),
          "' Added in the List of Created Itineraries of User: ", currentUser.getName());

      return uniqueKeyId;
    }

    logger.error(true, "Error Adding Itinerary to the KeyValueStore! ");
    return "Error Adding Itinerary";
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
