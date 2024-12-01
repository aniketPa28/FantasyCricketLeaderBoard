# Fantasy Cricket Leaderboard Application

This is a Java-based Fantasy Cricket Leaderboard application that calculates participant scores based on player performances and displays the results in a leaderboard format. The application reads data from an Excel file, calculates points based on pre-defined rules, and displays the leaderboard with ranks and scores. The leaderboard is also updated in the Excel file when the application is closed.

## Features

- **Read and process Excel files**: The application reads data from multiple sheets in an Excel file.
- **Calculate participant scores**: Based on the player performances and predefined rules.
- **Display leaderboard**: Ranks participants based on their calculated scores and displays the leaderboard in a table.
- **Update leaderboard**: Clears the existing data in the `LeaderBoard` sheet and updates it with new calculated scores and ranks.
- **JavaFX Interface**: The app provides a graphical user interface (GUI) to interact with the leaderboard.

## Prerequisites

Before running the application, ensure you have the following installed on your system:

- **Java 8 or higher**: Ensure that Java is installed and properly set up.
- **Maven**: The project uses Maven for dependency management.
- **Apache POI**: Used to read/write Excel files.
- **JavaFX**: Used for the graphical user interface.

### Libraries/Dependencies
- Apache POI (for reading/writing Excel files)
- JavaFX (for GUI development)
- StreamingReader (for reading large Excel files)

## Project Structure

The project is structured as follows:


### Key Sheets in Excel:

1. **AwardedPlayers**: This sheet contains the list of players and their corresponding participant names. Multiple players can be associated with one participant.
2. **PlayerPerformance**: Contains player performance data (e.g., runs, wickets, catches).
3. **Rules**: Defines the rules for calculating points based on player performance (e.g., runs scored, wickets taken).
4. **LeaderBoard**: This sheet is updated by the application with participant ranks, names, and scores.

## How to Use

1. **Set up the Excel file**:
    - Prepare an Excel file with the following sheets:
        - **AwardedPlayers**: Contains the columns `PlayerName` and `ParticipantName`.
        - **PlayerPerformance**: Contains player performance data like `Runs`, `Wickets`, etc.
        - **Rules**: Contains point rules (e.g., `Each Run Scored`, `Each Wicket Taken`).
        - **LeaderBoard**: This sheet is where the final leaderboard data will be written.

   Example:

   **AwardedPlayers Sheet:**

   | PlayerName | ParticipantName |
   |------------|-----------------|
   | Player1    | Participant1    |
   | Player2    | Participant1    |
   | Player3    | Participant2    |

   **PlayerPerformance Sheet:**

   | PlayerName | Runs | Wickets | Catches | StrikeRate |
   |------------|------|---------|---------|------------|
   | Player1    | 50   | 2       | 1       | 130        |
   | Player2    | 30   | 1       | 0       | 115        |

   **Rules Sheet:**

   | RuleDescription          | Points |
   |--------------------------|--------|
   | Each Run Scored          | 1      |
   | Each Wicket Taken        | 30     |
   | 50 Runs Scored           | 10     |
   | 100 Runs Scored          | 20     |
   | 3 Wickets in an Innings  | 10     |
   | 5 Wickets in an Innings  | 20     |

2. **Run the application**:
    - Open a terminal/command prompt.
    - Navigate to the project directory and run the following Maven command to build and run the application:
      ```bash
      mvn javafx:run
      ```

3. **Interact with the GUI**:
    - The application will display a window with a button to load the leaderboard.
    - When you click the **"Load Leaderboard"** button, the app will read data from the Excel file, calculate the scores for each participant based on the performance data and rules, and display the leaderboard with participant ranks, names, and scores.

4. **Check the Excel file**:
    - After the leaderboard is updated, the **LeaderBoard** sheet in the Excel file will be updated with participant ranks and scores.
    - The sheet will be sorted by the scores, with the highest points in the first rank.

### Classes:

- **`PlayerPerformance`**: Contains the player's performance details (runs, wickets, catches).
- **`ParticipantScore`**: Contains the participant's name and score, with properties to bind to the JavaFX table.

## Troubleshooting

- If you encounter errors such as `Cannot resolve symbol`, ensure that all required dependencies (Apache POI, JavaFX) are added to your Maven `pom.xml` file.
- If the leaderboard isn't displaying correctly, ensure that the Excel file is correctly structured, with appropriate data in the sheets.

## Author

Aniket Paratkar