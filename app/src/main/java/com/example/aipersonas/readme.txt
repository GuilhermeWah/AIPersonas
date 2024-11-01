# AI Personas Chat Application

## Project Overview
This project aims to create an AI-driven chat application that allows users to interact with a variety of virtual personas. Each persona specializes in a different field, such as cybersecurity, health, or travel planning. Users can initiate chats, create new personas, and revisit previous conversations, much like interacting with contacts in popular chat applications like WhatsApp or Telegram.

## Application Architecture
The project is built with an MVVM (Model-View-ViewModel) architecture to enhance code separation and scalability. Below are details about the implemented components, packages, and design patterns.

### Packages and Layers
1. **Activities**: This package contains all the activities of the application that define the various screens.
   - `MainActivity.java`: Displays all available personas to the user. Each persona can be selected to initiate a chat.
   - `ChatActivity.java`: Handles the chat UI, allowing users to exchange messages with personas.

2. **Adapters**: This package contains custom adapters used in RecyclerView components to display dynamic lists.
   - `PersonaAdapter.java`: Adapter to handle the list of personas. It binds each persona to the UI and handles click events.

3. **Models**: Contains data representation classes that define the structure of our data objects.
   - `Persona.java`: Defines the structure for the Persona object, including attributes like name, description, and ID.
   - `Chat.java`: Represents individual chat messages between a persona and a user.

4. **ViewModels**: Implements the logic behind the application and interacts with the repository for data handling.
   - `PersonaViewModel.java`: Manages the interaction between the PersonaRepository and the UI.
   - `ChatViewModel.java`: Manages data related to chats, ensuring consistency between the UI and the data source.

5. **Database**:
   - **Room Database**: Implements a local database for offline storage, ensuring users have access to their chats and personas when offline.
     - `PersonaDAO.java`: Data access object for CRUD operations on personas.
     - `ChatDAO.java`: Data access object for CRUD operations on chat data.
   - **Firebase Integration**: Firebase Firestore is used for cloud storage of chats and personas, providing persistence across different devices.

### Design Patterns
- **MVVM (Model-View-ViewModel)**: The separation of concerns between UI (Activities), ViewModels (which manage UI-related data), and the Repository/DAO model has been implemented to make our application modular and easier to maintain.
- **Repository Pattern**: To abstract data sources, `PersonaRepository` and `ChatRepository` are used to provide data to the ViewModel from either Room Database or Firebase Firestore.
- **Adapter Pattern**: Used by `PersonaAdapter` to bridge between UI components (RecyclerView) and data.

## Resources
### Layouts and Colors
- **Layouts**: The XML files for the UI components are designed with a consistent structure to create a unified user experience.
  - `activity_main.xml`: Contains the layout for the main activity, which shows a list of personas in a 3-column grid layout.
  - `item_persona_card.xml`: Defines the layout for each persona card in the RecyclerView, ensuring a clean and concise UI.
  - `activity_chat.xml`: Defines the layout for chat screens, with components such as user messages, persona messages, and input boxes.

- **Color Scheme**: The color scheme has been defined in `colors.xml` to match our branding requirements, emphasizing light backgrounds and contrasting text for readability.
  - Primary colors: `primaryBackground`, `headerTextColor`, `buttonTextColor`, etc.

## Completed Features
- **Firebase Authentication**: Sign-up and sign-in features have been fully integrated using Firebase Authentication.
- **MainActivity Implementation**: Displays available personas in a grid layout, allowing users to initiate a chat by selecting a persona.
- **Chat Activity**: Chat window layout and functionality for messaging personas.
- **Local Storage**: Implementation of Room Database for offline storage of chats and personas.
- **Dummy Data**: Currently, dummy personas have been added for testing the RecyclerView and UI flow.

## Work Yet to be Done
- **Firebase Integration for Chat Storage**: Implement Firestore to save and retrieve conversations, allowing cross-device sync.
- **Persona Creation Feature**: Allow users to create custom personas through a form in the UI.
- **Search Chat Feature**: Implement a search functionality for existing chats.
- **GTP API Integration**: Connect with OpenAI GPT to provide meaningful responses by personas during chat sessions.
- **Favorite/Delete Persona Feature**: Complete the implementation for long press actions on persona cards to favorite or delete.



#Architecture Database :

Users (Collection)
└── UserID (Document - based on uid)
    ├── Personas (Sub-Collection)
    │    ├── PersonaID_1 (Document)
    │    └── PersonaID_2 (Document)
    └── Chats (Sub-Collection)
         ├── ChatID_1 (Document)
         └── ChatID_2 (Document)


 Dataflow idea:

 Suggested Adjustments for Better Integration:
 Repository Adjustments:

 Fetch Data Based on User ID:
 Modify all repository classes (PersonaRepository, ChatRepository)
 to use Firebase Firestore with user-specific queries using the uid.
 Save Data Based on User ID: When adding new personas or chats,
 make sure they are stored under the current user’s document in Firestore.
 ViewModel Updates:

 User Scope Awareness: Update PersonaViewModel and ChatViewModel so that they are
  aware of the current user context.
  The ViewModel should receive the uid as a
  parameter or retrieve it using a shared mechanism
  (e.g., from FirebaseAuth.getInstance().getCurrentUser().getUid()).
 Room Database:

 Update Tables to Include uid: Modify the Room tables (Persona, Chat) to include a userId field
  so that local    caching respects different users.
 Filter Data by User: Ensure that local queries are filtered by userId so that only the
 authenticated user’s data is visible.

 Example Workflow with Firebase Authentication
 User Signs In:
 When the user signs in using Firebase Authentication, we retrieve their uid.
 Data Flow with uid:

 The uid is passed to the Repository, which will handle the data access.
 Firestore: When accessing Firestore, the uid is used to retrieve data belonging to
 that specific user.
 Room Database: The local Room database should only cache data for the specific uid.
 Queries are filtered to display data related to the logged-in user.

 Persona and Chat Isolation:
 Each user’s personas and chats are isolated from one another,
 ensuring privacy and a seamless user experience.

## Contributors
- Guilherme Miranda Falcão (Lead Developer & Project Manager)
- Anastasia (Developer)
- Aryan (Developer)