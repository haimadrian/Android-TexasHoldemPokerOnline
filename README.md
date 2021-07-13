# Android Project
Repository to work on the final project in Android course, HIT, year C, 2nd semester

# Video
Click on the image below in order to view it on YouTube  
[![Android Final Project (Texas Holdem app)](https://img.youtube.com/vi/pvrpi9yCU8E/0.jpg)](https://www.youtube.com/watch?v=pvrpi9yCU8E "Android Final Project (Texas Holdem app)")

# Note
You'll need Gradle and Lombok in order to open, and successfully run, the projects below

# Server
[Source](https://github.com/haimadrian/Android1/tree/main/Project/TexasHoldemServer)  
Server is written in java, using Spring Boot.  
REST API is wrritten in accordance to Spring MVC.  
Controllers handle the requests, which go to Services, that use Repository.  
User details, including profile image, are stored to My SQL (RDS) using Spring Data JPA.  
Users are authorized using JWT that the server generates after a successful authentication.  
Passwords are encrypted without being able to be decrypted. For security purposes.  
The certificate of the server is a self signed one.  
The server is built into Spring's bootJar, to make it easier to "install" it on a server.

# Common
[Source](https://github.com/haimadrian/Android1/tree/main/Project/TexasHoldemCommon)  
Common project contains all of the classes that are shared to both server and client: the model classes (settings, chat, user, player, etc.), and GameEngine, so we will be able to support a server managed game, and a local game (AI - In a future version)  
You may find important classes here, e.g.:
- [ThreadContextMap](https://github.com/haimadrian/Android1/blob/main/Project/TexasHoldemCommon/src/main/java/org/hit/android/haim/texasholdem/common/util/ThreadContextMap.java) - used to keep requesting player identifier, to make sure we hide all player hands except the requesting player. Thus avoid of revealing other player hands (which might be exposed to an http client)
- [GameEngine](https://github.com/haimadrian/Android1/blob/main/Project/TexasHoldemCommon/src/main/java/org/hit/android/haim/texasholdem/common/model/game/GameEngine.java) - the main engine of a game
- [HandRankCalculator](https://github.com/haimadrian/Android1/blob/main/Project/TexasHoldemCommon/src/main/java/org/hit/android/haim/texasholdem/common/model/game/rank/HandRankCalculator.java) - a calculator to give a rank for player hands, based on hand and board (board is optional), which hekps us to find the winner or winners in case of a draw, and the selected cards so a client can highlight them.
- [Pot](https://github.com/haimadrian/Android1/blob/main/Project/TexasHoldemCommon/src/main/java/org/hit/android/haim/texasholdem/common/model/game/Pot.java) - this class is used to keep players bet on every round basis, which helps us in splitting to side pots (when a player is ALL-IN, and there are other players left to play)

# Client
[Source](https://github.com/haimadrian/Android1/tree/main/Project/TexasHoldem)  
Client is written in Java for Android. HTTPS requests are sent using Retrofit. (See [TexasHoldemWebService](https://github.com/haimadrian/Android1/blob/main/Project/TexasHoldem/app/src/main/java/org/hit/android/haim/texasholdem/web/TexasHoldemWebService.java) that wraps our services)
There are two major activities: [Login](https://github.com/haimadrian/Android1/blob/main/Project/TexasHoldem/app/src/main/java/org/hit/android/haim/texasholdem/view/activity/LoginActivity.java) and [Main](https://github.com/haimadrian/Android1/blob/main/Project/TexasHoldem/app/src/main/java/org/hit/android/haim/texasholdem/view/activity/MainActivity.java)  
Another activity is the [ExitActivity](https://github.com/haimadrian/Android1/blob/main/Project/TexasHoldem/app/src/main/java/org/hit/android/haim/texasholdem/view/activity/ExitActivity.java) which is used for an ordinary shutdown of the application.  
And the [SplashActivity](https://github.com/haimadrian/Android1/blob/main/Project/TexasHoldem/app/src/main/java/org/hit/android/haim/texasholdem/view/activity/SplashActivity.java), which is used when the app is launched, to display an animation and redirect to the LoginActivity.  

The LoginActivity uses two fragments: [SignInFragment](https://github.com/haimadrian/Android1/blob/main/Project/TexasHoldem/app/src/main/java/org/hit/android/haim/texasholdem/view/fragment/login/SignInFragment.java) and [SignUpFragment](https://github.com/haimadrian/Android1/blob/main/Project/TexasHoldem/app/src/main/java/org/hit/android/haim/texasholdem/view/fragment/login/SignUpFragment.java). Those two fragments extend the AbstractSignInFragment, cause they both look the same, using two input fields, and a view model which is updated on every input change of the view. Thus following the MVVM pattern.  
Note that the JWT token is stored to **SharedPreferences**, so in case user exits the application and then comes back in, user is automatically signed in without having to retype the password.  

The MainActivity uses a NavigationView with a [MobileNavigation](https://github.com/haimadrian/Android1/tree/main/Project/TexasHoldem/app/src/main/res/navigation) that lists the items and their corresponding fragments in the navigation view.  With that said, all navigations between fragments are performed using the navigation view, and not creating fragments manually.  
Fragments related to the main activity are:
- [HomeFragment](https://github.com/haimadrian/Android1/blob/main/Project/TexasHoldem/app/src/main/java/org/hit/android/haim/texasholdem/view/fragment/home/HomeFragment.java)
- [PlayNetworkFragment](https://github.com/haimadrian/Android1/blob/main/Project/TexasHoldem/app/src/main/java/org/hit/android/haim/texasholdem/view/fragment/home/PlayNetworkFragment.java)
- [GameFragment](https://github.com/haimadrian/Android1/blob/main/Project/TexasHoldem/app/src/main/java/org/hit/android/haim/texasholdem/view/fragment/home/GameFragment.java)
- [ChatFragment](https://github.com/haimadrian/Android1/blob/main/Project/TexasHoldem/app/src/main/java/org/hit/android/haim/texasholdem/view/fragment/chat/ChatFragment.java)
- [AboutFragment](https://github.com/haimadrian/Android1/blob/main/Project/TexasHoldem/app/src/main/java/org/hit/android/haim/texasholdem/view/fragment/AboutFragment.java)  
The parent class of all fragments is [ViewBindedFragment](https://github.com/haimadrian/Android1/blob/main/Project/TexasHoldem/app/src/main/java/org/hit/android/haim/texasholdem/view/fragment/ViewBindedFragment.java) to implement a common usage of ViewBinding in android, and avoid of "findViewById" hell.  

There is also a [GameSoundService](https://github.com/haimadrian/Android1/blob/main/Project/TexasHoldem/app/src/main/java/org/hit/android/haim/texasholdem/view/GameSoundService.java) which we use to play sound effects in background. Thus letting a player to minimize the app, or switch to another app, and hear game steps (call/check/fold/timer) effects.  

Basically the game is managed at the server side, and the client asks for updates every second, to demo a "realtime" game.

# Screenshots
## Sign In
![SignIn](https://github.com/haimadrian/Android1/blob/main/Project/Resources/Screenshots/Signin.png)  

## Home
![Home](https://github.com/haimadrian/Android1/blob/main/Project/Resources/Screenshots/Home.png)  

## Navigation View
Here the user can purchase chips (the + and cart button) and modify profile picture by clicking on it  
![NavigationView](https://github.com/haimadrian/Android1/blob/main/Project/Resources/Screenshots/NavigationView.png)  

## Create Game
![CreateGame](https://github.com/haimadrian/Android1/blob/main/Project/Resources/Screenshots/CreateGame.png)   

## Seat Selection
The arrows are animated from top to bottom (bouncy) to make it clear that the user should pick up a seat  
![SeatSelection](https://github.com/haimadrian/Android1/blob/main/Project/Resources/Screenshots/SeatSelection.png)  

## Admin View
![AdminView](https://github.com/haimadrian/Android1/blob/main/Project/Resources/Screenshots/AdminView.png)  

## Game Start
You can see a circle progress bar around the current player, which counts the time for a turn before it runs out of time and fold automatically  
![StartAndTimer](https://github.com/haimadrian/Android1/blob/main/Project/Resources/Screenshots/StartAndTimer.png)  

## Chat
![Chat](https://github.com/haimadrian/Android1/blob/main/Project/Resources/Screenshots/Chat.png)  

## Winner
Confetti animation that the winner sees. (And the winner only)  
![Winner](https://github.com/haimadrian/Android1/blob/main/Project/Resources/Screenshots/Winner.png)  

## Game Log
![GameLog](https://github.com/haimadrian/Android1/blob/main/Project/Resources/Screenshots/GameLog.png)  
