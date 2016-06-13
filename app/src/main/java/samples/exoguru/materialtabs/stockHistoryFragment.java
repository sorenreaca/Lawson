package samples.exoguru.materialtabs;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Edwin on 15/02/2015.
 */
public class stockHistoryFragment extends Fragment {

    TableLayout priceTable;
    EditText ediBarcode;
    TextView viewSumRemine, viewSumRecive, viewSumDelivery, viewDepartment, viewName, viewPriceBKK, viewPriceKK, viewBarcode;
    int totalRemain, totalReceive, totalDelivery;
    Button btnSend;
    String barcode;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.stock_history_fragment,container,false);

        totalRemain = 0;
        totalDelivery = 0;
        totalReceive = 0;

        this.priceTable = (TableLayout) view.findViewById(R.id.priceTable);
        this.viewSumRecive = (TextView) view.findViewById(R.id.ediSumRecive);
        this.viewSumRemine = (TextView) view.findViewById(R.id.ediSumRemine);
        this.viewSumDelivery = (TextView) view.findViewById(R.id.ediSumDelivery);
        this.viewDepartment = (TextView) view.findViewById(R.id.ediDepartment);
        this.viewName = (TextView) view.findViewById(R.id.ediInventname);
        this.viewPriceBKK = (TextView) view.findViewById(R.id.ediBankokPrice);
        this.viewPriceKK = (TextView) view.findViewById(R.id.ediOtherPrice);
        this.viewBarcode = (TextView) view.findViewById(R.id.ediBarcode2);

        this.btnSend = (Button) view.findViewById(R.id.btnsend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                totalRemain = 0;
                totalDelivery = 0;
                totalReceive = 0;
                priceTable.removeAllViews();
                viewPriceBKK.setText("");
                viewPriceKK.setText("");
                viewName.setText("");
                viewSumRemine.setText("");
                viewSumRecive.setText("");
                viewSumDelivery.setText("");
                viewDepartment.setText("");
                barcode = ediBarcode.getText().toString();
                new RequestTask().execute(barcode);
                ediBarcode.setText("");
                ediBarcode.requestFocus();
            }
        });

        this.ediBarcode = (EditText) view.findViewById(R.id.ediBarcode);
        ediBarcode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == 66){

                }
                return false;

            }
        });

        return view;
    }

    class RequestTask extends AsyncTask<String, String, String> {

        String resServer = "[Network Error.!!!!]";
        String information  = "[Network Error.!!!!]";
        String priceBKK  = "[Network Error.!!!!]";
        String priceKK  = "[Network Error.!!!!]";

        private String loadData(String barcode, String url){
            // constants
            int timeoutSocket = 5000;
            int timeoutConnection = 5000;

            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
            HttpClient client = new DefaultHttpClient(httpParameters);

            //  HttpGet httpget = new HttpGet(url[0]);
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("content-type", "application/x-www-form-urlencoded;charset=utf-8");

            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                Log.d("doInBackground","arg = " + barcode);
                nameValuePairs.add(new BasicNameValuePair("barcode", barcode));
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse getResponse = client.execute(httpPost);
                final int statusCode = getResponse.getStatusLine().getStatusCode();

                if(statusCode != HttpStatus.SC_OK) {
                    Log.d("doInBackground", "Download Error: " + statusCode + "| for URL: " + url);
                    return null;
                }

                String line = "";
                StringBuilder total = new StringBuilder();

                HttpEntity getResponseEntity = getResponse.getEntity();

                BufferedReader reader = new BufferedReader(new InputStreamReader(getResponseEntity.getContent()));

                while((line = reader.readLine()) != null) {
                    total.append(line);
                }

                line = total.toString();
                Log.d("doInBackground", "return = " + line);
                return line;
            } catch (Exception e) {
                Log.d("doInBackground", "Download Exception : " + e.toString());
                String error = "0";
                return  error;
            }
        }

        @Override
        protected String doInBackground(String... args) {
            // constants
            String url = "http://203.151.92.125:888/transactionHistory.php";
            String url2 = "http://203.151.92.125:888/information.php";
            String url3 = "http://203.151.92.125:888/priceBKK.php";
            String url4 = "http://203.151.92.125:888/priceKK.php";

            resServer = this.loadData(args[0], url);
            information = this.loadData(args[0], url2);
            priceBKK = this.loadData(args[0], url3);
            priceKK = this.loadData(args[0], url4);

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (information.equals("0") || resServer.equals("0") || priceBKK.equals("0") || priceKK.equals("0") ) {
                AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
                ad.setTitle("Error!");
                ad.setIcon(android.R.drawable.btn_star_big_on);
                ad.setMessage("ไม่สามารถเชื่อมต่อกับเซิร์ฟเวอร์ได้ โปรดลองใหม่อีกครั้ง.");
                ad.setPositiveButton("Close", null);
                ad.show();
            }else {
                showinformation(information);
                createTableRow(resServer);
                showBkkPrice(priceBKK);
                showKKPrice(priceKK);
            }
        }
    }

    public void showBkkPrice(String response) {
        Log.d("createTableRow", response);
        int i = 0;
        try {
            JSONArray jsonArry = new JSONArray(response);

            Log.d("jsonArry", Integer.toString(jsonArry.length()));

            for (i=0;i<jsonArry.length();i++){

                TableRow rows = new TableRow(getActivity());
                rows.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                JSONObject obj = jsonArry.getJSONObject(i);

                String AMOUNT = obj.getString("AMOUNT");

                this.viewPriceBKK.setText(AMOUNT.toString());

            }
        }catch (JSONException e){
            Log.d("JSONException", e.getMessage());
        }
    }

    public void showKKPrice(String response) {
        Log.d("createTableRow", response);
        int i = 0;
        try {
            JSONArray jsonArry = new JSONArray(response);

            Log.d("jsonArry", Integer.toString(jsonArry.length()));

            for (i=0;i<jsonArry.length();i++){

                TableRow rows = new TableRow(getActivity());
                rows.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                JSONObject obj = jsonArry.getJSONObject(i);

                String AMOUNT = obj.getString("AMOUNT");

                this.viewPriceKK.setText(AMOUNT.toString());

            }
        }catch (JSONException e){
            Log.d("JSONException", e.getMessage());
        }
    }



    public void showinformation(String response) {

        String strStatusID = "0";
        String strError = "ไม่สามารถเชื่อมต่อกับเซิร์ฟเวอร์ได้ โปรดลองใหม่อีกครั้ง.";

        Log.d("createTableRow", response);
        int i = 0;
        try {
            JSONArray jsonArry = new JSONArray(response);

            Log.d("jsonArry", Integer.toString(jsonArry.length()));

            for (i=0;i<jsonArry.length();i++){

                TableRow rows = new TableRow(getActivity());
                rows.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                JSONObject obj = jsonArry.getJSONObject(i);

                String ITEMVENDREBATEGROUPID = obj.getString("ITEMVENDREBATEGROUPID");
                String NAME = obj.getString("NAME");

                this.viewName.setText(NAME.toString());
                this.viewDepartment.setText(ITEMVENDREBATEGROUPID.toString());
                this.viewBarcode.setText(barcode.toString());
            }


        }catch (JSONException e){
            Log.d("JSONException", e.getMessage());
        }
    }

    public void createTableRow(String response) {
        Log.d("createTableRow", response);
        int i = 0;
        try {
            JSONArray jsonArry = new JSONArray(response);

            Log.d("jsonArry", Integer.toString(jsonArry.length()));

            for (i=0;i<jsonArry.length();i++){

                TableRow rows = new TableRow(getActivity());
                rows.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                JSONObject obj = jsonArry.getJSONObject(i);

                String INVENTLOCATIONID = obj.getString("INVENTLOCATIONID");
                String REMAIN = obj.getString("REMAIN");
                String REMAINRECEIVE = obj.getString("REMAINDELIVERY");
                String REMAINDELIVERY = obj.getString("REMAINDELIVERY");

                totalRemain += Integer.parseInt(REMAIN);
                totalDelivery += Integer.parseInt(REMAINDELIVERY);
                totalReceive += Integer.parseInt(REMAINRECEIVE);

                TextView viewLocation = new TextView(getActivity());
                viewLocation.setTextSize(14);
                viewLocation.setPadding(0, 5, 0, 5);
                viewLocation.setText(INVENTLOCATIONID);

                TextView viewRemain = new TextView(getActivity());
                viewRemain.setTextSize(14);
                viewRemain.setPadding(0, 5, 0, 5);
                viewRemain.setText(REMAIN);

                TextView viewRecive = new TextView(getActivity());
                viewRecive.setTextSize(14);
                viewRecive.setPadding(0, 5, 0, 5);
                viewRecive.setText(REMAINRECEIVE);

                TextView viewDelivery = new TextView(getActivity());
                viewDelivery.setTextSize(14);
                viewDelivery.setPadding(0, 5, 0, 5);
                viewDelivery.setText(REMAINDELIVERY);

                rows.addView(viewLocation);
                rows.addView(viewRemain);
                rows.addView(viewRecive);
                rows.addView(viewDelivery);

                priceTable.addView(rows);
            }

            viewSumRemine.setText(Integer.toString(totalRemain));
            viewSumRecive.setText(Integer.toString(totalReceive));
            viewSumDelivery.setText(Integer.toString(totalDelivery));

        }catch (JSONException e){
            Log.d("JSONException", e.getMessage());
        }
    }

    private  void clearAll(){



    }
}
