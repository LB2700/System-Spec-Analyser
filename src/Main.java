import java.io.IOException;

public class Main {
	public static void main(String[] args) {

		idLookup usb;

		try {
			usb = idLookup.usb();

			System.out.println("USB devices: " + usbInfo.getCount());
			for (int i = 0; i < usbInfo.getCount(); i++) {
				int vendor = usbInfo.getVendorId(i);
				int product = usbInfo.getProductId(i);
				System.out.printf("  Bus %03d Device %03d: ID %04x:%04x  %s %s%n", usbInfo.getBusNumber(i),
						usbInfo.getDeviceNumber(i), vendor, product,
						usb.vendorName(vendor), usb.deviceName(vendor, product));
			}
		// } catch (IOException error) {
		// 	System.out.println(error.getMessage());
		// 	error.printStackTrace();
		} catch (Exception error) {
			System.out.println(error.getMessage());
			error.printStackTrace();
		}

	}
}
