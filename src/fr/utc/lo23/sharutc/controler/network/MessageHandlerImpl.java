package fr.utc.lo23.sharutc.controler.network;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import fr.utc.lo23.sharutc.controler.command.Command;
import fr.utc.lo23.sharutc.controler.command.account.IntegrateBroadcastConnectionCommand;
import fr.utc.lo23.sharutc.controler.command.account.IntegrateConnectionCommand;
import fr.utc.lo23.sharutc.controler.command.music.IntegrateRemoteCatalogCommand;
import fr.utc.lo23.sharutc.controler.command.music.IntegrateRemoteTagMapCommand;
import fr.utc.lo23.sharutc.controler.command.music.SendCatalogCommand;
import fr.utc.lo23.sharutc.controler.command.music.SendTagMapCommand;
import fr.utc.lo23.sharutc.controler.command.player.PlayIncomingMusicCommand;
import fr.utc.lo23.sharutc.controler.command.player.SendMusicToPlayCommand;
import fr.utc.lo23.sharutc.controler.command.search.InstallRemoteMusicsCommand;
import fr.utc.lo23.sharutc.controler.command.search.IntegrateMusicSearchCommand;
import fr.utc.lo23.sharutc.controler.command.search.PerformMusicSearchCommand;
import fr.utc.lo23.sharutc.controler.command.search.SendMusicsCommand;
import fr.utc.lo23.sharutc.controler.service.MusicService;
import fr.utc.lo23.sharutc.controler.service.UserService;
import fr.utc.lo23.sharutc.model.AppModel;
import fr.utc.lo23.sharutc.model.domain.Catalog;
import fr.utc.lo23.sharutc.model.domain.Music;
import fr.utc.lo23.sharutc.model.domain.SearchCriteria;
import fr.utc.lo23.sharutc.model.domain.TagMap;
import fr.utc.lo23.sharutc.model.userdata.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@inheritDoc}
 */
@Singleton
public class MessageHandlerImpl implements MessageHandler {

    private static final Logger log = LoggerFactory
            .getLogger(MessageHandlerImpl.class);
    private final AppModel appModel;
    private final MessageParser messageParser;
    private final MusicService musicService;
    private final UserService userService;
    private Command command = null;

    @Inject
    public MessageHandlerImpl(AppModel appModel, MusicService musicService, UserService userService, MessageParser messageParser) {
        this.appModel = appModel;
        this.userService = userService;
        this.musicService = musicService;
        this.messageParser = messageParser;
    }
    // all command interfaces used by network service
    @Inject
    private SendTagMapCommand sendTagMapCommand;
    @Inject
    private IntegrateRemoteTagMapCommand integrateRemoteTagMapCommand;
    @Inject
    private SendMusicsCommand sendMusicsCommand;
    @Inject
    private InstallRemoteMusicsCommand installRemoteMusicsCommand;
    @Inject
    private SendCatalogCommand sendCatalogCommand;
    @Inject
    private IntegrateRemoteCatalogCommand integrateRemoteCatalogCommand;
    @Inject
    private SendMusicToPlayCommand sendMusicToPlayCommand;
    @Inject
    private PlayIncomingMusicCommand playIncomingMusicCommand;
    @Inject
    private IntegrateBroadcastConnectionCommand integrateBroadcastConnectionCommand;
    @Inject
    private IntegrateConnectionCommand integrateConnectionCommand;
    @Inject
    private PerformMusicSearchCommand performMusicSearchCommand;
    @Inject
    private IntegrateMusicSearchCommand integrateMusicSearchCommand;
    //@Inject
    //private AddCommentCommand addCommentCommand;
    // more...

    /**
     * {inheritDoc}
     */
    @Override
    public void handleMessage(String string) {
        command = null;
        Message incomingMessage = messageParser.fromJSON(string);
        if (incomingMessage != null) {
            try {
                messageParser.read(incomingMessage);
                // searching which command to execute following message type
                log.info("Handling message '{}' from '{}'", incomingMessage.getType().name(), messageParser.getSource());
                switch (incomingMessage.getType()) {
                    case MUSIC_GET_CATALOG:
                        sendCatalogCommand.setConversationId(appModel.getNextConversationId());
                        sendCatalogCommand.setPeer(messageParser.getSource());
                        command = sendCatalogCommand;
                        break;
                    case MUSIC_CATALOG:
                        if (isMessageForCurrentConversation()) {
                            integrateRemoteCatalogCommand.setPeer(messageParser.getSource());
                            integrateRemoteCatalogCommand.setCatalog((Catalog) messageParser.getValue(Message.CATALOG));
                            command = integrateRemoteCatalogCommand;
                        }
                        break;
                    case TAG_GET_MAP:
                        sendTagMapCommand.setConversationId(appModel.getNextConversationId());
                        sendTagMapCommand.setPeer(messageParser.getSource());
                        command = sendTagMapCommand;
                        break;
                    case TAG_MAP:
                        // we must check the conversation ID, the user may have left the cloud of tags screen
                        if (isMessageForCurrentConversation()) {
                            integrateRemoteTagMapCommand.setTagMap((TagMap) messageParser.getValue(Message.TAG_MAP));
                            command = integrateRemoteTagMapCommand;
                        }
                        break;
                    case COMMENT_ADD:
                        //  addCommentCommand.setAuthorPeer(userService.getPeerById((Long) messageReader.getValue(Message.AUTHOR_PEER_ID)));
                        //  addCommentCommand.setMusic(musicService.getPeerById((Integer) messageReader.getValue(Message.MUSIC_ID)));
                        //  addCommentCommand.setOwnerPeer(userService.getPeerById((Long) messageReader.getValue(Message.OWNER_PEER_ID)));
                        //  addCommentCommand.setComment((String) messageReader.getValue(Message.COMMENT));
                        //  command = addCommentCommand;
                        break;
                    case COMMENT_EDIT:
                        break;
                    case COMMENT_REMOVE:
                        break;
                    case SCORE_SET:
                        break;
                    case SCORE_UNSET:
                        break;
                    case MUSIC_SEARCH:
                        performMusicSearchCommand.setConversationId(appModel.getNextConversationId());
                        performMusicSearchCommand.setPeer(messageParser.getSource());
                        performMusicSearchCommand.setSearchCriteria((SearchCriteria) messageParser.getValue(Message.SEARCH));
                        command = performMusicSearchCommand;
                        break;
                    case MUSIC_RESULTS:
                        if (isMessageForCurrentConversation()) {
                            integrateMusicSearchCommand.setResultsCatalog((Catalog) messageParser.getValue(Message.CATALOG));
                            command = integrateMusicSearchCommand;
                        }
                        break;
                    case MUSIC_GET:
                        sendMusicsCommand.setPeer(messageParser.getSource());
                        sendMusicsCommand.setCatalog((Catalog) messageParser.getValue(Message.CATALOG));
                        command = sendMusicsCommand;
                        break;
                    case MUSIC_INSTALL:
                        installRemoteMusicsCommand.setCatalog((Catalog) messageParser.getValue(Message.CATALOG));
                        command = installRemoteMusicsCommand;
                        break;
                    case MUSIC_GET_TO_PLAY:
                        sendMusicToPlayCommand.setConversationId(appModel.getNextConversationId());
                        sendMusicToPlayCommand.setPeer(messageParser.getSource());
                        sendMusicToPlayCommand.setMusic(appModel.getLocalCatalog().findMusicById((Long) messageParser.getValue(Message.MUSIC_ID)));
                        command = sendMusicToPlayCommand;
                        break;
                    case MUSIC_SEND_TO_PLAY:
                        if (isMessageForCurrentConversation()) {
                            playIncomingMusicCommand.setMusic((Music) messageParser.getValue(Message.MUSIC));
                            command = playIncomingMusicCommand;
                        }
                        break;
                    case CONNECTION:
                        integrateBroadcastConnectionCommand.setUserInfo((UserInfo) messageParser.getValue(Message.USER_INFO));
                        command = integrateBroadcastConnectionCommand;
                        break;
                    case CONNECTION_RESPONSE:
                        integrateConnectionCommand.setUserInfo((UserInfo) messageParser.getValue(Message.USER_INFO));
                        command = integrateConnectionCommand;
                        break;
                    case DISCONNECT:
                        break;
                    default:
                        command = null;
                        log.warn("Missing command : {}", incomingMessage.getType().name());
                        break;
                }
                if (command != null) {
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            log.info("Running new command : {}", command.getClass().getName());
                            command.execute();
                        }
                    };
                    thread.start();
                }
            } catch (Exception ex) {
                log.error(ex.toString());
            }
        }
    }

    private boolean isMessageForCurrentConversation() {
        return (Boolean) messageParser.getValue(Message.CONVERSATION_ID);
    }
}
