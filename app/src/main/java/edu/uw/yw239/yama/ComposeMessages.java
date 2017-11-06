package edu.uw.yw239.yama;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class ComposeMessages extends AppCompatActivity {

    static final int REQUEST_SELECT_PHONE_NUMBER = 1;

    public static final String ACTION_SMS_STATUS = "edu.uw.intentdemo.ACTION_SMS_STATUS";

    private static final int WRITE_REQUEST_CODE = 1;

    public static final String ACTIVITY_NAME = "compose massages";

    String number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose_messages);

        Button selectButton = (Button)findViewById(R.id.btn_select);

        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectContact();
            }
        });

        ImageButton sendButton = (ImageButton) findViewById(R.id.btn_send_message);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(v);

                // hide the keyboard
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });

        Bundle extras = getIntent().getExtras(); //All activities are started with an Intent!

        if(extras != null) {
            String address = extras.getString(ReadMessages.ADDRESS_KEY);

            EditText text = (EditText) findViewById(R.id.txt_contact);
            text.setText(address);
        }

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
                Intent intent = new Intent(ComposeMessages.this, SettingActivity.class);
                intent.putExtra(SettingActivity.PARENT_ACTIVITY_KEY, ACTIVITY_NAME);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void sendMessage(View v) {

        // get the number showed in the contact input
        TextView contact = (TextView)findViewById(R.id.txt_contact);
        if(contact.getText() != null && !contact.getText().toString().equals("")) {
            number = contact.getText().toString();
        }
        else{
            Snackbar snack = Snackbar.make(v, "Please select a phone number", Snackbar.LENGTH_LONG);
            snack.show();
            return;
        }

        // check send sms permission
        int permissionCheck = ContextCompat.checkSelfPermission(ComposeMessages.this, Manifest.permission.SEND_SMS);

        if(permissionCheck == PackageManager.PERMISSION_GRANTED) {
            //have permission, can go ahead and do stuff
            Intent intent = new Intent(ACTION_SMS_STATUS);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(ComposeMessages.this, 0, intent, 0);

            TextView textView = (TextView)findViewById(R.id.entered_content);
            String message = textView.getText().toString();

            if(message.equals("")){
                Snackbar snack = Snackbar.make(v, "Please enter a message!", Snackbar.LENGTH_LONG);
                snack.show();
                return;
            }
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(number, null, message, pendingIntent, null);

            Snackbar snack = Snackbar.make(v, "Message has been sent", Snackbar.LENGTH_LONG);
            snack.show();

            // clear input
            textView.setText("");
        }
        else { //if we're missing permission.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_REQUEST_CODE);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case WRITE_REQUEST_CODE: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //now we have permissions!
                    Intent intent = new Intent(ACTION_SMS_STATUS);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(ComposeMessages.this, 0, intent, 0);

                    TextView textView = (TextView)findViewById(R.id.entered_content);
                    String message = textView.getText().toString();
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(number, null, message, pendingIntent, null);
                }
                break;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }
    }

    public void selectContact() {
        // Start an activity for the user to pick a phone number from contacts
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_SELECT_PHONE_NUMBER);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SELECT_PHONE_NUMBER && resultCode == RESULT_OK) {
            // Get the URI and query the content provider for the phone number
            Uri contactUri = data.getData();
            String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
            Cursor cursor = getContentResolver().query(contactUri, projection,
                    null, null, null);
            // If the cursor returned is valid, get the phone number
            if (cursor != null && cursor.moveToFirst()) {
                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                number = cursor.getString(numberIndex);
                // Do something with the phone number
                TextView contact = (TextView)findViewById(R.id.txt_contact);
                contact.setText(number);
            }
        }
    }
}
