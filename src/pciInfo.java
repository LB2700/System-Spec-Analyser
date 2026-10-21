import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Comparator;

/**
 * The PCI devices that Linux has found, read from /sys/bus/pci/devices.
 *
 * Each device has a folder named like "0000:00:02.0", which means domain 0000,
 * bus 00, device 02, function 0. The devices are numbered 0, 1, 2 ... in that
 * order. The list is read once, the first time it is needed; call rescan() to
 * read it again.
 *
 * Vendor and device IDs are numbers (for example 0x8086 is Intel). The class code
 * is 24 bits: base class, sub class and interface. See idLookup to turn these
 * numbers into names.
 */
public class pciInfo
{
    private static final Path ROOT = sysFile.path("/sys/bus/pci/devices");
    private static List<Path> devices;

    private static List<Path> list()
    {
        if (devices == null) {
            List<Path> found = new ArrayList<>();
            if (Files.isDirectory(ROOT)) {
                try (DirectoryStream<Path> dir = Files.newDirectoryStream(ROOT)) {
                    for (Path p : dir)
                        found.add(p);
                } catch (IOException e) {
                    // leave the list as it is
                }
            }
            Collections.sort(found);
            devices = found;
        }
        return devices;
    }

    public static HashSet<Integer> getVendorIds() {
    	HashSet<Integer> allPciVendorIds = new HashSet<Integer>();
	
    	try {		
    		for (int i = 0; i < getCount(); i++) {
    			allPciVendorIds.add(getVendorId(i));
    		}			
    	} catch (Exception e) {
    		//System.out.println(e.getMessage());
    	}

    	return allPciVendorIds;
    }

    public static HashSet<Integer> getDeviceIds() {
    	HashSet<Integer> allPciDeviceIds = new HashSet<Integer>();
	
    	try {		
    		for (int i = 0; i < getCount(); i++) {
    			allPciDeviceIds.add(getDeviceId(i));
    		}			
    	} catch (Exception e) {
    		//System.out.println(e.getMessage());
    	}

    	return allPciDeviceIds;
    }

    /** Read the list of devices again (useful if hardware may have changed). */
    public static void rescan()
    {
        devices = null;
    }

    /** How many PCI devices there are. */
    public static int getCount()
    {
        return list().size();
    }

    /** The address as text, for example "0000:00:02.0". */
    public static String getAddress(int index)
    {
        return list().get(index).getFileName().toString();
    }

    /** The bus number, taken from the address. */
    public static int getBus(int index)
    {
        return Integer.parseInt(getAddress(index).substring(5, 7), 16);
    }

    /** The device number on the bus, taken from the address. */
    public static int getDevice(int index)
    {
        return Integer.parseInt(getAddress(index).substring(8, 10), 16);
    }

    /** The function number of the device, taken from the address. */
    public static int getFunction(int index)
    {
        return Integer.parseInt(getAddress(index).substring(11, 12), 16);
    }

    /** The vendor ID, for example 0x8086, or -1 if it cannot be read. */
    public static int getVendorId(int index)
    {
        return (int) sysFile.number(list().get(index).resolve("vendor"));
    }

    /** The device ID, or -1 if it cannot be read. */
    public static int getDeviceId(int index)
    {
        return (int) sysFile.number(list().get(index).resolve("device"));
    }

    /** The 24-bit class code, for example 0x060000 for a host bridge, or -1. */
    public static int getClassCode(int index)
    {
        return (int) sysFile.number(list().get(index).resolve("class"));
    }

    public static void listVendors(Scanner scanner) {
		// print list of vendors
		// prompt user to select one
		// print devices associate with vendor
		HashSet<Integer> vendorIds = getVendorIds();

		System.out.printf("Listing %d PCI Vendors\n\n", vendorIds.size());

		{ // keep i local to prevent name collision with later for loops
			int i = 0;
			for (Integer vendorId : vendorIds) {
				i++;
				System.out.printf("%d. %s (%04x)\n",i, Main.pci.vendorName(vendorId), vendorId);
			} 
		}


		// enforce a valid vendor id as input, or return if empty string is inputted
		Integer selectedVendorId;
	
		scanner.nextLine();
		while (true) {
			System.out.printf("\nEnter the vendor id to view its devices (Press enter to cancel): ", vendorIds.size());
			
			String res = scanner.nextLine();

			try {
				if (res.equals("")) return;
			
				selectedVendorId = Integer.parseInt(res,16);

				if (!vendorIds.contains(selectedVendorId)) throw new Exception();
				
			} catch (Exception error) {
				System.out.print("Please enter a valid vendor id");
				continue;
			}
			
			break;
		}

		System.out.print("\n");

		// print the list of devices associated with the chosen vendor
		int deviceCount = 0;
		for (int i = 0; i < getCount(); i++) {
			if (getVendorId(i) == selectedVendorId) {
				deviceCount++;
			}
		}
		
		System.out.printf("%d %s from %s (%04x) \n", 
			deviceCount, 
			(deviceCount == 1) ? "Device" : "Devices", // grammer is important
			Main.pci.vendorName(selectedVendorId), selectedVendorId);
		for (int count = 0, i = 0; i < getCount(); i++) {
			if (getVendorId(i) == selectedVendorId) {
				count++;
				System.out.printf("\t %d. ", count);
				printPciDevice(i);
			}
		}
		System.out.print("\n");	
    }

    public static void listDevices(Scanner scanner) {
    	System.out.printf("Sorting Criteria Options:\n\n");

		List<Comparator<Integer>> sortingCriteria = List.of(
		    (a, b) -> getVendorId(a) - getVendorId(b),
		    (a, b) -> getDeviceId(a) - getDeviceId(b),
		    (a, b) -> getBus(a) - getBus(b)
		);

    	System.out.print("\t 1. Vendor ID \n");
    	System.out.print("\t 2. Device ID \n");
    	System.out.print("\t 3. Bus \n");
    	System.out.print("\t 4. None \n");

		// enforce a valid vendor id as input, or return if empty string is inputted
		Integer selectedSortingCriteria;
	
		scanner.nextLine();
		while (true) {
			System.out.printf("\nEnter a criteria to sort by (Press enter to cancel): ");
			
			String res = scanner.nextLine();

			try {
				if (res.equals("")) return;
			
				selectedSortingCriteria = Integer.parseInt(res) - 1;

				if (selectedSortingCriteria < 0 || selectedSortingCriteria > sortingCriteria.size()) throw new Exception();
				
			} catch (Exception error) {
				System.out.printf("Please enter a valid index between 1 and %d", sortingCriteria.size());
				continue;
			}
			
			break;
		}

		System.out.print("\n");
	
		if (selectedSortingCriteria == sortingCriteria.size()) {
			printPciDevices();
		} else {
			listPciDevices(sortingCriteria.get(selectedSortingCriteria));
		}

    	
    }
    
    public static void listDevice(Scanner scanner) {

    	System.out.printf("PCI devices: %d \n\n", getCount());
		printPciDevices();
		
		System.out.print("\n");

		
   		Integer selectedIndex;

   		
   		scanner.nextLine();

   		while (true) {
   		
   			System.out.printf("Enter an index of a PCI to see a more detailed view [1-%d] (Press enter to cancel): ", getCount());

   			try {
   				String res = scanner.nextLine();

   				if (res.equals("")) return;

 				selectedIndex = Integer.parseInt(res) - 1;

   				if (selectedIndex >= 0 && selectedIndex < getCount()) break;

				throw new Exception();
   			} catch (Exception e) {
   				System.out.printf("Please enter a number from 1 to %d \n", getCount());
   			}
   		}

   		System.out.println(getAddress(selectedIndex));
    
    	// print devices 
    	// prompt user to select one
    	// print the chosen device
    }

    public static void printPciDevices() {
    	for (int i = 0; i < getCount(); i++) {
    		// print the index i as it iterates, followed by the correct amount of spaces to ensure every pci entry lines up in the terminal
    		System.out.printf("%d." + " ".repeat(Integer.toString(getCount()).length() - Integer.toString(i+1).length()), i+1);
          	printPciDevice(i);
   		}
    }

    public static void printPciDevices(ArrayList<Integer> indexArray) throws Exception {
    	System.out.println("PCI devices: " + getCount());
    	for (int i: indexArray) {
    		if (i >= getCount()) {
    			throw new Exception("indexArray contains indecies larger than the size of deviceInfo.");		
    		}
    		printPciDevice(i);
    	}
    }

    public static void printPciDevice(Integer i) {
    	int vendor = getVendorId(i);
    	int device = getDeviceId(i);
    	System.out.printf("  %s  %04x:%04x  %s %s%n",
    		pciInfo.getAddress(i),
    		vendor,
    		device,
    		Main.pci.vendorName(vendor),
    		Main.pci.deviceName(vendor, device)
    	);
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
