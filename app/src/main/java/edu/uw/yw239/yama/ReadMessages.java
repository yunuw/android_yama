package edu.uw.yw239.yama;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Telephony;
import android.provider.UserDictionary;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.provider.Telephony.Sms.Intents.getMessagesFromIntent;
import static edu.uw.yw239.yama.ComposeMessages.ACTION_SMS_STATUS;

public class ReadMessages extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    final SmsManager sms = SmsManager.getDefault();

    final int REQUEST_CODE_ASK_PERMISSIONS_READ_SMS = 10;

    final int REQUEST_CODE_ASK_PERMISSIONS_SEND_SMS = 11;

    final int REQUEST_CODE_ASK_PERMISSIONS_RECEIVE_SMS = 12;

    final int REQUEST_CODE_ASK_PERMISSIONS_CONTACTS = 13;

    final int REQUEST_CODE_ASK_PERMISSIONS_READ_PHONESTATE = 14;

    private SmsAdapter adapter;

    final static String NOTIFICATION_CHANNEL_ID = "chanel_id";

    final static int PENDING_READ_ID = 3;

    final static int PENDING_REPLY_ID = 4;

    final static int PENDING_AUTO_REPLY_ID = 5;

    final static int NOTIFICATION_ID = 6;

    public static final String ADDRESS_KEY = "address_key";

    public static final String ACTIVITY_NAME = "read massages";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //define the onClickListener for fab
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_to_compose_message);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ReadMessages.this, ComposeMessages.class);
                startActivity(intent);
            }
        });

        if(isAllPermissionGranted()) {
            getMessages();
        }
        else { //if we're missing permission.
            askForPermission();
        }
    }


    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS_READ_SMS:
            case REQUEST_CODE_ASK_PERMISSIONS_SEND_SMS:
            case REQUEST_CODE_ASK_PERMISSIONS_RECEIVE_SMS:
            case REQUEST_CODE_ASK_PERMISSIONS_CONTACTS:
            case REQUEST_CODE_ASK_PERMISSIONS_READ_PHONESTATE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (isAllPermissionGranted()) {
                        getMessages();
                    } else {
                        askForPermission();
                    }
                } else if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    finish();
                }
                return;
            }
        }
    }

    private boolean isAllPermissionGranted() {
        int permissionCheckReadSMS = ContextCompat.checkSelfPermission(ReadMessages.this, Manifest.permission.READ_SMS);
        int permissionCheckSendSMS = ContextCompat.checkSelfPermission(ReadMessages.this, Manifest.permission.SEND_SMS);
        int permissionCheckContacts = ContextCompat.checkSelfPermission(ReadMessages.this, Manifest.permission.READ_CONTACTS);
        int permissionCheckReadPhoneState = ContextCompat.checkSelfPermission(ReadMessages.this, Manifest.permission.READ_PHONE_STATE);
        int permissionCheckReceiveSMS = ContextCompat.checkSelfPermission(ReadMessages.this, Manifest.permission.RECEIVE_SMS);

        return permissionCheckReadSMS == PackageManager.PERMISSION_GRANTED
                && permissionCheckSendSMS == PackageManager.PERMISSION_GRANTED
                && permissionCheckContacts == PackageManager.PERMISSION_GRANTED
                && permissionCheckReadPhoneState == PackageManager.PERMISSION_GRANTED
                && permissionCheckReceiveSMS == PackageManager.PERMISSION_GRANTED;
    }

    private void askForPermission() {
        int permissionCheckReadSMS = ContextCompat.checkSelfPermission(ReadMessages.this, Manifest.permission.READ_SMS);
        int permissionCheckSendSMS = ContextCompat.checkSelfPermission(ReadMessages.this, Manifest.permission.SEND_SMS);
        int permissionCheckContacts = ContextCompat.checkSelfPermission(ReadMessages.this, Manifest.permission.READ_CONTACTS);
        int permissionCheckReadPhoneState = ContextCompat.checkSelfPermission(ReadMessages.this, Manifest.permission.READ_PHONE_STATE);
        int permissionCheckReceiveSMS = ContextCompat.checkSelfPermission(ReadMessages.this, Manifest.permission.RECEIVE_SMS);

        if (permissionCheckReadSMS != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ReadMessages.this, new String[]{Manifest.permission.READ_SMS}, REQUEST_CODE_ASK_PERMISSIONS_READ_SMS);
        } else if (permissionCheckSendSMS != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ReadMessages.this, new String[]{Manifest.permission.SEND_SMS}, REQUEST_CODE_ASK_PERMISSIONS_SEND_SMS);
        } else if (permissionCheckContacts != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ReadMessages.this, new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CODE_ASK_PERMISSIONS_CONTACTS);
        } else if (permissionCheckReadPhoneState != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ReadMessages.this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE_ASK_PERMISSIONS_READ_PHONESTATE);
        } else if (permissionCheckReceiveSMS != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ReadMessages.this, new String[]{Manifest.permission.RECEIVE_SMS}, REQUEST_CODE_ASK_PERMISSIONS_RECEIVE_SMS);
        }
    }

    private void getMessages() {
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(Telephony.Sms.Inbox.CONTENT_URI,  null, null, null, null);
        cursor.moveToFirst();

        adapter = new SmsAdapter(
                this,
                R.layout.message_info, //item to inflate
                null,
                false); //flags

        AdapterView listView = (AdapterView) findViewById(R.id.list_item);

        listView.setAdapter(adapter);

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = new String[] {
                //Telephony.Sms.Inbox.PERSON,
                //Telephony.Sms.Inbox.BODY
                //Telephony.Sms.Inbox.DATE
        };

        //create the CursorLoader
        CursorLoader loader = new CursorLoader(
                this,
                Telephony.Sms.Inbox.CONTENT_URI,
                projection,
                null,
                null,
                null
        );

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //replace the data
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //empty the data
        adapter.swapCursor(null);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_settings, menu); //inflate into this menu
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_settings :
                //do thing;
                Intent intent = new Intent(ReadMessages.this, SettingActivity.class);
                intent.putExtra(SettingActivity.PARENT_ACTIVITY_KEY, ACTIVITY_NAME);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
