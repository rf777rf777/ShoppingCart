package com.programming.syashin.facebooklogin;

import java.io.Serializable;

/**
 * Created by USER on 2016/3/19.
 */
public class Item implements Serializable{

    private String name;
    private int price;
    private String imageBase64;
    private String userUid;
    private String Key;

    public String getName(){
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }


    public String getKey() {
        return Key;
    }

    public void setKey(String key) {
        Key = key;
    }
}
