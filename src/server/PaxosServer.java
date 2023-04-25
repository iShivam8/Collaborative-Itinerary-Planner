package server;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface consists of methods for the Proposers and Acceptors, and the behavior
 * of the server that uses PAXOS for consensus.
 */
public interface PaxosServer extends Remote {

  /**
   * Method to connect this server with all other server replicas in the system.
   *
   * @param numberOfServers - number of server instances replicas
   * @param portNumber - port number used for RMI Registry
   * @throws RemoteException
   */
  void connectWithAllServers(int numberOfServers, int portNumber) throws RemoteException;

  /**
   * This method is used to start and initiate the PAXOS algorithm.
   * It is invoked when the server receives any PUT, DELETE, SHARE update operations.
   * And the server would then act as the proposer for the algorithm.
   *
   * @param inputTokens - input operation token of PUT, DELETE, SHARE operations
   * @return - Result of PUT and DELETE operation after PAXOS is completed.
   * @throws RemoteException
   */
  String startPaxos(String[] inputTokens, String operation) throws RemoteException;

  /**
   * Proposer uses this method for sending prepare(sequenceId) message of PAXOS.
   * This method is invoked on all the Acceptors by the Proposers.
   *
   * @param sequenceId - Proposer's Sequence number
   * @param key - Key value for which the PAXOS algorithm is currently running for
   * @return - true: if Acceptor accepts the proposal / false: if proposer rejects the proposal /
   *           null if Acceptor fails
   * @throws RemoteException
   */
  Promise prepare(long sequenceId, String key) throws RemoteException;

  /**
   * Proposer's uses this method for sending propose(sequenceId, proposedValue) message of PAXOS.
   * This method is invoked on all the Acceptors by the Proposers.
   *
   * @param sequenceId - Proposer's Sequence number
   * @param key - Key value for which the PAXOS algorithm is currently running for
   * @param value - Proposed value
   * @return - true: if Acceptor accepts the proposal / false: if proposer rejects the proposal /
   *           null if Acceptor fails
   * @throws RemoteException
   */
  Boolean propose(long sequenceId, String key, String value) throws RemoteException;

  /**
   * This method is used so all the Acceptors can learn the given value and commit their updates.
   *
   * @param key - Key that need to be updated
   * @param value - Value that need to be updated
   * @return - Result of PUT and DELETE operations when the commit actually occurs.
   * @throws RemoteException
   */
  String learn(String key, String value, String operation) throws RemoteException;
}