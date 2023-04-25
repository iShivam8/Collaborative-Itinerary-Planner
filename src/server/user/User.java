package server.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import server.itinerary.Itinerary;

/**
 * Model class for User.
 */
public class User implements Serializable {

  private final String name, emailId, password;

  // Created Itinerary that the user has created by himself
  // Shared Itinerary that other users have created, and shared with this user
  private final List<Itinerary> listOfCreatedItinerary, listOfSharedItinerary;

  private boolean isLoggedIn;

  // TODO - User should be able to see with whom he has shared the itineraries
  // TODO - The shared user must be able to retract the shared itinerary

  // Current User can access, which itinerary is shared with which users
  private final Map<Itinerary, List<User>> mapOfSharedItineraries;

  public User(String name, String emailId, String password) {
    this.name = name;
    this.emailId = emailId;
    this.password = password;
    this.listOfCreatedItinerary = new ArrayList<>();
    this.listOfSharedItinerary = new ArrayList<>();
    this.isLoggedIn = true;
    this.mapOfSharedItineraries = new HashMap<>();
  }

  public String getName() {
    return name;
  }

  public String getEmailId() {
    return emailId;
  }

  public String getPassword() {
    return password;
  }

  public boolean isLoggedIn() {
    return isLoggedIn;
  }

  public void setLoggedIn(boolean loggedIn) {
    isLoggedIn = loggedIn;
  }


  // This method is for current users (owner), who is the owner of itinerary
  // Current user can see how many itineraries he has created
  public void setListOfCreatedItinerary(Itinerary createdItinerary) {
    if (!this.listOfCreatedItinerary.contains(createdItinerary)) {
      this.listOfCreatedItinerary.add(createdItinerary);
    }
  }
  public List<Itinerary> getListOfCreatedItinerary() {
    return this.listOfCreatedItinerary;
  }



  // This if for the other user, with whom the itinerary is shared with, by the client
  // So If the current user wants to see, how many itineraries he has access to he can use this method
  // i.e. This User has access to These many Itineraries.
  public void setListOfSharedItinerary(Itinerary sharedItinerary) {
    this.listOfSharedItinerary.add(sharedItinerary);
  }
  public List<Itinerary> getListOfSharedItinerary() {
    return this.listOfSharedItinerary;
  }


  // Method to add shared users to the map of current users
  // So that the current user can see, with whom who he has shared the specified itinerary
  // THIS IF FOR CURRENT USER - who is the owner of created itinerary
  public void addSharedUserToMap(Itinerary itinerary, User sharedUser) {
    if (this.mapOfSharedItineraries.containsKey(itinerary)) {
      List<User> existingUsers = this.mapOfSharedItineraries.get(itinerary);
      existingUsers.add(sharedUser);
      this.mapOfSharedItineraries.put(itinerary, existingUsers);
    } else {
      List<User> tempListOfUser = new ArrayList<>();
      tempListOfUser.add(sharedUser);
      this.mapOfSharedItineraries.put(itinerary, tempListOfUser);
    }
  }
  public Map<Itinerary, List<User>> getMapOfSharedItineraries() {
    return this.mapOfSharedItineraries;
  }
}
