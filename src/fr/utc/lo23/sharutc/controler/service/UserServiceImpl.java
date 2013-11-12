package fr.utc.lo23.sharutc.controler.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import fr.utc.lo23.sharutc.model.AppModel;
import fr.utc.lo23.sharutc.model.ErrorMessage;
import fr.utc.lo23.sharutc.model.userdata.ActivePeerList;
import fr.utc.lo23.sharutc.model.userdata.Category;
import fr.utc.lo23.sharutc.model.userdata.Contact;
import fr.utc.lo23.sharutc.model.userdata.Peer;
import fr.utc.lo23.sharutc.model.userdata.Profile;
import fr.utc.lo23.sharutc.model.userdata.UserInfo;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.utc.lo23.sharutc.controler.command.account.IntegrateConnectionCommandImpl;
import fr.utc.lo23.sharutc.model.userdata.KnownPeerList;

/**
 * {@inheritDoc}
 */
@Singleton
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory
            .getLogger(UserServiceImpl.class);
    private final AppModel appModel;
    private final FileService fileService;
    //FIXME: un service n'est pas un conteneur d'objet, l'instance de profile
    // est à supprimer, j'imagine qu'elle devrait se trouver dans une commande
    private Profile profile;

    @Inject
    public UserServiceImpl(AppModel appModel, FileService fileService) {
        this.appModel = appModel;
        this.profile = appModel.getProfile();
        this.fileService = fileService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProfileFiles() {
        if (profile != null) {
            fileService.saveToFile(SharUTCFile.PROFILE, profile);
        } else {
            log.warn("Can't save current profile(null)");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addContact(Contact contact) {
        Long contactId = contact.getUserInfo().getPeerId();
        if (!contact.isInPublic()) {
            profile.getContacts().findById(contactId).addCategoryId(Category.PUBLIC_CATEGORY_ID);
        } 
        else {
            log.warn("This contact already exists");
            ErrorMessage nErrorMessage = new ErrorMessage("This contact already exists");
            appModel.getErrorBus().pushErrorMessage(nErrorMessage);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteContact(Contact contact) {
        profile.getContacts().remove(contact);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createCategory(String categoryName) {
        Category c = new Category(categoryName);
        profile.getCategories().add(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteCategory(Category category) {
        if (category.getId() != Category.PUBLIC_CATEGORY_ID) {
            profile.getCategories().remove(category);
        } else {
            log.warn("Can't delete Category Public");
            ErrorMessage nErrorMessage = new ErrorMessage("Can't delete Category Public");
            appModel.getErrorBus().pushErrorMessage(nErrorMessage);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addContactToCategory(Contact contact, Category category) {
        Long contactId = contact.getUserInfo().getPeerId();
        Set<Integer> CategoriesIdsList = contact.getCategoryIds();

        if (!profile.getCategories().contains(category)) {
            if (CategoriesIdsList.contains(Category.PUBLIC_CATEGORY_ID))
                profile.getContacts().findById(contactId).removeCategoryId(Category.PUBLIC_CATEGORY_ID);

            profile.getContacts().findById(contactId).addCategoryId(category.getId());
        } 
        else {
            log.warn("This contact already exists in this category");
            ErrorMessage nErrorMessage = new ErrorMessage("This contact already exists in this category");
            appModel.getErrorBus().pushErrorMessage(nErrorMessage);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeContactFromCategory(Contact contact, Category category) {
        if (category.getId() != Category.PUBLIC_CATEGORY_ID)
            profile.getContacts().findById(contact.getUserInfo().getPeerId()).removeCategoryId(category.getId());
        else {
            log.warn("Can't remove contact from Public category");
            ErrorMessage nErrorMessage = new ErrorMessage("Can't remove contact from Public category");
            appModel.getErrorBus().pushErrorMessage(nErrorMessage);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createAndSetProfile(UserInfo userInfo) {
        Profile nProfile = new Profile(userInfo);
        appModel.setProfile(nProfile);
        this.profile = nProfile;
        this.saveProfileFiles();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connectionRequest(String login, String password) {
        Profile profileToConnect = fileService.readFile(SharUTCFile.PROFILE, Profile.class);
        if (profileToConnect != null && profileToConnect.getUserInfo() != null) {
            UserInfo userInfo = profileToConnect.getUserInfo();
            boolean success = userInfo.getLogin().equals(login)
                    && userInfo.getPassword().equals(password);
            if (success) {
                appModel.setProfile(profileToConnect);
                this.integrateConnection(appModel.getProfile().getUserInfo());
            } else {
                String message = "Incorrect login or password";
                log.warn(message);
                appModel.getErrorBus().pushErrorMessage(new ErrorMessage(message));
            }
        } else {
            String message = "Unknown user";
            log.warn(message);
            appModel.getErrorBus().pushErrorMessage(new ErrorMessage(message));
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateConnectedPeers(UserInfo userInfo) {
        ActivePeerList activePeerList = appModel.getActivePeerList();
        KnownPeerList knownPeerList = appModel.getProfile().getKnownPeerList();
        Peer newPeer = new Peer(userInfo.getPeerId(), userInfo.getLogin());
        activePeerList.update(newPeer);
        //TODO: also update known peer list
        knownPeerList.update(newPeer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeFromConnectedPeers(long peerId) {
        ActivePeerList activePeerList = appModel.getActivePeerList();
        Peer removePeer = activePeerList.getByPeerId(peerId);
        activePeerList.remove(removePeer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void integrateConnection(UserInfo userinfo) {
        IntegrateConnectionCommandImpl command = new IntegrateConnectionCommandImpl(appModel,this);
        command.setUserInfo(userinfo);
        command.execute();
    }

    @Override
    public Contact findContactByPeerId(Long peerId) {
        return profile.getContacts().findById(peerId);
    }

    @Override
    public void disconnectionRequest() {
        this.saveProfileFiles();
    }
}
