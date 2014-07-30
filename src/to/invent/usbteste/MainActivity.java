package to.invent.usbteste;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import to.invent.usbteste.helpers.Helper;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private UsbManager manager;
	private TextView devicesText;
	private Button getDevicesButton;
	private UsbDevice device;
	private final int MSP_VENDOR = 1105;
	private byte[] bytes;
	private int TIMEOUT = 0;
	private boolean forceClaim = true;
	private String USB_TAG = "USB";
	private Intent intent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_main);

		devicesText = (TextView) findViewById(R.id.devicesText);
		getDevicesButton = (Button) findViewById(R.id.getDevicesButton);
		getDevicesButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				device = getDeviceByVendorId(MSP_VENDOR);
				UsbInterface interf0 = device.getInterface(0);
				UsbInterface interf1 = device.getInterface(1);
				Log.d(USB_TAG, String.valueOf(interf0.getEndpointCount()));
				String msg = "";
				if (device != null)
					msg += device.getDeviceId() + "\n";
				else
					Log.d(USB_TAG, "Device null");
				if (Helper.isNull(device.getInterface(0)))
					msg += "No Interfaces";
				else
					msg += device.getInterfaceCount();
				devicesText.setText(msg);
				mainLoop();
			}
		});
	}

	private HashMap<String, UsbDevice> devicesConnected() {
		manager = (UsbManager) getSystemService(Context.USB_SERVICE);
		return manager.getDeviceList();
	}

	/**
	 * Retorna um UsbDevice de acordo com o Vendor ID passado
	 * 
	 * @param vendorId
	 * @return {@link UsbDevice}
	 */
	public UsbDevice getDeviceByVendorId(int vendorId) {
		HashMap<String, UsbDevice> devices = devicesConnected();
		Iterator iterator = devices.entrySet().iterator();
		if (devices.isEmpty())
			Log.d(USB_TAG, "No devices");
		else
			Log.d(USB_TAG, "============" + devices.size());
		try {
			while (iterator.hasNext()) {
				Map.Entry deviceMap = (Map.Entry) iterator.next();
				UsbDevice device = (UsbDevice) deviceMap.getValue();
				Log.d(USB_TAG,
						deviceMap.getKey() + "--" + device.getDeviceProtocol()
								+ "======" + device.getDeviceId());
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

	public void mainLoop() {
		new Handler().post(new Runnable() {

			@Override
			public void run() {
				String out = "";
				UsbDeviceConnection connection = manager.openDevice(device);
				int count = device.getInterfaceCount();
				Toast.makeText(MainActivity.this,
						"tamanho interface: " + count, Toast.LENGTH_LONG)
						.show();

				UsbInterface iface = null;
				iface = device.getInterface(0);

				UsbRequest request = new UsbRequest();

				UsbEndpoint epIN = null;
				UsbEndpoint epOUT = null;

				for (int i = 0; i < iface.getEndpointCount(); i++) {
					if (iface.getEndpoint(i).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
						if (iface.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_IN) {
							// str = str +"Bulk and making epIN\n";
							epIN = iface.getEndpoint(i);
						} else {
							// str = str+ "Bulk and making epOUT\n";
							epOUT = iface.getEndpoint(i);
						}
					} else {

					}
				}

				while (true) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					int bufferMaxLength = epIN.getMaxPacketSize();
					ByteBuffer buffer = ByteBuffer.allocate(bufferMaxLength);
					request.initialize(connection, epIN);
					if (request.queue(buffer, bufferMaxLength)) {
						if (connection.requestWait() == request) {
							out += buffer.asCharBuffer();
							try {
								String result = new String(buffer.array(),
										"UTF-8");
								Log.d(USB_TAG, result);
								Log.d(USB_TAG, out);
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		});
	}
}
