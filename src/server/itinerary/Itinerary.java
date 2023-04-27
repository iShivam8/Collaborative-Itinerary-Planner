package server.itinerary;

import static server.keyvaluestore.UniqueIdGenerator.generateId;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Model class of the Itinerary.
 */
public class Itinerary implements Serializable {

  private final String itineraryId;
  private String name, location, description;
  private Date startDate, endDate;

  // The user email who created this itinerary
  private String createdBy;

  // List of users email id with whom this itinerary is shared with
  private List<String> listOfSharedWithUsers;

  // To keep a track of changes
  private int version;
  private String prevItineraryId;

  public Itinerary(String name, String location, Date startDate,
                   Date endDate, String description, String createdBy) {
    this.itineraryId = generateId();
    this.name = name;
    this.location = location;
    this.startDate = startDate;
    this.endDate = endDate;
    this.description = description;
    this.createdBy = createdBy;
    this.listOfSharedWithUsers = new ArrayList<>();
    this.version = 0;
    this.prevItineraryId = null;
  }

  public String getItineraryId() {
    return this.itineraryId;
  }

  public String getPrevItineraryId() {
    return this.prevItineraryId;
  }

  public void setPrevItineraryId(String prevItineraryId) {
    this.prevItineraryId = prevItineraryId;
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

  // Returns the email id of the user who created this itinerary
  public String getCreatedBy() {
    return this.createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public List<String> getListOfSharedWithUsers() {
    return this.listOfSharedWithUsers;
  }

  // Owner of the itinerary cannot add himself in this list
  public void setListOfSharedWithUsers(String sharedUserEmail) {
    if (!sharedUserEmail.equals(this.createdBy)) {
      if (!this.listOfSharedWithUsers.contains(sharedUserEmail)) {
        this.listOfSharedWithUsers.add(sharedUserEmail);
      }
    }
  }

  // This method updates the current list of shared user in the UPDATE call
  public void updateListOfSharedUsers(List<String> listOfSharedWithUsers) {
    this.listOfSharedWithUsers = listOfSharedWithUsers;
  }

  public int getVersion() {
    return version;
  }

  public void updateVersion() {
    this.version += 1;
  }

  public void setVersion(int newVersion) {
    this.version = newVersion;
  }

  @Override
  public String toString() {
    return "Itinerary Details:\n\n" +
        "Name: " + name + '\n' +
        "Location: " + location + '\n' +
        "Description: " + description + '\n' +
        "Start Date: " + startDate + '\n' +
        "End Date: " + endDate + '\n' +
        "Created by: " + createdBy + '\n' +
        "List of Users with whom this Itinerary is shared with: "
        + listOfSharedWithUsers + '\n' +
        "Version: " + version + '\n';
  }
}
