package server.keyvaluestore;

import com.google.gson.Gson;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
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
   * @param userDb - Server userDb
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
        return new String[] {"Valid Operation. GET.", "GET"};
      } else if (tokens[0].equalsIgnoreCase("DELETE")) {
        return new String[] {"Valid Operation. PAXOS. DELETE.", "DELETE"};
      } else if (tokens[0].equalsIgnoreCase("EDIT")) {
        return new String[] {"Valid Operation. PAXOS. EDIT.", "EDIT"};
      } else if (tokens[0].equalsIgnoreCase("LIST")) {
        if (tokens[1].equalsIgnoreCase("CREATED")) {
          return new String[] {"Valid Operation. LIST.", "CREATED"};
        } else if (tokens[1].equalsIgnoreCase("COLLAB")) {
          return new String[] {"Valid Operation. LIST.", "COLLAB."};
        } else {
          logger.error(true, "Invalid LIST operation: ", tokens[1]);
          return new String[] {
              "Invalid operation: " + tokens[0] + " ", tokens[1], " Only CREATED and COLLAB " +
              "are supported with LIST operands."};
        }
      }
      else {
        logger.error(true, "Invalid operation", tokens[0]);
        return new String[] {
            "Invalid operation: " + tokens[0] + ". Only GET, DELETE, EDIT, and LIST are supported " +
                "with Two operands."};
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
   * @param currentUser - The user who invokes this method will be the current user
   * @return - response of the executed operation
   */
  synchronized String executeOperation(String[] tokens, User currentUser) {

    // Tokens:  Put --> INSERT;    Get|123;    Delete|123;    EDIT|123;   Share|123|a@a.com;

    if (tokens[0].equalsIgnoreCase("PUT")) {
      // Need to update in User class, the list of Itineraries
      // USER <--> Itinerary
      return "Enter Itinerary Details";
    }
    else if (tokens[0].equalsIgnoreCase("INSERT")) {
      // tokens[1] contains unique Key ID
      // tokens[2] contains serialized itinerary

      // Deserializing Itinerary JSON object back to Itinerary
      Itinerary itinerary = new Gson().fromJson(tokens[2], Itinerary.class);
      logger.debug(true, "Successfully Deserialized the Itinerary: ", itinerary.getName());

      // TODO - The user is different due to diff Itinerary
      User ownerUser = itinerary.getCreatedBy();

      //addItinerary(itinerary);
      // TODO - What if the KVS contains key already?
      //  Can we skip that and use this method for EDIT as well?

      this.keyValueStore.put(tokens[1], itinerary);
      ownerUser.addSharedUserToMap(itinerary, null);

      // Adding this itinerary in the list of created itinerary of the Current User
      ownerUser.setListOfCreatedItinerary(itinerary);
      logger.debug(true, "Itinerary '", itinerary.getName(),
          "' Added in the List of Created Itineraries of User: ", ownerUser.getName());

      return tokens[1];

    }
    else if (tokens[0].equalsIgnoreCase("GET")) {
      // The Token[1] is the Random unique ID generated. Should it be int or string?

      // If Itinerary is not in the store
      if (!keyValueStore.containsKey(tokens[1])) {
        logger.debug(true, "Itinerary Key : ", tokens[1], " not found in the store.");
        return "Itinerary Not found";
      }

      Itinerary itinerary = keyValueStore.get(tokens[1]);
      User ownerUser = itinerary.getCreatedBy();

      // If the Itinerary is created by owner OR
      // whether the current user is in the list of shared user of itinerary
      if (ownerUser.getEmailId().equals(currentUser.getEmailId())
          || itinerary.getListOfSharedWithUsers().contains(currentUser)) {

        logger.debug(true, "Found Itinerary Key : ", tokens[1],
            "and Itinerary Name Value : ", keyValueStore.get(tokens[1]).getName());

        return keyValueStore.get(tokens[1]).toString();
      } else if (ownerUser.getMapOfSharedItineraries().containsKey(itinerary)) {
        List<User> tempUser = ownerUser.getMapOfSharedItineraries().get(itinerary);
        if (tempUser.contains(currentUser)) {
          logger.debug(true, "Found Itinerary Key : ", tokens[1],
              "and Itinerary Name Value : ", keyValueStore.get(tokens[1]).getName());

          return keyValueStore.get(tokens[1]).toString();
        }
      }

      logger.debug(true, "You're not the Owner of this itinerary or " +
          "You don't have access to it as a collaborator!");
      return "No Authorization Access";

    }
    else if (tokens[0].equalsIgnoreCase("EDIT")) {
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

    }
    else if (tokens[0].equalsIgnoreCase("SHARE")) {
      //   0      1       2
      // SHARE|1223123|s@s.com
      String itineraryId = tokens[1];
      if (tokens[2].length() > 40) {
        return "Invalid token input";
      }
      String sharedEmailId = tokens[2];

      // If no key i.e. Itinerary ID is found, return no itinerary found
      if (!keyValueStore.containsKey(itineraryId)) {
        logger.debug(true, "Itinerary Key : ", tokens[1], " not found in the store.");
        return "Itinerary Not found";
      }

      // Else, if the specified Key is found, then search for User with specified email
      try {
        // If there is no account of the shared user email, return no user found
        if (!this.userDatabase.getUserDatabase().containsKey(sharedEmailId)) {
          logger.debug(true, "No user found with Email: ", tokens[2]);
          return "No User Found";
        }

        User sharedUser = userDatabase.fetchUser(sharedEmailId);
        Itinerary itinerary = this.keyValueStore.get(itineraryId);

        // Only the owner of the itinerary can share the itinerary with the shared user
        User ownerUser = itinerary.getCreatedBy();

        // If current user (owner) wants to share the itinerary with himself, return can't do that
        if (ownerUser.getEmailId().equals(sharedEmailId)) {
          logger.debug(true, "Current User and Shared User are the same. Not proceeding.");
          return "Cannot share to own self.";
        }

        //System.out.println("CURRENT USER: " + userDbServer.getUserDB().getUserDatabase().get(tokens[2]).getName());
        //System.out.println("SHARED USER: " + userDatabase.getUserDatabase().get(tokens[2]).getName());

        // TODO - While it is shared by 1 user, while 2nd user tries to learn, he uses share again
        //  and this time it is current user + shared user, so rejects!
        // TODO - Third server does not have any user.

        // TODO - Current user will throw null if accessed by a server which is not logged in by a user
        //System.out.println("CURRENT USER: " + currentUser.getName());
        //System.out.println("SHARED USER: " + sharedUser.getName());

        // Else, if user is found, send itinerary id to that user to listOfSharedIts
        // If user is found in db, then share the key with that user
        logger.debug(true, "User found with Email: ", tokens[2],
            " Sharing the Itinerary with: ",
            userDbServer.getUserDB().getUserDatabase().get(tokens[2]).getName());

        if (currentUser != null) {
          // If the current user is not the owner of the itinerary, he cannot share it with other users
          if (!currentUser.getEmailId().equals(ownerUser.getEmailId())) {
            logger.debug(true, "You're not the Owner of this itinerary, " +
                "so you can't share with other users");
            return "You're not the Owner of this itinerary, so you can't share it with other users!";
          }

          logger.debug(true, "Current User: ", currentUser.getName(),
              " Shared User: ", sharedUser.getName());
        }

        /*
        if (!currentUser.getEmailId().equals(itinerary.getCreatedBy().getEmailId())) {
          logger.debug(true, "You're not the Owner of this itinerary, " +
              "so you can't share with other users");
          return "You're not the Owner of this itinerary, so you can't share it with other users!";
        }
         */

        // Updates Shared Users list of itineraries
        if (itinerary.getListOfSharedWithUsers().contains(sharedUser)) {
          logger.debug(true, "This Itinerary is already share with User: ",
              sharedUser.getName(), " Email: ", sharedUser.getEmailId());

          return "Itinerary is Already Shared";
        } else {

          // Adding the shared user to the itinerary created by owner
          itinerary.setListOfSharedWithUsers(sharedUser);

          // Setting list of shared user for the owner user, and shared itinerary for the shared users
          ownerUser.addSharedUserToMap(itinerary, sharedUser);

          // Adding the current itinerary as the Shared itinerary of the shared user
          // So that the shared user can know to which itinerary he has access to
          sharedUser.setListOfSharedItinerary(itinerary);

          logger.debug(true, "Itinerary '", itinerary.getName(),
              "' successfully shared with User: ", sharedUser.getName());

          return "Itinerary Successfully Shared";
        }

        // TODO - Although the Itinerary is shared, its not visible using get method

      } catch (Exception e) {
        logger.error(true, "Exception occurred! Can't share with the specified User!");
        e.printStackTrace();
        return "Can't share with specified User";
      }

    }
    else if (tokens[0].equalsIgnoreCase("DELETE")) {
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
    else {
      // LIST
      if (currentUser == null) {
        logger.debug(true, "User is null, User not found");
        return "User Not found";
      }

      User usefulUser = this.userDatabase.getUserDatabase().get(currentUser.getEmailId());

      if (tokens[1].equalsIgnoreCase("CREATED")) {

        // Map - Itineraries created by user, and list of shared users
        System.out.println("MAP: " + usefulUser.getMapOfSharedItineraries().toString());
        System.out.println("List Size: " + usefulUser.getListOfCreatedItinerary().size());
        //return usefulUser.getListOfCreatedItinerary().toString();
        return usefulUser.getMapOfSharedItineraries().toString();

      } else {
        // COLLAB
        return usefulUser.getListOfSharedItinerary().toString();
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
