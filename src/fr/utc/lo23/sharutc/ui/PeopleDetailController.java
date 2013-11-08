package fr.utc.lo23.sharutc.ui;

import fr.utc.lo23.sharutc.model.userdata.UserInfo;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class PeopleDetailController implements Initializable {

    public Label login;
    private UserInfo mUserInfo;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        
    }
    
    public void setUserInfo(UserInfo userInfo) {
        mUserInfo = userInfo;
        login.setText(mUserInfo.getLogin());
    }
}
