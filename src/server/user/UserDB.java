package server.user;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import logs.Logger;

/**
 * This class acts as the User Database that stores: Key: User Email, Value: User object.
 */
public class UserDB {

  // User Database with Key: Email, and Value: User object
  private final ConcurrentHashMap<String, User> userDatabase;
  private final Logger logger;
  private final Set<String> loggedInUsersEmailId;

  public UserDB(String fileName, String serverId) {
    this.userDatabase = new ConcurrentHashMap<>();
    this.logger = new Logger(fileName, serverId);
    this.loggedInUsersEmailId = new HashSet<>();
  }

  /**
   * Method to validate SignUp or Login Input tokens.
   *
   * @param tokens - 3 tokens for Signup (Name, Email, Password); 2 for Login (Email, Password)
   * @return - Valid or Invalid operation
   */
  String validateTokens(String[] tokens) {

    if (tokens.length == 3) {
      // Signup
      if (tokens[0].length() <= 100 && validateEmail(tokens[1]) && tokens[2].length() <= 100) {
        return "Valid Input Signup. 2PC.";
      } else {
        logger.error(true, "Invalid SignUp Input! Name & Password should be less " +
            "than 100 Characters. And Email should be valid!");
        return "Invalid Signup input. Signup requires 3 parameters: Name, Email, Password";
      }
    } else if (tokens.length == 2) {
      //Login
      if (validateEmail(tokens[0]) && tokens[1].length() <= 100) {
        return "Valid Input Login. 2PC.";
      } else {
        logger.error(true, "Invalid Login Input! Password should be less " +
            "than 100 Characters. And Email should be valid!");
        return "Invalid Login input. Login requires 2 parameters: Email, Password";
      }
    }

    logger.error(true, "Invalid number of operands in the request.");
    return "Invalid Request - Only SignUp or Login operations are supported.";
  }

  // Helper method to validate Email
  private boolean validateEmail(String email) {
    // Regular expression for email validation
    String regex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
    return email.matches(regex);
  }

  /**
   * Method that navigates the incoming 2PC requests of Signup, Login, Logout.
   *
   * @param tokens - Client credentials
   * @return - Executed Response of the specified operation
   */
  synchronized String executeOperation(String[] tokens) {
    // 3 - signup,  2 - login,  1 - logout

    if (tokens.length == 3) {
      return signUpUser(tokens);
    } else if (tokens.length == 2) {
      return loginUser(tokens);
    } else {
      return logout(tokens[0]);
    }
  }

  /**
   * Method to create a new user in the User Database.
   *
   * @param tokens - Sign up details: Name, Email, Password
   * @return
   */
  synchronized String signUpUser(String[] tokens) {
    // While creating a new user, Email will be Key, User object would be value

    // Create a new user, and store in the user db with email as key
    try {
      String name = tokens[0];
      String email = tokens[1];
      String password = tokens[2];
      User user = new User(name, email, password);

      if (userDatabase.containsKey(email)) {
        // Can't create user, user already exist
        logger.debug(true, "Can't create new user, since a user with email: ",
            email, " already exists");
        return "User already Exists";
      } else {
        // Add the newly created user in User database
        userDatabase.put(email, user);

        // Added the current user in Logged-in User
        this.loggedInUsersEmailId.add(user.getEmailId());

        // TODO - Send the user to client

        logger.debug(true, "Successfully created a new user with Email: ", email);
        return "User Created";
      }
    } catch (Exception e) {
      logger.error(true, "Error creating a new user! " +
          "Please try again with valid inputs!");
    }

    logger.error(true, "Error creating new user!");
    return "Error creating new User!";
  }

  /**
   * Method that logs in the user with valid credentials.
   *
   * @param tokens - Email, Password
   * @return - Whether the user is successfully logged in or not
   */
  synchronized String loginUser(String[] tokens) {

    try {
      String email = tokens[0];
      String password = tokens[1];

      if (!userDatabase.containsKey(email)) {
        logger.debug(true, "User Not Found with Email: ", email);
        return "User Not Found";
      }

      User user = this.userDatabase.get(email);

      if (user == null) {
        logger.debug(true, "No user exists with email: ", email, " Create a new " +
            "account or login with correct credentials");
        return "User not found";
      }

      if (this.loggedInUsersEmailId.contains(email)) {
        logger.debug(true, "User ", user.getName(), " is already logged-in on " +
            "a different terminal. First logout then try to sign-in again!");
        return "User already Logged-in";
      }

      if (user.getPassword().equals(password)) {
        // Sign in user: Correct Email, Password
        user.setLoggedIn(true);
        this.userDatabase.get(email).setLoggedIn(true);

        logger.debug(true, "User found with Email: ", email, ", and Name: ", user.getName());
        return "User Logged in";
      } else {
        logger.debug(true, "Invalid Credentials! Please enter correct password " +
            "for Email: ", email);
        return "Invalid Credentials";
      }
    } catch (Exception e) {
      logger.error(true, "Error while logging in! " +
          "Please try again with valid inputs!");
    }

    logger.error(true, "Error while logging in!");
    return "Error while logging in";
  }

  /**
   * Method to log out the specified user.
   *
   * @param emailId - email id of the user
   * @return - Response of logout
   */
  synchronized String logout(String emailId) {
    if (!this.userDatabase.containsKey(emailId)) {
      logger.error(true, "User with Email: ", emailId, " Not found!");
      return "User Not Found";
    }

    User user = this.userDatabase.get(emailId);
    this.userDatabase.get(emailId).setLoggedIn(false);
    this.loggedInUsersEmailId.remove(emailId);
    logger.debug(true, "User: ", user.getName(), " Successfully Logged out!");
    return "User Successfully Logged Out!";
  }

  Set<String> getListOfLoggedInUsers() {
    return this.loggedInUsersEmailId;
  }

  /**
   * Method to fetch Specified User via their Email id.
   *
   * @param emailId - Email id of the user
   * @return - User object with specified email id
   */
  public User fetchUser(String emailId) {
    if (!this.userDatabase.containsKey(emailId)) {
      return null;
    }

    return this.userDatabase.get(emailId);
  }

  public ConcurrentHashMap<String, User> getUserDatabase() {
    return this.userDatabase;
  }

  /**
   * Parse the tokens from the input message which is pipe separated.
   *
   * @param message - input message
   * @return - array containing tokens
   */
  String[] parseMessage(String message) {
    return message.split("\\|");
  }
}
