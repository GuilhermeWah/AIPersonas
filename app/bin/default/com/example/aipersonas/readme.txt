# AI Personas Chat Application

@hi guys,
I made this document to serve as a base, it will keep us on the same page.
All the things related to architecture and design can be found here.
@components, architecture being used, resources, packages ...
@let me know if you guys need anything.
@let's try to maintain a pattern. It will help us a lot while developing this project.
---> Thank you <-----

## Application Architecture
The project is built with an MVVM (Model-View-ViewModel) architecture to enhance code separation and scalability. Below are details about the implemented components, packages, and design patterns.

### Packages and Layers
1. **Activities**: This package contains all the activities of the application that define the various screens.
   - `MainActivity.java`: Displays all available personas to the user. Each persona can be selected to initiate a chat.
   - `ChatActivity.java`: Handles the chat UI, allowing users to exchange messages with personas.
   - `ChatListActivity.java`: Manages the list of chats for a specific persona, allowing users to see their existing chats.

2. **Adapters**: This package contains custom adapters used in RecyclerView components to display dynamic lists.
   - `PersonaAdapter.java`: Adapter to handle the list of personas. It binds each persona to the UI and handles click events.
   - `ChatAdapter.java`: Adapter to handle individual chat messages in the chat activity.
   - `ChatListAdapter.java`: Adapter for displaying the list of chats for each persona in the `ChatListActivity`.

3. **Models**: Contains data representation classes that define the structure of our data objects.
   - `Persona.java`: Defines the structure for the Persona object, including attributes like name, description, and ID.
   - `Chat.java`: Represents individual chats between a persona and a user.

4. **ViewModels**: Implements the logic behind the application and interacts with the repository for data handling.
   - `PersonaViewModel.java`: Manages the interaction between the PersonaRepository and the UI.
   - `ChatViewModel.java`: Manages data related to chats, ensuring consistency between the UI and the data source.

5. **Database**:
   - **Room Database**: Implements a local database for offline storage, ensuring users have access to their chats and personas when offline.
     - `PersonaDAO.java`: Data access object for CRUD operations on personas.
     - `ChatDAO.java`: Data access object for CRUD operations on chat data.
   - **Firebase Integration**: Firebase Firestore is used for cloud storage of chats and personas, providing persistence across different devices.

6. **Repositories**:
   - **PersonaRepository.java**: Handles data operations for personas, interacting with both Room and Firestore.
   - **ChatRepository.java**: Handles data operations for chats, interacting with both Room and Firestore.

7. **Utils**:
   - **ChatViewModelFactory.java**: Custom factory to instantiate `ChatViewModel` with parameters, specifically `personaId`.

### Design Patterns
- **MVVM (Model-View-ViewModel)**: The separation of concerns between UI (Activities), ViewModels (which manage UI-related data), and the Repository/DAO model has been implemented to make our application modular and easier to maintain.
- **Repository Pattern**: To abstract data sources, `PersonaRepository` and `ChatRepository` are used to provide data to the ViewModel from either Room Database or Firebase Firestore.
- **Adapter Pattern**: Used by `PersonaAdapter`, `ChatAdapter`, and `ChatListAdapter` to bridge between UI components (RecyclerView) and data.
- **Factory Pattern**: `ChatViewModelFactory` is used to pass `personaId` to `ChatViewModel`, which cannot be done directly in the default ViewModelProvider.

## Resources
### Layouts and Colors
- **Layouts**: The XML files for the UI components are designed with a consistent structure to create a unified user experience.
  - `activity_main.xml`: Contains the layout for the main activity, which shows a list of personas in a 3-column grid layout.
  - `item_persona_card.xml`: Defines the layout for each persona card in the RecyclerView, ensuring a clean and concise UI.
  - `activity_chat.xml`: Defines the layout for chat screens, with components such as user messages, persona messages, and input boxes.
  - `chat_list_activity.xml`: Defines the layout for displaying the list of chats for a persona, including a toolbar and a floating action button to add new chats.

- **Color Scheme**: The color scheme has been defined in `colors.xml` to match our branding requirements, emphasizing light backgrounds and contrasting text for readability.
  - Primary colors: `primaryBackground`, `headerTextColor`, `buttonTextColor`, etc.

## Completed Features
- **Firebase Authentication**: Sign-up and sign-in features have been fully integrated using Firebase Authentication.
- **MainActivity Implementation**: Displays available personas in a grid layout, allowing users to initiate a chat by selecting a persona.
- **Chat Activity**: Chat window layout and functionality for messaging personas.
- **Chat List Activity**: Lists all existing chats for a persona, allowing users to continue previous conversations.
- **Local Storage**: Implementation of Room Database for offline storage of chats and personas.
- **Dummy Data**: Currently, dummy personas have been added for testing the RecyclerView and UI flow.

## Work Yet to be Done
OK ==>  **Firebase Integration for Chat Storage**: Implement Firestore to save and retrieve conversations, allowing cross-device sync.
OK ==>  **Persona Creation Feature**: Allow users to create custom personas through a form in the UI.
------  **Search Chat Feature**: Implement a search functionality for existing chats.
------  **GTP API Integration**: Connect with OpenAI GPT to provide meaningful responses by personas during chat sessions.
OK ===> **Favorite/Delete Persona Feature**: Complete the implementation for long press actions on persona cards to favorite or delete.

## Database Architecture (Room)
Users (Collection)
└── UserID (Document - based on uid)
    ├── Personas (Sub-Collection)
    │    ├── PersonaID_1 (Document)
    │    └── PersonaID_2 (Document)
    └── Chats (Sub-Collection)
         ├── ChatID_1 (Document)
         └── ChatID_2 (Document)

## Database Architecture (Firestore) - Updated Nov 05
Firestore Root
└── Users (Collection)
    └── UserID (Document)
        ├── Personas (Sub-Collection)
        │   └── PersonaID (Document)
        │       ├── name: String
        │       └── description: String
        └── Chats (Sub-Collection)
            └── ChatID (Document)
                ├── chatId: String
                ├── title: String
                ├── timestamp: Timestamp
                └── Messages (Sub-Collection)
                    └── MessageID (Document)
                        ├── sender: String
                        ├── content: String
                        └── timestamp: Timestamp

## Dataflow Idea
### Suggested Adjustments for Better Integration:
#### Repository Adjustments:
- **Fetch Data Based on User ID**: Modify all repository classes (PersonaRepository, ChatRepository) to use Firebase Firestore with user-specific queries using the uid.
- **Save Data Based on User ID**: When adding new personas or chats, make sure they are stored under the current user’s document in Firestore.

#### ViewModel Updates:
- **User Scope Awareness**: Update PersonaViewModel and ChatViewModel so that they are aware of the current user context. The ViewModel should receive the
uid as a parameter or retrieve it using a shared mechanism (eg from `FirebaseAuth.getInstance().getCurrentUser().getUid()`).

#### Room Database:
- **Update Tables to Include uid**: Modify the Room tables (Persona, Chat) to include a userId field so that local caching respects different users.
- **Filter Data by User**: Ensure that local queries are filtered by userId so that only the authenticated user’s data is visible.

### Example Workflow with Firebase Authentication
- **User Signs In**: When the user signs in using Firebase Authentication, we retrieve their uid.
- **Data Flow with uid**:
  - The uid is passed to the Repository, which will handle the data access.
  - Firestore: When accessing Firestore, the uid is used to retrieve data belonging to that specific user.
  - Room Database: The local Room database should only cache data for the specific uid. Queries are filtered to display data related to the logged-in user.

### Persona and Chat Isolation
- Each user’s personas and chats are isolated from one another, ensuring privacy and a seamless user experience.

## Recent Progress
### 0. Interface Progress
We are almost done with the entire project's interface. We gathered all the resources we need and developed the activities and layouts. We are following the color pattern we described earlier in this document, but we still have to implement animations.

### 1. Updating the Persona Model
- We made some important changes to how our personas are managed: Instead of using an integer for the `personaId`, we switched to a unique string ID using a UUID, making our system more compatible with both Room and Firebase Firestore.
- We ensured the model works ok between Room and Firestore, automatically generating IDs in both constructors.

### 2. Handling Database Issues
- **Schema Issues**: We faced some headaches with Room's database schema, specifically around primary keys and "not null" constraints. To fix it, we swapped the `personaId` to a UUID, which solved the data integrity problems.
- **Keeping Room Updated**: We updated the database version to reflect the new schema changes. It took a bit of digging, but now Room and Firestore are on the same page.

### 3. Long-Click Features for Personas
- **Delete Persona**: We can now delete a persona from both Room and Firestore at the same time. Users get immediate feedback (a toast message) confirming whether the deletion was successful or not (using interface callback).
- **Favorite Persona**: We also added a favorite option, though right now it's more of a placeholder until we build out that feature fully.

### 4. Enhanced the UI
- **Persona RecyclerView**: There was a weird bug where both the persona name and description were the same, but we fixed that so they display correctly now.
- **Chat Creation Modal**: We added a pop-up modal for when users click "Create Chat." This modal prompts them for the persona's name and description, which will eventually be used for GPT-generated responses.
- **Chat Activity Layout**: Made more dynamic to update persona details properly.

### 5. Syncing Local and Cloud Data
- We made sure data retrieval from both the Room database and Firestore works seamlessly.
- **Write Syncing**: Whether we're adding, updating, or deleting a persona or a chat, those changes are now properly reflected in both Room and Firestore.

### 6. Handling Errors
- **Null Pointer Exceptions**: Fixed an error in `ChatActivity` that occurred when trying to access UI components before they were ready.
- **LiveData Not Updating**: Ensured our LiveData observer in the UI was doing its job. Now, when the data changes, the screen responds accordingly.

### 7. Chat Creation Flow
- Users can now click on a persona and initiate a chat. The persona’s name and ID are properly passed into `ChatActivity`.
- Updated the Chat model to make sure it has everything it needs—like the persona title and ID.
- Added a basic check to make sure users can only create a chat if all the required persona details are provided.

### 8. Firestore Console Manual Set Up
- We set up our collections and subcollections on Firestore. We also found that the userId generated by Firebase Authentication was not being
saved on Firestore automatically. The reason for that was mainly because the function `handleSignUp()` was not invoking `FirebaseFirestore.getInstance().collection()`...
- The collections and subcollections are now working properly. Whenever an account is created, the userId from Authentication is stored on Firestore, as well as the chats and messages.

### 05/11/2024: Starting the Testing Phase
- ChatList Initialization**: In our previous version, the chatList (`ChatAdapter`) was not explicitly initialized, which could lead to null pointer exceptions if the list was accessed before being assigned. Now our chatList is initialized to a new `ArrayList<>()`.
- Clearing and Adding New Data**: Instead of just assigning the new list, we are now clearing the existing list and adding all the new items to it, preventing issues related to reference changes since we are dealing with updates from both Room and Firestore.
- Delete All Chats for User**: Implemented `deleteAllChatsForUser(String userId)` on `ChatDAO`. This will clear all chats associated with a user whenever needed, which is essential for syncing with Firestore.
- UUID for Firestore**: Firebase doesn’t support autogeneration of IDs like Room does. Because of that, we use UUID to generate a unique identifier. Example: `this.chatId = UUID.randomUUID().toString();`
- **ChatID Bug in Firestore**: We were facing a bug with `ChatID` when storing in Firestore because it was set as an Integer. Now, it is correctly changed to a String in the chat model.

### Big Bug: Nov 05
We spent one day trying to figure out why we were getting the following error on LogCat:
"Failed to get service from broker. (Ask Gemini) java.lang.SecurityException: Unknown calling package name 'com.google.android.gms'."
- This error appeared after adding a new functionality in `ChatListActivity` and changing the Firebase Firestore structure. The issue was that old users didn't have the new collections.
- We need a strategy to handle future Firestore updates to ensure old users are updated accordingly.
- Note: I thought I had it fixed, however still getting it. Note: It does not crash our app, but am still investigating it.


### Nov 08 Updates
- **Architecture Changes**: We have updated our architecture to introduce a one-to-many relationship where one persona can have multiple chats. This led to the creation of the `ChatListActivity`, which allows users to manage different chats for a given persona.
- **Database Schema Updates**: The database schema has been updated to reflect the one-to-many relationship. Now, each persona can have multiple associated chats, and these changes are consistent across both Room and Firestore.
- **ChatList Activity**: Implemented `ChatListActivity` to manage chats for each persona. The new structure supports adding, listing, and selecting individual chats associated with a persona.
- **ViewModel Changes**: We added a `ChatViewModelFactory` to properly pass `personaId` to the `ChatViewModel`. This ensures that the ViewModel is aware of the specific persona context.
- **Error Handling with ViewModel Initialization**: We had to use a `ViewModelFactory` because Android's default ViewModelProvider doesn't support constructors with parameters like `personaId`. This change was necessary to ensure that the correct persona context is always available in the chat-related activities.

Here our db structure as of Nov 8th:

 room dbv:
 User (userId PK)
   └─ Persona (personaId PK, userId FK)
       └─ Chat (chatId PK, personaId FK, userId FK)
           └─ Message (messageId PK, chatId FK)

 Users (Collection)
   └─ UserID (Document)
       ├─ Personas (Sub-Collection)
       │    └─ PersonaID (Document)
       └─ Chats (Sub-Collection)
            └─ ChatID (Document)
                └─ Messages (Sub-Collection)

My last commit :  08/11/2024   (Gui)

### Recent Progress

### 0. Database Structure Updates
Guys, we’ve made some important changes to our database structure to ensure consistency and scalability.

**Firestore Structure**:
- I have decided to nest chats under their respective personas. Here’s how it looks now:

Users (Collection)
└── UserID (Document)
    ├── Personas (Sub-Collection)
    │    └── PersonaID (Document)
    │        └── Chats (Sub-Collection)
    │             └── ChatID (Document)




Ours chats chats are now explicitly nested under their respective personas.
Found it a better solution, it aligns better with the application guys.
The only problem would be searching for a specific chat. It will require
us to traverse multiple subcollections. Requires a lot of computational resource.
We can use the feature to search a specific chat inside persona chats; and restrict this search
to Chats subcollection inside each persona.

- This approach ensures a clear one-to-many relationship where a user can have multiple chats with the same persona.

**Room Database Schema**:
- The `chat_table` was updated to include:
- `userId`: Links chats to the authenticated user.
- `personaId`: Links chats to their associated persona.
- Now Room mirrors Firestore, making our architecture more consistent:


---

### 1. User Model Updates
- We added the `avatarUrl` attribute to the `User` model. This will support future functionality for profile pictures.
- Made sure all fields (`userId`, `email`, `name`) are initialized correctly when working with both Room and Firestore.
- With these changes, the `User` model is now ready for features like account customization.

---

### 2. Chat and Persona Integration
- Redesigned how chats and personas work together:
- Chats are now nested under personas in Firestore.
- Updated `ChatRepository` and `PersonaRepository` to handle this new nested structure.
- Added methods to fetch chats specific to a persona. This improves data isolation and makes both Room and Firestore easier to manage.

---

### 3. Firestore and Sync Enhancements
- We fixed mismatched data types—e.g., `ChatID` is now a `String` for Firestore compatibility.
- Solved an issue where older users didn’t have the updated Firestore subcollections:
- The app now creates any missing subcollections automatically when a user logs in.
- Syncing between Room and Firestore is now seamless, ensuring data consistency during persona and chat operations.

---

### 4. UI and Functional Improvements
**Bottom Navigation Menu**:
- Menu labels are now permanently visible to improve user accessibility.
- Fixed some transition issues for smoother navigation between screens.

**MainActivity Enhancements**:
- Integrated `UserViewModel` to display user-specific data like usernames.
- Fixed layout inconsistencies to ensure a better experience on different devices.

**Create Chat Modal**:
- Improved input validation to prevent users from creating chats with invalid or missing persona details.

---

### 5. Addressed Critical Bugs
**SQLite NOT NULL Constraint**:
- Resolved crashes caused by `NOT NULL` constraints in the `user_table` when inserting data.
- Added default values for optional fields like `avatarUrl` to prevent errors.

**LiveData Updates**:
- Fixed an issue where UI components didn’t respond to data changes in real-time.

**Firestore Write Errors**:
- Fixed schema mismatch problems causing Firestore write failures, especially for older user accounts.

---

### 6. Key Takeaways
- Nesting chats under personas in Firestore simplifies our data organization and aligns better with the app’s overall structure.
- While searching for specific chats across personas now requires traversing multiple subcollections, this approach makes sense for scalability.
- For now, searching will be limited to persona-level queries, which is less resource-intensive and easier to manage.

Updated Firestore Structure:





## Contributors
- Guilherme Miranda Falcão (Developer & Project Manager)
- Anastasia (Developer)
- Aryan (Developer)
