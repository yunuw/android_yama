package edu.uw.yw239.yama;
import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yunwu on 11/4/17.
 */

public class SmsAdapter extends ResourceCursorAdapter {

    public SmsAdapter(Context context, int layout, Cursor c, boolean autoRequery) {
        super(context, layout, c, autoRequery);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // set body
        String body = cursor.getString(cursor.getColumnIndex("body"));
        TextView bodyView = view.findViewById(R.id.txt_message_body);
        bodyView.setText(body);

        // set date
        String date = cursor.getString(cursor.getColumnIndex("date"));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat finalDateFormat = new SimpleDateFormat("E HH:mm a");
        Date convertedDate = new Date();

        try {
            convertedDate = dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        TextView dateView = view.findViewById(R.id.txt_message_date);
        dateView.setText(finalDateFormat.format(convertedDate));

        // set author
        String address = cursor.getString(cursor.getColumnIndex("address"));
        String author = getAuthor(address, context);
        TextView authorView = view.findViewById(R.id.txt_message_author);
        authorView.setText(author);
    }

    public static String getAuthor(final String address, Context context){
        String author = "";

        int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS);

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address));


        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);

        try {
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    author = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                }
                cursor.close();
            }
        } catch (Exception e) {

        }

        return author;
    }
}
