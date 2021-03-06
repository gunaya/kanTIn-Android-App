package com.zucc.todolist.admin;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.yanzhenjie.album.Action;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.AlbumFile;
import com.yanzhenjie.album.api.widget.Widget;
import com.zucc.todolist.R;
import com.zucc.todolist.apihelper.ApiUtils;
import com.zucc.todolist.apihelper.BaseApiService;
import com.zucc.todolist.fragment.DatePickerFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TambahActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{
    EditText name, buyPrice, sellPrice, stock;
    TextView date, pictName;
    String currentDate;
    String category[] = {"makanan","minuman"};
    int category_id;
    Button submitFood, getPict;
    Spinner getCategory;
    BaseApiService mApiService;
    String path;


    ArrayAdapter<String> adapter;

    MultipartBody.Part foto_barang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mApiService = ApiUtils.getApiService();

        setContentView(R.layout.activity_tambah);
        name = findViewById(R.id.food_name);
        pictName = findViewById(R.id.pict_name);
        getPict = findViewById(R.id.load_pict);
        date = findViewById(R.id.load_date);
        buyPrice = findViewById(R.id.buy_price);
        sellPrice = findViewById(R.id.sell_price);
        stock = findViewById(R.id.set_stock);
        submitFood = findViewById(R.id.submit_food);
        getCategory = findViewById(R.id.set_category);


//        getPict.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                startActivityForResult(intent,100);
//            }
//        });

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.support.v4.app.DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), "date picker");
            }
        });
        getPict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(getApplicationContext(), UpdateFotoProfille.class);
//                startActivity(intent);
                selectImage();
            }
        });
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, category);
        getCategory.setAdapter(adapter);
        getCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                switch (position)
                {
                    case 0:
                        category_id = 1;
                        break;

                    case 1:
                        category_id = 2;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        submitFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Hasil", "Tanggal:"+currentDate+". Kategori:"+category_id);
                sendData();
            }
        });

    }

    private void startActivityForResult(Intent intent) {
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DATE, day);
        month = month + 1;
        currentDate = year+"/"+month+"/"+day;
        String currentDateString = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
        date.setText(currentDateString);
    }


    private void selectImage() {
        Album.image(this)
                .singleChoice()
                .camera(true)
                .widget(
                        Widget.newDarkBuilder(this)
                                .build()
                )
                .onResult((Action<ArrayList<AlbumFile>>) result -> {
                    path = result.get(0).getPath();
                    Toast.makeText(TambahActivity.this,"path : "+path,Toast.LENGTH_SHORT).show();
                    String filename = path.substring(path.lastIndexOf("/")+1);
                    pictName.setText(filename);

//                    et_logo_kategori.setText(filename);
//                    mAlbumFiles = result;
//                    Bundle bundle
//                    Bundle bundle = new Bundle();
//                    bundle.putString("path", path);
//                    bundle.putString("filename", filename);
//                    intent.putExtras(bundle);
//                    startActivity(intent);
                })
                .onCancel(new Action<String>() {
                    @Override
                    public void onAction(@NonNull String result) {
                        Toast.makeText(TambahActivity.this, "cancell", Toast.LENGTH_LONG).show();
                    }
                })
                .start();
    }
    public void sendData(){

        String foodName = name.getText().toString();
        String buyFood = buyPrice.getText().toString();
        int buyFoodPrice = Integer.parseInt(buyFood);
        String sellFood = sellPrice.getText().toString();
        int sellFoodPrice = Integer.parseInt(sellFood);
        String stockString = stock.getText().toString();
        int stockInt = Integer.parseInt(stockString);
        String kategoriString = String.valueOf(category_id);
//      Data Dummy
        String pict = path;

        File file = new File(path);
        RequestBody requestFile = RequestBody.create(MediaType.parse("foto_barang"),file);
        RequestBody nama = RequestBody.create(MediaType.parse("text/plain"),foodName);
        RequestBody harga_beli = RequestBody.create(MediaType.parse("text/plain"),buyFood);
        RequestBody harga_jual = RequestBody.create(MediaType.parse("text/plain"),sellFood);
        RequestBody stok = RequestBody.create(MediaType.parse("text/plain"),stockString);
        RequestBody kaladuarsa = RequestBody.create(MediaType.parse("text/plain"),currentDate);
        RequestBody kategori = RequestBody.create(MediaType.parse("text/plain"),kategoriString);

        MultipartBody.Part foto_barang = MultipartBody.Part.createFormData("foto_barang", file.getName(),requestFile);

        Log.d("Data",""+foodName+" "+buyFoodPrice+" "+sellFoodPrice+" "+stockInt+" "+foto_barang+" "+pict+" "+category_id);
//        mApiService.tambahMakananRequest(foodName, pict, currentDate, buyFoodPrice, sellFoodPrice, stockInt, category_id).enqueue(new Callback<ResponseBody>() {
        mApiService.addNewMenu(foto_barang, nama, kaladuarsa, harga_beli, harga_jual, stok, kategori).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(TambahActivity.this, "success", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(TambahActivity.this, "gagal", Toast.LENGTH_SHORT).show();
                }
//                try {
//                    String responseData = response.body().string();
//                    JSONObject jsonResults = new JSONObject(responseData);
//                    Log.d("status", ""+jsonResults.getString("message"));
//                    if (jsonResults.getString("message").equals("success")) {
//                        Toast.makeText(TambahActivity.this,"Input Data Success",Toast.LENGTH_SHORT).show();
//                        Intent intent = new Intent(TambahActivity.this, ProfilActivity.class);
//                        startActivity(intent);
//                    } else {
//                        Toast.makeText(TambahActivity.this, "Input Correct Data", Toast.LENGTH_SHORT).show();
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                try {
//                    JSONObject jsonObject = new JSONObject(response.body().string());
//                    if (jsonObject.getString("message").equals("success")) {
//                        Log.d("status","keisini");
//                        Log.d("status", ""+jsonObject.getString("message"));
//                        Toast.makeText(TambahActivity.this,"Input Data Success",Toast.LENGTH_SHORT).show();
//                        Intent intent = new Intent(TambahActivity.this, FragmentActivity.class);
//                        startActivity(intent);
//                    }
//                } catch (IOException | JSONException e) {
//                    e.printStackTrace();
//                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("status","onfailuer");
            }
        });
    }
}
