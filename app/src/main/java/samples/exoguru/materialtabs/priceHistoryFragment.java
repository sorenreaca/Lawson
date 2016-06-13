package samples.exoguru.materialtabs;

import android.app.AlertDialog;
import android.content.res.Resources;
import android.graphics.Color;
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
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Edwin on 15/02/2015.
 */
public class priceHistoryFragment extends Fragment {


    Button btnSend;
    EditText ediBarcode;

    TableLayout salesHisttable;
    String barcode;
    TextView itemNameTextView;
    TextView itemIDTextView;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.price_history_fragment,container,false);

        this.salesHisttable = (TableLayout) view.findViewById(R.id.salesHisttable);
        this.itemNameTextView = (TextView) view.findViewById(R.id.itemName);
        this.itemIDTextView = (TextView) view.findViewById(R.id.itemId);

        this.ediBarcode = (EditText) view.findViewById(R.id.ediBarcode);

        this.btnSend = (Button) view.findViewById(R.id.btnsend);
        this.btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                barcode = ediBarcode.getText().toString();
                salesHisttable.removeAllViews();
                new RequestTask().execute(barcode);
                ediBarcode.setText("");
                ediBarcode.requestFocus();
            }
        });

        this.ediBarcode = (EditText) view.findViewById(R.id.ediBarcode);
        ediBarcode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == 66) {

                }
                return false;

            }
        });

        return view;
    }


    class RequestTask extends AsyncTask<String, String, String> {

        String resServer = "[Network Error.!!!!]";

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
            String url = "http://203.151.92.125:888/salesHist.php";
            Log.d("doInBackground", url);

            resServer = this.loadData(args[0], url);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (resServer.equals("0") ) {
                AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
                ad.setTitle("Error!");
                ad.setIcon(android.R.drawable.btn_star_big_on);
                ad.setMessage("ไม่สามารถเชื่อมต่อกับเซิร์ฟเวอร์ได้ โปรดลองใหม่อีกครั้ง.");
                ad.setPositiveButton("Close", null);
                ad.show();
            }else {
                itemIDTextView.setText("");
                itemNameTextView.setText("");
                createTableRow(resServer);
            }
        }

        private TableRow createHeader(){
            TableRow headerRows = new TableRow(getActivity());
            Resources resource = getActivity().getResources();

            TextView viewHeaderYear = new TextView(getActivity());
            viewHeaderYear.setTextSize(14);
            viewHeaderYear.setPadding(0, 5, 0, 5);
            viewHeaderYear.setText("ปี");

            TextView viewHeaderMonth = new TextView(getActivity());
            viewHeaderMonth.setTextSize(14);
            viewHeaderMonth.setPadding(0, 5, 0, 5);
            viewHeaderMonth.setText("เดือน");

            TextView viewHeaderWH = new TextView(getActivity());
            viewHeaderWH.setTextSize(14);
            viewHeaderWH.setPadding(0, 5, 0, 5);
            viewHeaderWH.setText("WH");

            TextView viewHeaderQTY = new TextView(getActivity());
            viewHeaderQTY.setTextSize(14);
            viewHeaderQTY.setPadding(0, 5, 0, 5);
            viewHeaderQTY.setText("ยอด");

            headerRows.addView(viewHeaderYear);
            headerRows.addView(viewHeaderMonth);
            headerRows.addView(viewHeaderWH);
            headerRows.addView(viewHeaderQTY);
            headerRows.setBackgroundColor(Color.parseColor("#80CBC4"));
            return headerRows;

        }

        private TableRow createfootter(int sumQty){
            TableRow footerRows = new TableRow(getActivity());

            TextView viewHeaderYear = new TextView(getActivity());
            viewHeaderYear.setTextSize(14);
            viewHeaderYear.setPadding(0, 5, 0, 5);
            viewHeaderYear.setText(" ");

            TextView viewHeaderMonth = new TextView(getActivity());
            viewHeaderMonth.setTextSize(14);
            viewHeaderMonth.setPadding(0, 5, 0, 5);
            viewHeaderMonth.setText(" ");

            TextView viewHeaderWH = new TextView(getActivity());
            viewHeaderWH.setTextSize(14);
            viewHeaderWH.setPadding(0, 5, 0, 5);
            viewHeaderWH.setText("รวม");
            viewHeaderWH.setBackgroundColor(Color.parseColor("#F4AFBD"));


            TextView viewHeaderQTY = new TextView(getActivity());
            viewHeaderQTY.setTextSize(14);
            viewHeaderQTY.setPadding(0, 5, 0, 5);
            viewHeaderQTY.setText(Integer.toString(sumQty));
            viewHeaderQTY.setBackgroundColor(Color.parseColor("#F4AFBD"));

            footerRows.addView(viewHeaderYear);
            footerRows.addView(viewHeaderMonth);
            footerRows.addView(viewHeaderWH);
            footerRows.addView(viewHeaderQTY);
           // footerRows.setBackgroundColor(Color.parseColor("#F4AFBD"));

            return footerRows;

        }


        public void createTableRow(String response){
            Log.d("createTableRow", response);
            int i = 0;
            int totalQty = 0;
            String privPeriod = "", privMonth = "";
            String SalesPeriod = "";
            String SalesMonth = "";
            String SalesQty = "";
            String InventLocationId = "";
            String Itemid = "";
            String Itemname ="";
            try {
                JSONArray jsonArry = new JSONArray(response);

                Log.d("jsonArry", Integer.toString(jsonArry.length()));

                //add Header
                salesHisttable.addView(createHeader());

                for (i=0;i<jsonArry.length();i++){

                    TableRow rows = new TableRow(getActivity());
                    rows.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                    JSONObject obj = jsonArry.getJSONObject(i);

                    SalesPeriod = obj.getString("SalesPeriod");
                    SalesMonth = obj.getString("SalesMonth");
                    SalesQty = obj.getString("SalesQty");
                    InventLocationId = obj.getString("InventLocationId");
                    Itemid = obj.getString("ITEMID");
                    Itemname = obj.getString("DESCRIPTION");

                    TextView viewSalesPeriod = new TextView(getActivity());
                    viewSalesPeriod.setTextSize(14);
                    viewSalesPeriod.setPadding(0, 5, 0, 5);

                    TextView viewSalesMonth = new TextView(getActivity());
                    viewSalesMonth.setTextSize(14);
                    viewSalesMonth.setPadding(0, 5, 0, 5);
                    if(SalesMonth.equals(privMonth) && SalesPeriod.equals(privPeriod)){
                        viewSalesPeriod.setText("");
                        viewSalesMonth.setText("");
                    }else{
                        viewSalesPeriod.setText(SalesPeriod);
                        viewSalesMonth.setText(SalesMonth);

                        if(i!=0) {
                            salesHisttable.addView(createfootter(totalQty));
                            totalQty = 0;
                        }
                    }

                    TextView viewWH = new TextView(getActivity());
                    viewWH.setTextSize(14);
                    viewWH.setPadding(0, 5, 0, 5);
                    viewWH.setText(InventLocationId.toString());

                    TextView viewQty = new TextView((getActivity()));
                    viewQty.setTextSize(14);
                    viewQty.setPadding(0, 5, 0, 5);
                    viewQty.setText(SalesQty.toString());
                    totalQty = totalQty + Integer.parseInt(SalesQty);


                    rows.addView(viewSalesPeriod);
                    rows.addView(viewSalesMonth);
                    rows.addView(viewWH);
                    rows.addView(viewQty);

                    salesHisttable.addView(rows);

                    privPeriod = SalesPeriod;
                    privMonth = SalesMonth;
                }
                salesHisttable.addView(createfootter(totalQty));

                itemIDTextView.setText(Itemid);
                itemNameTextView.setText(Itemname);

            }catch (JSONException e){
                Log.d("JSONException", e.getMessage());
            }

        }
    }

}
