package server.itinerary;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import server.user.User;

/**
 * Model class of the Itinerary.
 */
public class Itinerary implements Serializable {

  private String name, location, description;
  private Date startDate, endDate;

  // The user who created this itinerary
  private User createdBy;

  // List of users who the itinerary is shared with
  private List<User> listOfSharedWithUsers;

  // To keep a track of changes
  private int version;

  public Itinerary(String name, String location, Date startDate,
                   Date endDate, String description, User createdBy) {
    this.name = name;
    this.location = location;
    this.startDate = startDate;
    this.endDate = endDate;
    this.description = description;
    this.createdBy = createdBy;
    this.listOfSharedWithUsers = new ArrayList<>();
    this.version = 1;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public User getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(User createdBy) {
    this.createdBy = createdBy;
  }

  public List<User> getListOfSharedWithUsers() {
    return listOfSharedWithUsers;
  }

  public void setListOfSharedWithUsers(List<User> listOfSharedWithUsers) {
    this.listOfSharedWithUsers = listOfSharedWithUsers;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  @Override
  public String toString() {
    return "Itinerary Details:\n\n" +
        "Name: " + name + '\n' +
        "Location: " + location + '\n' +
        "Description: " + description + '\n' +
        "Start Date: " + startDate + '\n' +
        "End Date: " + endDate + '\n' +
        "Created by: " + createdBy.getName() + '\n' +
        "List of Users with whom this Itinerary is shared with: " + listOfSharedWithUsers.toString() + '\n' +
        "Version: " + version;
  }
}
