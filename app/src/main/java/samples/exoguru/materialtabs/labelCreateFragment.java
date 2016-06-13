package samples.exoguru.materialtabs;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
import android.support.annotation.Nullable;
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

public class labelCreateFragment extends android.support.v4.app.Fragment {

    String url = "http://203.151.92.125:888/labelupload.php";
    String uploadfileName = "";

    String strBarcode="", strQty = "", strStore = "", strFilename = "", docName = "", strUserCode = "";

    EditText ediFileName, ediBarcode, ediQty, ediUserCode;
    TextView viewResult, viewTotalItem, viewTotalQty;

    Button btnClearBarcode, btnSend, btnClearAll, btnSendBarcode;

    Spinner spinStore;

    //HashMap<String,Integer> qtymap;
    //  HashMap<String,String> datamap;
    ArrayList<String> datalist;

    int totalQty = 0,totalItem = 0;

    short selectedMode = 0;

    public InputMethodManager imm;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.label_creater_fragment,container,false);

      //  qtymap = new HashMap<String,Integer>();
        // datamap = new HashMap<String,String>();
        datalist = new ArrayList<String>();
        ediBarcode = (EditText) v.findViewById(R.id.inBarcode);
        viewResult = (TextView) v.findViewById(R.id.viewResult);
        btnClearBarcode = (Button) v.findViewById(R.id.btnClearBarcode);
        btnSend = (Button) v.findViewById(R.id.btnSend);
        btnClearAll = (Button) v.findViewById(R.id.btnClerall);
        btnSendBarcode = (Button) v.findViewById(R.id.btnSendBarcode);
        ediUserCode = (EditText) v.findViewById(R.id.ediUserCode);
        imm = (InputMethodManager) v.getContext().getSystemService(v.getContext().INPUT_METHOD_SERVICE);

        btnClearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAll();
            }
        });

        ediUserCode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                viewResult.setText("ปิดเครื่องสแกนบาร์โค้ทก่อน");
            }
        });

        List<String> storeList = new ArrayList<String>();
        storeList.add("00 - คลังกลาง");
        storeList.add("01 - ขอนแก่น");
        storeList.add("02 - มหาวิทยาลัยขอนแก่น");
        storeList.add("03 - สยามสแควร์");
        storeList.add("04 - อโศก");
        storeList.add("05 - รังสิต");
        storeList.add("06 - เมกะ บางนา");


        ediBarcode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == 66){
                      insertRecord();

                    //inBarCode.setNextFocusDownId(inQty.getId());
                    ediBarcode.requestFocus();

                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
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
                    //if(datamap.size() == 0){
                    if(datalist.size() ==0){
                        Toast.makeText(getActivity(), "ยังไม่มีการกรอกข้อมูล", Toast.LENGTH_SHORT).show();
                    }else writeCSV();
                }catch (IOException e){
                    viewResult.setText(e.getMessage());
                }
                final String strSDPath = uploadfileName;
                final String strUrlServer = url;

                new UploadFileAsync().execute(strSDPath, strUrlServer);
            }
        });
        btnSendBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertRecord();
            }
        });
        this.setEdi(v);
        this.initSpinner(v, storeList);
        return v;
    }




    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setEdi(View v){

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
        ediBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedMode = 1;

            }
        });
        ediBarcode.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                selectedMode = 1;
                return false;
            }
        });

    }

    private void clearAll(){
        totalItem = 0;
        totalQty = 0;

       // qtymap.clear();
        //  datamap.clear();
        datalist.removeAll(datalist);

        strQty = "";
        strFilename = "";
        strStore = "00";
        strBarcode="";

        uploadfileName = "";

        ediBarcode.setText("");
        viewResult.setText("");

        spinStore.setSelection(0);

        strUserCode = "";
        ediUserCode.setText("");

        selectedMode = 0;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser) {
            Activity a = getActivity();
            if(a != null) a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }


    public void appendQty(String strBtnValue){
        switch (selectedMode){
            case 0: //filename
                if(strUserCode.equals("") && strUserCode == null && ediUserCode.getText().equals("")) strUserCode = strBtnValue;
                    //  else strFilename = strFilename.concat(strBtnValue);
                else {
                    strUserCode = ediUserCode.getText().toString();
                    strUserCode = strUserCode.concat(strBtnValue);
                }
                ediUserCode.setText(strUserCode);
                break;
            case 1: //barcode
                if(strBarcode.equals("") && strBarcode == null && ediBarcode.getText().equals("")) strBarcode = strBtnValue;
                    //else strBarcode = strBarcode.concat(strBtnValue);
                else {
                    strBarcode = ediBarcode.getText().toString();
                    strBarcode = strBarcode.concat(strBtnValue);
                }
                ediBarcode.setText(strBarcode);
                break;
        }

    }

    private void initSpinner(View view, List<String> storeList){

        spinStore = (Spinner) view.findViewById(R.id.spinStore);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item, storeList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinStore.setAdapter(dataAdapter);

        spinStore.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                String splitStore[] = selectedItem.split(" - ");
                strStore = splitStore[0];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    private void clearData(){
        switch (selectedMode){
            case 0: //UserCode
                strUserCode = "";
                ediUserCode.setText(strUserCode);
                break;
            case 1: //barcode
                strBarcode = "";
                ediBarcode.setText(strBarcode);
                break;
        }
    }

    private void clearBarcode(){
        strBarcode = "";
        ediBarcode.setText(strBarcode);
    }


    public void writeCSV() throws IOException {
        final String fileCreate;
        String line;
        viewResult.setText(Environment.getExternalStorageDirectory().toString());

        File folder = new File(Environment.getExternalStorageDirectory(), "CSV");
        if (!folder.exists()) {
            folder.mkdirs();
        }

       /* for (Map.Entry<String,String> entry : datamap.entrySet()){
            line = entry.getValue();
            line.concat("\r\n");
            Log.d("line", line.toString());
        }
        */


        //  fileCreate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "_"+ "PO" +".csv";
        //fileCreate = new SimpleDateFormat("ddMMyy").format(new Date()) + "_PO_"+ strFilename + ".TXT";
        fileCreate = strStore + "_" + strUserCode + "_" + new SimpleDateFormat("ddMMyy").format(new Date())  + ".TXT";
        docName = fileCreate;

        File csvfile = new File(folder, fileCreate);
        csvfile.createNewFile();
        FileOutputStream fOut = new FileOutputStream(csvfile);
        OutputStreamWriter sw =
                new OutputStreamWriter(fOut);

     /*   for (Map.Entry<String,String> entry : datamap.entrySet()){
            line = entry.getValue();
            Log.d("CSV2", line);
            sw.append(line);
                sw.append("\r\n");
    }*/
        for (String value : datalist){
            sw.append(value);
            sw.append("\r\n");
        }

        sw.close();
        fOut.close();

        datalist.removeAll(datalist);

        viewResult.setText("Create Success : " + Environment.getExternalStorageDirectory().toString() + "/" + fileCreate);
        uploadfileName = Environment.getExternalStorageDirectory().toString() + "/CSV/" + fileCreate;
    }

    public void insertRecord(){
        String line = "";

        strBarcode = ediBarcode.getText().toString();
        strQty = "1";
        strUserCode = ediUserCode.getText().toString();
        strFilename = strUserCode;

        if(strQty == null || strQty.equals("") || strQty.equals("0")){
            strQty = "0";
            ediQty.setText(strQty);
        }

        int iQty = Integer.parseInt(strQty);
        if (!isEmpty()) {
           /* if (qtymap.containsKey(strBarcode)){
                qtymap.put(strBarcode, qtymap.get(strBarcode) + iQty);
            }else {
                    qtymap.put(strBarcode, iQty);
            }*/

         //   qtymap.put(strBarcode, iQty);


            line = strBarcode.trim();
            Log.d("CSV1", line);
            //  datamap.put(strBarcode, line);
            datalist.add(line);
            viewResult.setText(line);

            totalQty += iQty;
            // totalItem = datamap.size();
            totalItem = datalist.size();

            clearBarcode();
            clearData();

            //   strQty = "1";
            strQty = "";
        }
        /*else{
            clearBarcode();
        }*/
    }

    public boolean isEmpty(){
        if(strUserCode == null || strUserCode.equals("")){
            viewResult.setText("โปรดระบุรหัสผู้ใช้งาน");
            return true;
        }
        if(strBarcode == null || strBarcode.equals("")){
            viewResult.setText("โปรดระบุบาร์โค้ท");
            return true;
        }
        if( strStore== null || strStore.equals("")){
            viewResult.setText("โปรดระบุที่อยู่คลังสินค้า");
            return true;
        }

        return false;
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
            /* // wait for fix
            String deletefile = "";
            deletefile = Environment.getExternalStorageDirectory().toString() + "/CSV/" + docName;

            File file = new File(deletefile);

            boolean delete = file.delete();
*/
            Toast.makeText(getActivity(), "Upload file Successfully", Toast.LENGTH_SHORT).show();
            clearAll();
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
            String boundary = "*****";

            String strSDPath = params[0];
            String strUrlServer = params[1];

            try {
                File file = new File(strSDPath);
                if (!file.exists()) {
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
                if (resCode == HttpURLConnection.HTTP_OK) {
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
                Log.d("resMessage=", resMessage.toString());

                fileInputStream.close();
                outputStream.flush();
                outputStream.close();

                resServer = resMessage.toString();


            } catch (Exception ex) {
                // Exception handling
                return null;
            }
            return null;
        }
    }


}
