package server.user;

import java.rmi.RemoteException;
import logs.Logger;
import server.Server;

public class UserDBServer implements Server {

  private final UserDB userDB;
  private final String serverId;
  private final Logger logger;

  public UserDBServer(String serverId) {
    this.userDB = new UserDB("src/logs/userDB.log", serverId);
    this.serverId = serverId;
    this.logger = new Logger("src/logs/userDatabase_" + serverId + ".log", serverId);
  }

  @Override
  public String executeOperation(String inputMessage) throws RemoteException {
    return null;
  }

  @Override
  public String signUp(String signupInfo) throws RemoteException {
    logger.debug(true, "SignUp Info received from the Client: ", signupInfo);

    String result = null;
    String[] tokens = userDB.parseMessage(signupInfo);
    String validatedResponse = userDB.validateTokens(tokens);

    if (validatedResponse.startsWith("Invalid")) {
      result = validatedResponse;
    } else if (validatedResponse.contains("Valid Input")) {
      result = userDB.signUpUser(tokens);
    }

    logger.debug(true, "Sending response message to the Client: ", result);
    return result;
  }

  @Override
  public String login(String loginInfo) throws RemoteException {
    logger.debug(true, "Login Info received from the Client: ", loginInfo);

    String result = null;
    String[] tokens = userDB.parseMessage(loginInfo);
    String validatedResponse = userDB.validateTokens(tokens);

    if (validatedResponse.startsWith("Invalid")) {
      result = validatedResponse;
    } else if (validatedResponse.contains("Valid Input")) {
      result = userDB.loginUser(tokens);
    }

    logger.debug(true, "Sending response message to the Client: ", result);
    return result;
  }
}
