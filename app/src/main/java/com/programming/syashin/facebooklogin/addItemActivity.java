package com.programming.syashin.facebooklogin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;

public class addItemActivity extends AppCompatActivity {

    ImageView imageViewItemPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        imageViewItemPicture = (ImageView) findViewById(R.id.imageViewItemPicture);
    }

    public void getPicture(View view) {
        Intent it = new Intent(Intent.ACTION_PICK);
        it.setType("image/*");
        startActivityForResult(it, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {

            Uri uri = data.getData();
            Picasso.with(this).load(uri).into(imageViewItemPicture);
        } else if (requestCode == 2) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap bitmap = (Bitmap) extras.get("data");
                imageViewItemPicture.setImageBitmap(bitmap);
            }
        }
    }

    public void takePicture(View view) {
        Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(it, 2);
    }

    public void addItem(View view) {

        EditText editTextItemName = (EditText) findViewById(R.id.editTextItemName);
        EditText editTextItemPrice = (EditText) findViewById(R.id.editTextItemPrice);

        String itemName = editTextItemName.getText().toString();
        String itemPrice = editTextItemPrice.getText().toString();

        if (TextUtils.isEmpty(itemName)) {
            Toast.makeText(this, "請輸入商品名稱", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(itemPrice)) {
            Toast.makeText(this, "請輸入商品價格", Toast.LENGTH_SHORT).show();
            return;
        }

        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageViewItemPicture.getDrawable();
        if (bitmapDrawable == null) {
            Toast.makeText(this, "請提供商品圖片", Toast.LENGTH_SHORT).show();
            return;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Bitmap bitmap = bitmapDrawable.getBitmap();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        String imageBase64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

        int price = Integer.parseInt(itemPrice);
        String userUid = getIntent().getStringExtra("userUid");
        Firebase firebase = new Firebase("https://blinding-heat-8237.firebaseio.com");

        //        Map<String, Object> item = new HashMap<>();
        //
        //        item.put("name", itemName);
        //        item.put("price", price);
        //        item.put("imageBase64", imageBase64);
        //        item.put("userUid", userUid);

        Item item = new Item();
        item.setName(itemName);
        item.setPrice(price);
        item.setImageBase64(imageBase64);
        item.setUserUid(userUid);

        final ProgressDialog progressDialog = ProgressDialog.show(addItemActivity.this, "處理中", "請稍候", false, false);

        firebase.child("items").push().setValue(item, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                progressDialog.dismiss();
                finish();
            }
        });
    }
}
