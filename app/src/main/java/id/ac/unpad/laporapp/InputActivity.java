package id.ac.unpad.laporapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

import id.ac.unpad.laporapp.db.AppDatabase;
import id.ac.unpad.laporapp.http.ApiClient;
import id.ac.unpad.laporapp.http.ApiInterface;
import id.ac.unpad.laporapp.model.Lapor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InputActivity extends AppCompatActivity {

    //Deklarasi variabel sesuasi tipe komponen
    ImageView imgLapor;
    EditText txtKeterangan,txtLokasi;
    RadioGroup rbGroup;
    RadioButton rbPileg,rbPilpres;
    Button btnSimpan,btnHapus,btnKirim;
    //deklarasi variabel lapor (null)
    Lapor lapor;
    //foto
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int GALLERY_REQUEST_CODE = 200;
    private Uri file;
    private String imagePath;
    //upload ke server
    private ProgressDialog progressDialog;
    private ApiInterface api;
    //untuk GPS
    GPSTracker gps;
    double latitude;
    double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);
        //inisialisasi
        imgLapor = findViewById(R.id.img_lapor);
        txtKeterangan = findViewById(R.id.txt_keterangan);
        txtLokasi = findViewById(R.id.txt_lokasi);
        rbGroup = findViewById(R.id.rb_group);
        rbPileg = findViewById(R.id.rb_pileg);
        rbPilpres = findViewById(R.id.rb_pilpres);
        btnSimpan = findViewById(R.id.btn_simpan);
        btnHapus = findViewById(R.id.btn_hapus);
        btnKirim = findViewById(R.id.btn_kirim);

        gps = new GPSTracker(InputActivity.this);
        if(gps.canGetLocation()){
            //jika gps enable
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
        }else{
            //jika belum enable gpsnya,tampilkan dialog
            gps.showSettingsAlert();
        }

        //mengambil variabel yang dikirim dari activity lain
        Intent intent = getIntent();
        lapor = (Lapor)intent.getSerializableExtra("lapor");
        //Permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        //Untuk enable mengambil URI dari galery
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //mengisikan nilai ke form
        if(lapor != null){
            txtKeterangan.setText(lapor.keterangan);
            txtLokasi.setText(lapor.lokasi);
            if(lapor.pil.equalsIgnoreCase("Pileg")){
                rbPileg.setChecked(true);
                rbPilpres.setChecked(false);
            }else{
                rbPileg.setChecked(false);
                rbPilpres.setChecked(true);
            }
            if(lapor.path != null){
                Glide.with(this).load(lapor.path)
                        .into(imgLapor);
            }
            //jika akan mengedit/hapus/kirim
            btnHapus.setVisibility(View.VISIBLE);
            btnKirim.setVisibility(View.VISIBLE);
            //jika sudah sent,tidak boleh diedit
            if(lapor.sent == 1){
                btnKirim.setVisibility(View.GONE);
                btnHapus.setVisibility(View.GONE);
                btnSimpan.setVisibility(View.GONE);
            }
        }

        //tombol simpan
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ambil nilai dan insert ke database
                String keterangan = txtKeterangan.getText().toString();
                String lokasi = txtLokasi.getText().toString();
                if (TextUtils.isEmpty(keterangan)) {
                    txtKeterangan.setError("keterangan harus diisi");
                }
                RadioButton selected = findViewById(rbGroup.getCheckedRadioButtonId());

                if(lapor == null) {
                    lapor = new Lapor();
                    lapor.keterangan = keterangan;
                    lapor.pil = selected.getText().toString();
                    lapor.waktu = new Date();
                    lapor.sent = 0;
                    lapor.lokasi = lokasi;
                    lapor.path = imagePath;
                    lapor.latitude = gps.getLatitude();
                    lapor.longitude = gps.getLongitude();
                    AppDatabase.getInstance(getApplicationContext())
                            .laporDao().insert(lapor);
                }else{
                    //update
                    lapor.latitude = gps.getLatitude();
                    lapor.longitude = gps.getLongitude();
                    lapor.keterangan = keterangan;
                    lapor.pil = selected.getText().toString();
                    lapor.waktu = new Date();
                    lapor.sent = 0;
                    lapor.lokasi = lokasi;
                    lapor.path = imagePath;
                    AppDatabase.getInstance(getApplicationContext())
                            .laporDao().update(lapor);
                }
                Intent intent = new Intent(InputActivity.this,
                        MainActivity.class);
                startActivity(intent);

            }
        });

        imgLapor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseDialog();
            }
        });
        //kirim ke server
        btnKirim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upload(lapor);
            }
        });
        //hapus
        btnHapus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.app.AlertDialog.Builder builder =
                        new android.app.AlertDialog.Builder(InputActivity.this);
                builder.setTitle("Konfirmasi");
                builder.setMessage("Hapus data?");
                builder.setPositiveButton("Hapus",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                AppDatabase.getInstance(getApplicationContext()).laporDao().delete(lapor);
                                Intent intent = new Intent(InputActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                        });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                android.app.AlertDialog alert = builder.create();
                alert.show();
            }
        });



    }

    ////////////////////////////////////////// camera/gallery //////////////////////////////////////////
    //foto
    private void chooseDialog() {
        CharSequence menu[] = new CharSequence[]{"Take From Galery",
                "Open Camera"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a Picture");
        builder.setItems(menu, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    gallery();
                } else {
                    takePicture();
                }
            }
        });
        builder.show();

    }

    //camera
    public void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file = Uri.fromFile(getOutputMediaFile());
        //Toast.makeText(this,file.toString(),Toast.LENGTH_SHORT).show();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, file);
        startActivityForResult(intent, 100);
    }

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");
    }

    private void gallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }


    private String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            Log.e("EditProfileActivity", "getRealPathFromURI Exception : " + e.toString());
            return "";
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private String saveImage(Bitmap image, String fileName) {
        String savedImagePath = null;
        String imageFileName = "JPEG_" + fileName + ".jpg";
        File storageDir = new File(Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                + "");
        boolean success = true;
        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
        }
        if (success) {
            File imageFile = new File(storageDir, imageFileName);
            savedImagePath = imageFile.getAbsolutePath();
            try {
                OutputStream fOut = new FileOutputStream(imageFile);
                //perkecil
                image.compress(Bitmap.CompressFormat.JPEG, 60, fOut);
                fOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Add the image to the system gallery
            galleryAddPic(savedImagePath);
            //Toast.makeText(DetailEventActivity.this, "IMAGE SAVED", Toast.LENGTH_LONG).show();
        }
        return savedImagePath;
    }

    private void galleryAddPic(String imagePath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imagePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
    }

    ////////////////////////////////////////// camera/gallery //////////////////////////////////////////

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Glide.with(this).load(file).into(imgLapor);
                imagePath = file.getPath();
            }
        } else {
            if (resultCode == RESULT_OK) {
                Glide.with(this).load(data.getData()).into(imgLapor);
                imagePath = getRealPathFromURI(this, data.getData());
            }
        }
        Toast.makeText(this, imagePath, Toast.LENGTH_SHORT).show();
    }

    private void upload(final Lapor lapor){
        File file = new File(lapor.path);//path image
        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("berkas", file.getName(),
                reqFile);
        RequestBody lokasi = RequestBody.create(MediaType.parse("text/plain"), lapor.lokasi);
        RequestBody keterangan = RequestBody.create(MediaType.parse("text/plain"), lapor.keterangan);
        RequestBody waktu = RequestBody.create(MediaType.parse("text/plain"),
                new SimpleDateFormat("dd-mm-yyyy").format(new Date()));
        RequestBody pil = RequestBody.create(MediaType.parse("text/plain"), lapor.pil);
        RequestBody type = RequestBody.create(MediaType.parse("text/plain"), "image");
        RequestBody lat = RequestBody.create(MediaType.parse("text/plain"),String.valueOf(lapor.latitude));
        RequestBody lng = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(lapor.latitude));
        progressDialog = new ProgressDialog(InputActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Mengirim data...");
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
        api = ApiClient.getClient().create(ApiInterface.class);
        Call<String> call = api.upload(body,lokasi,keterangan,waktu,pil,type,lat,lng);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
                //mendapatkan message dari server
                String result = response.body();
                //Jika status dari server 200,300
                if(response.isSuccessful()){
                    //update terkirim = 1
                    lapor.sent = 1;
                    AppDatabase.getInstance(getApplicationContext())
                            .laporDao().update(lapor);
                    Intent intent = new Intent(InputActivity.this,
                            MainActivity.class);
                    startActivity(intent);
                }else{
                    //status dari server 400,500
                    Toast.makeText(InputActivity.this,
                            "tidak dapat mengupload file,silahkan kirim lagi",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(InputActivity.this,
                        "tidak dapat terhubung ke server",Toast.LENGTH_SHORT).show();
                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
                //menampilkan detail eror di logcat
                t.printStackTrace();
            }
        });

    }
}
