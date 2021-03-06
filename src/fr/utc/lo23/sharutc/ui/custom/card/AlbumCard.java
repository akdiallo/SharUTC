package fr.utc.lo23.sharutc.ui.custom.card;

import fr.utc.lo23.sharutc.model.domain.Music;
import fr.utc.lo23.sharutc.ui.SongDetailController;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * A SimpleCard that displays the Album of a given [@link Music].
 * The card notifies an {@link IAlbumCard} on double click.
 */
public class AlbumCard extends SimpleCard implements EventHandler<Event> {

    @FXML
    public Label albumNameLabel;
    @FXML
    public Label artistNameLabel;
    private Music mMusic;
    private IAlbumCard mInterface;
    private String mAlbumName;
    private String mArtistName;
    private SongDetailController.CatalogType type = SongDetailController.CatalogType.local;

    public AlbumCard(String albumName, String artistName, IAlbumCard i) {
        super("/fr/utc/lo23/sharutc/ui/fxml/album_card.fxml");
        setOnMouseClicked(this);
        mInterface = i;
        mArtistName = artistName;
        mAlbumName = albumName;
        artistNameLabel.setText(mArtistName);
        albumNameLabel.setText(mAlbumName);
    }

    public void setCatalogType(SongDetailController.CatalogType type) {
        this.type = type;
    }

    public AlbumCard(Music m, IAlbumCard i) {
        this(m.getAlbum(), m.getArtist(), i);
    }

    /**
     * Return the name of the artist.
     *
     * @return the name of the artist.
     */
    public String getArtistName() {
        return mArtistName;
    }

    /**
     * Return the name of the album.
     *
     * @return the name of the album.
     */
    public String getAlbumName() {
        return mAlbumName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(final Event event) {
        if (event instanceof MouseEvent) {
            final MouseEvent mouseEvent = (MouseEvent) event;
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)
                    && mouseEvent.getClickCount() == 2) {
                mInterface.onAlbumDetailRequested(mAlbumName, type);
            }
        }
    }

    /**
     * A simple interface used as callback.
     */
    public interface IAlbumCard {

        /**
         * Notify the interface that the user wants to view the details of an
         * album.
         *
         * @param albumName the name of the album shown on the card
         */
        public void onAlbumDetailRequested(String albumName, SongDetailController.CatalogType type);
    }
}
