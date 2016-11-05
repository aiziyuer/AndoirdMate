package app.aiziyuer.com.androidmate;

import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import net.sqlcipher.database.SQLiteDatabase;

import java.io.File;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final String TAG = getClass().toString();

    @BindView(R.id.textView2)
    TextView textView2;
    @BindView(R.id.fingerprintCheckBtn)
    Button fingerprintCheckBtn;
    private CancellationSignal cancellationSignal;
    private FingerprintManagerCompat fingerprintManager;
    private Cipher cipher;


    protected void InitializeSQLCipher() {
        SQLiteDatabase.loadLibs(this);
        File databaseFile = getDatabasePath("demo.db");

        Log.d(TAG, databaseFile.getAbsolutePath());

        databaseFile.mkdirs();
        databaseFile.delete();
        SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(databaseFile, "test123",
                null);
        database.execSQL("create table t1(a, b)");
        database.execSQL("insert into t1(a, b) values(?, ?)", new Object[]{"one for the money",
                "two for the show"});
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action",
                Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string
                .navigation_drawer_close);
        assert drawer != null;
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(this);

        cancellationSignal = new CancellationSignal();
        fingerprintManager = FingerprintManagerCompat.from(this);

        try {
            // This can be key name you want. Should be unique for the app.
            String KEY_NAME = "com.aiziyuer.app.androidmate.fingerprint_authentication_key";
            // We always use this keystore on Android.
            String KEYSTORE_NAME = "AndroidKeyStore";
            // Should be no need to change these values.
            String KEY_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES;
            String BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC;
            String ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7;
            String TRANSFORMATION = KEY_ALGORITHM + "/" +
                    BLOCK_MODE + "/" +
                    ENCRYPTION_PADDING;

            // 生成Cipher
            KeyGenerator keyGen = KeyGenerator.getInstance(KEY_ALGORITHM, KEYSTORE_NAME);
            KeyGenParameterSpec keyGenSpec =
                    new KeyGenParameterSpec.Builder(KEY_NAME, KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(BLOCK_MODE)
                            .setEncryptionPaddings(ENCRYPTION_PADDING)
                            .setUserAuthenticationRequired(true)
                            .build();
            keyGen.init(keyGenSpec);
            Key key = keyGen.generateKey();

            cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key);
        } catch (KeyPermanentlyInvalidatedException e) {
            Log.e("MainActivity", "Could not create the cipher for fingerprint authentication.", e);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException |
                NoSuchPaddingException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Log.d("MainActivity", "item:" + item);

        boolean ret = true;
        switch (id) {
            case R.id.nav_safebox:
                // 启动一个safebox的页面

                Log.d("MainActivity", "click");

                break;

            default:
                ret = false;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        drawer.closeDrawer(GravityCompat.START);

        return ret;
    }

    @OnClick({R.id.textView2, R.id.fingerprintCheckBtn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.textView2:
                break;
            case R.id.fingerprintCheckBtn:
                Log.d("fingerprintCheckBtn", "click");

                if (!fingerprintManager.isHardwareDetected()) {
                    Log.e("MainActivity", "not detected hardware.");

                } else if (!fingerprintManager.hasEnrolledFingerprints()) {
                    Log.e("MainActivity", "not rolled fingerprints.");
                } else {

                    try {

                        fingerprintManager.authenticate(new FingerprintManagerCompat.CryptoObject
                                (cipher), 0, cancellationSignal, new FingerprintManagerCompat
                                .AuthenticationCallback() {
                            @Override
                            public void onAuthenticationSucceeded(FingerprintManagerCompat
                                                                          .AuthenticationResult
                                                                          result) {
                                super.onAuthenticationSucceeded(result);
                                try {
                                    result.getCryptoObject().getCipher().doFinal();
                                } catch (Exception e) {
                                    Log.e("MainActivity", e.toString());
                                }

                            }

                            @Override
                            public void onAuthenticationError(int errMsgId, CharSequence
                                    errString) {
                                super.onAuthenticationError(errMsgId, errString);
                            }

                            @Override
                            public void onAuthenticationFailed() {
                                super.onAuthenticationFailed();
                            }

                            @Override
                            public void onAuthenticationHelp(int helpMsgId, CharSequence
                                    helpString) {
                                super.onAuthenticationHelp(helpMsgId, helpString);
                            }
                        }, null);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancellationSignal.cancel();
    }
}
