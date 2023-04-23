package server.user;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class for User.
 */
public class User {

  private String name, emailId, password;

  // Created Itinerary that the user has created by himself
  // Shared Itinerary that other users have created, and shared with this user
  private List<Integer> listOfCreatedItinerary, listOfSharedItinerary;

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

  public List<Integer> getListOfCreatedItinerary() {
    return listOfCreatedItinerary;
  }

  public void setListOfCreatedItinerary(List<Integer> listOfCreatedItinerary) {
    this.listOfCreatedItinerary = listOfCreatedItinerary;
  }

  public List<Integer> getListOfSharedItinerary() {
    return listOfSharedItinerary;
  }

  public void setListOfSharedItinerary(List<Integer> listOfSharedItinerary) {
    this.listOfSharedItinerary = listOfSharedItinerary;
  }

  public boolean isLoggedIn() {
    return isLoggedIn;
  }

  public void setLoggedIn(boolean loggedIn) {
    isLoggedIn = loggedIn;
  }
}
