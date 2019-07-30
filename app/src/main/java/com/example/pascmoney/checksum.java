package com.example.pascmoney;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;


public class checksum extends AppCompatActivity implements PaytmPaymentTransactionCallback {
    String custid="", orderId="", mid="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        //initOrderId();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        Intent intent = getIntent();
        orderId = intent.getExtras().getString("orderid");
        custid = intent.getExtras().getString("custid");
        mid = "axESEi44386194598879"; /// your merchant id
        sendUserDetailTOServerdd dl = new sendUserDetailTOServerdd();
        dl.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
// vollye , retrofit, asynch
    }
    public class sendUserDetailTOServerdd extends AsyncTask<ArrayList<String>, Void, String> {
        private ProgressDialog dialog = new ProgressDialog(checksum.this);
        //private String orderId , mid, custid, amt;
        String url ="https://pascpayment.000webhostapp.com/generateChecksum.php";
        String verifyurl = "https://securegw.paytm.in/theia/paytmCallback?ORDER_ID=" + orderId;

        //String varifyurl = "https://pascpayment.000webhostapp.com/verifyChecksum.php";
        // "https://pguat.paytm.com/paytmchecksum/paytmCallback.jsp";
        String CHECKSUMHASH ="";
        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Please wait");
            this.dialog.show();
        }
        protected String doInBackground(ArrayList<String>... alldata) {
            JSONParser jsonParser = new JSONParser(checksum.this);
            String param=
                    "MID="+mid+
                            "&ORDER_ID=" + orderId+
                            "&CUST_ID="+custid+
                            "&CHANNEL_ID=WAP&TXN_AMOUNT=1&WEBSITE=PRODUCTION" +
                            "&CALLBACK_URL=" + verifyurl + "&INDUSTRY_TYPE_ID=Retail";
            JSONObject jsonObject = jsonParser.makeHttpRequest(url,"POST",param);
            // yaha per checksum ke saht order id or status receive hoga..
            Log.e("CheckSum result >>",jsonObject.toString());
            if(jsonObject != null){
                Log.e("CheckSum result >>",jsonObject.toString());
                try {
                    CHECKSUMHASH=jsonObject.has("CHECKSUMHASH")?jsonObject.getString("CHECKSUMHASH"):"";
                    Log.e("CheckSum result >>",CHECKSUMHASH);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return CHECKSUMHASH;
        }
        @Override
        protected void onPostExecute(String result) {
            Log.e(" setup acc ","  signup result  " + result);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            PaytmPGService Service = PaytmPGService.getProductionService();
            // when app is ready to publish use production service
            // PaytmPGService  Service = PaytmPGService.getProductionService();
            // now call paytm service here
            //below parameter map is required to construct PaytmOrder object, Merchant should replace below map values with his own values
            HashMap<String, String> paramMap = new HashMap<String, String>();
            //these are mandatory parameters
            paramMap.put("MID", mid); //MID provided by paytm
            paramMap.put("ORDER_ID", orderId);
            paramMap.put("CUST_ID", custid);
            paramMap.put("CHANNEL_ID", "WAP");
            paramMap.put("TXN_AMOUNT", "1");
            paramMap.put("WEBSITE", "PRODUCTION");
            paramMap.put("CALLBACK_URL", verifyurl);
            //paramMap.put( "EMAIL" , "abc@gmail.com");   // no need
            // paramMap.put( "MOBILE_NO" , "9144040888");  // no need
            paramMap.put("CHECKSUMHASH" ,CHECKSUMHASH);
            //paramMap.put("PAYMENT_TYPE_ID" ,"CC");    // no need
            paramMap.put("INDUSTRY_TYPE_ID", "Retail");
            PaytmOrder Order = new PaytmOrder(paramMap);
            Log.i("checksum ", "param "+ paramMap.toString());
            Service.initialize(Order,null);
            // start payment service call here
            Service.startPaymentTransaction(checksum.this, true, true, new PaytmPaymentTransactionCallback() {
                /*Call Backs*/
                public void someUIErrorOccurred(String inErrorMessage) {
                }

                public void onTransactionResponse(Bundle inResponse) {
                    Toast.makeText(getApplicationContext(), "Payment Transaction response " + inResponse.toString(), Toast.LENGTH_LONG).show();
                    Log.e("Response", inResponse.toString());

                    Intent i = new Intent(getApplicationContext(), Success.class);
                    i.putExtra("Response", inResponse.toString());
                    startActivity(i);
                    finish();
                }

                public void networkNotAvailable() {
                }

                public void clientAuthenticationFailed(String inErrorMessage) {
                }

                public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl) {
                }

                public void onBackPressedCancelTransaction() {
                    Log.e("checksum ", " cancel call back respon  ");
                    Toast.makeText(getApplicationContext(), "Payment Transaction response ", Toast.LENGTH_LONG).show();

                    Intent i = new Intent(getApplicationContext(), Success.class);
                    startActivity(i);
                    finish();


                }

                public void onTransactionCancel(String inErrorMessage, Bundle inResponse) {

                    Toast.makeText(getApplicationContext(), "Payment Transaction response " + inResponse.toString(), Toast.LENGTH_LONG).show();
                    Log.e("Response", inResponse.toString());

                    Intent i = new Intent(getApplicationContext(), Success.class);
                    i.putExtra("Response", inResponse.toString());
                    startActivity(i);
                    finish();
                }
            });
        }
    }
    @Override
    public void onTransactionResponse(Bundle inResponse) {
        /*Display the message as below */
        Toast.makeText(getApplicationContext(), "Payment Transaction response " + inResponse.toString(), Toast.LENGTH_LONG).show();
    }
    @Override
    public void networkNotAvailable() {
        Toast.makeText(this, "Network=null", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void clientAuthenticationFailed(String s) {
    }
    @Override
    public void someUIErrorOccurred(String s) {
        Log.e("checksum ", " ui fail respon  "+ s );
    }
    @Override
    public void onErrorLoadingWebPage(int i, String s, String s1) {
        Log.e("checksum ", " error loading pagerespon true "+ s + "  s1 " + s1);
    }
    @Override
    public void onBackPressedCancelTransaction() {
        Log.e("checksum ", " cancel call back respon  " );
        Toast.makeText(getApplicationContext(), "Payment Transaction response ", Toast.LENGTH_LONG).show();

        Intent i = new Intent(getApplicationContext(), Success.class);
        startActivity(i);
        finish();

    }
    @Override
    public void onTransactionCancel(String s, Bundle bundle) {
        Log.e("checksum ", "  transaction cancel " );
    }
}
//8059130863