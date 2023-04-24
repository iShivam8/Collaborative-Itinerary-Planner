package server.keyvaluestore;

import static server.keyvaluestore.UniqueIdGenerator.generateId;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;
import logs.Logger;
import server.Server;
import server.itinerary.Itinerary;
import server.user.User;

public class KeyValueStore {

  // KeyValueStore that stores Unique ID as Key, and Itinerary Object as value
  private final ConcurrentHashMap<String, Itinerary> keyValueStore;
  private final Logger logger;
  private final Server userDbServer;

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
  }

  /**
   * This method validates all the tokens to see if they form a valid key-value store operation.
   *
   * @param tokens - tokens those need the validation
   * @return - Valid or Invalid operation response
   */
  String validateTokens(String[] tokens) {

    if (tokens.length == 1) {
      // PUT
      if (tokens[0].equalsIgnoreCase("PUT")) {
        return "Valid Operation. PUT.";
      } else {
        logger.error(true, "Invalid operation", tokens[0]);
        return "Invalid operation: " + tokens[0] + ". Only PUT is supported with Single operand.";
      }
    } else if (tokens.length == 2) {
      if (tokens[0].equalsIgnoreCase("GET")) {
        return "Valid Operation.";
      } else if (tokens[0].equalsIgnoreCase("DELETE")) {
        return "Valid Operation. PAXOS. DELETE.";
      } else if (tokens[0].equalsIgnoreCase("EDIT")) {
        return "Valid Operation. PAXOS. EDIT.";
      } else {
        logger.error(true, "Invalid operation", tokens[0]);
        return "Invalid operation: " + tokens[0] + ". Only GET, DELETE, and EDIT are supported with " +
            "Two operands.";
      }
    } else if (tokens.length == 3) {
      if (tokens[0].equalsIgnoreCase("SHARE")) {
        // TODO Add PAXOS.
        return "Valid Operation. SHARE.";
      } else {
        logger.error(true, "Invalid operation", tokens[0]);
        return "Invalid operation: " + tokens[0] + ". Only SHARE is supported with " +
            "Three operands.";
      }
    } else {
      logger.error(true, "Invalid number of operands in the request.");
      return "Invalid number of operands in the request. Only PUT, GET, DELETE, EDIT, and SHARE " +
          "are supported as operations.";
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

    // Tokens:  Put;    Get|123;    Delete|123;    EDIT|123;   Share|123|a@a.com;

    if (tokens[0].equalsIgnoreCase("PUT")) {
      // Need to update in User class, the list of Itineraries
      // USER <--> Itinerary

      return "Enter Itinerary Details";

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

      // If the specified Key is found, then search for User with email
      if (keyValueStore.containsKey(tokens[1])) {
        try {

          // Now search for user, if user is found, send itinerary id to that user to listOfSharedIts
          if (userDbServer.getUserDB().getUserDatabase().containsKey(tokens[2])) {
            // If user is found in db, then share the key with that user
            logger.debug(true, "User found with Email: ", tokens[2],
                " Sharing the Itinerary: ",
                userDbServer.getUserDB().getUserDatabase().get(tokens[2]).getName());

            Itinerary itinerary = keyValueStore.get(tokens[1]);
            User sharedUser = null;

            try {
              sharedUser = userDbServer.getUserDB().getUserDatabase().get(tokens[2]);
              logger.debug(true, "Current User: ", currentUser.getName(),
                  " Shareable User: ", sharedUser.getName());
              System.out.println("Current User: " + currentUser.getName() +
                  ", Shareable User: " + sharedUser.getName());

              // Setting list of shared user for current user, and shared itinerary for the shared users
              currentUser.addSharedUserToMap(itinerary, sharedUser);
              sharedUser.setListOfSharedItinerary(itinerary);

              // TODO - Although the Itinerary is shared, its not visible using get method
              // Probably because the servers are not in sync with each other

              logger.debug(true, "Itinerary '", itinerary.getName(),
                  "' successfully shared with User: ", sharedUser.getName());

              return "Itinerary Successfully Shared";

            } catch (RemoteException e) {
              e.printStackTrace();
            }

            // It Key = tokens[1]

          } else {
            logger.debug(true, "No user found with Email: ", tokens[2]);
            return "No User Found";
          }
        } catch (Exception e) {
          logger.debug(true, "Exception occurred! Can't share with the specified User!");
          e.printStackTrace();
        }
      } else {
        // If no key is found, return no itinerary found
        logger.debug(true, "Itinerary Key : ", tokens[1], " not found in the store.");
        return "Itinerary Not found";
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

    logger.error(true, "Invalid Operation! Please enter correct operands");
    return null;
  }

  /**
   * Method that adds the itinerary to the KeyValueStore
   * @param itinerary - Created Itinerary by the user
   * @param currentUser - Current User who created the Itinerary
   * @return - Itinerary ID KEY
   */
  public String addItinerary(Itinerary itinerary, User currentUser) {
    if (itinerary != null) {
      String uniqueKeyId = generateId();

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
