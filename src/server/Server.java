package server;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;
import server.itinerary.Itinerary;
import server.user.User;
import server.user.UserDB;

public interface Server extends Remote {

  /**
   * Client can use this method as a part of the server stub. It allows the client to interact with
   * the KeyValueStore for the three operations of - PUT / GET / DELETE / EDIT / SHARE.
   *
   * @param inputMessage - Client's input message specifying the operation to execute.
   * @return - Response of the executed operation to the client.
   * @throws RemoteException
   */
  String executeOperation(String inputMessage) throws IOException,
      ClassNotFoundException;

  /**
   * Method that adds a new itinerary in the KeyValueStore after getting the trip details
   * from the client.
   *
   * @param itinerary - Itinerary object
   * @return - Response whether the object is successfully added or not
   * @throws RemoteException
   */
  String putItinerary(Itinerary itinerary) throws IOException, ClassNotFoundException;

  /**
   * Method used to allow the client to create a new account.
   *
   * @param signupInfo - Name, Email, Password
   * @return - Response whether the account is created or not
   * @throws RemoteException
   */
  String signUp(String signupInfo) throws RemoteException;

  /**
   * Method to login to an old account.
   *
   * @param loginInfo - Email, Password
   * @return - Whether the client is logged in or not
   * @throws RemoteException
   */
  String login(String loginInfo) throws RemoteException;

  /**
   * Method returns the current logged-in user.
   *
   * @return - User
   */
  User getUser(String emailId) throws RemoteException;

  UserDB getUserDB() throws RemoteException;

  Set<String> getSetOfLoggedInUsers() throws RemoteException;

  /**
   * Method to Log out the specified user from the server.
   *
   * @param emailId - Email id of the user to logout
   * @return - String specifying whether the user is logout or not
   */
  String logout(String emailId) throws RemoteException;
}
