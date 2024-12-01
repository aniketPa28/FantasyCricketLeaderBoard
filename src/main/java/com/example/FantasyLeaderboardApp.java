package com.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import com.github.pjfanning.xlsx.StreamingReader;

public class FantasyLeaderboardApp extends Application {

    private TableView<ParticipantScore> leaderboardTable = new TableView<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        String filePath = "src/resources/FantasyCricketData.xlsx";

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        Button loadLeaderboardButton = new Button("Load Leaderboard");
        loadLeaderboardButton.setOnAction(e -> loadLeaderboard(filePath));

        leaderboardTable = createLeaderboardTable();
        layout.getChildren().addAll(loadLeaderboardButton, leaderboardTable);

        Scene scene = new Scene(layout, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Fantasy Cricket Leaderboard");
        primaryStage.show();
    }

    private void loadLeaderboard(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            // Use StreamingReader to open the workbook
            Workbook workbook = StreamingReader.builder()
                    .rowCacheSize(100)
                    .bufferSize(4096)
                    .open(fis);

            clearLeaderBoardSheet(filePath);

            Map<String, PlayerPerformance> playerPerformances = loadPlayerPerformances(workbook);
            Map<String, Integer> rules = loadRules(workbook);
            Map<String, List<String>> participantPlayers = loadAwardedPlayers(workbook);

            Map<String, Integer> participantScores = calculateParticipantScores(playerPerformances, rules, participantPlayers);

            List<ParticipantScore> scores = new ArrayList<>();
            participantScores.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEach(entry -> scores.add(new ParticipantScore(entry.getKey(), entry.getValue())));

            leaderboardTable.getItems().clear();
            leaderboardTable.getItems().addAll(scores);

            writeLeaderboardToSheet(filePath, scores);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, PlayerPerformance> loadPlayerPerformances(Workbook workbook) {
        Map<String, PlayerPerformance> playerPerformances = new HashMap<>();
        Sheet sheet = workbook.getSheet("PlayerPerformance");

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;

            String playerName = getCellValueAsString(row.getCell(0));
            int runs = (int) getCellValueAsNumeric(row.getCell(1));
            int wickets = (int) getCellValueAsNumeric(row.getCell(2));
            int catches = (int) getCellValueAsNumeric(row.getCell(3));
            int strikeRate = (int) getCellValueAsNumeric(row.getCell(4));

            playerPerformances.put(playerName, new PlayerPerformance(runs, wickets, catches, strikeRate));
        }
        return playerPerformances;
    }

    private Map<String, Integer> loadRules(Workbook workbook) {
        Map<String, Integer> rules = new HashMap<>();
        Sheet sheet = workbook.getSheet("Rules");

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;

            String rule = getCellValueAsString(row.getCell(0));
            int points = (int) getCellValueAsNumeric(row.getCell(1));

            rules.put(rule, points);
        }
        return rules;
    }

    private Map<String, List<String>> loadAwardedPlayers(Workbook workbook) {
        Map<String, List<String>> participantPlayers = new HashMap<>();
        Sheet sheet = workbook.getSheet("AwardedPlayers");

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;

            String playerName = getCellValueAsString(row.getCell(0));
            String participantName = getCellValueAsString(row.getCell(1));

            participantPlayers.computeIfAbsent(participantName, k -> new ArrayList<>()).add(playerName);
        }
        return participantPlayers;
    }

    private Map<String, Integer> calculateParticipantScores(
            Map<String, PlayerPerformance> playerPerformances,
            Map<String, Integer> rules,
            Map<String, List<String>> participantPlayers) {

        Map<String, Integer> participantScores = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : participantPlayers.entrySet()) {
            String participantName = entry.getKey();
            List<String> players = entry.getValue();

            int totalScore = 0;
            for (String player : players) {
                PlayerPerformance performance = playerPerformances.get(player);
                if (performance != null) {
                    totalScore += calculateScore(performance, rules);
                }
            }
            participantScores.put(participantName, totalScore);
        }
        return participantScores;
    }

    private void clearLeaderBoardSheet(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = StreamingReader.builder()
                     .rowCacheSize(100)  // Cache 100 rows in memory at once
                     .bufferSize(4096)  // Set buffer size
                     .open(fis)) {

            Sheet sheet = workbook.getSheet("LeaderBoard");  // Get the LeaderBoard sheet
            if (sheet == null) {
                System.out.println("No LeaderBoard sheet found.");
                return;
            }

            // Iterate over the rows using rowIterator()
            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (row.getRowNum() > 0) {  // Skip the header row
                    sheet.removeRow(row);  // Remove each row
                }
            }

            // After clearing, you can write back the changes to the file, if needed

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeLeaderboardToSheet(String filePath, List<ParticipantScore> scores) {
        try (Workbook workbook = WorkbookFactory.create(new File(filePath))) {
            Sheet sheet = workbook.getSheet("LeaderBoard");
            if (sheet == null) {
                sheet = workbook.createSheet("LeaderBoard");
            }

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Rank");
            headerRow.createCell(1).setCellValue("Participant");
            headerRow.createCell(2).setCellValue("Score");

            int rowIndex = 1;
            int rank = 1;
            for (ParticipantScore score : scores) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(rank++);
                row.createCell(1).setCellValue(score.getName());
                row.createCell(2).setCellValue(score.getScore());
            }
        } catch(Exception e) {
        e.printStackTrace();
    }
}

    private int calculateScore(PlayerPerformance performance, Map<String, Integer> rules) {
        int score = 0;
        score += performance.runs * rules.getOrDefault("Each Run Scored", 1);
        score += performance.wickets * rules.getOrDefault("Each Wicket Taken", 30);
        if (performance.runs >= 50) score += rules.getOrDefault("50 Runs Scored", 10);
        if (performance.runs >= 100) score += rules.getOrDefault("100 Runs Scored", 20);
        if (performance.wickets >= 3) score += rules.getOrDefault("3 Wickets in an Innings", 10);
        if (performance.wickets >= 5) score += rules.getOrDefault("5 Wickets in an Innings", 20);
        if (performance.catches > 0) score += performance.catches * rules.getOrDefault("Catch Taken", 10);
        if (performance.strikeRate > 150) score += rules.getOrDefault("Strike Rate Above 150", 10);
        return score;
    }

    private String getCellValueAsString(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) return "";
        return cell.getCellType() == CellType.STRING ? cell.getStringCellValue() : cell.toString();
    }

    private double getCellValueAsNumeric(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) return 0.0;
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return Double.parseDouble(cell.getStringCellValue());
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    private TableView<ParticipantScore> createLeaderboardTable() {
        TableView<ParticipantScore> table = new TableView<>();

        TableColumn<ParticipantScore, Number> rankCol = new TableColumn<>("Rank");
        rankCol.setCellValueFactory(data -> new SimpleIntegerProperty(leaderboardTable.getItems().indexOf(data.getValue()) + 1));

        TableColumn<ParticipantScore, String> nameCol = new TableColumn<>("Participant");
        nameCol.setCellValueFactory(data -> data.getValue().nameProperty());

        TableColumn<ParticipantScore, Number> scoreCol = new TableColumn<>("Score");
        scoreCol.setCellValueFactory(data -> data.getValue().scoreProperty());

        table.getColumns().addAll(rankCol, nameCol, scoreCol);
        return table;
    }

    public static class PlayerPerformance {
        int runs, wickets, catches, strikeRate;

        public PlayerPerformance(int runs, int wickets, int catches, int strikeRate) {
            this.runs = runs;
            this.wickets = wickets;
            this.catches = catches;
            this.strikeRate = strikeRate;
        }
    }

    public static class ParticipantScore {
        private final StringProperty name = new SimpleStringProperty();
        private final IntegerProperty score = new SimpleIntegerProperty();

        public ParticipantScore(String name, int score) {
            this.name.set(name);
            this.score.set(score);
        }

        public StringProperty nameProperty() {
            return name;
        }

        public IntegerProperty scoreProperty() {
            return score;
        }

        public String getName() {
            return name.get();
        }

        public int getScore() {
            return score.get();
        }
    }
}