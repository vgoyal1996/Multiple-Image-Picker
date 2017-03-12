package com.example.vipul.myapplication;

import android.app.AlertDialog;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private GridView gridView;
    private static final int REQUEST_CAMERA=1;
    private static final int SELECT_FILE=2;
    private GridViewAdapter gridAdapter;
    private byte[] finalImage = null;
    private ArrayList<ImageItem> imageList;
    private boolean isGalleryFull = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gridView = (GridView) findViewById(R.id.gridView);
        imageList = new ArrayList<>();
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_camera_alt_black_48dp);
        imageList.add(new ImageItem(largeIcon));
        gridAdapter = new GridViewAdapter(this, R.layout.grid_item_layout,imageList);
        gridView.setAdapter(gridAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!isGalleryFull) {
                    if (position != imageList.size() - 1) {
                        ImageItem item = (ImageItem) parent.getItemAtPosition(position);
                        //Create intent
                        Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                        intent.putExtra("image", item.getImage());
                        //Start details activity
                        startActivity(intent);
                    } else {
                        selectImage();
                    }
                }
                else{
                    ImageItem item = (ImageItem) parent.getItemAtPosition(position);
                    //Create intent
                    Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                    intent.putExtra("image", item.getImage());
                    //Start details activity
                    startActivity(intent);
                }
            }
        });

    }

    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Gallery", "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals("Choose from Gallery")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(
                            Intent.createChooser(intent, "android.intent.action.SEND_MULTIPLE"), SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

                File destination = new File(Environment.getExternalStorageDirectory(),
                        System.currentTimeMillis() + ".jpg");

                FileOutputStream fo;
                try {
                    destination.createNewFile();
                    fo = new FileOutputStream(destination);
                    fo.write(bytes.toByteArray());
                    fo.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                finalImage = bytes.toByteArray();

            } else if (requestCode == SELECT_FILE) {
                ImageItem finalItem = imageList.get(imageList.size() - 1);
                imageList.remove(imageList.size() - 1);
                if (data.getClipData() == null) {
                    Uri selectedImageUri = data.getData();
                    String[] projection = {MediaStore.MediaColumns.DATA};
                    CursorLoader cursorLoader = new CursorLoader(this, selectedImageUri, projection, null, null,
                            null);
                    Cursor cursor = cursorLoader.loadInBackground();
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                    cursor.moveToFirst();

                    String selectedImagePath = cursor.getString(column_index);
                    Bitmap bm;
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(selectedImagePath, options);
                    final int REQUIRED_SIZE = 200;
                    int scale = 1;
                    while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                            && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                        scale *= 2;
                    options.inSampleSize = scale;
                    options.inJustDecodeBounds = false;
                    bm = BitmapFactory.decodeFile(selectedImagePath, options);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.PNG, 100, bos);
                    finalImage = bos.toByteArray();
                    if (!isGalleryFull)
                        imageList.add(new ImageItem(bm));
                    else
                        Toast.makeText(MainActivity.this, "only 8 items allowed", Toast.LENGTH_SHORT).show();
                    if (imageList.size() == 8)
                        isGalleryFull = true;
                } else {
                    for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                        Uri selectedImageUri = data.getClipData().getItemAt(i).getUri();
                        String[] projection = {MediaStore.MediaColumns.DATA};
                        CursorLoader cursorLoader = new CursorLoader(this, selectedImageUri, projection, null, null,
                                null);
                        Cursor cursor = cursorLoader.loadInBackground();
                        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                        cursor.moveToFirst();

                        String selectedImagePath = cursor.getString(column_index);
                        Bitmap bm;
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(selectedImagePath, options);
                        final int REQUIRED_SIZE = 200;
                        int scale = 1;
                        while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                                && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                            scale *= 2;
                        options.inSampleSize = scale;
                        options.inJustDecodeBounds = false;
                        bm = BitmapFactory.decodeFile(selectedImagePath, options);
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        bm.compress(Bitmap.CompressFormat.PNG, 100, bos);
                        finalImage = bos.toByteArray();
                        if (!isGalleryFull)
                            imageList.add(new ImageItem(bm));
                        else {
                            Toast.makeText(MainActivity.this, "only 8 items allowed", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        if (imageList.size() == 8)
                            isGalleryFull = true;
                    }
                }
                if (!isGalleryFull)
                    imageList.add(finalItem);
                gridAdapter.notifyDataSetChanged();
            }
        }
    }
}
