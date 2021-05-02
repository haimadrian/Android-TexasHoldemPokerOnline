package org.hit.android.server.calc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * @author Haim Adrian
 * @since 11-Apr-21
 */
public class Main {

   public static void main(String[] args) {
      String response;

      try (Scanner inFromUser = new Scanner(System.in);
           Socket clientSocket = new Socket("localhost", 1234)) {

         System.out.println("Enter mail:");
         String mail = inFromUser.nextLine().trim();
         System.out.println("Enter password:");
         String pwd = inFromUser.nextLine().trim();
         System.out.println("Enter full name:");
         String fullName = inFromUser.nextLine().trim();
         try (BufferedWriter outToServer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
              BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            outToServer.write("{ \"actionType\": \"CREATE_USER\", \"dynamicValue\": \"" + mail + "##" + pwd + "##" + fullName + "\" }\n\n");
            outToServer.flush();

            response = inFromServer.readLine();
            System.out.println("Result: " + response);
            outToServer.write("{ \"actionType\": \"DISCONNECT\" }\n\n");
            outToServer.flush();
         }
      } catch (Exception e) {
         System.err.println("Error has occurred: " + e);
         e.printStackTrace();
      }
   }
}

