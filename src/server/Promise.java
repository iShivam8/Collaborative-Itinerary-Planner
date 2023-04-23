package server;

import java.io.Serializable;

/**
 * This class is used for tracking PAXOS metadata for each key.
 * Also used for prepare() method's response.
 */
public class Promise implements Serializable {

  // Sequence Numbers for a key if a proposal for that key is accepted or not
  private Long sequenceNumber, acceptedSequenceNumber;

  // Flag for checking whether the proposal for a key was accepted or not
  private Boolean accepted;

  // Accepted value for the key, if proposal was accepted and the status - Accepted / Promised / Rejected
  private String acceptedValue, status;

  public Promise() {
    this.sequenceNumber = (long) Integer.MIN_VALUE;
    this.acceptedSequenceNumber = null;
    this.accepted = false;
    this.acceptedValue = null;
    this.status = null;
  }

  public Long getSequenceNumber() {
    return sequenceNumber;
  }

  public void setSequenceNumber(Long sequenceNumber) {
    this.sequenceNumber = sequenceNumber;
  }

  public Long getAcceptedSequenceNumber() {
    return acceptedSequenceNumber;
  }

  public void setAcceptedSequenceNumber(Long acceptedSequenceNumber) {
    this.acceptedSequenceNumber = acceptedSequenceNumber;
  }

  public String getAcceptedValue() {
    return acceptedValue;
  }

  public void setAcceptedValue(String acceptedValue) {
    this.acceptedValue = acceptedValue;
  }

  public Boolean getAccepted() {
    return accepted;
  }

  public void setAccepted(Boolean accepted) {
    this.accepted = accepted;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Promise(Promise promise) {
    this.sequenceNumber = promise.getSequenceNumber();
    this.acceptedSequenceNumber = promise.getAcceptedSequenceNumber();
    this.accepted = promise.getAccepted();
    this.acceptedValue = promise.getAcceptedValue();
    this.status = promise.getStatus();
  }

  @Override
  public String toString() {
    return "Promise{" +
        "sequenceNumber = " + sequenceNumber +
        ", acceptedSequenceNumber = " + acceptedSequenceNumber +
        ", acceptedValue = '" + acceptedValue + '\'' +
        ", accepted = " + accepted +
        ", status = '" + status + '\'' +
        '}';
  }
}