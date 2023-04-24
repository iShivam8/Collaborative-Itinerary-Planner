package server.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import server.itinerary.Itinerary;

/**
 * Model class for User.
 */
public class User implements Serializable {

  private String name, emailId, password;

  // Created Itinerary that the user has created by himself
  // Shared Itinerary that other users have created, and shared with this user
  private List<Itinerary> listOfCreatedItinerary, listOfSharedItinerary;

  private boolean isLoggedIn;

  public User(String name, String emailId, String password) {
    this.name = name;
    this.emailId = emailId;
    this.password = password;
    this.listOfCreatedItinerary = new ArrayList<>();
    this.listOfSharedItinerary = new ArrayList<>();
    isLoggedIn = true;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmailId() {
    return emailId;
  }

  public void setEmailId(String emailId) {
    this.emailId = emailId;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public List<Itinerary> getListOfCreatedItinerary() {
    return this.listOfCreatedItinerary;
  }

  public void setListOfCreatedItinerary(Itinerary createdItinerary) {
    this.listOfCreatedItinerary.add(createdItinerary);
  }

  public List<Itinerary> getListOfSharedItinerary() {
    return this.listOfSharedItinerary;
  }

  public void setListOfSharedItinerary(Itinerary sharedItinerary) {
    this.listOfSharedItinerary.add(sharedItinerary);
  }

  public boolean isLoggedIn() {
    return isLoggedIn;
  }

  public void setLoggedIn(boolean loggedIn) {
    isLoggedIn = loggedIn;
  }
}
