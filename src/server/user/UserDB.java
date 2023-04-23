package server.user;

import java.util.concurrent.ConcurrentHashMap;
import logs.Logger;

public class UserDB {

  // User Database with Key: Email, and Value: User object
  private final ConcurrentHashMap<String, User> userDatabase;
  private final Logger logger;

  public UserDB(String fileName, String serverId) {
    this.userDatabase = new ConcurrentHashMap<>();
    this.logger = new Logger(fileName, serverId);
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
        return "Valid Input Signup";
      } else {
        logger.error(true, "Invalid SignUp Input! Name & Password should be less " +
            "than 100 Characters. And Email should be valid!");
        return "Invalid Signup input. Signup requires 3 parameters: Name, Email, Password";
      }
    } else if (tokens.length == 2) {
      //Login
      if (validateEmail(tokens[0]) && tokens[1].length() <= 100) {
        return "Valid Input Login";
      } else {
        logger.error(true, "Invalid Login Input! Password should be less " +
            "than 100 Characters. And Email should be valid!");
        return "Invalid Login input. Login requires 2 parameters: Email, Password";
      }
    }

    logger.error(true, "Invalid number of operands in the request.");
    return "Invalid Request - Only SignUp or Login operations are supported.";
  }

  // TODO - Helper method to validate Email id
  private boolean validateEmail(String emailId) {
    return true;
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

      User user = userDatabase.get(email);

      if (user == null) {
        logger.debug(true, "No user exists with email: ", email, " Create a new " +
            "account or login with correct credentials");
        return "User not found";
      }

      if (user.getPassword().equals(password)) {
        // Sign in user: Correct Email, Password
        user.setLoggedIn(true);
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
   * Parse the tokens from the input message which is pipe separated.
   *
   * @param message - input message
   * @return - array containing tokens
   */
  String[] parseMessage(String message) {
    return message.split("\\|");
  }
}
