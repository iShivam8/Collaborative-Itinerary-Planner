package server.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model class for User.
 */
public class User implements Serializable {

  // Basic User details
  private final String name, emailId, password;

  // Created Itinerary that the user has created by himself
  // Shared Itinerary that other users have created, and shared with this user
  // List of Itinerary IDs
  private final List<String> listOfCreatedItinerary, listOfSharedItinerary;

  // Whether the current user is logged in or not
  private boolean isLoggedIn;

  // Current User can access, which itinerary is shared with which user email id
  // Itinerary ID + List of user email ids with whom the itinerary is shared with
  private final Map<String, List<String>> mapOfSharedItineraries;

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
  public void setListOfCreatedItinerary(String createdItineraryKeyId) {
    if (!this.listOfCreatedItinerary.contains(createdItineraryKeyId)) {
      this.listOfCreatedItinerary.add(createdItineraryKeyId);
    }
  }
  public List<String> getListOfCreatedItinerary() {
    return this.listOfCreatedItinerary;
  }

  // This if for the other user, with whom the itinerary is shared with, by the client
  // So If the current user wants to see, how many itineraries he has access to he can use this method
  // i.e. This User has access to These many Itineraries.
  public void setListOfSharedItinerary(String sharedItineraryId) {
    if (!this.listOfSharedItinerary.contains(sharedItineraryId)) {
      this.listOfSharedItinerary.add(sharedItineraryId);
    }
  }
  public List<String> getListOfSharedItinerary() {
    return this.listOfSharedItinerary;
  }

  // Method to add shared users to the map of current users
  // So that the current user can see, with whom who he has shared the specified itinerary
  // THIS IF FOR CURRENT USER - who is the owner of created itinerary
  synchronized public void addSharedUserToMap(String itineraryId, String sharedUserEmailId) {
    if (this.mapOfSharedItineraries.containsKey(itineraryId)) {
      List<String> existingUsersEmailId = this.mapOfSharedItineraries.get(itineraryId);
      existingUsersEmailId.add(sharedUserEmailId);
      this.mapOfSharedItineraries.replace(itineraryId, existingUsersEmailId);
    } else {
      List<String> tempListOfUserEmailId = new ArrayList<>();
      tempListOfUserEmailId.add(sharedUserEmailId);
      this.mapOfSharedItineraries.put(itineraryId, tempListOfUserEmailId);
    }
  }
  public Map<String , List<String>> getMapOfSharedItineraries() {
    return this.mapOfSharedItineraries;
  }
}