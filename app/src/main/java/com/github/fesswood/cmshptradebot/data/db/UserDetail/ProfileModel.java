package com.github.fesswood.cmshptradebot.data.db.UserDetail;

import io.realm.RealmObject;

/**
 * Created by fesswood on 02.06.16.
 */
public class ProfileModel extends RealmObject {

    String login;
    String password;

    public ProfileModel() {
    }

    public ProfileModel(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
