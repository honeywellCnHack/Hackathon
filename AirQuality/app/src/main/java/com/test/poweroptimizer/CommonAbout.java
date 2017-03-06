package com.test.poweroptimizer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class CommonAbout extends AppCompatActivity {
    public static void showCommomAbout(Context context) {
        Intent intent = new Intent(context,
                CommonAbout.class);
        context.startActivity(intent);
    }

    public static AlertDialog create(Context context)
            throws PackageManager.NameNotFoundException {
        PackageInfo pInfo = context.getPackageManager().getPackageInfo(
                context.getPackageName(), PackageManager.GET_META_DATA);

        String title = context.getString(pInfo.applicationInfo.labelRes);
        String versionString = getVersion(context);
        // Set up the TextView
        final TextView message = new TextView(context);
        final SpannableString copyright = new SpannableString(context.getString(R.string.copyright));

        final SpannableString year = new SpannableString(context.getString(R.string.copyright_year));

        // Set some padding
        message.setPadding(getDimensionByName(context, "dialog_padding_left"),
                getDimensionByName(context, "dialog_padding_top"),
                getDimensionByName(context, "dialog_padding_right"),
                getDimensionByName(context, "dialog_padding_bottom"));

        message.setText(versionString + "\n\n" + year + copyright);

        message.setTextColor(context.getResources().getColor(android.R.color.white));
        Linkify.addLinks(message, Linkify.WEB_URLS);

        return new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_DARK)
                .setTitle(title)
                .setCancelable(true)
                .setIcon(pInfo.applicationInfo.icon)
                .setPositiveButton(context.getString(android.R.string.ok), null)
                .setView(message).create();
    }

    static int getDimensionByName(Context context, String name) {
        return (int) context.getResources().getDimension(getResourseIdByName(context, "dimen", name));
    }

    static public String getVersion(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static int getResourseIdByName(Context context, String className, String name) {
        Class<?> r;
        int id = 0;

        try {
            r = Class.forName(context.getApplicationInfo().packageName + ".R");

            Class<?>[] classes = r.getClasses();
            Class<?> desireClass = null;

            for (int i = 0; i < classes.length; i++) {
                if (classes[i].getName().split("\\$")[1].equals(className)) {
                    desireClass = classes[i];

                    break;
                }
            }

            if (desireClass != null)
                id = desireClass.getField(name).getInt(desireClass);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        return id;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_about);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(
                    getPackageName(), PackageManager.GET_META_DATA);

            ImageView appIcon = (ImageView) findViewById(R.id.imageView);
            appIcon.setImageResource(pInfo.applicationInfo.icon);

            TextView version = (TextView) findViewById(R.id.versionInfo);
            version.setText("Version: " + getVersion(this));

            final SpannableString copyright = new SpannableString(getString(getResourseIdByName(this,
                    "string", "copyright")));

            final SpannableString year = new SpannableString(getString(getResourseIdByName(this,
                    "string", "copyright_year")));

            TextView copyRight = (TextView) findViewById(R.id.copyRight);
            copyRight.setText("" + year + copyright);

            TextView webLink = (TextView) findViewById(R.id.webLink);
            webLink.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);

            webLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("http://www.honeywellaidc.com"));
                    startActivity(intent);
                }
            });

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
