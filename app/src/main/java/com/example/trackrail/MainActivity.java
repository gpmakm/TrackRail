package com.example.trackrail;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class MainActivity extends AppCompatActivity {
AndroidNetworking an;
FloatingActionButton btn;
String temp;
TextView trainnum,trainame,st,prstname,postname,delay;
int train;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        postname=findViewById(R.id.nextst);
        prstname=findViewById(R.id.platform);
        trainame=findViewById(R.id.trainname);
        trainnum=findViewById(R.id.trainnum);
        delay=findViewById(R.id.delay);
        st=findViewById(R.id.status);
        AndroidNetworking.initialize(getApplicationContext());
        Toast.makeText(this, "GOT Context", Toast.LENGTH_SHORT).show();
        btn=findViewById(R.id.btn);
        SearchView srch;
         srch=findViewById(R.id.search);
         srch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
             @Override
             public boolean onQueryTextSubmit(String query) {
                 getTrain(query);temp=query;
                 return true;
             }

             @Override
             public boolean onQueryTextChange(String newText) {
                 return false;
             }
         });
btn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        getTrain(temp);
    }
});

    }
    private void getTrain(String train){
        AndroidNetworking.get("https://api.railradar.in/v1/trains/" +train+"/live")
                .addHeaders("Authorization","Bearer rg_3dfa2af566bb4cf29050501b932437b1")
                .build().getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        Log.d("RESP", String.valueOf(jsonObject));

                        try {
                            boolean resp = jsonObject.getBoolean("success");
                            if(resp){
                                JSONObject data=jsonObject.getJSONObject("data");
                                boolean isive=data.getBoolean("isLive");
                                if (isive){
                                    JSONObject details=data.optJSONObject("currentLocation");
                                    JSONObject prev=data.optJSONObject("previousHalt");
                                    JSONObject next=data.optJSONObject("nextHalt");
                                    String status=details != null ? details.optString("status", "") : "";
                                    String trainname=data.optString("trainName", "");
                                    String trainnnum=data.optString("trainNumber", "");
                                    String nstname=next != null ? next.optString("stationName", "N/A") : "N/A";
                                    String stname=prev != null ? prev.optString("stationName", "N/A") : "N/A";
                                    String delayMins=details != null ? details.optString("delayMinutes", "0") : "0";
                                    postname.setText(nstname);
                                    prstname.setText(stname);
                                    delay.setText(String.format("%sminutes", delayMins));
                                    trainnum.setText(trainnnum);
                                    st.setText(status);
                                    trainame.setText(trainname);


                                }
                            }
                        } catch (JSONException e) {
                            Log.e("JSON_ERR", "Error parsing JSON", e);
                            Toast.makeText(MainActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                        }

                        Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(ANError anError) {
                        Toast.makeText(MainActivity.this, "Err"+anError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}