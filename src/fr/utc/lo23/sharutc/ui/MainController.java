package fr.utc.lo23.sharutc.ui;

import com.cathive.fx.guice.GuiceFXMLLoader;
import com.cathive.fx.guice.GuiceFXMLLoader.Result;
import com.google.inject.Inject;
import fr.utc.lo23.sharutc.model.userdata.UserInfo;
import fr.utc.lo23.sharutc.ui.custom.PeopleCard;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MainController implements Initializable, PeopleHomeController.IPeopleHomeController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);
    @Inject
    private GuiceFXMLLoader mFxmlLoader;
    private StackPane mDragPreview;
    public Button songsbutton;
    public Button peoplebutton;
    public Button artistsbutton;
    public Button albumsbutton;
    public Pane rightpane;
    public HBox bottombar;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            bottombar.getChildren().add((Node) FXMLLoader.load(getClass().getResource("fxml/player.fxml")));
        } catch (IOException exception) {
        }

        mDragPreview = new StackPane();
        mDragPreview.setOpacity(0.6);
        mDragPreview.setMouseTransparent(true);
        mDragPreview.toFront();

    }

    @FXML
    private void handleMenuButtonAction(ActionEvent event) throws IOException {

        ObservableList<Node> children = rightpane.getChildren();
        children.clear();

        if (event.getSource() == songsbutton) {
            children.add((Node) mFxmlLoader.load(getClass().getResource("fxml/songs_detail.fxml")).getRoot());
        } else if (event.getSource() == peoplebutton) {
            final Result loadingResult = mFxmlLoader.load(getClass().getResource("fxml/people_home.fxml"));
            ((PeopleHomeController) loadingResult.getController()).setInterface(this);
            children.add((Node) loadingResult.getRoot());
        } else if (event.getSource() == artistsbutton) {
            children.add((Node) mFxmlLoader.load(getClass().getResource("fxml/artists_detail.fxml")).getRoot());
        } else if (event.getSource() == albumsbutton) {
            children.add((Node) mFxmlLoader.load(getClass().getResource("fxml/albums_detail.fxml")).getRoot());
        }
    }

    @FXML
    public void handleTextEntered(ActionEvent actionEvent) throws IOException {
        ObservableList<Node> children = rightpane.getChildren();
        children.clear();
        children.add((Node) mFxmlLoader.load(getClass().getResource("fxml/searchresult_detail.fxml")).getRoot());
    }

    @Override
    public void onPeopleDetailRequested(UserInfo user) {
        ObservableList<Node> children = rightpane.getChildren();
        children.clear();
        log.info("people detail requested : " + user.getLogin());
        try {
            final Result loadingResult = mFxmlLoader.load(getClass().getResource("fxml/people_detail.fxml"));
            ((PeopleDetailController) loadingResult.getController()).setUserInfo(user);
            children.add((Node) loadingResult.getRoot());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public void onGroupDetailRequested() {
    }

    @Override
    public void onDraggedCardDisplayRequested(ArrayList<PeopleCard> cards, MouseEvent event) {
        final AnchorPane root = (AnchorPane) rightpane.getScene().getRoot();
        int i = 0;
        for (PeopleCard people : cards) {
            final ImageView preview = new ImageView(people.snapshot(null, null));
            StackPane.setMargin(preview, new Insets(20 * i, 20 * i, 0, 0));
            mDragPreview.getChildren().add(preview);
            i++;
        }
        root.getChildren().add(mDragPreview);
        mDragPreview.relocate(
                (int) (event.getSceneX() - mDragPreview.getBoundsInParent().getWidth() / 2),
                (int) (event.getSceneY() - mDragPreview.getBoundsInParent().getHeight() / 2));
        root.setOnDragOver(new javafx.event.EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent dragEvent) {
                Point2D localPoint = root.sceneToLocal(new Point2D(dragEvent.getSceneX(), dragEvent.getSceneY()));
                mDragPreview.relocate(
                        (int) (localPoint.getX() - mDragPreview.getBoundsInParent().getWidth() / 2),
                        (int) (localPoint.getY() - mDragPreview.getBoundsInParent().getHeight() / 2));
                dragEvent.consume();
            }
        });
    }

    @Override
    public void onDraggedCardHideRequested() {
        final AnchorPane root = (AnchorPane) rightpane.getScene().getRoot();
        root.getChildren().remove(mDragPreview);
        mDragPreview.getChildren().clear();
    }
}
