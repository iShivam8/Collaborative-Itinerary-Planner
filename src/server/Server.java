package server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Server extends Remote {

  /**
   * Client can use this method as a part of the server stub. It allows the client to interact with
   * the KeyValueStore for the three operations of - PUT / GET / DELETE / EDIT / SHARE.
   *
   * @param inputMessage - Client's input message specifying the operation to execute.
   * @return - Response of the executed operation to the client.
   * @throws RemoteException
   */
  String executeOperation(String inputMessage) throws RemoteException;

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
}
