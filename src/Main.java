import java.io.IOException;

public class Main {
	public static void main(String[] args) {

		idLookup usb;

		try {
			usb = idLookup.usb();
			System.out.println("USB devices: " + usbInfo.getCount());
			if (usbInfo.getCount() == 0) {
			    System.out.println("  No USB devices have been found");
			}
			else {
			    for (int i = 0; i < usbInfo.getCount(); i++) {
			    int vendor = usbInfo.getVendorId(i);
			    int product = usbInfo.getProductId(i);
			    System.out.printf("  Bus %03d Device %03d: ID %04x:%04x  %s %s%n", usbInfo.getBusNumber(i),
			                      usbInfo.getDeviceNumber(i), vendor, product,
			                      usb.vendorName(vendor), usb.deviceName(vendor, product));
			    }
			}
		// } catch (IOException error) {
		// 	System.out.println(error.getMessage());
		// 	error.printStackTrace();
		} catch (Exception error) {
			System.out.println(error.getMessage());
			error.printStackTrace();
		}

		idLookup pci;
		try {
			pci = idLookup.pci();
	        System.out.println("PCI devices: " + pciInfo.getCount());
	        if (pciInfo.getCount() == 0) {
	        	System.out.println("\t No PCI devices have been found");
	        }
	        else {
	            for (int i = 0; i < pciInfo.getCount(); i++) {
	            	int vendor = pciInfo.getVendorId(i);
	            	int device = pciInfo.getDeviceId(i);
	            	System.out.printf("  %s  %04x:%04x  %s %s%n", pciInfo.getAddress(i), vendor, device,
					pci.vendorName(vendor), pci.deviceName(vendor, device));
	       		}
			}
		} catch (Exception error) {
			System.out.println(error.getMessage());
			error.printStackTrace();
		}
	}
}
