package com.satejkalebere.kaleberemedicals;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CALL=1;
    final int GOOGLE_PAY_REQUEST_CODE=123;


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new HomeFragment()).commit();

    }
    private void makePhoneCall(){
        String number="+918308365608";
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CALL_PHONE},REQUEST_CALL);
        }else{
            String dial="tel:"+number;
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==REQUEST_CALL){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                makePhoneCall();

            }else {
                Toast.makeText(this,"Permission DENIED",Toast.LENGTH_SHORT).show();
            }
        }
    }


    private BottomNavigationView.OnNavigationItemSelectedListener navListener= new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;
            Intent intent=null;

            switch (item.getItemId()){
                case R.id.nav_home:
                    selectedFragment=new HomeFragment();
                    break;
                case R.id.nav_call:
                    selectedFragment=new CallFragment();
                    makePhoneCall();
                    break;
               case R.id.nav_location:
                    intent=new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("geo:16.410082, 73.997315"));
                   Intent chooser = Intent.createChooser(intent, "Launch Maps");
                    startActivity(chooser);
                    selectedFragment=new LocateFragment();
                    break;
                case R.id.nav_whatsapp:
                    boolean installed= appInstalledOrNot("com.whatsapp");
                    if(installed){
                        intent= new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("http://api.whatsapp.com/send?phone="+"+918308365608"+"&text=Required medicines: "));
                        startActivity(intent);
                    }else{
                        Toast.makeText(MainActivity.this, "WhatsApp is not installed on your phone!", Toast.LENGTH_SHORT).show();
                    }
                    selectedFragment=new WhatsAppFragment();
                    break;

                case R.id.nav_payment:
                    selectedFragment=new PaymentFragment();
                    payUsingUpi();
                    break;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,selectedFragment).commit();
            return true;
        }

    };
    private void payUsingUpi(){
        String GOOGLE_PAY_PACKAGE_NAME = "com.google.android.apps.nbu.paisa.user";

        Uri uri = new Uri.Builder()
                        .scheme("upi").authority("pay")
                        .appendQueryParameter("pa", "tkalebere-1@okicici")
                        .appendQueryParameter("pn", "Tejas Kalebere")
                        //.appendQueryParameter("mc", "your-merchant-code")
                        //.appendQueryParameter("tr", "your-transaction-ref-id")
                        .appendQueryParameter("tn", "Payment done by using Kalebere Medicals App")
                       .appendQueryParameter("am", "")
                        .appendQueryParameter("cu", "INR")
                       // .appendQueryParameter("url", "your-transaction-url")
                        .build();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setPackage(GOOGLE_PAY_PACKAGE_NAME);
        intent.setData(uri);






        Intent chooser=Intent.createChooser(intent,"Pay with");

        if (chooser.resolveActivity(getPackageManager())!=null){
            startActivityForResult(chooser, GOOGLE_PAY_REQUEST_CODE);
        }else {
            Toast.makeText(this, "No UPI app found on your phone!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GOOGLE_PAY_REQUEST_CODE){

            if(isInternetAvailabe(MainActivity.this)){

                if (data == null) {
                    ArrayList<String> dataList = new ArrayList<>();
                    dataList.add("nothing");
                    String temp = "nothing";
                    Toast.makeText(this, "Transaction not complete", Toast.LENGTH_SHORT).show();
                }else {
                    String text = data.getStringExtra("response");
                    ArrayList<String> dataList = new ArrayList<>();
                    dataList.add(text);

                    upiPaymentCheck(text);
                }
            }

        }
    }

    void upiPaymentCheck(String data){
        String str = data;

        String payment_cancel = "";
        String status = "";
        String response[] = str.split("&");

        for (int i = 0; i < response.length; i++)
        {
            String equalStr[] = response[i].split("=");
            if(equalStr.length >= 2)
            {
                if (equalStr[0].toLowerCase().equals("Status".toLowerCase()))
                {
                    status = equalStr[1].toLowerCase();
                }
            }
            else
            {
                payment_cancel = "Payment cancelled";
            }
        }
        if(status.equals("success")){
            Toast.makeText(this, "Transaction Successfull", Toast.LENGTH_SHORT).show();
        }else if("Payment cancelled".equals(payment_cancel)){
            Toast.makeText(this, "payment cancelled by user", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Transaction failed", Toast.LENGTH_SHORT).show();
        }
    }
    public static boolean isInternetAvailabe(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(context.CONNECTIVITY_SERVICE);
        if(connectivityManager != null){
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if(networkInfo.isConnected() && networkInfo.isConnectedOrConnecting() && networkInfo.isAvailable()){
                return true;
            }
        }
        return false;
    }

    private boolean appInstalledOrNot(String url){
        PackageManager packageManager=getPackageManager();
        boolean app_installed;
        try {
            packageManager.getPackageInfo(url,PackageManager.GET_ACTIVITIES);
            app_installed=true;
        }catch (PackageManager.NameNotFoundException e){
            app_installed=false;
        }

        return app_installed;
    }


}