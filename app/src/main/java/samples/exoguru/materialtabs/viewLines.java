package samples.exoguru.materialtabs;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by jelim_000 on 9/11/2558.
 */
public class viewLines extends Fragment {

    HashMap<String,Integer> qtymap;
    HashMap<String,String> datamap;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.update, container, false);
        TableLayout dataTable = (TableLayout) view.findViewById(R.id.dataTable);
        Bundle bundle = this.getArguments();

        datamap = (HashMap<String, String>) bundle.getSerializable("datamap");
        for (Map.Entry<String, String> entry : datamap.entrySet()) {
            TableRow rows = new TableRow(getActivity());
            rows.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

            TextView data = new TextView(getActivity());
            data.setTextSize(18);
            data.setText(entry.getValue().toString());

            rows.addView(data);
            dataTable.addView(rows);
        }

        return view;
    }


}
