/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androidthings.doorbell;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

//import com.firebase.client.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    //private DoorbellEntryAdapter mAdapter;
    //private FirebaseStorage mFirebaseStorage;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Reference for doorbell events from embedded device
       // DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("logs");
       // StorageReference imageRef = FirebaseStorage.getInstance().getReference();

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("logs/OCRIMAGE");


        //Bitmap bitmap;

        final ImageView imageView = findViewById(R.id.imageViewOCRIMAGE);

        /*imageRef.child("OCR_IMAGE.jpeg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                try
                {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

                    //ocrTextRecognization(bitmap);

                    imageView.setImageBitmap(bitmap);

                }
                catch(Exception ex)
                {
                    System.out.println(ex.getMessage());
                }

            }
        });
*/

        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                if (snapshot.getValue() != null) {

                    String imageURL = snapshot.child("image").getValue().toString();

                    imageURL = imageURL.replaceAll("\\]","");
                    imageURL = imageURL.replaceAll("\\[","");


					/*To use Picasso, you have to add this dependency:

					 implementation 'com.squareup.picasso:picasso:2.5.2'

					 if any latest dependency is available, use it.
					*/

                    //Picasso.with(MainActivity.this).load(imageURL).resize(320, 240).into(imageView);



					/*THis should help you get the bitmap object from the imageURL. Just try this and convert the imageURL to URI, and use
					that uri to convert it into the bitmap like this:
                    */
					//Uri uri = Uri.parse(imageURL);

					//now since you have obtained the uri object, you can try this and check if the bitmap works.

//                    Bitmap bitmap;

                    Picasso.with(MainActivity.this)
                            .load(imageURL)
                            .into(new Target() {
                                @Override
                                public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                                    /* Save the bitmap or do something with it here */

                                    // Set it in the ImageView
                                    imageView.setImageBitmap(bitmap);

                                    ocrTextRecognization(bitmap);

                                }

                                @Override
                                public void onBitmapFailed(Drawable errorDrawable) {

                                }

                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {

                                }
                            });


                    /*try
                    {
                       bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        imageView.setImageBitmap(bitmap);
                    }
                    catch(Exception ex) {

                        Toast.makeText(MainActivity.this,"SOmething is wrong with the bitmap",Toast.LENGTH_SHORT);
                    }*/


					//try the above two lines of code. If your Picasso.with() works, then this should also probably work.



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Log.v("Error at onCancelled", databaseError.toString());

            }
        });


        //mRecyclerView = (RecyclerView) findViewById(R.id.doorbellView);
        // Show most recent items at the top
        //LinearLayoutManager layoutManager =
          //      new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true);
        //mRecyclerView.setLayoutManager(layoutManager);

        // Initialize RecyclerView adapter
        //mAdapter = new DoorbellEntryAdapter(this, ref);
        //mRecyclerView.setAdapter(mAdapter);
    }

    public void ocrTextRecognization(Bitmap bitmap)
    {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();

        //ImageView imageView = findViewById(R.id.imageViewOCRIMAGE);
        final TextView textView = findViewById(R.id.textOCR);


        Log.d("TEXT OCR", "TEXT CHECK - 1");
        //imageView.setImageBitmap(bitmap);

        //final String text;

        Task<FirebaseVisionText> result =
                detector.processImage(image)
                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                // Task completed successfully
                                // ...

                                for(FirebaseVisionText.TextBlock block: firebaseVisionText.getTextBlocks())
                                {

                                    String text = block.getText();

                                    textView.setText(text);

                                    Log.d("TEXT OCR", "TEXT CHECK - 2");

                                    Log.d("OCR Text: ", text);



                                }

                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                        Log.d("Task Exception:", e.getMessage());
                                    }
                                });




    }

    @Override
    public void onStart() {
        super.onStart();

       /* // Initialize Firebase listeners in adapter
        mAdapter.startListening();

        // Make sure new events are visible
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount());
            }
        });*/
    }

    @Override
    public void onStop() {
        super.onStop();

        // Tear down Firebase listeners in adapter
        //mAdapter.stopListening();
    }

}
