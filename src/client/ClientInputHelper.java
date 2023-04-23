package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Helper class for client to continuously fetch the user inputs.
 */
public class ClientInputHelper {

  static String fetchSignupOrLoginInput() {

    System.out.println("\nChoose from the following Requests: ");
    System.out.println("1] Sign Up for New Account: Enter - Signup");
    System.out.println("2] Log In your Account: Enter - Login");
    System.out.println("\nPlease enter your intended action: ");

    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

    try {
      String userInput = bufferedReader.readLine().trim();

      if (userInput.equalsIgnoreCase("Signup")) {
        return "SignUp";
      } else if (userInput.equalsIgnoreCase("Login")) {
        return "LogIn";
      }

    } catch (Exception e) {
      System.out.println("Exception Occurred! Incorrect Sign In Inputs! Please try again!");
    }

    return null;
  }

  /**
   * Method to fetch user sign up information: Name, Email, Password
   *
   * @return
   */
  static String fetchSignUpInput() {

    System.out.println("\nEnter the following Details to Register a New Account: ");
    System.out.println("1] Enter Your First Name only");
    System.out.println("2] Enter Your Email ID");
    System.out.println("3] Enter Your Password");
    System.out.println("\nPlease enter the above asked details: ");
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

    try {
      String userInput = bufferedReader.readLine();

      String[] array = userInput.split("\\s");
      StringBuilder stringBuilder = new StringBuilder();

      for (String str: array) {
        if (str != null && str.length() != 0) {
          stringBuilder.append(str).append("|");
        }
      }

      return stringBuilder.substring(0, stringBuilder.length() - 1);
    } catch (Exception e) {
      System.out.println("Exception Occurred while taking Sign Up input details! Please try again!");
    }

    return null;
  }

  /**
   * Method to take Login user input: Email & password.
   *
   * @return
   */
  static String fetchLoginInput() {
    System.out.println("\nEnter the following Credentials to Login: ");
    System.out.println("1] Enter Your Email ID");
    System.out.println("2] Enter Your Password");
    System.out.println("\nPlease enter the above asked details: ");

    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

    try {
      String userInput = bufferedReader.readLine();

      String[] array = userInput.split("\\s");
      StringBuilder stringBuilder = new StringBuilder();

      for (String str: array) {
        if (str != null && str.length() != 0) {
          stringBuilder.append(str).append("|");
        }
      }

      return stringBuilder.substring(0, stringBuilder.length() - 1);
    } catch (Exception e) {
      System.out.println("Exception Occurred while taking Login input details! Please try again!");
    }

    return null;
  }

  static String fetchUserOperationInput(boolean takeUserInput) {

    // Keep taking the user input unless the boolean value turns false
    if (takeUserInput) {
      System.out.println("\nChoose from the following Requests: ");
      System.out.println("1] Create a new Itinerary: PUT");
      System.out.println("2] Get an Itinerary:       GET  (Itinerary-ID)");
      System.out.println("3] Delete an Itinerary:    DELETE  (Itinerary-ID)");
      System.out.println("4] Edit an Itinerary:      EDIT (Itinerary-ID)");
      System.out.println("5] Share an Itinerary:     SHARE (Itinerary-ID) (Email id)");
      System.out.println("Enter X to exit the Application");
    }

    System.out.println("\nPlease enter your intended action: ");

    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

    try {
      String userInput = bufferedReader.readLine();

      // If the user decides to quit the application
      if (userInput.equalsIgnoreCase("X")) {
        return userInput;
      }

      String[] array = userInput.split("\\s");
      StringBuilder stringBuilder = new StringBuilder();

      for (String str: array) {
        if (str != null && str.length() != 0) {
          stringBuilder.append(str).append("|");
        }
      }

      return stringBuilder.substring(0, stringBuilder.length() - 1);
    } catch (Exception e) {
      System.out.println("Exception Occurred! Incorrect User Input! Please try again!");
      e.printStackTrace();
    }

    return null;
  }
}
