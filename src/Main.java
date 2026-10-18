import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;


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
		ArrayList<Integer> sortedByName = new ArrayList<>();		
		ArrayList<Integer> sortedByBus = new ArrayList<>();
		
		try {
			pci = idLookup.pci();
	        if (pciInfo.getCount() == 0) {
	        	System.out.println("\t No PCI devices have been found");
	        }
	        else {

	            // populate arrays with indecies that point to PCI devices
	            for (int i = 0; i < pciInfo.getCount(); i++) {
					sortedByName.add(i);
					sortedByBus.add(i);
	       		}
				
	       		// sort arrays
	       		sortedByName.sort((a, b) -> {return pciInfo.getVendorId(a) - pciInfo.getVendorId(b);});	       	
	       		sortedByBus.sort((a, b) -> {return pciInfo.getBus(a) - pciInfo.getBus(b);});


				// uncomment to print whichever sorted version you want. User input through CLI will decide this - luke
	       		//printPciDevices(sortedByName);	       	
	       		//printPciDevices(sortedByBus);

				// unsorted version stays printed for now
	       		printPciDevices();

			}
		} catch (Exception error) {
			System.out.println(error.getMessage());
			error.printStackTrace();
		}

		Scanner userInput = new Scanner(System.in);

		// Multiple menus can be defined. The main menu might call start() on another menu in response to the user selecting an option
		Menu mainMenu = new Menu(
			"Welcome to System Spec Analyser\n" + 
			"Options:\n" +
			"\t1. Processor\n" +
			"\t2. Memory\n" + 
			"\t3. USB Devices\n" +
			"\t4. PCI Devices\n" +
			"\t5. Quit\n\n", 5
		);

		mainMenu.setOption(4, Main::printPciDevices); // connects the number 4 option to actually running a function
		mainMenu.start(userInput);
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
}
