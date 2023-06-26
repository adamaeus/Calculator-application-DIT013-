import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/*
     A graphical user interface for the Calculator

     NOTE: It's also possible to use FXML markup to define the GUI

      **** NOTHING TO DO HERE ****
 */
public class CalculatorGUI extends Application {

    final Calculator calculator = new Calculator();

    @Override
    public void start(Stage stage) throws Exception {

        BorderPane root = new BorderPane();

        root.setPadding(new Insets(10));
        root.setTop(createDisplayPane());
        root.setCenter(createButtons());

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.setTitle("Calculator");
        stage.setResizable(false);
        stage.show();
    }

    Pane createButtons() {
        GridPane p = new GridPane();
        String labels = "123+C" + "456-^" + "789* " + "0()/=";
        int i = 0;
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 5; c++) {
                String s = String.valueOf(labels.charAt(i));
                if (!" ".equals(s)) {
                    Button b = new Button(s);
                    b.setMaxWidth(Double.MAX_VALUE);
                    b.setOnMouseReleased(this::buttonHandler);
                    p.add(b, c, r);
                } else {
                    p.add(new Pane(), c, r);
                }
                i++;
            }
        }
        return p;
    }

    private TextField t;

    Pane createDisplayPane() {
        VBox v = new VBox();
        v.setPadding(new Insets(3));
        t = new TextField();
        t.setPrefColumnCount(12);
        t.setFont(Font.font("Verdana", 16));
        v.getChildren().add(t);
        return v;

    }

    void buttonHandler(MouseEvent evt) {
        String text = ((Button) evt.getSource()).getText();
        switch (text) {
            case "=":
                double d = calculator.eval(t.getText());
                t.setText(String.valueOf(d));
                break;
            case "C":
                t.setText("");
                break;
            default:
                t.setText(t.getText() + text);
        }
    }
}
