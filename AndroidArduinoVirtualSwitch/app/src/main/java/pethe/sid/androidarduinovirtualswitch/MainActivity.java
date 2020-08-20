package pethe.sid.androidarduinovirtualswitch;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Set;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = RESULT_FIRST_USER;
    private static final int RESULT_BT_NAME = 3;
    private static final String EXTRA_MESSAGE="pethe.sid.androidarduinovirtualswitch.MESSAGE";
    private BluetoothSocket sock;
    private BluetoothDevice defaultBlue;
    private BluetoothAdapter bluedaat = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ConnectedThread connectedThread = new ConnectedThread(sock);
        connectedThread.cancel();
        Toast.makeText(this, "Connection Terminated", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!bluedaat.isEnabled()) {
            btEnableRequestFunc();
        }
        else
            btEnabled();
    }

    protected void btEnableRequestFunc()
    {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK)
                btEnabled();
            else {
                Toast.makeText(this, "Bluetooth Enable Request Denied; Retrying...",
                        Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode==RESULT_BT_NAME)
            SetNewBtDevice(data.getStringExtra(EXTRA_MESSAGE));
        super.onActivityResult(requestCode, resultCode, data);
    }
    private void SetNewBtDevice(String newDeviceName){
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.defaultBt), newDeviceName);
        editor.apply();

        btEnabled();
    }
    protected void btEnabled() {
        final Set<BluetoothDevice> pairedDevices = bluedaat.getBondedDevices();
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String defaultBtName=sharedPref.getString(getString(R.string.defaultBt),getString(R.string.defaultBt));
        // If there are paired devices
        if ((pairedDevices.size()) > 0) {
            // Loop through paired devices
            for (BluetoothDevice HC5 : pairedDevices) {
                if(HC5.getName().equals(defaultBtName)) {
                    defaultBlue = HC5;
                }
            }
            if(defaultBlue==null){
                Toast.makeText(this,defaultBtName+getString(R.string.BtDeviceNotPaired),Toast.LENGTH_LONG).show();
            }
            else ConnectCall(defaultBlue);
        }
        CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int id = buttonView.getId();
                Send(id, isChecked);
            }
        };
        ToggleButton tb = findViewById(R.id.toggleButton);
        ToggleButton tb3 = findViewById(R.id.toggleButton3);
        ToggleButton tb2 = findViewById(R.id.toggleButton2);
        ToggleButton tb1 = findViewById(R.id.toggleButton4);
        tb.setOnCheckedChangeListener(onCheckedChangeListener);
        tb1.setOnCheckedChangeListener(onCheckedChangeListener);
        tb2.setOnCheckedChangeListener(onCheckedChangeListener);
        tb3.setOnCheckedChangeListener(onCheckedChangeListener);

        ImageButton imb=findViewById(R.id.imageButton);
        imb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentEditBtDeviceActivity=new Intent(MainActivity.this,EditBtDeviceActivity.class);
                startActivityForResult(intentEditBtDeviceActivity,RESULT_BT_NAME);
            }
        });
    }

    private void ConnectCall(BluetoothDevice HC5){
        ConnectThread connection = new ConnectThread(HC5);
        connection.run();
        sock = connection.getSocket();
        if (sock.isConnected()) {
            Toast.makeText(this,"Connected",Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(this,"Unable to Connect",Toast.LENGTH_LONG).show();
        }
    }
    public void Send(int id, boolean on) {
        ConnectedThread blue = new ConnectedThread(sock);
        switch (id) {
            case (R.id.toggleButton2):
                if (on) {
                    blue.write('A');
                } else {
                    blue.write('a');
                }
                break;
            case (R.id.toggleButton3):
                if (on) {
                    blue.write('B');
                } else {
                    blue.write('b');
                }
                break;
            case (R.id.toggleButton):
                if (on) {
                    blue.write('C');
                } else {
                    blue.write('c');
                }
                break;
            case (R.id.toggleButton4):
                if (on) {
                    blue.write('D');
                } else {
                    blue.write('d');
                }
                break;
        }
    }
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        BluetoothAdapter bluedaat = BluetoothAdapter.getDefaultAdapter();
        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;
            UUID MY_UUID =UUID.fromString("Android Arduino Virtual Switch");

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            bluedaat.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                }

            }

        }

        public BluetoothSocket getSocket() {
            return (mmSocket);
        }

    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    byte MESSAGE_READ = 0;
                    Handler mHandler = new Handler();
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();

                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(char key) {
            try {
                mmOutStream.write(key);
            } catch (IOException e) {

            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();

            } catch (IOException e) {
            }
        }
    }
}
