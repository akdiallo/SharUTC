package fr.utc.lo23.sharutc.ui;

import com.google.inject.Inject;
import fr.utc.lo23.sharutc.model.AppModel;
import fr.utc.lo23.sharutc.model.domain.Music;
import fr.utc.lo23.sharutc.ui.custom.card.AlbumCard;
import fr.utc.lo23.sharutc.util.Pair;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.TextAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlbumsDetailController implements RighpaneInterface, Initializable, AlbumCard.IAlbumCard {

    private static final Logger log = LoggerFactory.getLogger(AlbumsDetailController.class);
    public IAlbumsDetailController mInterface;
    @FXML
    public FlowPane albumsContainer;
    @FXML
    public StackPane contentContainer;
    @FXML
    public Label titleLabel;
    @Inject
    private AppModel mAppModel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    public void setInterface(IAlbumsDetailController i) {
        mInterface = i;
    }

    public void showAlbums() {
        showAlbumsWithArtistFilter(null, SongDetailController.CatalogType.local);
    }

    public void showAlbumsWithArtistFilter(String artistFilter, SongDetailController.CatalogType type) {
        final ArrayList<Pair<String, String>> albumArtistPairs = new ArrayList<Pair<String, String>>();

        if (artistFilter != null) {
            titleLabel.setText("Browse " + artistFilter + "'s albums");
        }
        List<Music> musics = null;
        if (type.equals(SongDetailController.CatalogType.remote)) {
            musics = mAppModel.getRemoteUserCatalog().getMusics();
        } else if (type.equals(SongDetailController.CatalogType.local)) {
            musics = mAppModel.getLocalCatalog().getMusics();
        } else {
            musics = mAppModel.getSearchResults().getMusics();
        }

        //retrieve the albums from the local catalog
        for (Music m : musics) {
            final Pair<String, String> currentAlbumArtistPair =
                    new Pair<String, String>(m.getAlbum(), m.getArtist());
            if (!albumArtistPairs.contains(currentAlbumArtistPair)
                    && (artistFilter == null || artistFilter.equals(m.getArtist()))) {
                albumArtistPairs.add(currentAlbumArtistPair);
            }
        }

        //display the albums
        if (albumArtistPairs.isEmpty()) {
            final Label placeHolder = new Label("You have no songs. Please add a song before browsing albums.");
            placeHolder.getStyleClass().add("placeHolderLabel");
            placeHolder.setWrapText(true);
            placeHolder.setTextAlignment(TextAlignment.CENTER);
            contentContainer.getChildren().add(placeHolder);
        } else {
            for (Pair<String, String> albumArtistPair : albumArtistPairs) {
                AlbumCard card = new AlbumCard(
                        albumArtistPair.getLeft(), albumArtistPair.getRight(), this);
                card.setCatalogType(type);
                albumsContainer.getChildren().add(card);
            }
        }
    }

    @Override
    public void onAlbumDetailRequested(String albumName, SongDetailController.CatalogType type) {
        log.info("onArtistDetailRequested " + albumName);
        mInterface.onAlbumDetailRequested(albumName, type);
    }

    @Override
    public void onDetach() {
    }

    public interface IAlbumsDetailController {

        /**
         * display user details
         *
         * @param music
         */
        public void onAlbumDetailRequested(String albumName, SongDetailController.CatalogType type);
    }
}
