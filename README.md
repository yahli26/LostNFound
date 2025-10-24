LostNFound - Full-Stack Android Application

LostNFound is a comprehensive, full-stack Android application built in Java, demonstrating a robust, feature-rich platform for community-based item recovery.

More than just a simple utility, this project showcases a sophisticated architecture integrating a real-time interactive Google Map, a powerful search and filtering system, and a complete two-sided system for posting 'lost' and 'found' items, all built on Object-Oriented principles.

It leverages a full Firebase backend (Cloud Firestore and Cloud Storage) for real-time data management and image hosting.

The application also features a complete, role-based administrative "Manager" system for user and content management, including moderation, advanced search capabilities, and a cascading delete function that removes a user, all their posts, and all their stored images, making it a truly impressive, large-scale mobile solution.

📋 Table of Contents

Core Features
User Features
Administrator (Manager) Features
Technology Stack
Core Data Model (OOP)
How It Works: Key Flows
Getting Started
Prerequisites
Firebase Setup

✨ Core Features

User Features

Dual Posting System: Users can create posts for items they've either lost or found, each with unique logic:

Lost Items: Can include an optional monetary tip to incentivize finders.

Found Items: Can include an optional security question (e.g., "What is the dog's name?") to verify the true owner before revealing contact details.

Interactive Map View: The main screen (Map_Activity) displays all relevant items on a Google Map with custom markers (Lost vs. Found).

Each item is visualized with a circle representing its set radius.

Clicking a marker opens a quick-view dialog of the item.

Dedicated Location Picker: A separate map activity (PointOnMap_Activity) provides a user-friendly way to pin a location and select a radius using a BubbleSeekBar, which visualizes the area as a circle.

Powerful List & Search: A dedicated activity (Show_List_Activity) provides two modes:

"Search" Mode: Browse all items posted by other users.

"My List" Mode: View, edit, or delete your own posts.

Advanced Filtering & Sorting: Users can filter the list by description, item type, or Lost/Found status. Results can be sorted by posting date or location (distance from user).

Secure Owner Verification:

For "Found" items with a security question (Item_To_Show_Activity), the creator's phone number is hidden.

A finder has three attempts to answer the question correctly.

If successful, the creator's phone number is revealed, and a "Call" button is enabled.

Image Upload: Users can upload an image of the item from their gallery or take a new photo with their camera (Create_Item_Activity), which is stored in Firebase Cloud Storage.

Item Reporting: Users can "report" inappropriate or spam posts (Item_To_Show_Activity), which increments a counter for administrator review.

Share Functionality: Users can share an item's details (including the image and a formatted message) to other social media or messaging apps.

🛡️ Administrator (Manager) Features

The app includes a separate "Manager" role (set via a boolean isManager flag in the user's Firebase document) with special privileges:

Full User Management: A dedicated Show_Users_Activity allows managers to:

View a complete list of all registered users from the Users collection.

Search for users by username or name.

Filter users by their role (Manager or Guest).

Sort users by their registration date or total upload count.

Cascading User Deletion:

Managers can delete any user from the system.

This action triggers a cascading delete (DeleteItemsOfUser): the app finds all items in Firestore posted by that user, deletes each item's associated image from Firebase Cloud Storage, and then deletes all the item documents from Firestore before finally deleting the user document itself.

Complete Content Moderation:

Managers can view user-submitted report counts on any item.

Managers can clear the report count for an item after review (ClearReports).

Managers can delete any item post from any user (deleteAlert).

Advanced Search & Sorting: In the main list view (Show_List_Activity), managers have additional sorting options, including sorting by report count or upload date.

Full Data Visibility: Managers bypass the security question on "Found" items and can immediately see all user details (phone, email, name) associated with any post.

💻 Technology Stack

Language: Java

Platform: Android (Native)

Backend (BaaS): Firebase

Database: Cloud Firestore for real-time data storage of user and item information (Users and BaseLostFound collections).

Storage: Firebase Cloud Storage for hosting user-uploaded item images.

APIs & Libraries:

Google Maps SDK for Android: For the core interactive map functionality.

Google Play Services (Location): For fetching the user's current location.

Picasso: For asynchronous image loading, caching, and display.

com.xw.repo.BubbleSeekBar: A custom UI component for selecting the location radius.

📦 Core Data Model (OOP)

The project's data model is built on Object-Oriented principles, using abstraction and inheritance to manage item data cleanly.

// Abstract parent class storing all common fields
public abstract class BaseLostFound  {
    protected String LF; // "L" or "F" to distinguish in Firestore queries
    protected String type;
    protected String description;
    protected GeoPoint location;
    protected int radius;
    protected String creatorUsername;
    protected String date; // Date item was lost/found
    protected String dateUploaded; // Date item was posted
    protected String img; // URL from Firebase Storage
    protected boolean relevant;
    protected int reports;
    protected String documentId; // Firestore document ID
    
    // ...getters and setters
}

// Concrete implementation for Lost items
public class Lost extends BaseLostFound {
    protected int tip; // The tip that the finder will get
    
    // ...getters and setters
}

// Concrete implementation for Found items
public class Found extends BaseLostFound {
    protected String securityQustation;
    protected String securityAnswer;
    
    // Method to check the user's answer
    public Boolean checkSecAnswer(String answer) { ... }
}


⚙️ How It Works: Key Flows

Creating an Item: A user taps "Create Lost" or "Create Found". They are taken to Create_Item_Activity. They fill out the form, use PointOnMap_Activity to select a location/radius, and (optionally) add an image. On "Upload," the image is sent to Cloud Storage. Once the image URL is returned, all data (including the URL) is saved as a new Lost or Found document in the BaseLostFound collection in Firestore.

Editing an Item: A user goes to "My List," long-presses an item, and is taken to Create_Item_Activity, which populates all fields with the existing item's data. When saved, the existing document is overwritten. If the image is changed, the original image is deleted from Cloud Storage before the new one is uploaded.

Viewing an Item (Finder): A user in "Search" long-clicks an item, opening it in Item_To_Show_Activity in view-only mode.

If it's a Lost item, the creator's phone number is visible to arrange the return and claim the tip.

If it's a Found item, the phone number is hidden, and the security question is shown. The user must answer correctly (within 3 tries) to unlock the contact information.

Deleting a User (Admin): An admin goes to Show_Users_Activity, finds a user, and clicks to delete. The app first queries Firestore for all items in BaseLostFound where creatorUsername matches the user being deleted. It iterates through these items, deleting each one's image from Cloud Storage (if one exists), and then deletes the item document itself. Finally, it deletes the user's document from the Users collection.

🏁 Getting Started

To get a local copy up and running, follow these simple steps.

Prerequisites

Android Studio (latest version)

A Google Account for Firebase

A Google Maps API Key

Firebase Setup

This project requires a Firebase backend to function.

Create a Firebase Project:

Go to the Firebase Console.

Click "Add project" and follow the setup steps.

Add Your Android App:

In your project's dashboard, click the Android icon to add an Android app.

Use the package name: com.gmail.yahlieyal.lostnfound (This must match the package name in the .java files).

Download the google-services.json file and place it in the app/ directory of this project.

Enable Firebase Services:

Firestore:

From the "Build" menu, select "Firestore Database."

Create a new database.

You must create two collections:

BaseLostFound: This collection will store all the Lost and Found item documents.

Users: This collection will store user data (e.g., firstName, phoneNumber, mail).

Storage:

From the "Build" menu, select "Storage."

Click "Get started" and follow the setup wizard.

Google Maps API:

You must enable the Google Maps SDK for Android in your Google Cloud Console and generate an API key.

This key should be added to your project's AndroidManifest.xml or local.properties file.

Set Up an Admin (Manager) User:

After running the app and registering a new user, go to your Firestore console.

Find your user's document in the Users collection.

Manually add a new field:

Field: isManager

Type: Boolean

Value: true

Relaunch the app. The app will now grant you manager privileges.
