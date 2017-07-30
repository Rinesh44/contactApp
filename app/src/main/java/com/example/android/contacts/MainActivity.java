package com.example.android.contacts;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {
    private TextInputEditText inputname;
    private TextInputEditText inputphone;
    private TextInputEditText inputmobile;
    private TextInputEditText inputemail;
    private TextInputEditText inputaddress;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int PICK_IMAGE = 1;
    private TextView inputimage;
    private static final int REQUEST_STORAGE_ACCESS = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    String fileName;
    public String encodedImage;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    public static String userId;
    private Button save;
    private TextView result;
    private ImageView imageView;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Uri uri = data.getData();
        if (uri != null) {
            if (uri.toString().startsWith("file:")) {
                fileName = uri.getPath();
            } else { // uri.startsWith("content:")

                Cursor c = getContentResolver().query(uri, null, null, null, null);

                if (c != null && c.moveToFirst()) {

                    int id = c.getColumnIndex(MediaStore.Images.Media.DATA);
                    if (id != -1) {
                        fileName = c.getString(id);
                    }
                }
            }
        }

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = 2;  //you can also calculate your inSampleSize
        options.inJustDecodeBounds = false;
        options.inTempStorage = new byte[16 * 1024];
        if (requestCode == PICK_IMAGE) {
            inputimage.setText(fileName);

            Bitmap bm = BitmapFactory.decodeFile(fileName, options);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
            byte[] b = baos.toByteArray();
            encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
            bm.recycle();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputname = (TextInputEditText) findViewById(R.id.TextName);
        inputphone = (TextInputEditText) findViewById(R.id.NumberPhone);
        inputmobile = (TextInputEditText) findViewById(R.id.NumberMobile);
        inputemail = (TextInputEditText) findViewById(R.id.TextEmail);
        inputaddress = (TextInputEditText) findViewById(R.id.TextAddress);
        save = (Button) findViewById(R.id.save);
        result = (TextView) findViewById(R.id.result);
        imageView = (ImageView) findViewById(R.id.contactImage);

        inputimage = (TextView) findViewById(R.id.imagebtn);

        mFirebaseInstance = FirebaseDatabase.getInstance();

        // get reference to 'users' node
        mFirebaseDatabase = mFirebaseInstance.getReference("users");

        // store app title to 'app_title' node
        mFirebaseInstance.getReference("app_title").setValue("Realtime Database");


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_STORAGE_ACCESS);

        }

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mName = inputname.getText().toString();
                String mPhone = inputphone.getText().toString();
                String mMoble = inputmobile.getText().toString();
                String mEmail = inputemail.getText().toString();
                String mAddress = inputaddress.getText().toString();
                String mImage = encodedImage;

                // Check for already existed userId
                if (TextUtils.isEmpty(userId)) {
                    createUser(mName, mEmail, mPhone, mMoble, mAddress, mImage);
                } else {
                    createUser(mName, mEmail, mPhone, mMoble, mAddress, mImage);
                }
                byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
                final Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                Bitmap thumbnail = ThumbnailUtils.extractThumbnail(bitmap, 150, 150);
                imageView.setImageBitmap(thumbnail);

            }
        });

        inputimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }

        });

    }

    /**
     * Creating new user node under 'users'
     */
    private void createUser(String name, String email, String phone, String mobile, String address, String image) {

        if (TextUtils.isEmpty(userId)) {
            userId = mFirebaseDatabase.push().getKey();
        }

        Contact user = new Contact(name, email, phone, mobile, address, image);

        mFirebaseDatabase.child(userId).setValue(user);

        addUserChangeListener();
    }

    /**
     * User data change listener
     */
    private void addUserChangeListener() {
        // User data change listener
        mFirebaseDatabase.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Contact user = dataSnapshot.getValue(Contact.class);

                // Check for null
                if (user == null) {
                    Log.e(TAG, "User data is null!");
                    return;
                }

                Log.e(TAG, "User data is changed!" + user.Name + ", " + user.Email);


                result.setText(user.Name + "\n" + user.Address + "\n" + user.Email + "\n" + user.Mobile + "\n" + user.Phone);
                // clear edit text
                inputaddress.setText("");
                inputname.setText("");
                inputemail.setText("");
                inputphone.setText("");
                inputimage.setText("Browse...");
                inputmobile.setText("");

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read user", error.toException());
            }
        });

    }

}

