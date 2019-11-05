package main;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;

public class GUIUtilities {

    public static void showErrorToUser(String i_AlertMessage){
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle("Error!");
        errorAlert.setContentText(i_AlertMessage);
        errorAlert.showAndWait();
    }

    public static void showInformationToUser(String i_AlertMessage){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information!");
        alert.setContentText(i_AlertMessage);
        alert.showAndWait();
    }

    public static Optional<ButtonType> showYesNoAlertToUser(String i_AlertMessage){
        Alert alert = new Alert(Alert.AlertType.WARNING, i_AlertMessage, ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = alert.showAndWait();

        return result;
    }

    public static String getTextInput(String i_Title,String i_HeaderText,String i_Content,String i_DefaultValue){
        TextInputDialog dialog = new TextInputDialog(i_DefaultValue);
        dialog.setTitle(i_Title);
        dialog.setHeaderText(i_HeaderText);
        dialog.setContentText(i_Content);
        Optional<String> op = dialog.showAndWait();
        if(!op.equals(Optional.empty())){
            return op.get();
        }

        return null;
    }
}