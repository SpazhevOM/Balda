package controllers;

import interfaces.implementations.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import objects.*;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class SinglePlayerGameController implements Initializable {

    @FXML
    private Label                    labelNameOfTheMovingPlayer;
    private static Label             copyLabelNameOfTheMovingPlayer;

    @FXML
    private Label                    labelNameOfThePlayer;

    @FXML
    private Label                    labelNameOfPC;

    @FXML
    private Label                    labelScoresOfThePlayer;

    @FXML
    private Label                    labelScoresOfPC;

    @FXML
    private TableView                tablePlayingField;

    @FXML
    private TableColumn<Row, String> columnNamesOfRows;

    @FXML
    private TableColumn<Row, String> columnY1;

    @FXML
    private TableColumn<Row, String> columnY2;

    @FXML
    private TableColumn<Row, String> columnY3;

    @FXML
    private TableColumn<Row, String> columnY4;

    @FXML
    private TableColumn<Row, String> columnY5;

    @FXML
    private ListView<String>         listViewWordsOfPC;

    @FXML
    private ListView<String>         listViewWordsOfPlayer;

    private static HumanPlayer       humanPlayer  = new HumanPlayer();
    private static PCPlayer          pcPlayer     = new PCPlayer();

    private static Table             table        = new Table();

    private static ListOfWords       pcUsed       = new ListOfWords();
    private static ListOfWords       playerUsed   = new ListOfWords();
    private static Word              startWord;

    private FXMLLoader               fxmlLoader   = new FXMLLoader();
    private Parent                   fxmlEdit;
    private static Scene             scene;


    @FXML
    public void initialize(URL location, ResourceBundle resources)  {

        loadFXMLFile();

        new Vocabulary();
        // chose starting word
        startWord = Vocabulary.getFiveLetterWord();

        // set starting word to the table
        table.setStartWord(startWord);

        // set to the columns appropriate field of object person
        columnNamesOfRows.setCellValueFactory(new PropertyValueFactory<>("name"));
        columnY1.setCellValueFactory(new PropertyValueFactory<>("y1"));
        columnY2.setCellValueFactory(new PropertyValueFactory<>("y2"));
        columnY3.setCellValueFactory(new PropertyValueFactory<>("y3"));
        columnY4.setCellValueFactory(new PropertyValueFactory<>("y4"));
        columnY5.setCellValueFactory(new PropertyValueFactory<>("y5"));

        // set style for columns
        columnNamesOfRows.setStyle("-fx-alignment: CENTER;");
        columnY1.setStyle("-fx-alignment: CENTER;");
        columnY2.setStyle("-fx-alignment: CENTER;");
        columnY3.setStyle("-fx-alignment: CENTER;");
        columnY4.setStyle("-fx-alignment: CENTER;");
        columnY5.setStyle("-fx-alignment: CENTER;");

        // connect gaming table with FXML table
        tablePlayingField.setItems(table.getTable());

        // connect gaming usedWords Lists with FXML Lists View
        listViewWordsOfPC.setItems(pcUsed.getList());
        listViewWordsOfPlayer.setItems(playerUsed.getList());

        tablePlayingField.setSelectionModel(null);
    }

    private void loadFXMLFile() {

        try {

            fxmlLoader.setLocation(getClass().getResource("/fxml/DialogMoveOfThePlayer.fxml"));
            fxmlEdit = fxmlLoader.load();
            scene = new Scene(fxmlEdit);
        } catch (IOException e) {
            ErrorDialog.callDialog("Пороблеми зі сценою", "Проблеми при завантажені fxml файлу");
        }

    }

    public void showDialogMoveOfThePlayer(ActionEvent actionEvent) {
        Stage stage = new Stage();

        stage.setTitle("Хід гравця");
        stage.setScene(scene);
        stage.setResizable(false);

        // do new window modal
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(((Node) actionEvent.getSource()).getScene().getWindow());

        stage.show();
    }

    private static void setNameOfTheMovingPlayer(String name) {
        String currentPlayer = "Зараз хід гравця: " + name;
        copyLabelNameOfTheMovingPlayer.setText(currentPlayer);
    }

    void setPlayersNamesToLabels(String humanPlayerName) {

        // create players
        humanPlayer = new HumanPlayer(humanPlayerName);
        pcPlayer    = new PCPlayer();

        // set player`s names to the appropriate labels
        labelNameOfThePlayer.setText(humanPlayer.getName());
        labelNameOfPC.setText(pcPlayer.getName());

        // set connection between labels and appropriate fields
        pcPlayer.setListener(labelScoresOfPC);
        humanPlayer.setListener(labelScoresOfThePlayer);

        copyLabelNameOfTheMovingPlayer = labelNameOfTheMovingPlayer;

        setNameOfTheMovingPlayer(humanPlayer.getName());
    }

    private static void closeCurrentWindow() {
        Stage stage = (Stage) copyLabelNameOfTheMovingPlayer.getScene().getWindow();
        stage.close();
    }

    private static boolean isWordUsed(Move possibleMove) {
        return pcUsed.isUsed(possibleMove.getWord())
                || playerUsed.isUsed(possibleMove.getWord())
                || startWord.equals(possibleMove.getWord());
    }

    private static void outResults() {
        if (table.isFull())
        {
            String finalScores = "Кінцевий рахунок гри " + humanPlayer.getScores() + ":" + pcPlayer.getScores();
            String winner;

            if (humanPlayer.getScores() > pcPlayer.getScores()) {
                winner = "Виграв гравець " + humanPlayer.getName();
            } else if (humanPlayer.getScores() < pcPlayer.getScores()) {
                winner = "Виграв гравець " + pcPlayer.getName();
            } else {
                winner = "Гра завершилась з нічийним рахунком";
            }

            EndGameDialog.callDialog(winner, finalScores);
            closeCurrentWindow();
        }
    }

    static boolean tryToMakeMoveByPlayer(Move possibleMove) {
        try{
            setNameOfTheMovingPlayer(humanPlayer.getName());
            boolean flagMoveSuccessful = false;

            String headerText = "Помилка при спробі зробити хід";

            Move copyPossibleMove = new Move(possibleMove);
            // if word is in vocabulary
            if (Vocabulary.isWordInVocabulary(copyPossibleMove.getWord())) {
                // if word wasn't used
                if (!isWordUsed(possibleMove)) {
                    // if chosen cell is empty
                    if (table.isCellEmpty(copyPossibleMove)) {
                        // if neighboring cells isn't empty
                        if (!table.isNeighboringCellsEmpty(copyPossibleMove)) {
                            flagMoveSuccessful = table.isMovePossible(copyPossibleMove);
                            if (!flagMoveSuccessful) {
                                WarningDialog.callDialog("При спробі зробити хід щось пішло не так",
                                        "Введене вами слово відсутнє в таблиці або не містить нової літери");
                            }
                        } else {
                            WarningDialog.callDialog(headerText, "Жодна з сусідніх клітинок не містить літери");
                        }
                    } else {
                        WarningDialog.callDialog(headerText, "Дана клітинка поля вже зайнята");
                    }
                } else {
                    WarningDialog.callDialog(headerText, "Дане слово вже було використане в цій грі");
                }
            } else {
                WarningDialog.callDialog(headerText, "Такого слова немає в словнику");
            }

            if (flagMoveSuccessful) {
                playerUsed.add(possibleMove.getWord());
                humanPlayer.addScores(possibleMove.wordLength());
                setNameOfTheMovingPlayer(pcPlayer.getName());
                table.setValueForCell(copyPossibleMove);
            }

            return flagMoveSuccessful;
        } finally {
            outResults();
        }
    }

    static void makeMoveByPC() {
        setNameOfTheMovingPlayer(pcPlayer.getName());

        List<Move> possibleMoves = table.findAllMoves();

        Random rand = new Random();
        int randIndex = Math.abs(rand.nextInt()) % possibleMoves.size();
        Move pcMove = possibleMoves.get(randIndex);
        while (isWordUsed(pcMove)) {
            randIndex = Math.abs(rand.nextInt()) % possibleMoves.size();
            pcMove = possibleMoves.get(randIndex);
        }

        pcUsed.add(pcMove.getWord());
        pcPlayer.addScores(pcMove.wordLength());
        table.setValueForCell(pcMove);
        setNameOfTheMovingPlayer(humanPlayer.getName());

        outResults();
    }

    public void skipMove() {
        playerUsed.skipMove();
        setNameOfTheMovingPlayer(pcPlayer.getName());
        makeMoveByPC();
    }

    public void giveUp() {
        String winner = "Виграв гравець " + pcPlayer.getName() + ", оскільки гравець " + humanPlayer.getName() + " здався";
        String finalScores = "Кінцевий рахунок гри " + humanPlayer.getScores() + ":" + pcPlayer.getScores();
        EndGameDialog.callDialog(winner, finalScores);
        closeCurrentWindow();

    }
}
