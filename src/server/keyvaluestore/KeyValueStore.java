package server.keyvaluestore;

import static server.keyvaluestore.UniqueIdGenerator.generateId;
import java.util.concurrent.ConcurrentHashMap;
import logs.Logger;
import server.itinerary.Itinerary;

public class KeyValueStore {

  // KeyValueStore that stores Unique ID as Key, and Itinerary Object as value
  private final ConcurrentHashMap<String, Itinerary> keyValueStore;
  private final Logger logger;

  /**
   * Constructor of KeyValueStore that initializes the Key-Value Store.
   *
   * @param fileName - Filename for logger
   */
  public KeyValueStore(String fileName, String serverId) {
    this.keyValueStore = new ConcurrentHashMap<>();
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
        return "Valid Operation. PAXOS. SHARE.";
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

  synchronized String executeOperation(String[] tokens) {

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

      } else {
        logger.debug(true, "Itinerary Key : ", tokens[1], " not found in the store.");
        return "Itinerary Not found";
      }

    } else if (tokens[0].equalsIgnoreCase("SHARE")) {

      // If the specified Key is found, then search for User with email
        // Now search for user, if user is found, send the random it to that user to listOfSharedIts

      // If no key is found, return no itinerary found

      // TODO

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
   * @param itinerary
   * @return
   */
  public String addItinerary(Itinerary itinerary) {
    if (itinerary != null) {
      String uniqueKeyId = generateId();

      this.keyValueStore.put(uniqueKeyId, itinerary);
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
