package samples.exoguru.materialtabs;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Edwin on 15/02/2015.
 */

public class poFragment extends Fragment {

    String url = "http://203.151.92.125:888/uploadfile.php";
    String uploadfileName = "";

    String strBarcode="", strQty = "", strFrom = "", strTo = "", strRef = "", strfilename = "",strUserCode = "";
    EditText inBarCode, inRef, inQty, ediUserCode;
    TextView viewResult,  viewTotalItem, viewTotalQty;

    Button btnClearBarcode, btnSend, btnClearAll, btnSendBarcode;

    int totalQty = 0,totalItem = 0;

    HashMap<String,Integer> qtymap;
  //  HashMap<String,String> datamap;

    ArrayList<String> datalist;

    Spinner spinFrom,spinTo;

    short selectedMode = 0;

    public InputMethodManager imm;
    /* 0 = usereCode
       1 = barcode
       2 = ref
       3 = qty
       */

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
     //   super.onCreate(savedInstanceState);
      /*  qtymap = new HashMap<String,Integer>();
        datamap = new HashMap<String,String>();
        */

        datalist = new ArrayList<String>();
        qtymap = new HashMap<String,Integer>();


        final View view = inflater.inflate(R.layout.po_fragment, container, false);
        inQty = (EditText) view.findViewById(R.id.inQTY);
        inBarCode = (EditText) view.findViewById(R.id.inBarcode);
        viewResult = (TextView) view.findViewById(R.id.viewResult);
        btnClearBarcode = (Button) view.findViewById(R.id.btnClearBarcode);
        btnSend = (Button) view.findViewById(R.id.btnSend);
        btnClearAll = (Button) view.findViewById(R.id.btnClerall);
        btnSendBarcode = (Button) view.findViewById(R.id.btnSendBarcode);
        ediUserCode = (EditText) view.findViewById(R.id.ediUserCode);
        imm = (InputMethodManager) view.getContext().getSystemService(view.getContext().INPUT_METHOD_SERVICE);

        List<String> storeList = new ArrayList<String>();
        storeList.add("00 - คลังกลาง");
        storeList.add("01 - ขอนแก่น");
        storeList.add("02 - มหาวิทยาลัยขอนแก่น");
        storeList.add("03 - สยามสแควร์");
        storeList.add("04 - อโศก");
        storeList.add("05 - รังสิต");
        storeList.add("06 - เมกะ บางนา");

        this.initSpinner(view, storeList);
        this.initBtnOperation(view);

        inRef = (EditText) view.findViewById(R.id.inRef);
        inBarCode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //  if (event.getAction() == KeyEvent.ACTION_UP || keyCode == KeyEvent.KEYCODE_ENTER ) {
                if (keyCode == 66) {
                    //  insertRecord();
                    selectedMode = 3;
                    //inBarCode.setNextFocusDownId(inQty.getId());
                    inBarCode.clearFocus();
                    inBarCode.setCursorVisible(false);

                    inQty.requestFocus();

                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                    inQty.setCursorVisible(true);
                }
                return false;

            }
        });

        btnClearBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearBarcode();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                   // if (datamap.size() == 0) {
                     if (datalist.size() == 0) {
                        Toast.makeText(getActivity(), "ยังไม่มีกาๆกรอกข้อมูล", Toast.LENGTH_SHORT).show();
                    } else writeCSV();
                } catch (IOException e) {
                    viewResult.setText(e.getMessage());
                }
                final String strSDPath = uploadfileName;
                final String strUrlServer = url;


                new UploadFileAsync().execute(strSDPath, strUrlServer);
            }
        });

        btnClearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAll();
            }
        });

        viewTotalItem = (TextView) view.findViewById(R.id.viewTotalItem);
        viewTotalQty = (TextView) view.findViewById(R.id.viewToTalQty);

        ediUserCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedMode = 0;
            }
        });
        ediUserCode.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                selectedMode = 0;
                return false;
            }
        });
        inRef.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                selectedMode = 1;
                return false;
            }
        });
        inRef.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedMode = 1;
            }
        });
        inBarCode.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                inBarCode.setCursorVisible(true);
                selectedMode = 2;
                return false;
            }
        });
        inBarCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inBarCode.setCursorVisible(true);
                selectedMode = 2;
            }
        });
        inQty.setShowSoftInputOnFocus(false);
        inQty.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                selectedMode = 3;
                return false;
            }
        });
        inQty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                selectedMode = 3;
            }
        });

        btnSendBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertRecord();
            }
        });


    /*    Button btnClickme = (Button) view.findViewById(R.id.btnClickme);
        btnClickme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            //    for (String value : datamap.values()){
                for (String value : datalist){
                    Log.d("Click ME :", value);

                }
            }
        });

        */
        return view;
    }



    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser) {
            Activity a = getActivity();
            if(a != null) a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    public void insertRecord(){
        String line = "";

        strBarcode = inBarCode.getText().toString();
        strQty = inQty.getText().toString();
        strRef = inRef.getText().toString();
        strUserCode = ediUserCode.getText().toString();

        if(strQty == null || strQty.equals("") || strQty.equals("0")){
           // strQty = "1";
            strQty = "0";
            inQty.setText(strQty);
        }

        int iQty = Integer.parseInt(strQty.trim());
        if (!isEmpty()) {
          /*  if (qtymap.containsKey(strBarcode)){
                qtymap.put(strBarcode, qtymap.get(strBarcode) + iQty);
            }else qtymap.put(strBarcode, iQty);

            */

            qtymap.put(strBarcode, iQty);

            line = strRef.trim() + "," + strFrom.trim() + "," + strTo.trim() + "," + strBarcode.trim() + "," + qtymap.get(strBarcode).toString().trim();
           // datamap.put(strBarcode, line);
            datalist.add(line);
            viewResult.setText(line);

            totalQty += iQty;
            //totalItem = datamap.size();
            totalItem = datalist.size();

            viewTotalItem.setText(Integer.toString(totalItem));
            viewTotalQty.setText(Integer.toString(totalQty));

            clearBarcode();
            clearData();

            strQty = "";
            inQty.setText(strQty);

        }
        /*else{
            clearBarcode();
        }*/
    }

    private void initSpinner(View view, List<String> storeList){

        spinFrom = (Spinner) view.findViewById(R.id.spinFrom);

        spinTo = (Spinner) view.findViewById(R.id.spinTo);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item, storeList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinFrom.setAdapter(dataAdapter);

        spinFrom.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                String splitStore[] = selectedItem.split(" - ");
                strFrom = splitStore[0];

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinTo.setAdapter(dataAdapter);

        spinTo.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                String splitStore[] = selectedItem.split(" - ");
                strTo = splitStore[0];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initBtnOperation(View view){

        final Button btn0 = (Button) view.findViewById(R.id.btn0);
        btn0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appendQty(btn0.getText().toString());
            }
        });

        final Button btn1 = (Button) view.findViewById(R.id.btn1);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appendQty(btn1.getText().toString());
            }
        });

        final Button btn2 = (Button) view.findViewById(R.id.btn2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appendQty(btn2.getText().toString());
            }
        });

        final Button btn3 = (Button) view.findViewById(R.id.btn3);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appendQty(btn3.getText().toString());
            }
        });

        final Button btn4 = (Button) view.findViewById(R.id.btn4);
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appendQty(btn4.getText().toString());
            }
        });

        final Button btn5 = (Button) view.findViewById(R.id.btn5);
        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appendQty(btn5.getText().toString());
            }
        });

        final Button btn6 = (Button) view.findViewById(R.id.btn6);
        btn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appendQty(btn6.getText().toString());
            }
        });

        final Button btn7 = (Button) view.findViewById(R.id.btn7);
        btn7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appendQty(btn7.getText().toString());
            }
        });

        final Button btn8 = (Button) view.findViewById(R.id.btn8);
        btn8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appendQty(btn8.getText().toString());
            }
        });

        final Button btn9 = (Button) view.findViewById(R.id.btn9);
        btn9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appendQty(btn9.getText().toString());
            }
        });

        final Button btnClear = (Button) view.findViewById(R.id.btnClear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearData();
            }
        });

        final  Button btnEnter = (Button) view.findViewById(R.id.btnEnter);
        btnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertRecord();
                inQty.clearFocus();
                inBarCode.requestFocus();
                selectedMode = 2;
            }
        });
    }

    private void clearData(){
        switch (selectedMode){
            case 0: //ref
                strUserCode = "";
                ediUserCode.setText(strUserCode);
                break;
            case 1: //ref
                strRef = "";
                inRef.setText(strRef);
                break;
            case 2: //barcode
                strBarcode = "";
                inBarCode.setText(strBarcode);
                break;
            case 3: //qty
                strQty = "";
                inQty.setText(strQty);
                break;
        }

    }

    private void clearBarcode(){
        strBarcode = "";
        inBarCode.setText(strBarcode);
    }

    private void clearAll(){
        totalItem = 0;
        totalQty = 0;

        qtymap.clear();
    //    datamap.clear();
        datalist.clear();

        strQty = "";
        strRef = "";
        strFrom = "01";
        strTo = "01";
        strBarcode="";
        strUserCode =  "";


        uploadfileName = "";

        viewTotalItem.setText("");
        viewTotalQty.setText("");
        viewResult.setText("");
        inRef.setText("");
        inBarCode.setText("");
        inQty.setText("");
        ediUserCode.setText("");

        spinFrom.setSelection(0);
        spinTo.setSelection(0);

        selectedMode = 1;

    }

    private void appendQty(String strBtnValue){
        switch (selectedMode){
            case 0: //ref
                if(strUserCode.equals("") && strUserCode == null && ediUserCode.getText().equals("")) strUserCode = strBtnValue;
                else {
                    strUserCode = ediUserCode.getText().toString();
                    strUserCode = strUserCode.concat(strBtnValue);
                }
                ediUserCode.setText(strUserCode);
                break;
            case 1: //ref
                if(strRef.equals("") && strRef == null && inRef.getText().equals("")) strRef = strBtnValue;
                //else strRef = strRef.concat(strBtnValue);
                else {
                    strRef = inRef.getText().toString();
                    strRef = strRef.concat(strBtnValue);
                }
                inRef.setText(strRef);
                break;
            case 2: //barcode
                if(strBarcode.equals("") && strBarcode == null && inBarCode.getText().equals("")) strBarcode = strBtnValue;
             //   else strBarcode = strBarcode.concat(strBtnValue);
                else {
                    strBarcode = inBarCode.getText().toString();
                    strBarcode = strBarcode.concat(strBtnValue);
                }
                inBarCode.setText(strBarcode);
                break;
            case 3: //qty
                if(strQty.equals("0") && strQty.equals("") && strQty == null && inQty.getText().equals("")) strQty = strBtnValue;
               // else strQty = strQty.concat(strBtnValue);
                else {
                    strQty = inQty.getText().toString();
                    strQty = strQty.concat(strBtnValue);
                }
                inQty.setText(strQty);
                break;
        }

    }

    public boolean isEmpty(){
        if (strUserCode == null || strUserCode.equals("")){
            viewResult.setText("โปรดระบุรหัสผู้ใช้งาน");
            return true;
        }
        if (strRef == null || strRef.equals("")){
            viewResult.setText("โปรดระบุหมายเลขอ้างอิง");
            return true;
        }
        if(strBarcode == null || strBarcode.equals("")){
            viewResult.setText("โปรดระบุบาร์โค้ท");
            return true;
        }
        if(strFrom == null || strFrom.equals("")){
            viewResult.setText("โปรดระบุต้นทาง");
            return true;
        }
        if(strTo == null || strTo.equals("")){
            viewResult.setText("โปรดระบุปลายทาง");
            return true;
        }
        if(strQty == null || strQty.equals("") || strQty.equals("0") || strQty == "0"){
            viewResult.setText("โปรดระบุจำนวน");
            return true;
        }
        return false;
    }



    public void writeCSV() throws IOException {
            final String fileName;
            //String line;

            File folder = new File(Environment.getExternalStorageDirectory(), "CSV");
            if (!folder.exists()) {
                folder.mkdirs();
            }

   //         fileName =  strDocnum + "_" + strRef + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".csv";
           // fileName =  strDocnum + "_" + strRef + "_" + ".csv";

            fileName = strUserCode + "_" + new SimpleDateFormat("ddMMyy").format(new Date()) + "_" + "Transfer" + "_" + strRef + ".TXT";
            strfilename = fileName;

            File csvfile = new File(folder, fileName);
            csvfile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(csvfile);
            OutputStreamWriter sw =
                    new OutputStreamWriter(fOut);

    /*        for (Map.Entry<String,String> entry : datamap.entrySet()){
                line = entry.getValue();
                sw.append(line);
                sw.append("\r\n");
            }
*/
            for (String value : datalist){
                sw.append(value);
                sw.append("\r\n");
            }

            sw.close();
            fOut.close();

            viewResult.setText("Create Success : " + Environment.getExternalStorageDirectory().toString() + "/CSV/" + fileName);
            uploadfileName = Environment.getExternalStorageDirectory().toString() + "/CSV/" + fileName;
    }

    public void showSuscess(String resServer) {

        /** Get result from Server (Return the JSON Code)
         * StatusID = ? [0=Failed,1=Complete]
         * Error	= ?	[On case error return custom error message]
         *
         * Eg Upload Failed = {"StatusID":"0","Error":"Cannot Upload file!"}
         * Eg Upload Complete = {"StatusID":"1","Error":""}
         */

        /*** Default Value ***/
        clearAll();
        String strStatusID = "0";
        String strError = "อัพโหลดล้มเหลว โปรดลองใหม่ภายหลัง.";

        try {

            JSONObject c = new JSONObject(resServer);

            strStatusID = c.getString("StatusID");
            strError = c.getString("Error");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // Prepare Status
        if (strStatusID.equals("0")) {
            AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
            ad.setTitle("Error!");
            ad.setIcon(android.R.drawable.btn_star_big_on);
            ad.setMessage(strError);
            ad.setPositiveButton("Close", null);
            ad.show();
        } else {
            /*  // wait for fix
            String deletefile = "";
            deletefile = Environment.getExternalStorageDirectory().toString() + "/CSV/" + strfilename;

            File file = new File(deletefile);

            boolean delete = file.delete();
            */
            Toast.makeText(getActivity(), "Upload file Successfully", Toast.LENGTH_SHORT).show();
        }
    }

    public class UploadFileAsync extends AsyncTask<String, Void, Void> {

        String resServer = "[Network Error.!!!!]";

        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            // TODO Auto-generated method stub

            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            int resCode = 0;
            String resMessage = "";

            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary =  "*****";

            String strSDPath = params[0];
            String strUrlServer = params[1];

            try {
                File file = new File(strSDPath);
                if(!file.exists())
                {
                    resServer = "ไม่พบเอกสารใน SD Card";
                    return null;
                }

                FileInputStream fileInputStream = new FileInputStream(new File(strSDPath));

                URL url = new URL(strUrlServer);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");

                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Type",
                        "multipart/form-data;boundary=" + boundary);

                DataOutputStream outputStream = new DataOutputStream(conn
                        .getOutputStream());
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream
                        .writeBytes("Content-Disposition: form-data; name=\"filUpload\";filename=\""
                                + strSDPath + "\"" + lineEnd);
                outputStream.writeBytes(lineEnd);

                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // Read file
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    outputStream.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Response Code and  Message
                resCode = conn.getResponseCode();
                if(resCode == HttpURLConnection.HTTP_OK)
                {
                    InputStream is = conn.getInputStream();
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();

                    int read = 0;
                    while ((read = is.read()) != -1) {
                        bos.write(read);
                    }
                    byte[] result = bos.toByteArray();
                    bos.close();

                    resMessage = new String(result);

                }

                Log.d("resCode=", Integer.toString(resCode));
                Log.d("resMessage=",resMessage.toString());

                fileInputStream.close();
                outputStream.flush();
                outputStream.close();


                Log.d("send = ", resServer);

                resServer = resMessage.toString();

                Log.d("resServer = ",  resServer);

            } catch (Exception ex) {
                // Exception handling
                return null;
            }

            return null;
        }

        protected void onPostExecute(Void unused) {
            showSuscess(resServer);
        }

    }

}