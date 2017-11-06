package edu.uw.yw239.yama;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Telephony;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

import static android.content.Context.MODE_PRIVATE;
import static android.provider.Telephony.Sms.Intents.getMessagesFromIntent;
import static edu.uw.yw239.yama.ComposeMessages.ACTION_SMS_STATUS;
import static edu.uw.yw239.yama.ReadMessages.NOTIFICATION_ID;
import static edu.uw.yw239.yama.ReadMessages.PENDING_AUTO_REPLY_ID;
import static edu.uw.yw239.yama.ReadMessages.PENDING_READ_ID;
import static edu.uw.yw239.yama.ReadMessages.PENDING_REPLY_ID;

/**
 * Created by yunwu on 11/4/17.
 */

public class SmsReceiver extends BroadcastReceiver {

    private static final String NOTIFICATION_CHANNEL_ID = "yama_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction() == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {

            SmsMessage[] message = getMessagesFromIntent(intent);
            String body = message[0].getDisplayMessageBody();
            String address = message[0].getDisplayOriginatingAddress();
            String display = address;

            String author = SmsAdapter.getAuthor(address, MyApplication.getContext());

            if(!author.equals("")){
                display = author;
            }

            showNotification(body, display, address);
        }
    }

    public void showNotification(String body, String display, String address){
        Context context = MyApplication.getContext();


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            //Orea support
            Notification.Builder builder = new Notification.Builder(MyApplication.getApplication())
                    .setSmallIcon(R.mipmap.ic_message_notification)
                    .setContentTitle("From: " + display + "\n")
                    .setContentText("Content: " + body)
                    .setChannelId(NOTIFICATION_CHANNEL_ID);
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Demo channel", NotificationManager.IMPORTANCE_MAX);
            channel.setDescription("Channel description");
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            channel.enableVibration(true);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);

            // set read message intent
            Intent readIntent = new Intent(context, ReadMessages.class);
            TaskStackBuilder readStackBuilder = TaskStackBuilder.create(context);
            readStackBuilder.addParentStack(ReadMessages.class);
            readStackBuilder.addNextIntent(readIntent);
            PendingIntent pendingReadIntent = readStackBuilder.getPendingIntent(PENDING_READ_ID,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setContentIntent(pendingReadIntent)
                    .addAction(R.mipmap.ic_read, context.getString(R.string.intent_view), pendingReadIntent);


            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());

            if(prefs.getBoolean("pref_auto_reply", false)) {
                String defaultMessage = MyApplication.getContext().getString(R.string.default_reply_message);
                String autoMessage = prefs.getString("pref_reply_content", defaultMessage);
                if(autoMessage.equals("")){
                    autoMessage = defaultMessage;
                }
                Intent intent = new Intent(ACTION_SMS_STATUS);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(MyApplication.getContext(), 0, intent, 0);
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(address, null, autoMessage, pendingIntent, null);
            } else {
                // set reply intent
                Intent replyIntent = new Intent(context, ComposeMessages.class);
                replyIntent.putExtra(ReadMessages.ADDRESS_KEY, address);
                TaskStackBuilder replyStackBuilder = TaskStackBuilder.create(context);
                replyStackBuilder.addParentStack(ComposeMessages.class);
                replyStackBuilder.addNextIntent(replyIntent);
                PendingIntent pendingReplyIntent = replyStackBuilder.getPendingIntent(PENDING_REPLY_ID,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                builder.setContentIntent(pendingReadIntent)
                        .addAction(R.mipmap.ic_reply, context.getString(R.string.intent_reply), pendingReplyIntent);
            }

            // activate notification
            notificationManager.notify(NOTIFICATION_ID, builder.build()); //post the notification!
        } else {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(MyApplication.getApplication())
                    .setSmallIcon(R.mipmap.ic_message_notification)
                    .setContentTitle("From: " + display + "\n")
                    .setContentText("Content: " + body)
                    .setChannel(NOTIFICATION_CHANNEL_ID)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setVibrate(new long[]{0, 500, 500, 5000})
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

            // set read message intent
            Intent readIntent = new Intent(context, ReadMessages.class);
            TaskStackBuilder readStackBuilder = TaskStackBuilder.create(context);
            readStackBuilder.addParentStack(ReadMessages.class);
            readStackBuilder.addNextIntent(readIntent);
            PendingIntent pendingReadIntent = readStackBuilder.getPendingIntent(PENDING_READ_ID,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setContentIntent(pendingReadIntent)
                    .addAction(R.mipmap.ic_read, context.getString(R.string.intent_view), pendingReadIntent);


            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());

            if(prefs.getBoolean("pref_auto_reply", false)) {
                String defaultMessage = MyApplication.getContext().getString(R.string.default_reply_message);
                String autoMessage = prefs.getString("pref_reply_content", defaultMessage);
                if(autoMessage.equals("")){
                    autoMessage = defaultMessage;
                }
                Intent intent = new Intent(ACTION_SMS_STATUS);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(MyApplication.getContext(), 0, intent, 0);
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(address, null, autoMessage, pendingIntent, null);
            } else {
                // set reply intent
                Intent replyIntent = new Intent(context, ComposeMessages.class);
                replyIntent.putExtra(ReadMessages.ADDRESS_KEY, address);
                TaskStackBuilder replyStackBuilder = TaskStackBuilder.create(context);
                replyStackBuilder.addParentStack(ComposeMessages.class);
                replyStackBuilder.addNextIntent(replyIntent);
                PendingIntent pendingReplyIntent = replyStackBuilder.getPendingIntent(PENDING_REPLY_ID,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                builder.setContentIntent(pendingReadIntent)
                        .addAction(R.mipmap.ic_reply, context.getString(R.string.intent_reply), pendingReplyIntent);
            }

            // activate notification
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID, builder.build()); //post the notification!
        }
    }
}