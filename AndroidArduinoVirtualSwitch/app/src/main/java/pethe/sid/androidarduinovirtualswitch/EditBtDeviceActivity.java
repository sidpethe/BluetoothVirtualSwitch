package pethe.sid.androidarduinovirtualswitch;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class EditBtDeviceActivity extends AppCompatActivity {
private static final String EXTRA_MESSAGE="pethe.sid.androidarduinovirtualswitch.MESSAGE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_bt_device);
        setTitle(R.string.activity_title_editbtdevice);
    }
    public void setBtDevice(View view){
        EditText editText=findViewById(R.id.editText);
        String newBtDeviceName=editText.getText().toString();

        Intent resultIntent=new Intent("com.example.RESULT_ACTION", Uri.parse("content://result_uri"));
        resultIntent.putExtra(EXTRA_MESSAGE,newBtDeviceName);
        setResult(Activity.RESULT_OK,resultIntent);
        finish();
    }
}
