package org.hit.android.server.calc;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

/**
 * @author Haim Adrian
 * @since 11-Apr-21
 */
public class Main {

   public static void main(String[] args) {
      String sentence;
      String modifiedSentence;

      try (Scanner inFromUser = new Scanner(System.in);
           Socket clientSocket = new Socket("localhost", 1234)) {

         try (DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
              BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

            do {
               System.out.println("Enter expression to solve: ");
               sentence = inFromUser.next();

               outToServer.writeBytes(sentence + '\n');

               modifiedSentence = inFromServer.readLine();
               System.out.println("Result: " + modifiedSentence);
            } while (!sentence.equalsIgnoreCase("bye"));
         }
      } catch (Exception e) {
         System.err.println("Error has occurred: " + e.toString());
         e.printStackTrace();
      }
   }
}

