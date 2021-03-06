package org.karsav;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.mysql.cj.jdbc.Blob;
import com.r0adkll.slidr.Slidr;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static org.karsav.FinanceAdd.PICK_IMAGE;
import static org.karsav.FinanceAdd.TAKE_PICTURE;

/**
 * Created by YUNUS KARAASLAN
 * Mail: karaaslan.21@hotmail.com
 */
public class FinanceUpdate extends AppCompatActivity {

    private static final String SETTINGS_PREF_NAME = "textSize";
    AutoCompleteTextView name;
    EditText totalAmount, explanation;
    Toolbar toolbar;
    FinanceItem financeItem;
    String idString, nameString, amountString, explanationString, spinnerString, tourString;
    ProgressDialog progressDialog;
    DataBase karsav = new DataBase();
    String names, surnames;
    Spinner spinner;
    ImageView imageView;
    Blob image;
    ProgressBar progressBar;
    Bitmap imageBitmap, imageBitmapTwo;
    Button openCamera, openGallery;
    SharedPreferences settings;
    int textSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finance_update);
        Slidr.attach(this);
        init();
        getImage();
    }

    private void init() {
        name = findViewById(R.id.finance_name);
        totalAmount = findViewById(R.id.amount);
        explanation = findViewById(R.id.explanation);
        spinner = findViewById(R.id.spinner);
        toolbar = findViewById(R.id.toolbar);
        imageView = findViewById(R.id.image);
        progressBar = findViewById(R.id.progress_bar);
        openCamera = findViewById(R.id.open_camera);
        openGallery = findViewById(R.id.open_gallery);

        imageView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        setSupportActionBar(toolbar);
        View logoView = toolbar.getChildAt(1);
        logoView.setOnClickListener(v -> finish());
        toolbar.setTitle(getString(R.string.finance_update));

        settings = getSharedPreferences(SETTINGS_PREF_NAME, MODE_PRIVATE);
        textSize = settings.getInt(SETTINGS_PREF_NAME, 15);

        name.setTextSize(textSize);
        totalAmount.setTextSize(textSize);
        explanation.setTextSize(textSize);
        openCamera.setTextSize(textSize);
        openGallery.setTextSize(textSize);

        Map map = RegisterMemberGet.getFullName();
        Collection<String> values = map.values();
        ArrayList<String> listOfValues = new ArrayList<>(values);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.list_item_for_users, R.id.users_list_item, listOfValues);
        name.setAdapter(adapter);

        financeItem = (FinanceItem) getIntent().getSerializableExtra("UpdateFinance");
        image = getIntent().getParcelableExtra("image");

        idString = financeItem.getId();
        name.setText(financeItem.getResponsible());
        totalAmount.setText(financeItem.getAmount());
        explanation.setText(financeItem.getDescription());
        tourString = financeItem.getTour();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnerString = String.valueOf(spinner.getSelectedItem());
                if (spinnerString.equalsIgnoreCase("Income")) {
                    spinnerString = "GİRDİ";
                } else {
                    spinnerString = "ÇIKTI";
                    amountString = "-" + amountString;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getApplicationContext(), getString(R.string.select_income_or_expense), Toast.LENGTH_SHORT).show();
            }
        });

        if (tourString.equalsIgnoreCase("GİRDİ")) {
            spinner.setSelection(0);
        } else
            spinner.setSelection(1);

        openCamera.setOnClickListener(v -> openCamera());

        openGallery.setOnClickListener(v -> openGallery());
    }

    private void openCamera() {
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        }
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, TAKE_PICTURE);
        }
    }

    @SuppressLint("IntentReset")
    private void openGallery() {
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
        }
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, getString(R.string.select_image));
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        startActivityForResult(chooserIntent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_PICTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) Objects.requireNonNull(extras).get("data");
            imageView.setImageBitmap(imageBitmap);
            imageView.setVisibility(View.VISIBLE);
        }

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            if (data == null) {
                return;
            }
            InputStream inputStream = null;
            try {
                inputStream = getApplicationContext().getContentResolver().openInputStream(Objects.requireNonNull(data.getData()));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            imageBitmap = BitmapFactory.decodeStream(bufferedInputStream);
            imageView.setImageBitmap(imageBitmap);
            imageView.setVisibility(View.VISIBLE);
        }
        try {
            imageBitmapTwo = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.done_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.done) {
            done();
        }
        return super.onOptionsItemSelected(item);
    }

    private void done() {
        try {
            nameString = name.getText().toString();
            amountString = totalAmount.getText().toString();
            explanationString = explanation.getText().toString();
        } catch (Exception ex) {
            Toast.makeText(FinanceUpdate.this, getString(R.string.mainactivity_blank), Toast.LENGTH_SHORT).show();
        }

        if (nameString.isEmpty() || amountString.isEmpty() || explanationString.isEmpty() || spinnerString.isEmpty()) {
            Toast.makeText(FinanceUpdate.this, getString(R.string.sign_up_blank), Toast.LENGTH_SHORT).show();
        } else {
            String text = getString(R.string.assignment_update_dialog_text);
            String yes = getString(R.string.assignment_add_yes);
            String no = getString(R.string.assignment_add_no);
            AlertDialog.Builder builder = new AlertDialog.Builder(FinanceUpdate.this);
            builder.setMessage(text).setPositiveButton(yes, (dialog, which) -> update()).setNegativeButton(no, (dialog, which) -> {

            }).show();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void getImage() {
        new AsyncTask<Boolean, Boolean, Boolean>() {

            final String connection = getString(R.string.mainactivity_connection);

            @Override
            protected Boolean doInBackground(Boolean... booleans) {
                try {
                    if (karsav.Begin()) {
                        return false;
                    }
                    image = karsav.financeImage(idString);
                    karsav.Disconnect();
                    return true;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                progressBar.setVisibility(View.GONE);
                if (aBoolean) {
                    try {
                        setImage(image);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(FinanceUpdate.this, connection, Toast.LENGTH_LONG).show();
                }
                super.onPostExecute(aBoolean);
            }
        }.execute(true);
    }

    @SuppressLint("ResourceAsColor")
    private void setImage(Blob image) throws SQLException {
        int blobLength = (int) image.length();
        byte[] blobAsBytes = image.getBytes(1, blobLength);
        Bitmap btm = BitmapFactory.decodeByteArray(blobAsBytes, 0, blobAsBytes.length);
        imageView.setImageBitmap(btm);
        imageView.setBackgroundColor(Color.WHITE);
        imageView.setVisibility(View.VISIBLE);
        try {
            imageBitmapTwo = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public byte[] getBytesFromBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            return stream.toByteArray();
        }
        return null;
    }

    @SuppressLint("StaticFieldLeak")
    private void update() {
        new AsyncTask<String, String, String>() {

            final String progress_title = getString(R.string.finance_update_dialog_text);
            final String progress_message = getString(R.string.register_reference_progress_message);
            final String connection = getString(R.string.mainactivity_connection);
            final String updated = getString(R.string.finance_updated);

            protected void onPreExecute() {
                progressDialog = new ProgressDialog(FinanceUpdate.this);
                progressDialog.setTitle(progress_title);
                progressDialog.setMessage(progress_message);
                progressDialog.show();
            }

            @Override
            protected String doInBackground(String... strings) {
                try {
                    if (karsav.Begin()) {
                        return "wrong";
                    }
                    String[] nameSurname = nameString.split((" "), 3);
                    if (nameSurname.length == 2) {
                        names = nameSurname[0].trim();
                        surnames = nameSurname[1].trim();
                    } else if (nameSurname.length == 3) {
                        names = nameSurname[0].trim() + " " + nameSurname[1].trim();
                        surnames = nameSurname[2].trim();
                    }
                    byte[] blob = getBytesFromBitmap(imageBitmapTwo);
                    karsav.updateFinance(idString, nameString, explanationString, amountString, spinnerString, names, surnames, blob);
                    karsav.Disconnect();
                    return "done";
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return "false";
                }
            }

            @Override
            protected void onPostExecute(String s) {
                progressDialog.dismiss();
                switch (s) {
                    case "false":
                        Toast.makeText(FinanceUpdate.this, connection, Toast.LENGTH_SHORT).show();
                        break;

                    case "done":
                        startActivity(new Intent(FinanceUpdate.this, HomePage.class));
                        Toast.makeText(FinanceUpdate.this, updated, Toast.LENGTH_LONG).show();
                        finish();
                        break;
                }
            }
        }.execute("updateFinance");
    }
}
