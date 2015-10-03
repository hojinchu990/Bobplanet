package kr.bobplanet.backend.model;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 * Created by hkjinlee on 2015. 10. 3..
 */
@Entity
public class User {
    @Id
    String id;

    String nickName;

    public User(String id) {
        this.id = id;
    }
}