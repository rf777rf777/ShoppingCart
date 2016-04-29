package com.programming.syashin.facebooklogin;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;
    private AuthData authData;
    ListView listView;
    Firebase firebase;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(this.getApplicationContext());
        Firebase.setAndroidContext(this.getApplicationContext());

        setContentView(R.layout.activity_main);

        //final Firebase firebase = new Firebase(Config.FIRE_BASE_URL);
        firebase = new Firebase(Config.FIRE_BASE_URL);

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    Toast.makeText(MainActivity.this, "還沒登入facebook", Toast.LENGTH_SHORT).show();
                    firebase.unauth();
                } else {
                    //Toast.makeText(MainActivity.this, "已經登入facebook", Toast.LENGTH_SHORT).show();
                    firebase.authWithOAuthToken("facebook", currentAccessToken.getToken(), new Firebase.AuthResultHandler() {
                                @Override
                                public void onAuthenticated(AuthData authData) {
                                    Toast.makeText(MainActivity.this, "已經登入firebase with facebook", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onAuthenticationError(FirebaseError firebaseError) {
                                    Toast.makeText(MainActivity.this, "登入firebase失敗", Toast.LENGTH_SHORT).show();
                                }
                            }

                    );
                }
            }
        };

        firebase.addAuthStateListener(new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                MainActivity.this.authData = authData;

                View layoutUserActions = findViewById(R.id.layoutUserActions);

                ImageView imageView = (ImageView) findViewById(R.id.imageViewUserPicture);

                if (authData == null) {
                    //Toast.makeText(MainActivity.this, "還沒登入firebase", Toast.LENGTH_SHORT).show();
                    imageView.setImageResource(0);
                    layoutUserActions.setVisibility(View.GONE);

                } else {
                    //Toast.makeText(MainActivity.this, "已經登入firebase", Toast.LENGTH_SHORT).show();

                    String imageUrl = (String) authData.getProviderData().get("profileImageURL");
                    String userName = (String) authData.getProviderData().get("displayName");
                    String userId = authData.getUid();

                    Picasso.with(MainActivity.this).load(imageUrl).into(imageView);
                    firebase.child("users").child(userId).child("name").setValue(userName);

                    layoutUserActions.setVisibility(View.VISIBLE);

                    findOrders();

                }
            }
        });


        listView = (ListView) findViewById(R.id.listView);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);


        //以下為登入FB
        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.FBlogin);

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(MainActivity.this, "登入成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(MainActivity.this, "登入取消", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(MainActivity.this, "登入失敗", Toast.LENGTH_SHORT).show();
            }
        });

        firebase.child("items").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ObjectMapper objectMapper = new ObjectMapper();

                List<Item> items = new ArrayList<Item>();
                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {

                    Item item = objectMapper.convertValue(itemSnapshot.getValue(), Item.class);


                    String key = itemSnapshot.getKey();
                    File file = new File(getCacheDir(), key);

                    if (file.exists() == false) {
                        try {
                            byte[] bytes = Base64.decode(item.getImageBase64(), Base64.DEFAULT);
                            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

                            FileOutputStream fos = new FileOutputStream(file);
                            IOUtils.copy(bais, fos);
                        } catch (IOException e) {

                        }
                    }

                    item.setKey(key);
                    items.add(item);

                }

                // ListView listView = (ListView) findViewById(R.id.listView);
                // ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, itemNames);
                ItemListAdapter adapter = new ItemListAdapter();
                adapter.setItems(items);
                listView.setAdapter(adapter);

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        /*
        //取得FB KEY HASH------------------------
        PackageInfo info = null;
        try{
            try {
                info = getPackageManager().getPackageInfo("com.programming.syashin.facebooklogin",PackageManager.GET_SIGNATURES);
            } catch (PackageManager.NameNotFoundException e2) {
                e2.printStackTrace();
                Log.e("name not found", e2.toString());
            }
            for(Signature signature : info.signatures)
            {      MessageDigest md;
                try {
                    md =MessageDigest.getInstance("SHA");
                    md.update(signature.toByteArray());
                    String KeyResult =new String(Base64.encode(md.digest(),0));//String something = new String(Base64.encodeBytes(md.digest()));
                    Log.e("hash key", KeyResult);
                    Toast.makeText(this,"My FB Key is \n"+ KeyResult , Toast.LENGTH_LONG ).show();

                } catch (NoSuchAlgorithmException e1) {
                    e1.printStackTrace();
                    Log.e("no such an algorithm", e1.toString());

                }

            }
        }
        catch(Exception e)
        {
            Log.e("exception", e.toString());
        }
        //取得FB KEY HASH END-----------------------------

        */

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Item item = (Item) parent.getItemAtPosition(position);

                boolean canAddToCart = true;
                if (authData != null && item.getUserUid().equals(authData.getUid())) {
                    canAddToCart = false;
                }

                Intent intent = new Intent(MainActivity.this, ItemDetailActivity.class);
                intent.putExtra("item", item);
                intent.putExtra("canAddToCart", canAddToCart);

                startActivity(intent);

            }


        });


    }

    @Override
    protected void onResume() {
        super.onResume();

        View layoutCart = findViewById(R.id.layoutCart);
        if (Cart.getItemKeys().isEmpty()) {
            layoutCart.setVisibility(View.GONE);
        } else {
            layoutCart.setVisibility(View.VISIBLE);

            TextView textViewCartCount = (TextView) findViewById(R.id.textViewCartCount);
            //textViewCartCount.setText("購物車("+Cart.getItemKeys().size()+")");
            textViewCartCount.setText(getString(R.string.info_cart_count, Cart.getItemKeys().size()));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void goAddItem(View view) {

        //檢查是否有登入
        if (authData == null) {
            Toast.makeText(MainActivity.this, "請先登入，才可以上架商品", Toast.LENGTH_SHORT).show();
        } else {
            Intent it = new Intent(this, addItemActivity.class);

            //把FB的使用者ID傳過去
            it.putExtra("userUid", authData.getUid());
            startActivity(it);
        }
    }

    public void goCartList(View view) {
        if (authData == null) {
            Toast.makeText(MainActivity.this, "請先登入，才可以結帳", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(MainActivity.this, CartListActivity.class);
            intent.putExtra("userUid", authData.getUid());
            startActivity(intent);
        }
    }

    private void findOrders() {

        firebase.child("orders")
                .orderByChild("buyerUserUid")
                .equalTo(authData.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Button buttonBuyingOrders = (Button) findViewById(R.id.buttonBuyingOrders);
                        buttonBuyingOrders.setText(getString(R.string.info_buying, dataSnapshot.getChildrenCount()));
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });

        firebase.child("orders")
                .orderByChild("item/userUid")
                .equalTo(authData.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Button buttonSellingOrders = (Button) findViewById(R.id.buttonSellingOrders);
                        buttonSellingOrders.setText(getString(R.string.info_selling, dataSnapshot.getChildrenCount()));
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });

    }

    public void goOrderList(View view) {
        Intent intent = new Intent(MainActivity.this, OrderListActivity.class);
        intent.putExtra("userUid", authData.getUid());

        if (view.getId() == R.id.buttonBuyingOrders) {
            intent.putExtra("mode", "buying");
        } else if (view.getId() == R.id.buttonSellingOrders) {
            intent.putExtra("mode", "selling");
        }

        startActivity(intent);
    }


    class ItemListAdapter extends BaseAdapter {

        List<Item> items;

        public void setItems(List<Item> items) {
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.listitem, null);
            }

            Item item = (Item) getItem(position);
            TextView textViewItemName = (TextView) convertView.findViewById(R.id.textViewItemName);
            TextView textViewItemPrice = (TextView) convertView.findViewById(R.id.textViewItemPrice);
            ImageView imageView = (ImageView) convertView.findViewById(R.id.imageViewItemPicture);

            textViewItemName.setText(item.getName());
            textViewItemPrice.setText(String.valueOf(item.getPrice()));

            Picasso.with(MainActivity.this).load(new File(getCacheDir(), item.getKey())).into(imageView);

            return convertView;
        }
    }

}


