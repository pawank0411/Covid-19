package india.coronavirus.fight;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import india.coronavirus.fight.utilities.DownloadDistrictFile;

public class MainActivity extends AppCompatActivity {
    private NavController navController;
    private String api;
    private SharedPreferences.Editor editor;
    private String new_version;
    private String newVersion;
    private Thread thread;
    private String lastUp;
    private ArrayList<String> quoteList = new ArrayList<>();
    private Integer i = 0;
    private static final int EXTERNAL_STORAGE_PERMISSION_CODE = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        MaterialTextView quotetext = findViewById(R.id.quoteText);
        MaterialTextView textView = findViewById(R.id.marquee);
        MaterialTextView lastup = findViewById(R.id.lastupdated);
        textView.setSelected(true);
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();
        checkFolder();
        SharedPreferences sharedPreferences = this.getSharedPreferences("API", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        askPermission();
        DownloadDistrictFile downloadDistrictFile = new DownloadDistrictFile(this);
        downloadDistrictFile.newDownload(sharedPreferences.getString("DIST",""));

        RequestQueue requestQueue = Volley.newRequestQueue(getApplication());
        @SuppressLint("SetTextI18n") StringRequest stringRequest = new StringRequest(Request.Method.GET, sharedPreferences.getString("API", "http://ac41bf31.ngrok.io") + "/api/extras", response -> {
            try {
                JSONObject json = new JSONObject(response);
                lastUp = json.getString("timestamp");
                editor.putString("time", lastUp);
                editor.apply();
                if (!Objects.equals(sharedPreferences.getString("time", null), "")) {
                    lastup.setText("Last updated " + sharedPreferences.getString("time", "null").substring(3));
                }
            } catch (JSONException e) {
                sharedPreferences.getString("time", "null");
                if (!Objects.equals(sharedPreferences.getString("time", null), "")) {
                    lastup.setText("Last updated " + sharedPreferences.getString("time", "null").substring(3));
                }
                Log.e("Error", String.valueOf(e));
            }
        }, error -> {
            sharedPreferences.getString("time", "null");
            if (!Objects.equals(sharedPreferences.getString("time", null), "")) {
                lastup.setText("Last updated " + sharedPreferences.getString("time", "null").substring(3));
            }
            Log.d("Error", Objects.requireNonNull(error.toString()));
        });
        requestQueue.add(stringRequest);

        CollectionReference apiCollection = FirebaseFirestore.getInstance().collection("apilink");
        apiCollection.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (queryDocumentSnapshots != null) {
                for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                    api = documentChange.getDocument().getString("api");
                    new_version = documentChange.getDocument().getString("version");
                    String showgraph = documentChange.getDocument().getString("showGraph");
                    editor.putString("showgraph", showgraph);
                    editor.putString("new_version", new_version);
                    editor.putString("API", api);
                    editor.apply();
                }
            }
            PackageInfo pInfo = null;
            try {
                pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException ex) {
                ex.printStackTrace();
            }
            if (pInfo != null) {
                newVersion = pInfo.versionName;
                if (sharedPreferences.getString("new_version", "").equals(newVersion)) {
                    AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                            R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications, R.id.navigation_guides,
                            R.id.navigation_statewise)
                            .build();
                    navController = Navigation.findNavController(this, R.id.nav_host_fragment);
                    NavigationUI.setupWithNavController(navView, navController);
                } else {
                    Intent intent = new Intent(this, About.class);
                    intent.putExtra("fromMainActivity", true);
                    startActivity(intent);
                }
            }
        });
        quoteList.add("Lockdown means LOCKDOWN! Avoid going out unless absolutely necessary. Stay safe!  ");
        quoteList.add("Don't Hoard groceries and essentials. Please ensure that people who are in need don't face a shortage because of you!");
        quoteList.add("Plan ahead! Take a minute and check how much you have at home. Planning ahead let's you buy exactly what you need!");
        quoteList.add("If you have symptoms and suspect you have coronavirus - reach out to your doctor or call state helplines. \uD83D\uDCDE Get help.");
        quoteList.add("Panic mode : OFF! ❌ ESSENTIALS ARE ON! ✔️");
        quoteList.add("Help out the elderly by bringing them their groceries and other essentials.");
        quoteList.add("Be considerate : While buying essentials remember : You need to share with 130 Crore Others!  ");
        quoteList.add("Stand Against FAKE News and WhatsApp Forwards! Do NOT ❌ forward a message until you verify the content it contains.");
        quoteList.add("Be compassionate! Help those in need like the elderly and poor. They are facing a crisis you cannot even imagine!  ");
        quoteList.add("If you have any queries, reach out to your district administration or doctors!  ");
        quoteList.add("The hot weather will not stop the virus! You can! Stay home, stay safe. ");
        quoteList.add("Avoid going out during the lockdown. Help break the chain of spread.");
        quoteList.add("Help the medical fraternity by staying at home!");
//        quoteList.add("Plan and calculate your essential needs for the next three weeks and get only what is bare minimum needed.");
        quoteList.add("Call up your loved ones during the lockdown, support each other through these times.");
        quoteList.add("Our brothers from the north east are just as Indian as you! Help everyone during this crisis ❤");

        thread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!thread.isInterrupted()) {
                        Thread.sleep(5000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (i < quoteList.size()) {
                                    quotetext.setText(quoteList.get(i));
                                    i++;
                                } else {
                                    i = 0;
                                }
                            }
                        });
                    }
                } catch (InterruptedException ignored) {
                }
            }
        };
        thread.start();


    }

    private void askPermission() {
        if (!hasPermission()) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(this)
                        .setTitle("Permission needed")
                        .setMessage("Storage permission is needed to store Districts wise stats")
                        .setCancelable(false)
                        .setPositiveButton("OK", (dialog, which) -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                        Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CODE);
                            }
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            dialog.dismiss();
                            finish();
                        }).create().show();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CODE);
                }
            }

        }
    }


    private boolean hasPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == EXTERNAL_STORAGE_PERMISSION_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) || !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    new AlertDialog.Builder(this)
                            .setTitle("Permission needed")
                            .setMessage("Storage permission is needed to store Districts wise stats. Press OK to enable in settings.")
                            .setCancelable(false)
                            .setPositiveButton("OK", (dialog, which) -> {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivityForResult(intent, EXTERNAL_STORAGE_PERMISSION_CODE);
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> {
                                dialog.dismiss();
                                finish();
                            }).create().show();


                } else {

                    new AlertDialog.Builder(this)
                            .setTitle("Permission needed")
                            .setMessage("Storage permission is needed to store Districts wise stats")
                            .setCancelable(false)
                            .setPositiveButton("OK", (dialog, which) -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                            Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CODE);
                                }
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> {
                                dialog.dismiss();
                                finish();
                            }).create().show();
                }
            }
        }
    }

    public void checkFolder() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/covid19India/";
        File dir = new File(path);
        boolean isDirectoryCreated = dir.exists();
        if (!isDirectoryCreated) {
            isDirectoryCreated = dir.mkdir();
        }
        if (isDirectoryCreated) {
            // do something\
            Log.d("Path", path);
            Log.d("Folder", "Already Created");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nav, menu);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        MenuCompat.setGroupDividerEnabled(menu, true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.about_app) {
            Intent intent = new Intent(this, About.class);
            startActivity(intent);
            return (true);
        }
        return (super.onOptionsItemSelected(item));
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp();
    }

}
