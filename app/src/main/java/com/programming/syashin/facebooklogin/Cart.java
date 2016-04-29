package com.programming.syashin.facebooklogin;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by USER on 2016/3/26.
 */
public class Cart {

    private static Set<String> itemKeys = new HashSet<>();

    public static void addToCart(String key){
        itemKeys.add(key);
    }

    public static void removeFromCart(String key){
        itemKeys.remove(key);
    }

    public static Set<String> getItemKeys() {
        return itemKeys;
    }
}
