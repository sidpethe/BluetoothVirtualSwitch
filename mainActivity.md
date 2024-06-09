# MainActivity.java Documentation 📝

Welcome to the documentation for the `MainActivity.java` file! This file is the main activity for an Android application that establishes a Bluetooth connection to an Arduino device to control it via virtual switches.

## Table of Contents 📑
1. [Overview](#overview)
2. [Imports](#imports)
3. [Class Variables](#class-variables)
4. [Lifecycle Methods](#lifecycle-methods)
   - [onDestroy](#ondestroy)
   - [onCreate](#oncreate)
5. [Bluetooth Methods](#bluetooth-methods)
   - [btEnableRequestFunc](#btenablerequestfunc)
   - [onActivityResult](#onactivityresult)
   - [SetNewBtDevice](#setnewbtdevice)
   - [btEnabled](#btenabled)
   - [ConnectCall](#connectcall)
6. [Send Method](#send-method)
7. [Inner Classes](#inner-classes)
   - [ConnectThread](#connectthread)
   - [ConnectedThread](#connectedthread)

## Overview 🧐
`MainActivity.java` handles Bluetooth communication between an Android device and an Arduino. The app allows users to control the Arduino via toggle buttons, sending specific commands over Bluetooth.

## Imports 📥
The file imports various Android and Java libraries essential for Bluetooth communication, UI handling, and data persistence.

```java
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
```

## Class Variables 📋
The following variables are defined in the class:

| Variable Name       | Type                | Description                                           |
|---------------------|---------------------|-------------------------------------------------------|
| `REQUEST_ENABLE_BT` | `int`               | Request code for enabling Bluetooth.                  |
| `RESULT_BT_NAME`    | `int`               | Result code for Bluetooth device selection.           |
| `EXTRA_MESSAGE`     | `String`            | Constant for intent extra message.                    |
| `sock`              | `BluetoothSocket`   | Bluetooth socket for communication.                   |
| `defaultBlue`       | `BluetoothDevice`   | Default Bluetooth device.                             |
| `bluedaat`          | `BluetoothAdapter`  | Bluetooth adapter of the device.                      |

## Lifecycle Methods 📅

### onDestroy
Handles cleanup when the activity is destroyed.

```java
@Override
protected void onDestroy() {
    super.onDestroy();
    ConnectedThread connectedThread = new ConnectedThread(sock);
    connectedThread.cancel();
    Toast.makeText(this, "Connection Terminated", Toast.LENGTH_LONG).show();
}
```

### onCreate
Initializes the activity, sets up the UI, and handles Bluetooth enabling.

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    if (!bluedaat.isEnabled()) {
        btEnableRequestFunc();
    } else {
        btEnabled();
    }
}
```

## Bluetooth Methods 📶

### btEnableRequestFunc
Requests the user to enable Bluetooth.

```java
protected void btEnableRequestFunc() {
    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
}
```

### onActivityResult
Handles the result of the Bluetooth enable request and device selection.

```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if(requestCode == REQUEST_ENABLE_BT) {
        if (resultCode == RESULT_OK) {
            btEnabled();
        } else {
            Toast.makeText(this, "Bluetooth Enable Request Denied; Retrying...", Toast.LENGTH_SHORT).show();
        }
    }
    if(requestCode == RESULT_BT_NAME) {
        SetNewBtDevice(data.getStringExtra(EXTRA_MESSAGE));
    }
    super.onActivityResult(requestCode, resultCode, data);
}
```

### SetNewBtDevice
Sets a new default Bluetooth device.

```java
private void SetNewBtDevice(String newDeviceName) {
    SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPref.edit();
    editor.putString(getString(R.string.defaultBt), newDeviceName);
    editor.apply();
    btEnabled();
}
```

### btEnabled
Handles Bluetooth enabling, device pairing, and sets up UI listeners.

```java
protected void btEnabled() {
    final Set<BluetoothDevice> pairedDevices = bluedaat.getBondedDevices();
    SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
    String defaultBtName = sharedPref.getString(getString(R.string.defaultBt), getString(R.string.defaultBt));
    
    if ((pairedDevices.size()) > 0) {
        for (BluetoothDevice HC5 : pairedDevices) {
            if(HC5.getName().equals(defaultBtName)) {
                defaultBlue = HC5;
            }
        }
        if(defaultBlue == null) {
            Toast.makeText(this, defaultBtName + getString(R.string.BtDeviceNotPaired), Toast.LENGTH_LONG).show();
        } else {
            ConnectCall(defaultBlue);
        }
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

    ImageButton imb = findViewById(R.id.imageButton);
    imb.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intentEditBtDeviceActivity = new Intent(MainActivity.this, EditBtDeviceActivity.class);
            startActivityForResult(intentEditBtDeviceActivity, RESULT_BT_NAME);
        }
    });
}
```

### ConnectCall
Attempts to connect to the specified Bluetooth device.

```java
private void ConnectCall(BluetoothDevice HC5) {
    ConnectThread connection = new ConnectThread(HC5);
    connection.run();
    sock = connection.getSocket();
    if (sock.isConnected()) {
        Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show();
    } else {
        Toast.makeText(this, "Unable to Connect", Toast.LENGTH_LONG).show();
    }
}
```

## Send Method 📤
Sends data to the Arduino based on the toggle button activity.

```java
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
```

## Inner Classes 📦

### ConnectThread
Handles the Bluetooth connection process.

```java
private class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    BluetoothAdapter bluedaat = BluetoothAdapter.getDefaultAdapter();

    public ConnectThread(BluetoothDevice device) {
        BluetoothSocket tmp = null;
        mmDevice = device;
        UUID MY_UUID = UUID.fromString("Android Arduino Virtual Switch");
        try {
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
        }
        mmSocket = tmp;
    }

    public void run() {
        bluedaat.cancelDiscovery();
        try {
            mmSocket.connect();
        } catch (IOException connectException) {
            try {
                mmSocket.close();
            } catch (IOException closeException) {
            }
        }
    }

    public BluetoothSocket getSocket() {
        return mmSocket;
    }
}
```

### ConnectedThread
Manages the Bluetooth connection once established, handling data transmission.

```java
private class ConnectedThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;

    public ConnectedThread(BluetoothSocket socket) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
        }
        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        byte[] buffer = new byte[1024];
        int bytes;
        while (true) {
            try {
                bytes = mmInStream.read(buffer);
                byte MESSAGE_READ = 0;
                Handler mHandler = new Handler();
                mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
            } catch (IOException e) {
                break;
            }
        }
    }

    public void write(char key) {
        try {
            mmOutStream.write(key);
        } catch (IOException e) {
        }
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
        }
    }
}
```

## Conclusion 🏁
This concludes the detailed documentation of `MainActivity.java`. This file serves as the main controller for establishing and managing Bluetooth connections, sending commands to an Arduino device, and handling user interactions through toggle buttons. The provided inner classes `ConnectThread` and `ConnectedThread` handle the underlying Bluetooth communication.

Feel free to reach out for further clarification or questions! 😊
