package server;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface for the Two Phase Commit Protocol that has a series of methods which follows
 * the 2PC protocol for a given transaction.
 */
public interface TwoPCServer extends Remote {

  /**
   * This method is used to connect this server with all the other instances of server.
   *
   * @param numberOfServerInstances - number of instances
   * @param port - port number for RMI registry
   */
  void connectWithAllServers(int numberOfServerInstances, int port) throws RemoteException;

  /**
   * This method is used to start the Two Phase Commit (2PC) protocol.
   * The main coordinator for the operation would be this server instance.
   *
   * @param operation - operation to be performed
   * @return - response after the operation is executed
   */
  String start2PC(String[] operation) throws RemoteException;

  /**
   * This method is used by the coordinator to take a vote by the participants,
   * whether they can commit the current transaction or not.
   *
   * @param transactionId - transaction ID of the operation for tracking the status of the operation
   * @param operation - operation to be performed
   * @param coordinatorId - the id of the coordinator
   * @return - true if vote is yes, false otherwise
   */
  boolean canCommit(String transactionId, String[] operation, String coordinatorId)
      throws RemoteException;

  /**
   * This method is used by the coordinator to make the participant commit to the transaction.
   *
   * @param transactionId - transaction id of the transaction to be committed
   * @return - response after the operation is executed after the transaction is committed
   */
  String doCommit(String transactionId) throws RemoteException;

  /**
   * This method is used by the participants on the coordinator to confirm whether the
   * transaction has been committed or not.
   *
   * @param transactionId - transaction id for confirmation
   * @throws RemoteException
   */
  void haveCommitted(String transactionId) throws RemoteException;

  /**
   * This method is used by the coordinator to make the participant abort the transaction.
   *
   * @param transactionId - transaction id to be aborted
   */
  void abortTransaction(String transactionId) throws RemoteException;
}