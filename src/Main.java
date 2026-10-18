import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Comparator;

public class Main {

	public static idLookup usb;
	public static idLookup pci;


	public static void main(String[] args) {

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


		// TODO: put all this in its own method
		try {
			pci = idLookup.pci();
		} catch (Exception e) {
			System.out.println("Error finding PCI lookup table");
		}
		pciDevices();
		listPciDevices((a,b) -> {return pciInfo.getVendorId(a) - pciInfo.getVendorId(b);});
		listPciDevices((a,b) -> {return pci.vendorName(pciInfo.getVendorId(a)).compareTo(pci.vendorName(pciInfo.getVendorId(b)));});
		//sortedByName.sort((a, b) -> {return pciInfo.getVendorId(a) - pciInfo.getVendorId(b);});	       	
			       		//sortedByBus.sort((a, b) -> {return pciInfo.getBus(a) - pciInfo.getBus(b);});
						
		
	}

	public static void printPciDevices() {
		System.out.println("PCI devices: " + pciInfo.getCount());
    	for (int i = 0; i < pciInfo.getCount(); i++) {
          	printPciDevice(i);
   		}
	}

	public static void printPciDevices(ArrayList<Integer> indexArray) throws Exception {
		System.out.println("PCI devices: " + pciInfo.getCount());
		for (int i: indexArray) {
			if (i >= pciInfo.getCount()) {
				throw new Exception("indexArray contains indecies larger than the size of deviceInfo.");		
			}
			printPciDevice(i);
		}
	}

	public static void printPciDevice(Integer i) {
		int vendor = pciInfo.getVendorId(i);
		int device = pciInfo.getDeviceId(i);
		System.out.printf("  %s  %04x:%04x  %s %s%n",
			pciInfo.getAddress(i),
			vendor,
			device,
			pci.vendorName(vendor),
			pci.deviceName(vendor, device)
		);
	}

	public static void pciDevices() {

		// Create a set that contains a list of unique vendorIds and deviceIds that show up
		HashSet<Integer> allPciVendorIds = new HashSet<Integer>();
		HashSet<Integer> allPciDeviceIds = new HashSet<Integer>();
	
		try {		
			for (int i = 0; i < pciInfo.getCount(); i++) {
				allPciVendorIds.add(pciInfo.getVendorId(i));
				allPciDeviceIds.add(pciInfo.getDeviceId(i));
			}			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		// for (Integer deviceId : allPciDeviceIds) {
		// 	System.out.printf("%04x\n",deviceId);
		// }
		
		System.out.printf("Total Unique Vendors: %d \n", allPciVendorIds.size());
		System.out.printf("Total Unique Devices: %d \n\n", allPciDeviceIds.size());	

		// Group PCI devices by vendor
		for (Integer vendorId : allPciVendorIds) {

			int deviceCount = 0;
			for (int i = 0; i < pciInfo.getCount(); i++) {
				if (pciInfo.getVendorId(i) == vendorId) {
					deviceCount++;
				}
			}
		
			System.out.printf("%d %s from %s (%04x) \n", 
				deviceCount, 
				(deviceCount == 1) ? "Device" : "Devices",
				pci.vendorName(vendorId),vendorId);
			for (int count = 0, i = 0; i < pciInfo.getCount(); i++) {
				if (pciInfo.getVendorId(i) == vendorId) {
					count++;
					System.out.printf("\t %d. ", count);
					printPciDevice(i);
				}
			}
			System.out.print("\n");
		}
	}

	public static void listPciDevices(Comparator<Integer> sortingFunction) {
		ArrayList<Integer> sorted = new ArrayList<>();		

		try {
			
	        if (pciInfo.getCount() == 0) {
	        	System.out.println("\t No PCI devices have been found");
	        }
	        else {

	            // populate arrays with indecies that point to PCI devices
	            for (int i = 0; i < pciInfo.getCount(); i++) {
					sorted.add(i);
	       		}
				
	       		// sort arrays
	       		//sortedByName.sort((a, b) -> {return pciInfo.getVendorId(a) - pciInfo.getVendorId(b);});	       	
	       		//sortedByBus.sort((a, b) -> {return pciInfo.getBus(a) - pciInfo.getBus(b);});
				sorted.sort(sortingFunction);

				// unsorted version stays printed for now
	       		printPciDevices(sorted);
			}
		} catch (Exception error) {
			System.out.println(error.getMessage());
			error.printStackTrace();
		}
	}
			
}
