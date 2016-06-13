package samples.exoguru.materialtabs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Edwin on 15/02/2015.
 */

import samples.exoguru.materialtabs.viewLines;

public class viewFile extends Fragment {

    String url = "http://203.151.92.125:888/uploadfile.php";
    String uploadfileName = "";

    TableLayout table;

    View views;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
    //    super.onCreate(savedInstanceState);
        final View view = inflater.inflate(R.layout.viewfile, container, false);
        views = view;
        File folder = new File(Environment.getExternalStorageDirectory(), "CSV");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        Button btnCheck = (Button) view.findViewById(R.id.btnCheck);
        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redrawEverything();
            }
        });
        drawTable();
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
     //   redrawEverything();
    }

    public void drawTable(){
        table = (TableLayout) views.findViewById(R.id.dataTable);
        File file[] = lockupCSV();
        createTableRow(file);
    }

    private void redrawEverything()
    {
        table.removeAllViews();
        drawTable();
    }

    public void createTableRow(File file[]){
        int id = 0;
        final File files[] = file;
         int i;

        for (i=0; i<file.length; i++){
            final int y=i;
            TableRow rows = new TableRow(getActivity());
            rows.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            TextView data = new TextView(getActivity());
            data.setText(file[i].getName().toString());

            Button btnSend = new Button(getActivity());
            btnSend.setId(id);
            btnSend.setText("ส่ง");
            btnSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    uploadfileName = Environment.getExternalStorageDirectory().toString() + "/CSV/" + files[y].getName();
                    final String strSDPath = uploadfileName;
                    final String strUrlServer = url;
                    Log.d("createTableRow", "FileName:" + files[y].getName());
                    new UploadFileAsync().execute(strSDPath, strUrlServer);
                    redrawEverything();

                }
            });

            Button btnDelete = new Button(getActivity());
            btnDelete.setId(id);
            btnDelete.setText("ลบ");
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    uploadfileName = Environment.getExternalStorageDirectory().toString() + "/CSV/" + files[y].getName();
                    File file = new File(uploadfileName);

                    boolean delete = file.delete();
                    redrawEverything();
                }
            });

            rows.addView(data);
            rows.addView(btnSend);
            rows.addView(btnDelete);
            id++;
            table.addView(rows);
        }
    }

    public File[] lockupCSV(){
        String path = Environment.getExternalStorageDirectory().toString()+"/CSV";
        Log.d("Files", "Path: " + path);
        File f = new File(path);
        File file[] = f.listFiles();
        Log.d("Files", "Size: " + file.length);
        for (int i=0; i < file.length; i++)
        {
            Log.d("lockupCSV", "FileName:" + file[i].getName());
        }
        return file;
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
            Toast.makeText(getActivity(), "Upload file Successfully", Toast.LENGTH_SHORT).show();
            //comment wait for fix
        //    File file = new File(uploadfileName);
        //    boolean delete = file.delete();
            redrawEverything();

        }
    }

    public class UploadFileAsync extends AsyncTask<String, Void, Void> {

        String resServer = "[Network Error.!!!!]";
        String uploadFile;

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

                resServer = resMessage.toString();


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