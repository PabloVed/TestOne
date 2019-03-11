package com.example.testone;

    import android.app.Activity;
    import android.app.AlertDialog;
    import android.content.DialogInterface;
    import android.content.Intent;
    import android.graphics.Bitmap;
    import android.graphics.BitmapFactory;
    import android.graphics.ColorMatrixColorFilter;
    import android.graphics.Matrix;
    import android.graphics.drawable.BitmapDrawable;
    import android.graphics.drawable.Drawable;
    import android.media.MediaScannerConnection;
    import android.net.Uri;
    import android.os.Bundle;
    import android.os.Environment;
    import android.provider.MediaStore;
    //import android.support.v7.widget.GridLayoutManager;
    //import android.support.v7.widget.RecyclerView;
    import android.util.Log;
    import android.view.View;
    import android.widget.Button;
    //import android.widget.EditText;
    import android.widget.ImageView;
    //import android.widget.LinearLayout;
    import android.widget.Toast;
    import java.io.ByteArrayOutputStream;
    import java.io.File;
    //import java.io.FileInputStream;
    import java.io.FileOutputStream;
    import java.io.IOException;
    //import java.io.InputStream;
    //import java.io.OutputStream;
    //import java.util.ArrayList;
    import java.util.Calendar;

public class MainActivity extends Activity implements View.OnClickListener {
   /* private final String image_paths[] = {
            "",
            "",
            ""
    };
    private final String image_titles[] = {
            "Img1",
            "Img2",
            "Img3",
    };*/
    //private ArrayList<CreateList> image = new ArrayList<>();
    Button btnRotate;
    Button btnInvertColors;
    Button btnMirrorImage;
    //LinearLayout gallery;
    ImageView imageView;
    ImageView imageViewResult;
    ImageView imageView3, imageView4, imageView5, imageView6;  //Галлерея, сделанная по-тупому. Хотелось бы через Recycler View, но не разобрался до конца.
    private static final String IMAGE_DIRECTORY = "/Pictures/";
    private static final String TEMP_IMAGE_DIR = "/Pictures/temp";
    private int GALLERY = 1, CAMERA = 2;
    private int option; // переменная только чтобы отслеживать из какой картинки в галерее мы открыли меню. Плохая практика, но я не вижу как иначе мне пробрасывать номер картинки.
    private static boolean inverted = false;
    private int gallerypos = 0;
    String cachefilepath[] ={"","","",""};

    //Маска для получения негатива фотки
    private static final float[] NEGATIVE = {
            -1.0f,     0,     0,    0, 255, // red
            0, -1.0f,     0,    0, 255, // green
            0,     0, -1.0f,    0, 255, // blue
            0,     0,     0, 1.0f,   0  // alpha
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Все нажитое непосильным трудом на активити
        //Закомментировал заготовку для recyclerView
        /*RecyclerView recyclerView = (RecyclerView)findViewById(R.id.imagegallery);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(),3);
        recyclerView.setLayoutManager(layoutManager);
        ArrayList<CreateList> createLists = prepareData(image);
        MyAdapter adapter = new MyAdapter(getApplicationContext(), createLists);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        */
        btnRotate = (Button) findViewById(R.id.btnRotate);
        btnInvertColors = (Button) findViewById(R.id.btnInvertColors);
        btnMirrorImage = (Button) findViewById(R.id.btnMirrorImage);
        imageView = (ImageView) findViewById(R.id.imageView);
        imageViewResult = (ImageView) findViewById(R.id.imageViewResult);
        imageView3 = (ImageView) findViewById(R.id.imageView3);
        imageView4 = (ImageView) findViewById(R.id.imageView4);
        imageView5 = (ImageView) findViewById(R.id.imageView5);
        imageView6 = (ImageView) findViewById(R.id.imageView6);

        //Обрабатываем нажатия
        btnMirrorImage.setOnClickListener(this);
        btnInvertColors.setOnClickListener(this);
        btnRotate.setOnClickListener(this);

        //Отдельно обрабатываем нажатие на imageView
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPictureDialog();
            }
        });

        imageViewResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageViewResult.isDirty()){
                imageViewResult.buildDrawingCache();
                Bitmap result = imageViewResult.getDrawingCache();
                saveImage(result, IMAGE_DIRECTORY);
                Toast.makeText(getApplicationContext(), "Image Saved!", Toast.LENGTH_SHORT).show();}
            }
        });

        imageView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                option = 3;
                galleryOptionDialog();
            }
        });
        imageView4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                option = 2;
                galleryOptionDialog();
            }
        });
        imageView5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                option = 1;
                galleryOptionDialog();
            }
        });
        imageView6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                option =0;
                galleryOptionDialog();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnRotate:{//Вращение через получение Bitmap из ImageViewResult
                Bitmap bInput= ((BitmapDrawable)imageViewResult.getDrawable()).getBitmap();
                float degrees = 90;//Угол вращения
                Matrix matrix = new Matrix();
                matrix.postRotate(degrees);
                Bitmap bOutput = Bitmap.createBitmap(bInput, 0, 0, bInput.getWidth(), bInput.getHeight(), matrix, true);
                imageViewResult.setImageBitmap(bOutput);
                // scaleImage(imageViewResult);
                //отображение в галерею
                chooseGallery();
                cachefilepath[gallerypos] = saveImage(bOutput, TEMP_IMAGE_DIR);
                break;
            }

            case R.id.btnInvertColors:{ //Негатив
                if(!inverted) {
                    imageViewResult.getDrawable().setColorFilter(new ColorMatrixColorFilter(NEGATIVE));
                    //иначе не сохранит изменения
                    imageViewResult.buildDrawingCache();
                    Bitmap result = imageViewResult.getDrawingCache();
                    inverted = true;
                    chooseGallery();
                    cachefilepath[gallerypos] = saveImage(result, TEMP_IMAGE_DIR);
                }
                else {
                    imageViewResult.getDrawable().clearColorFilter();
                    imageViewResult.buildDrawingCache();
                    Bitmap result = imageViewResult.getDrawingCache();
                    inverted = false;
                    chooseGallery();
                    cachefilepath[gallerypos] = saveImage(result, TEMP_IMAGE_DIR);
                }
                break;
            }

            case R.id.btnMirrorImage:{ //Зеркалирование
                Matrix m = new Matrix();
                m.preScale(-1, 1);
                Bitmap bInput= ((BitmapDrawable)imageViewResult.getDrawable()).getBitmap();
                Bitmap bOutput =  Bitmap.createBitmap(bInput, 0, 0, bInput.getWidth(), bInput.getHeight(), m, false);
                imageViewResult.setImageBitmap(bOutput);
                chooseGallery();
                cachefilepath[gallerypos] = saveImage(bOutput, TEMP_IMAGE_DIR);
            }
            break;
        }
    }

    private void showPictureDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Image source");
        String[] pictureDialogItems = {"Photo from gallery", "Photo from camera"};
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                choosePhotoFromGallery();
                                break;
                            case 1:
                                takePhotoFromCamera();
                                break;

                        }
                    }
                });
        pictureDialog.show();
    }


    public void choosePhotoFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY);
    }

    private void takePhotoFromCamera() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA);
    }

    public void chooseGallery(){
        switch (gallerypos){
            case 0: {
                imageView6.setImageBitmap(((BitmapDrawable)imageViewResult.getDrawable()).getBitmap());
                scaleImage(imageView6);
                gallerypos++;
                break;}
            case 1:{imageView5.setImageBitmap(((BitmapDrawable)imageViewResult.getDrawable()).getBitmap());
                scaleImage(imageView5);
                gallerypos++;
                break;}
            case 2:{imageView4.setImageBitmap(((BitmapDrawable)imageViewResult.getDrawable()).getBitmap());
                scaleImage(imageView4);
                gallerypos++;
                break;}
            case 3:{imageView3.setImageBitmap(((BitmapDrawable)imageViewResult.getDrawable()).getBitmap());
                scaleImage(imageView3);
                gallerypos=0;
                break;}
        }
    }

    public void galleryOptionDialog(){

        AlertDialog.Builder galleryDialog = new AlertDialog.Builder(this);
        galleryDialog.setTitle("Select action");
        String[] pictureDialogItems = {"Save this", "Continue work with this"};
        galleryDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                saveGalleryItem(option);
                                break;
                            case 1:
                                moveToActive(option);
                                break;
                        }
                    }
                });
        galleryDialog.show();
    }

    public void saveGalleryItem(int i){ // перемещаем файл из temp
        File picture = new File(cachefilepath[i]);
        picture.renameTo(new File("/storage/sdcard/Pictures/" + Calendar.getInstance().getTimeInMillis() + ".jpg"));
    }

    public void moveToActive(int i){ // перемещаем в редактирование
        File imgFile = new  File(cachefilepath[i]);
        if(imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            imageViewResult.setImageBitmap(myBitmap);
            imageView.setImageBitmap(myBitmap);
            deleteTempFiles(new File(TEMP_IMAGE_DIR));
        }
    }

    private void scaleImage(ImageView view){// Иначе получается ерунда, когда мы в галерею пытаемся изображение сунуть.
        // Bitmap form imageView
        Bitmap bitmap = null;
        Drawable drawing = view.getDrawable();
        bitmap = ((BitmapDrawable) drawing).getBitmap();

        // С чем придется работать
        int width = 0;
        width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int bounding = dpToPx(250);
        Log.i("Test", "original width = " + Integer.toString(width));
        Log.i("Test", "original height = " + Integer.toString(height));
        Log.i("Test", "bounding = " + Integer.toString(bounding));

        // Determine how much to scale: the dimension requiring less scaling is
        // closer to the its side. This way the image always stays inside your
        // bounding box AND either x/y axis touches it.
        float xScale = ((float) bounding) / width;
        float yScale = ((float) bounding) / height;
        float scale = (xScale <= yScale) ? xScale : yScale;
        Log.i("Test", "xScale = " + Float.toString(xScale));
        Log.i("Test", "yScale = " + Float.toString(yScale));
        Log.i("Test", "scale = " + Float.toString(scale));

        // Create a matrix for the scaling and add the scaling data
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        //Новый Bitmap с конвертированием для ImageViewer
        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        width = scaledBitmap.getWidth();
        height = scaledBitmap.getHeight();
        BitmapDrawable result = new BitmapDrawable(scaledBitmap);
        Log.i("Test", "scaled width = " + Integer.toString(width));
        Log.i("Test", "scaled height = " + Integer.toString(height));

        // Применяем отмасштабированный
        view.setImageDrawable(result);
        Log.i("Test", "done");
    }

    private int dpToPx(int dp) {
        float density = getApplicationContext().getResources().getDisplayMetrics().density;
        return Math.round((float)dp * density);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {
            return;
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                Uri contentURI = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                    imageView.setImageBitmap(bitmap);
                    imageViewResult.setImageBitmap(bitmap);

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Failed!", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (requestCode == CAMERA) {
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(thumbnail);
            imageViewResult.setImageBitmap(thumbnail);
            saveImage(thumbnail, IMAGE_DIRECTORY);
            Toast.makeText(getApplicationContext(), "Image Saved!", Toast.LENGTH_SHORT).show();
        }
    }

   /* private ArrayList<CreateList> prepareData(ArrayList<CreateList> image){
        ArrayList<CreateList> theimage = new ArrayList<>();
        for(int i = 0; i< image_titles.length; i++){
            CreateList createList = new CreateList();
            createList.setImage_title(image_titles[i]);
            createList.setImage_ID(image_paths[i]);
            createList.getImage();
            theimage.add(createList);
        }
        return theimage;
    }*/
    public String saveImage(Bitmap myBitmap, String filepath) {

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File wallpaperDirectory = new File(Environment.getExternalStorageDirectory() + filepath);
        if (!wallpaperDirectory.exists()) {  // have the object build the directory structure, if needed.
            wallpaperDirectory.mkdirs();
        }

        try {
            File f = new File(wallpaperDirectory, Calendar.getInstance().getTimeInMillis() + ".jpg");
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(this,
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            Log.d("TAG", "File Saved::---&gt;" + f.getAbsolutePath());
            return f.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }


    @Override protected void onDestroy() {
        super.onDestroy();
        File directory = new File(TEMP_IMAGE_DIR);
        deleteTempFiles(directory);

    }

    private boolean deleteTempFiles(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        deleteTempFiles(f);
                    } else {
                        f.delete();
                    }
                }
            }
        }
        return file.delete();
    }
}