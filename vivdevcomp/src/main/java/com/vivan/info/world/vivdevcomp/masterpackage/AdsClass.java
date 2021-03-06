package com.vivan.info.world.vivdevcomp.masterpackage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.InterstitialAdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;


import com.vivan.info.world.vivdevcomp.R;


import java.util.ArrayList;
import java.util.concurrent.Callable;

import cz.msebera.android.httpclient.Header;




public class AdsClass extends AppCompatActivity  {


    public static Boolean fst = true;
    public MasterListAdapter masterAdapter;
    GsonUtils gsonUtils;
    ProgressDialog pDialog;
    public static ArrayList<DialogDetail> dialogList = new ArrayList<>();
    public ArrayList<AdsDetail> adsList = new ArrayList<>();
    public static String currentadnetwork = "";



    public com.google.android.gms.ads.InterstitialAd googleInterstitialAd;
    private com.facebook.ads.InterstitialAd facebookInterstitialAd;
    private com.facebook.ads.AdView adView;




    private static String google_appid = "";
    private static String googleInterastialAdsId = "";
    private static String googleBannerAdsId = "";


    //for facebook
    private static String facebookInterstitialAdid = "";
    private static String facebookBannerAdid = "";

    public static boolean nodata = false;

    public static String AppKey = "";


    private MyAppClass showcaseApplication;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gsonUtils = GsonUtils.getInstance();
        showcaseApplication = ((MyAppClass) getApplication());
        if (isConnected(this)) {

            if (fst) {

                ApplicationInfo ai = null;
                try {
                    ai = this.getPackageManager().getApplicationInfo( this.getPackageName(), PackageManager.GET_META_DATA );
                    AppKey = String.valueOf(ai.metaData.get("my_app_id"));

                    // Toast.makeText(this, AppKey, Toast.LENGTH_SHORT).show();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }


                Webservice();
                fst = false;
            }


        }
    }

    public void setAppKey(String appKEY) {
        this.AppKey = appKEY;
    }

    private void Webservice() {
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading Message... Please wait");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params1 = new RequestParams();
        params1.put("app_key", AppKey);
        try {
            client.setConnectTimeout(50000);

            client.post("http://vivaninfoworld.com/api/get_appdata.php", params1, new BaseJsonHttpResponseHandler<AdsData>() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, AdsData response) {

                    //  Log.d("response",response.toString());
                    if (response.getSuccess() == 1) {
                        currentadnetwork = response.getCurrentad();
                        if (response.getDialogDetail().size() > 0) {
                            dialogList = new ArrayList<>();
                            dialogList.addAll(response.getDialogDetail());



                            if(!currentadnetwork.equals("addapptr"))
                            {
                                adsList.addAll(response.getAdsDetail());
                            }

                            for (int i = 0; i < adsList.size(); i++) {
                                if (currentadnetwork.equals("google")) {
                                    if (adsList.get(i).getAdNetwork().equals("google") && (adsList.get(i).getAdsType().equals("inter"))) {
                                        google_appid = adsList.get(i).getNetworkAppId();
                                        googleInterastialAdsId = adsList.get(i).getPlacementId();
                                    } else if (adsList.get(i).getAdNetwork().equals("google") && (adsList.get(i).getAdsType().equals("banner"))) {
                                        google_appid = adsList.get(i).getNetworkAppId();
                                        googleBannerAdsId = adsList.get(i).getPlacementId();
                                    }
                                    //loadInterastialAds();
                                } else if (currentadnetwork.equals("facebook")) {
                                    if (adsList.get(i).getAdNetwork().equals("facebook") && (adsList.get(i).getAdsType().equals("inter"))) {
                                        facebookInterstitialAdid = adsList.get(i).getPlacementId();
                                    } else if (adsList.get(i).getAdNetwork().equals("facebook") && (adsList.get(i).getAdsType().equals("banner"))) {
                                        facebookBannerAdid = adsList.get(i).getPlacementId();
                                    }
                                    //  loadInterastialAds();
                                }
                                else if (currentadnetwork.equals("addapptr")) {


                                }


                            }



                            initAdNetwork();


                            pDialog.dismiss();
                        } else {

                            pDialog.dismiss();
                            //   Toast.makeText(AdsClass.this, "No record Found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        nodata = true;
                        pDialog.dismiss();
                        Toast.makeText(AdsClass.this, "internal server error", Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, AdsData errorResponse) {
                    pDialog.dismiss();
                    Toast.makeText(AdsClass.this, "Server Fail", Toast.LENGTH_SHORT).show();
                }


                @Override
                protected AdsData parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                    pDialog.dismiss();
                    try {
                        if (!isFailure && !rawJsonData.isEmpty()) {
                            return gsonUtils.getGson().fromJson(rawJsonData, AdsData.class);
                        }
                    } catch (Exception e) {
                        Log.d("response--", e.getMessage().toString());
                    }
                    return null;
                }

                @Override
                public void onStart() {
                    super.onStart();
                }

                @Override
                public void onFinish() {
                    super.onFinish();

                }
            });

        } catch (Exception e) {

        }
    }




    private void showToastMessage(String message) {
        // Toast.makeText(AdsClass.this, message, Toast.LENGTH_SHORT).show();
    }


    @SuppressLint("MissingPermission")
    public void initGoogleAds() {
        MobileAds.initialize(AdsClass.this, google_appid);
    }

    @SuppressLint("MissingPermission")
    private void loadGoogleInterastialAds() {
        googleInterstitialAd = new com.google.android.gms.ads.InterstitialAd(this);
        googleInterstitialAd.setAdUnitId(googleInterastialAdsId);
        googleInterstitialAd.loadAd(new AdRequest.Builder().build());


    }

    private void showGoogleInterastialAds(Callable<Void> callable) {

        if (googleInterstitialAd.isLoaded()) {
            googleInterstitialAd.show();
            googleInterstitialAd.setAdListener(new com.google.android.gms.ads.AdListener() {

                @Override
                public void onAdClosed() {
                    try {
                        callable.call();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //  loadGoogleInterastialAds();
                }

                @Override
                public void onAdFailedToLoad(LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    try {
                        callable.call();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            try {
                callable.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showGoogleBannerAds() {
        //google banner
        AdView adView = new AdView(this);

        RelativeLayout bannerContainer = (RelativeLayout) findViewById(R.id.layout_banner);
        bannerContainer.addView(adView);
        adView.setAdUnitId(googleBannerAdsId);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.setAdSize(AdSize.LARGE_BANNER);
        adView.loadAd(adRequest);

    }

    public void initfacebookAds() {
        AudienceNetworkAds.initialize(this);
    }

    private void loadfacebookInterastialAds() {

        facebookInterstitialAd = new com.facebook.ads.InterstitialAd(AdsClass.this, facebookInterstitialAdid);
        facebookInterstitialAd.loadAd();

    }

    public void showFacebookAds(Callable<Void> callable) {
        if (facebookInterstitialAd.isAdLoaded()) {
            facebookInterstitialAd.show();
            InterstitialAdListener interstitialAdListener = new InterstitialAdListener() {
                @Override
                public void onInterstitialDisplayed(Ad ad) {

                }

                @Override
                public void onInterstitialDismissed(Ad ad) {
                    try {
                        callable.call();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //  loadfacebookInterastialAds();
                }

                @Override
                public void onError(Ad ad, AdError adError) {
                    try {
                        callable.call();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // loadfacebookInterastialAds();

                }

                @Override
                public void onAdLoaded(Ad ad) {


                }

                @Override
                public void onAdClicked(Ad ad) {

                }

                @Override
                public void onLoggingImpression(Ad ad) {

                }
            };

            facebookInterstitialAd.buildLoadAdConfig().withAdListener(interstitialAdListener);
        } else {
            try {
                callable.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void showfacebookBanner() {
        // facebook banner
        adView = new com.facebook.ads.AdView(this, facebookBannerAdid, com.facebook.ads.AdSize.BANNER_HEIGHT_50);
        RelativeLayout adContainer = (RelativeLayout) findViewById(R.id.layout_banner);
        adContainer.addView(adView);
        adView.loadAd();
    }



    public final static boolean isConnected(Context context) {
        final ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission") final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public class ViewDialog {

        public void showDialog(Activity activity, String msg, String title, String name, Boolean btn_update_show, String btn_update_txt, Boolean btn_cancel_show, String btn_cancel_txt, Boolean btn_start_show, String btn_start_txt, Boolean cancelable, String appurl, String iconurl) {
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.update_dialog);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            if (cancelable) {
                dialog.setCancelable(true);
            } else {
                dialog.setCancelable(false);
            }

            TextView txtMessage = (TextView) dialog.findViewById(R.id.txtMessage);
            TextView txtTitle = (TextView) dialog.findViewById(R.id.txtTitle);
            TextView txtName = (TextView) dialog.findViewById(R.id.txtAppName);
            ImageView imgIcon = (ImageView) dialog.findViewById(R.id.imgIcon);
            Glide.with(AdsClass.this)
                    .load(Uri.parse(iconurl))
                    .into(imgIcon);


            txtMessage.setText(msg);
            txtTitle.setText(title);
            txtName.setText(name);

            Button btn_update = (Button) dialog.findViewById(R.id.btn_update);
            if (!(btn_update_txt.equals(null) || btn_update_txt.equals(""))) {
                btn_update.setText(btn_update_txt);
            }

            if (btn_update_show) {
                btn_update.setVisibility(View.VISIBLE);
            } else {
                btn_update.setVisibility(View.GONE);
            }

            btn_update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(appurl));
                    startActivity(browserIntent);
                }
            });

            Button btn_cancel = (Button) dialog.findViewById(R.id.btn_cancel);
            if (!(btn_cancel_txt.equals(null) || btn_cancel_txt.equals(""))) {
                btn_cancel.setText(btn_cancel_txt);
            }
            if (btn_cancel_show) {
                btn_cancel.setVisibility(View.VISIBLE);
            } else {
                btn_cancel.setVisibility(View.GONE);
            }


            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Toast.makeText(getApplicationContext(),"Okay" ,Toast.LENGTH_SHORT).show();
                    dialog.cancel();
                }
            });

            Button btn_start = (Button) dialog.findViewById(R.id.btn_start);
            if (!(btn_start_txt.equals(null) || btn_start_txt.equals(""))) {
                btn_start.setText(btn_start_txt);
            }
            if (btn_start_show) {
                btn_start.setVisibility(View.VISIBLE);
            } else {
                btn_start.setVisibility(View.GONE);
            }


            btn_start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent in = new Intent(AdsClass.this, MasterActivity.class);
                    startActivity(in);

                }
            });

            dialog.show();
        }
    }

    public void showDialog() {

        if (isConnected(this)) {

            if (!nodata) {
                ViewDialog alert = new ViewDialog();

                Boolean btnCancelShow = Integer.parseInt(dialogList.get(0).getBtnCancelShow()) == 1 ? true : false;
                Boolean btnStartShow = Integer.parseInt(dialogList.get(0).getBtnStartShow()) == 1 ? true : false;
                Boolean btnUpdateShow = Integer.parseInt(dialogList.get(0).getBtnUpdateShow()) == 1 ? true : false;
                Boolean dialogcancelable = Integer.parseInt(dialogList.get(0).getDialogCancelable()) == 1 ? true : false;
                Boolean imgDialogShow = Integer.parseInt(dialogList.get(0).getDialogShow()) == 1 ? true : false;
                Boolean imgBannerShow = Integer.parseInt(dialogList.get(0).getImgBannerShow()) == 1 ? true : false;
                if (imgDialogShow) {
                    alert.showDialog(AdsClass.this, dialogList.get(0).getTxtMessage(), dialogList.get(0).getDialogType(), dialogList.get(0).getTxtName(), btnUpdateShow, dialogList.get(0).getBtnUpdateTxt(), btnCancelShow, dialogList.get(0).getBtnCancelTxt(), btnStartShow, dialogList.get(0).getBtnStartTxt(), dialogcancelable, dialogList.get(0).getAppUrl(), dialogList.get(0).getAppIconUrl());
                }
            }
        }

    }

    public void initAdNetwork() {

        if (currentadnetwork.equals("google")) {
            initGoogleAds();

        } else if (currentadnetwork.equals("facebook")) {
            initfacebookAds();

        }
        else{
            //  Toast.makeText(this, "No ads", Toast.LENGTH_SHORT).show();
        }

    }

    public void loadInterastialAds() {
        //   Log.d("adnetwork--",currentadnetwork);
        if (currentadnetwork.equals("google")) {
            loadGoogleInterastialAds();
        } else if (currentadnetwork.equals("facebook")) {
            loadfacebookInterastialAds();
        }
        else {
            //   Toast.makeText(this, "No ads", Toast.LENGTH_SHORT).show();
        }
    }


    public void showInterstitialAds(Callable<Void> callable) {
        if (currentadnetwork.equals("google")) {
            showGoogleInterastialAds(callable);
        } else if (currentadnetwork.equals("facebook")) {
            showFacebookAds(callable);
        }
        else {
            try {
                callable.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //  Toast.makeText(this, "No ads", Toast.LENGTH_SHORT).show();
        }
    }


    public void showBannerAds() {
        if (currentadnetwork.equals("google")) {
            initGoogleAds();
            showGoogleBannerAds();
        } else if (currentadnetwork.equals("facebook")) {
            initfacebookAds();
            showfacebookBanner();
        }
        else {
            //   Toast.makeText(this, "No ads", Toast.LENGTH_SHORT).show();
        }
    }







}
