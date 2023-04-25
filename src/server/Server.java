package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
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
  String executeOperation(String inputMessage, User user) throws RemoteException;

  /**
   * Method that adds a new itinerary in the KeyValueStore after getting the trip details
   * from the client.
   *
   * @param itinerary - Itinerary object
   * @return - Response whether the object is successfully added or not
   * @throws RemoteException
   */
  String putItinerary(Itinerary itinerary) throws RemoteException;

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
}
