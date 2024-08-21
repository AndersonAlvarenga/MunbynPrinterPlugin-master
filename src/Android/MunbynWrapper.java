package com.bgraefnitz.wrapper;

import android.content.Context;
import android.os.Handler;

import zj.com.cn.bluetooth.sdk.BluetoothService;
import zj.com.command.sdk.PrinterCommand;
// Cordova-required packages
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import android.Manifest;
import android.os.Build;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import java.util.Hashtable;




public class MunbynWrapper extends CordovaPlugin {
    private static final String LOG_TAG = "BluetoothPrinter";
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mmDevice;
      private final int REQUEST_BT_ENABLE = 59627; /*Random integer*/
      private final int REQUEST_ACCESS_FINE_LOCATION = 59628;
      private final int REQUEST_LOCATION_SOURCE_SETTINGS = 59629;
      private final int REQUEST_BLUETOOTH_SCAN = 59630;
      private final int REQUEST_BLUETOOTH_ADVERTISE = 59631;
      private final int REQUEST_BLUETOOTH_CONNECT = 59632;
      private BluetoothAdapter bluetoothAdapter;
      private boolean isReceiverRegistered = false;
      private boolean isBondReceiverRegistered = false;

    @Override
    public boolean execute(String action, JSONArray args,
    final CallbackContext callbackContext) throws JSONException {
        if (action.equals("write")) {
            Log.e(LOG_TAG, "Entrou 1");
            String name = args.getString(0);
            Log.e(LOG_TAG, "Entrou 2");
            String message = args.getString(1);
            Log.e(LOG_TAG, "Entrou 3");
            if (findBT(callbackContext, name)) {
                Log.e(LOG_TAG, "Entrou 4");
                try {
                    Log.e(LOG_TAG, "Entrou 5");
                    Context context = this.cordova.getActivity().getApplicationContext();
                    BluetoothService btService = new BluetoothService(context, new Handler());
                    btService.start();
                    try {
                        Log.e(LOG_TAG, "Entrou 6");
                        Log.e(LOG_TAG, mmDevice.getAddress().toString());
                        btService.connect(mmDevice);
                        long t= System.currentTimeMillis();
                        long end = t+5000;
                        Log.e(LOG_TAG, "Entrou 7");
                        



                        Log.e(LOG_TAG, "Entrou 9");
                        if(btService.mConnectedThread == null)
                        {
                            Log.e(LOG_TAG, "Entrou 10");
                            callbackContext.error("Failed to connect to Bluetooth device.");
                            throw new JSONException("Failed to connect to Bluetooth device.");
                        }
                        message = message + "\n";
                        byte[] sendCommand = PrinterCommand.POS_Print_Text(message, "GBK", 0, 0, 0, 0);
                        btService.write(sendCommand);
                        btService.stop();
                        Log.e(LOG_TAG, "Entrou 11");
                        callbackContext.success("written");
                        return true;
                    } catch (Exception e) {
                        Log.e(LOG_TAG, e.getMessage());
                        callbackContext.error("Bluetooth connection error");
                    }
                    callbackContext.success("connected");

                } catch (Exception e) {
                    Log.e(LOG_TAG, e.getMessage());
                    e.printStackTrace();
                }
            } else {
                callbackContext.error("Bluetooth Device Not Found: " + name);
            }
            return true;
        }
        else if (action.equals("list")) {
            listBT(callbackContext);
            return true;
        }else 
        if(action.equals("seachPermissionConnect")){
            Log.e(LOG_TAG, "Entrou");
            JSONArray json = new JSONArray();
            Hashtable map = new Hashtable();
            try{
                if(
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.S || 
                    cordova.hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
                ){
                    Log.e(LOG_TAG, "Permissao True");
                    map.put("PermissaoConnect", true);
                    
                }else{
                    map.put("PermissaoConnect", false);
                    Log.e(LOG_TAG, "Permissao False");
                }
                JSONObject jObj = new JSONObject(map);
                callbackContext.success(jObj);
            }catch(Exception e){
                map.put("PermissaoConnect", false);
                map.put("Erro", e.getMessage());
                Log.e(LOG_TAG, "Erro"+e.getMessage());
                JSONObject jObj = new JSONObject(map);
                callbackContext.error(jObj);
            }
            return true;

        }else if(action.equals("solicitaPermissaoConnect")){
            try{
                solicitaPermissaoConnect();
                callbackContext.success("Solicitado Permissoes");
            }catch (Exception e){
                callbackContext.error("Erro Permissao "+e.getMessage());
            }

            return true;
        }else
            if(action.equals("solicitaPermissaoScan")){
            try{
                solicitaPermissaoScan();
                callbackContext.success("Solicitado Permissoes");
            }catch (Exception e){
                callbackContext.error("Erro Permissao "+e.getMessage());
            }

            return true;
        }
      return true;
    }
    
    void solicitaPermissaoConnect(){
            cordova.requestPermission(this, REQUEST_BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_CONNECT);

    }
    void solicitarPermissaoScan(){
           
           cordova.requestPermission(this, REQUEST_BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_SCAN);

    }
    //list all bluetooth devices (return list of names)
    void listBT(CallbackContext callbackContext) {
        BluetoothAdapter mBluetoothAdapter = null;
        String errMsg = null;
        try {



            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
                errMsg = "No bluetooth adapter available";
                Log.e(LOG_TAG, errMsg);
                callbackContext.error(errMsg);
                return;
            }
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                solicitaPermissaoConnect();
                this.cordova.getActivity().startActivityForResult(enableBluetooth, 0);
            }
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                JSONArray json = new JSONArray();
                for (BluetoothDevice device : pairedDevices) {
                    /*
                    Hashtable map = new Hashtable();
                    map.put("type", device.getType());
                    map.put("address", device.getAddress());
                    map.put("name", device.getName());
                    JSONObject jObj = new JSONObject(map);
                    */
                    json.put(device.getName());
                }
                callbackContext.success(json);
            } else {
                callbackContext.error("No Bluetooth Device Found");
            }
            //Log.d(LOG_TAG, "Bluetooth Device Found: " + mmDevice.getName());
        } catch (Exception e) {
            errMsg = e.getMessage();
            Log.e(LOG_TAG, errMsg);
            e.printStackTrace();
            callbackContext.error(errMsg);
        }
    }

    // This will find a bluetooth printer device
    boolean findBT(CallbackContext callbackContext, String name) {
        try {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
                Log.e(LOG_TAG, "No bluetooth adapter available");
            }
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                this.cordova.getActivity().startActivityForResult(enableBluetooth, 0);
            }
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    String btName = device.getName();
                    if (device.getName().equalsIgnoreCase(name)) {
                        mmDevice = device;
                        return true;
                    }
                }
            }
            Log.d(LOG_TAG, "Bluetooth Device Found: " + mmDevice.getName());
        } catch (Exception e) {
            String errMsg = e.getMessage();
            Log.e(LOG_TAG, errMsg);
            e.printStackTrace();
            callbackContext.error(errMsg);
        }
        return false;
    }
}