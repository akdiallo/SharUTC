package fr.utc.lo23.sharutc.controler.command.account;

import fr.utc.lo23.sharutc.controler.command.Command;
import fr.utc.lo23.sharutc.model.userdata.UserInfo;

/**
 * TODO: add comments
 */
public interface AccountCreationCommand extends Command {

    /**
     *
     * @return
     */
    public UserInfo getUserInfo();

    /**
     *
     * @param userInfo
     */
    public void setUserInfo(UserInfo userInfo);
}