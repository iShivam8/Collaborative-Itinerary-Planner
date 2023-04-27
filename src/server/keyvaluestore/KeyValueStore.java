package server.keyvaluestore;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import logs.Logger;
import server.Server;
import server.itinerary.Itinerary;
import server.user.User;
import server.user.UserDB;

/**
 * KeyValueStore that stores the Itinerary in a Concurrent HashMap with
 * Key: Itinerary ID,  Value: Itinerary.
 */
public class KeyValueStore {

  // KeyValueStore that stores Unique ID as Key, and Itinerary Object as value
  private final ConcurrentHashMap<String, Itinerary> keyValueStore;
  private final Logger logger;
  private UserDB userDatabase;

  /**
   * Constructor of KeyValueStore that initializes the Key-Value Store.
   *
   * @param fileName - Filename for logger
   * @param userDb   - Server userDb
   */
  public KeyValueStore(String fileName, String serverId, Server userDb) {
    this.keyValueStore = new ConcurrentHashMap<>();
    this.logger = new Logger(fileName, serverId);

    try {
      this.userDatabase = userDb.getUserDB();
    } catch (Exception e) {
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
      } else {
        logger.error(true, "Invalid operation", tokens[0]);
        return new String[] {
            "Invalid operation: " + tokens[0] +
                ". Only GET, DELETE, EDIT, and LIST are supported " +
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
   * @param tokens        - input message token received from the client
   * @param clientEmailId - The user who invokes this method will be the current client user
   * @return - response of the executed operation
   */
  synchronized String executeOperation(String[] tokens, String clientEmailId)
      throws IOException, ClassNotFoundException {

    // Tokens:  Put --> INSERT;    Get|123;    Delete|123;    EDIT|123;   Share|123|a@a.com;
    //          LIST CREATED       LIST COLLAB

    // When executeOperation is invoked at the time of Learning new values
    //if (clientEmailId == null) {}

    if (tokens[0].equalsIgnoreCase("PUT")) {
      // Need to update in User class, the list of Itineraries
      // USER <--> Itinerary
      return "Enter Itinerary Details";
    } else if (tokens[0].equalsIgnoreCase("INSERT")) {
      // tokens[1] contains unique Key ID
      // tokens[2] contains serialized itinerary

      // Deserializing the Itinerary Byte Sized array object back to Itinerary
      byte[] decodedBytes = Base64.getDecoder().decode(tokens[2]);
      ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(decodedBytes));
      Itinerary itinerary = (Itinerary) ois.readObject();

      logger.debug(true, "Successfully Deserialized the Itinerary: ", itinerary.getName());

      // Update Call
      if (itinerary.getVersion() >= 1) {

        // I first need to check if the user who is modifying it is owner or not, then call update created by

        // Fetch old id, and update it with new itinerary
        String oldItineraryId = itinerary.getPrevItineraryId();

        // Preserving the previous Unique ID, but updating the Itinerary object
        Itinerary updatedItinerary = this.keyValueStore.get(oldItineraryId);

        // Updating the Old Itinerary with New Values
        updatedItinerary.setName(itinerary.getName());
        updatedItinerary.setLocation(itinerary.getLocation());
        updatedItinerary.setDescription(itinerary.getDescription());
        updatedItinerary.setStartDate(itinerary.getStartDate());
        updatedItinerary.setEndDate(itinerary.getEndDate());
        //updatedItinerary.setCreatedBy(itinerary.getCreatedBy());
        // Before putting the updated itinerary, gets the stored Itinerary list of shared user
        updatedItinerary.updateListOfSharedUsers(
            this.keyValueStore.get(oldItineraryId).getListOfSharedWithUsers());
        updatedItinerary.setPrevItineraryId(itinerary.getPrevItineraryId());
        // First get the version of the stored Itinerary, and then update it
        updatedItinerary.setVersion(this.keyValueStore.get(oldItineraryId).getVersion());
        updatedItinerary.updateVersion();

        // Update the old itinerary with an updated one
        this.keyValueStore.replace(oldItineraryId, updatedItinerary);
        //printAllUsersAndHisCreatedItineraries();

        logger.debug(true, "Itinerary Updated to: ", updatedItinerary.getName());

        return oldItineraryId;
      }

      User ownerUser = this.userDatabase.fetchUser(itinerary.getCreatedBy());

      itinerary.updateVersion();
      this.keyValueStore.put(tokens[1], itinerary);

      // Adding this itinerary in the list of created itinerary of the Current User
      ownerUser.setListOfCreatedItinerary(tokens[1]);
      // Adding this itinerary id in the Map of Shared user itinerary - with shared user as null
      //ownerUser.addSharedUserToMap(itinerary.getItineraryId(), null);

      logger.debug(true, "Itinerary '", itinerary.getName(),
          "' Added in the List of Created Itineraries of User: ", ownerUser.getName());

      //printAllUsersAndHisCreatedItineraries();

      return tokens[1];

    } else if (tokens[0].equalsIgnoreCase("GET")) {
      // The Token[1] is the Random unique ID generated. Should it be int or string?

      // If Itinerary is not in the store
      if (!this.keyValueStore.containsKey(tokens[1])) {
        logger.debug(true, "Itinerary Key : ", tokens[1], " not found in the store.");
        return "Itinerary Not found";
      }

      Itinerary itinerary = this.keyValueStore.get(tokens[1]);
      User ownerUser = this.userDatabase.fetchUser(itinerary.getCreatedBy());

      // Code for No Authorization
      /*
      logger.debug(true, "Found Itinerary Key : ", tokens[1],
          "and Itinerary Name Value : ", this.keyValueStore.get(tokens[1]).getName());
      return this.keyValueStore.get(tokens[1]).toString();
      */

      // If the Itinerary is created by owner OR
      // whether the current user is in the list of shared user of itinerary
      // This if for Authorization

      if (ownerUser.getEmailId().equals(clientEmailId)
          || itinerary.getListOfSharedWithUsers().contains(clientEmailId)) {

        logger.debug(true, "Found Itinerary Key : ", tokens[1],
            "and Itinerary Name Value : ", this.keyValueStore.get(tokens[1]).getName());

        return this.keyValueStore.get(tokens[1]).toString();
      } else if (ownerUser.getMapOfSharedItineraries().containsKey(itinerary.getItineraryId())) {
        List<String> listOfTempUserEmailId = ownerUser
            .getMapOfSharedItineraries().get(itinerary.getItineraryId());

        if (listOfTempUserEmailId.contains(clientEmailId)) {
          logger.debug(true, "Found Itinerary Key : ", tokens[1],
              "and Itinerary Name Value : ", this.keyValueStore.get(tokens[1]).getName());

          return this.keyValueStore.get(tokens[1]).toString();
        }
      }

      logger.debug(true, "You're not the Owner of this itinerary or " +
          "You don't have access to it as a collaborator!");
      return "No Authorization Access";

    } else if (tokens[0].equalsIgnoreCase("EDIT")) {
      //  0       1
      // EDIT  123123213
      String itineraryId = tokens[1];

      // If No Itinerary found with key
      if (!keyValueStore.containsKey(tokens[1])) {
        logger.debug(true, "Itinerary Key : ", tokens[1], " not found in the store.");
        return "Itinerary Not found";
      }

      Itinerary itinerary = this.keyValueStore.get(itineraryId);
      User ownerUser = this.userDatabase.fetchUser(itinerary.getCreatedBy());
      //User currentUser = this.userDatabase.fetchUser(clientEmailId);

      logger.debug(true, "Found Itinerary Key : ", tokens[1],
          "and Itinerary Name Value : ", keyValueStore.get(tokens[1]).getName());

      // TODO - If the itinerary is in the createdList or SharedList of the current user, then only he will be able to edit it
      // Shares the current It Id to preserver the Unique Key ID for the updated Itinerary

      //printAllUsersAndHisCreatedItineraries();

      // Code for no authorization
      return "Update Itinerary Details|" + itineraryId;

      // TODO - Checking  for authorization
      // If the current user is owner of the itinerary
      // OR Itinerary contains the current user as collaborator
      // Then allow access to edit
      /*
      if (itinerary.getListOfSharedWithUsers().contains(clientEmailId)
          || currentUser.getListOfSharedItinerary().contains(itinerary.getItineraryId())
          || currentUser.getListOfCreatedItinerary().contains(itinerary.getItineraryId())
          || currentUser.getEmailId().equals(ownerUser.getEmailId())) { // TODO - Objects won't be equal so might need to use names

        // Access Granted
        logger.debug(true, "Found Itinerary Key : ", tokens[1],
            "and Itinerary Name Value : ", keyValueStore.get(tokens[1]).getName());

        // TODO - If the itinerary is in the createdList or SharedList of the current user, then only he will be able to edit it

        printAllUsersAndHisCreatedItineraries();

        return "Update Itinerary Details|" + itineraryId ;

      } else {
        // No Authorization
        logger.error(true, "User: ", currentUser.getName(),
            " does not have access to Itinerary: ", itinerary.getName());
        return "No Authorization access to Edit";
      }
      */

    } else if (tokens[0].equalsIgnoreCase("SHARE")) {
      //   0      1       2
      // SHARE|1223123|s@s.com

      if (tokens[2].length() > 40) {
        return "Invalid token input";
      }

      String itineraryId = tokens[1];
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

        Itinerary itinerary = this.keyValueStore.get(itineraryId);

        // Only the owner of the itinerary can share the itinerary with the shared user
        User ownerUser = this.userDatabase.fetchUser(itinerary.getCreatedBy());
        User sharedUser = this.userDatabase.fetchUser(sharedEmailId);

        // If current user (owner) wants to share the itinerary with himself, return can't do that
        if (ownerUser.getEmailId().equals(sharedEmailId)) {
          logger.debug(true, "Current User and Shared User are the same. Not proceeding.");
          return "Cannot share to own self.";
        }

        // TODO - While it is shared by 1 user, while 2nd user tries to learn, he uses share again
        //  and this time it is current user + shared user, so rejects!

        // TODO - Current user will throw null if accessed by a server which is not logged in by a user

        // Else, if user is found, send itinerary id to that user to listOfSharedIts
        // If user is found in db, then share the key with that user
        logger.debug(true, "User found with Email: ", tokens[2],
            " Sharing the Itinerary with: ", sharedUser.getName());

        // TODO - DO I even need this - for Authorization?
        User currentUser = this.userDatabase.fetchUser(clientEmailId);
        System.out.println("CURRENT USER IN SHARE: " + currentUser.getName());
        // If the current user is not the owner of the itinerary, he cannot share it with other users
        if (!currentUser.getEmailId().equals(ownerUser.getEmailId())) {
          logger.debug(true, "You're not the Owner of this itinerary, " +
              "so you can't share with other users");
          return "You're not the Owner of this itinerary, so you can't share it with other users!";
        }

        logger.debug(true, "Current User: ", currentUser.getName(),
            " Shared User: ", sharedUser.getName());


        // Updates Shared Users list of itineraries
        if (itinerary.getListOfSharedWithUsers().contains(sharedUser.getEmailId())) {
          logger.debug(true, "This Itinerary is already share with User: ",
              sharedUser.getName(), " Email: ", sharedUser.getEmailId());

          return "Itinerary is Already Shared";
        } else {

          // Adding the shared user to the itinerary created by owner
          itinerary.setListOfSharedWithUsers(sharedUser.getEmailId());

          // Setting list of shared user for the owner user, and shared itinerary for the shared users
          ownerUser.addSharedUserToMap(tokens[1], sharedUser.getEmailId());

          // Adding the current itinerary as the Shared itinerary of the shared user
          // So that the shared user can know to which itinerary he has access to
          sharedUser.setListOfSharedItinerary(tokens[1]);

          logger.debug(true, "Itinerary '", itinerary.getName(),
              "' successfully shared with User: ", sharedUser.getName());

          //printAllUsersAndHisCreatedItineraries();

          return "Itinerary Successfully Shared";
        }

      } catch (Exception e) {
        logger.error(true, "Exception occurred! Can't share with the specified User!");
        e.printStackTrace();
        return "Can't share with specified User";
      }

    } else if (tokens[0].equalsIgnoreCase("DELETE")) {
      // TODO - Authorization for Delete
      if (keyValueStore.containsKey(tokens[1])) {
        logger.debug(true, "Deleted key :", tokens[1], " from the store.");
        keyValueStore.remove(tokens[1]);
        return "Deleted";
      } else {
        logger.debug(true, "Key : ", tokens[1], " not found in the store. " +
            "Not deleting anything.");

        return "Key Not Found";
      }
    } else {
      // Requires Authorization
      // LIST
      if (clientEmailId == null) {
        logger.debug(true, "User is null, User not found");
        return "User Not found";
      }

      User currentClientUser = this.userDatabase.getUserDatabase().get(clientEmailId);

      if (tokens[1].equalsIgnoreCase("CREATED")) {

        // Map - Itineraries created by user, and list of shared users
        System.out.println("MAP: " + currentClientUser.getMapOfSharedItineraries().toString());
        System.out.println("List Size: " + currentClientUser.getListOfCreatedItinerary().size());

        //return printUserCreatedItineraryMap(currentClientUser.getMapOfSharedItineraries());
        return printListOfCreatedOrSharedItineraries(currentClientUser.getListOfCreatedItinerary());
      } else {
        // COLLAB
        return printListOfCreatedOrSharedItineraries(currentClientUser.getListOfSharedItinerary());
      }

    }

    //return "INVALID OPERATION - Under Development";
  }

  // Helper method to print list of created itineraries by the user
  private String printListOfCreatedOrSharedItineraries(List<String> listOfCreatedItinerary) {
    StringBuilder sb = new StringBuilder();

    for (String itineraryKeyId : listOfCreatedItinerary) {
      Itinerary itinerary = this.keyValueStore.get(itineraryKeyId);
      sb.append("Itinerary Id: ");
      sb.append(itineraryKeyId);
      sb.append(" Itinerary Name: ");
      sb.append(itinerary.getName());
      sb.append("   ");
    }

    return sb.toString();
  }

  // Can't use this due to null users in map
  private String printUserCreatedItineraryMap(Map<String, List<String>> mapOfSharedItineraries) {

    // {[asdasd: [s@s.com]], [asdasd: [s@s.com]]}
    StringBuilder sb = new StringBuilder();

    for (Map.Entry<String, List<String>> entry : mapOfSharedItineraries.entrySet()) {
      System.out.println();
      Itinerary itinerary = this.keyValueStore.get(entry.getKey());
      List<String> listOfEmailIds = entry.getValue();

      sb.append(itinerary.getName());
      sb.append(" - Shared with - ");

      for (String str : listOfEmailIds) {
        sb.append(str);
        sb.append("  ");
      }

      System.out.println();
    }

    return sb.toString();
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

  // This method uses the stored Itinerary and prints all the users who has access to that itinerary
  private void printAllUsersAndHisCreatedItineraries() {
    for (Map.Entry<String, Itinerary> e : this.keyValueStore.entrySet()) {
      System.out.println();
      String tripName = e.getValue().getName();
      System.out.println("Key: " + e.getKey() + ",  Value: " + tripName);
      System.out.println("User who created " + tripName + ": " + e.getValue().getCreatedBy());
      List<String> sharedUsers = e.getValue().getListOfSharedWithUsers();
      System.out.println("List of users with whom " + tripName + " is shared with: " +
          printUsersEmail(sharedUsers));
      System.out.println();
    }
  }

  private String printUsersEmail(List<String> sharedUsers) {
    StringBuilder stringBuilder = new StringBuilder();
    for (String userEmailId : sharedUsers) {
      stringBuilder.append(userEmailId);
      stringBuilder.append(" ");
    }

    return stringBuilder.toString();
  }

}
