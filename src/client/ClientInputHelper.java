package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Helper class for client to continuously fetch the user inputs.
 */
public class ClientInputHelper {

  static String fetchUserLoginInput(boolean takeUserInput) {

    // Keep taking the user input unless the boolean value turns false
    if (takeUserInput) {
      System.out.println("\nChoose from the following Requests: ");
      System.out.println("1] Sign Up for New Account: Enter - Signup");
      System.out.println("2] Log In your Account: Enter - Login");
      System.out.println("Enter X to exit the Application");
    }

    System.out.println("\nPlease enter your intended action: ");

    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

    try {

      String userInput = bufferedReader.readLine().trim();

      // If the user decides to quit the application
      if (userInput.equalsIgnoreCase("X")) {
        return userInput;
      } else if (userInput.equalsIgnoreCase("Signup")) {
        return "SignUp";
      } else if (userInput.equalsIgnoreCase("Login")) {
        return "LogIn";
      }

    } catch (Exception e) {
      System.out.println("Exception Occurred! Incorrect Sign In Inputs! Please try again!");
    }

    return null;
  }

  static String fetchUserInput(boolean takeUserInput) {

    // Keep taking the user input unless the boolean value turns false
    if (takeUserInput) {
      System.out.println("\nChoose from the following Requests: ");
      System.out.println("1] Add a new Itinerary:       PUT");
      System.out.println("2] Get an Itinerary:          GET  (Itinerary-ID)");
      System.out.println("3] Delete an Itinerary from the key-store:   DELETE  (Itinerary-ID)");
      System.out.println("4] Edit an Itinerary: EDIT (Itinerary-ID)");
      System.out.println("5] Share an Itinerary: SHARE (It-ID) (Email id)");
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
