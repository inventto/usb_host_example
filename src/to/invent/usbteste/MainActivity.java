package to.invent.usbteste;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	private UsbManager manager;
	private TextView devicesText;
	private Button getDevicesButton;
	private UsbDevice device;
	private final int MSP_VENDOR = 1105;
	private byte[] bytes;
	private int TIMEOUT = 0;
	private boolean forceClaim = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_main);

		devicesText = (TextView) findViewById(R.id.devicesText);
		getDevicesButton = (Button) findViewById(R.id.getDevicesButton);
		device = getDeviceByVendorId(MSP_VENDOR);
	}
	
	public void openConnection(){
		UsbInterface intf = device.getInterface(0);
		UsbEndpoint endpoint = intf.getEndpoint(0);
		UsbDeviceConnection connection = manager.openDevice(device); 
		connection.claimInterface(intf, forceClaim);
		connection.bulkTransfer(endpoint, bytes, bytes.length, TIMEOUT);
	}

	private HashMap<String, UsbDevice> devicesConnected() {
		manager = (UsbManager) getSystemService(Context.USB_SERVICE);
		return manager.getDeviceList();
	}

	/**
	 * Retorna um UsbDevice de acordo com o Vendor ID passado
	 * @param vendorId
	 * @return {@link UsbDevice}
	 */
	public UsbDevice getDeviceByVendorId(int vendorId) {
		HashMap<String, UsbDevice> devices = devicesConnected();
		Iterator iterator = devices.entrySet().iterator();
		try {
			while (iterator.hasNext()) {
				Map.Entry deviceMap = (Map.Entry) iterator.next();
				UsbDevice device = (UsbDevice) deviceMap.getValue();
//				Log.d("Device",
//						deviceMap.getKey() + "--" + device.getDeviceProtocol()
//								+ "======" + device.getDeviceId());
				iterator.remove();
				if (vendorId == device.getVendorId()) {
					return device;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void showLogs() {
		try {
			Process process = Runtime.getRuntime().exec("logcat -d");
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));

			StringBuilder log = new StringBuilder();
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				log.append(line + "\n");
			}
			TextView tv = (TextView) findViewById(R.id.logText);
			tv.setText("Log: \n" + log.toString());
		} catch (IOException e) {
		}
	}
}
