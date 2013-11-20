package fr.utc.lo23.sharutc.ui;

import com.google.inject.Inject;
import fr.utc.lo23.sharutc.controler.command.music.AddToLocalCatalogCommand;
import fr.utc.lo23.sharutc.model.AppModel;
import fr.utc.lo23.sharutc.model.domain.Catalog;
import fr.utc.lo23.sharutc.model.domain.Music;
import fr.utc.lo23.sharutc.ui.custom.card.SongCard;
import fr.utc.lo23.sharutc.ui.custom.card.TagCard;
import fr.utc.lo23.sharutc.util.CollectionChangeListener;
import fr.utc.lo23.sharutc.util.CollectionEvent;
import fr.utc.lo23.sharutc.util.CollectionEvent.Type;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;

public class SongListController extends SongSelectorController implements Initializable,
        CollectionChangeListener<Music>{

    private static final Logger log = LoggerFactory
            .getLogger(SongListController.class);
    @FXML
    public Button addNewSongButton;
    @FXML
    public FlowPane songsContainer;
    @FXML
    public HBox tagContainer;
    @FXML
    public ScrollPane tagScrollPane;
    @Inject
    public AppModel mAppModel;
    @Inject
    private AddToLocalCatalogCommand mAddToLocalCatalogCommand;

    @Override
    public void init(StackPane dragPreview) {
        super.init(dragPreview);
        //TODO retrieve all user song and add them to the view
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        tagScrollPane.getStyleClass().add("myScrollPaneWithTopBorder");

        addNewSongButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                log.info("Add a new song Button Clicked");
                final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Music files (*.mp3)", "*.mp3");
                final FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Add a Song");
                fileChooser.getExtensionFilters().add(extFilter);
                final List<File> files = fileChooser.showOpenMultipleDialog(addNewSongButton.getScene().getWindow());
                if (files != null && !files.isEmpty()) {
                    log.info("import songs !");
                    mAddToLocalCatalogCommand.setFiles(files);
                    mAddToLocalCatalogCommand.execute();
                }
            }
        });
        
        //Listen to changes on the Local Catalog
        mAppModel.getLocalCatalog().addPropertyChangeListener(this);

        showTags();
        showCatalog();

    }

    public void showCatalog() {
        final Catalog catalog = mAppModel.getLocalCatalog();
        ArrayList<Music> musics = new ArrayList<Music>();

        for (final Music m : catalog.getMusics()) {
            musics.add(m);
        }

        if (musics.isEmpty()) {            
            musics.add(MainController.population.get(0));
        }

        //TODO filter

        songsContainer.getChildren().clear();
        for (Music m : musics) {
            songsContainer.getChildren().add(new SongCard(m, this, true));
        }
    }
    
    private void songAdded(Music music) {
        songsContainer.getChildren().add(new SongCard(music, this, true));
    }

    public void createCards(String artistName, String albumName) {
        for (Music m : MainController.population) {
            if ((m.getArtist().equals(artistName) && m.getAlbum().equals(albumName)) || (artistName.equals("") && albumName.equals(""))) {
                SongCard newCard = new SongCard(m, this, true);
                songsContainer.getChildren().add(newCard);
            }
        }
    }

    public void createCards() {
        createCards("", "");
    }

    /**
     * user add a new song
     *
     * @param song new song added
     */
    private void songAdded(File song) {
        final Music m = new Music();
        m.setFileName(song.getName());
        songsContainer.getChildren().add(new SongCard(m, this, true));
        //TODO add song to user songs
    }

    private void showTags() {
        tagContainer.getChildren().clear();

        //The "virtual" "All songs" tag
        showTagCard(new TagCard("All Songs"));

        //The "virtual" "My Songs" tag
        showTagCard(new TagCard("My Songs"));

        //TODO remove when we get the real tags
        for (int i = 0; i < 5; i++) {
            showTagCard(new TagCard("Tag " + String.valueOf(i)));
        }
    }

    private void showTagCard(TagCard tagCard) {
        HBox.setMargin(tagCard, new Insets(0, 5, 0, 5));
        tagContainer.getChildren().add(tagCard);
    }

    @Override
    public void onDetach() {
        mAppModel.getLocalCatalog().removePropertyChangeListener(this);
    }


    @Override
    public void collectionChanged(CollectionEvent<Music> ev) {
        final Type eventType = ev.getType();
        if(CollectionEvent.Type.ADD.equals(eventType)) {
            log.info("a new music has just been added to the local catalog");
            songAdded(ev.getItem());
        }
    }
}
