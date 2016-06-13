package com.github.fesswood.cmshptradebot.data.db.UserDetail;

import io.realm.Realm;

/**
 * Created by fesswood on 02.06.16.
 */
public class ProfileRepository {

    public static void saveProfile(String login, String password) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            realm1.delete(ProfileModel.class);
            realm.copyToRealm(new ProfileModel(login,
                    password));
        });
        realm.close();
    }

    public static ProfileModel getProfile() {
        Realm realm = Realm.getDefaultInstance();
        ProfileModel profileModel = realm.copyFromRealm(realm.where(ProfileModel.class).findFirst());
        realm.close();
        return profileModel;
    }

    public static boolean isProfileExist() {
        Realm realm = Realm.getDefaultInstance();
        long profileModel = realm.where(ProfileModel.class).count();
        realm.close();
        return profileModel > 0;
    }
}
