import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Comparator;
import java.util.Scanner;

public class Main {

	public static idLookup usb;
	public static idLookup pci;

	public static void main(String[] args) {

		try {
			usb = idLookup.usb();
		} catch (Exception e) {
			System.out.println("Error finding USB lookup table");
		}
		// usbDevices();


		try {
			pci = idLookup.pci();
		} catch (Exception e) {
			System.out.println("Error finding PCI lookup table");
		}
		// pciDevices();
		// listPciDevices((a,b) -> {return pciInfo.getVendorId(a) - pciInfo.getVendorId(b);});
		// listPciDevices((a,b) -> {return pci.vendorName(pciInfo.getVendorId(a)).compareTo(pci.vendorName(pciInfo.getVendorId(b)));});
						    
		// showCoreCount();
		// showCpuArch();
		// showCpuModelName();
		// showCpuUsage();
	

		Scanner userInput = new Scanner(System.in);

		// Multiple menus can be defined. The main menu might call start() on another menu in response to the user selecting an option
		Menu mainMenu = new Menu(
			"Welcome to System Spec Analyser\n\n" + 
			"Options:\n" +
			"\t1. Processor\n" +
			"\t2. Memory\n" + 
			"\t3. USB Devices\n" +
			"\t4. PCI Devices\n" +
			"\t5. Quit\n\n", 5
		);

		Menu pciMenu = new Menu(
			"Options:\n" +
			"\t 1. List all PCI Devices\n" + 
			"\t 2. Sort and List all PCI Devices\n" +
			"\t 3. PCI device detailed view\n" +
			"\t 4. Back\n\n", 4
		);

		Menu memoryMenu = new Menu(
			"Options:\n" +
			"\t 1. Total Memory\n" + 
			"\t 2. Total Used Memory\n" +
			"\t 3. Percentage of Memory Used\n" +
			"\t 4. Back\n\n", 4
		);

		pciMenu.setOption(4, () -> {mainMenu.start(userInput);});

		memoryMenu.setOption(1, () -> {memInfo.PrintTotalMemory();});
		memoryMenu.setOption(2, () -> {memInfo.PrintTotalUsedMemory();});
		memoryMenu.setOption(3, () -> {memInfo.PrintPercentageOfMemoryUsed();});
		memoryMenu.setOption(4, () -> {mainMenu.start(userInput);});
		
		mainMenu.setOption(2, () -> {memoryMenu.start(userInput);}); // connects the number 2 option to actually running a function
		mainMenu.setOption(4, () -> {pciMenu.start(userInput);}); // connects the number 4 option to actually running a function
		mainMenu.start(userInput);
	}
	//Cpu Requirement 1
	public static long[] getCpuTimes(String coreInfo){
		//Gets rid of empty space and splits into array
		String[] arrayOfStrings = coreInfo.trim().split("\\s+");
	
		long idle = 0;
		long busy = 0;

		//e.g cpu 79242 0 74306 842486413 756859 6140 67701 0 ->  index 4 in array is idle (842486413)
		final int IDLE_TIME_INDEX = 4;
		
		//turn each value into long and add the values together, exclude "cpu" -> i = 1 and exclude "idle" to find busy count
		for (int i = 1; i < arrayOfStrings.length; i++) {
			if (i != IDLE_TIME_INDEX){
    	    	busy += Long.parseLong(arrayOfStrings[i]);
			} else { 
				idle = Long.parseLong(arrayOfStrings[IDLE_TIME_INDEX]);
			}
		}
		long[] busyidle = {busy, idle};

		return busyidle;
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

	public static void usbDevices() {
		try {			
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
		} catch (Exception error) {
			System.out.println(error.getMessage());
			error.printStackTrace();
		}
	}

	public static void showCoreCount() {
		System.out.println("Processors (cores) avaiable: " + Runtime.getRuntime().availableProcessors());
		
		try {
			for (String i : Files.readAllLines(Path.of("/proc/cpuinfo"))) {
				if (i.startsWith("cpu cores")){
					System.out.println("There are: " + i + " processers (cores)");
					break;
				}
			}
		} catch (IOException error) {
			System.out.println(error.getMessage());
		}
	}

	public static void showCpuArch() {
		//Gets archeitecture, not what the requirements wants, even then it's something cool to add
		System.out.println("You have: " + System.getProperty("os.arch"));
	}

	public static void showCpuModelName() {
		try {
			for (String i : Files.readAllLines(Path.of("/proc/cpuinfo"))) {
				if (i.startsWith("model name")) {
					//e.g Model Name : QEMU Virtual CPU version 2.5+ -> splits at colon ":", gets rid of extra space
					//[1] selects part -> QEMU Virtual CPU version 2.5+ as index starts at 0
					System.out.println("Model Name: " + i.split(":")[1].trim());
					break;
				}
			}
		} catch (java.io.IOException error) {
			System.out.println(error.getMessage());
		}
	}

	public static void showCpuUsage() {
		//https://support.checkpoint.com/results/sk/sk65143 - please read
		
		ArrayList<Double> idleR1 = new ArrayList<Double>();
		ArrayList<Double> busyR1 = new ArrayList<Double>();
		ArrayList<Double> idleR2 = new ArrayList<Double>();
		ArrayList<Double> busyR2 = new ArrayList<Double>();

		ArrayList<Double> busyDifference = new ArrayList<Double>();
		ArrayList<Double> idleDifference = new ArrayList<Double>();
		ArrayList<Double> loadRatio = new ArrayList<Double>();

		try {
			int numOfCores = Runtime.getRuntime().availableProcessors();

			List<String> coreInfoLines = Files.readAllLines(Path.of("/proc/stat"));
			for (int i = 0; i < (numOfCores + 1); i++){

				String coreInfo = coreInfoLines.get(i);
				getLoad(idleR1,busyR1, coreInfo);
			}
			
			Thread.sleep(1000);
	
			coreInfoLines = Files.readAllLines(Path.of("/proc/stat"));
			for (int i = 0; i < (numOfCores + 1); i++){

				String coreInfo = coreInfoLines.get(i);
				getLoad(idleR2,busyR2, coreInfo);
			}

			for (int i = 0; i < idleR1.size(); i++) {
			
				busyDifference.add(busyR2.get(i) - busyR1.get(i));
				idleDifference.add(idleR2.get(i) - idleR1.get(i));

				double load = 100.0 * busyDifference.get(i) / (busyDifference.get(i) + idleDifference.get(i));
				loadRatio.add(load);
			}

			double totalLoad = 0;
		
			for (int i = 1; i < loadRatio.size(); i++) {
				System.out.println("Cpu: " + i + "has a load of " + loadRatio.get(i) + "%");
				totalLoad += loadRatio.get(i);
			}

			double averageLoad = totalLoad / numOfCores;
			System.out.println("Average Cpu load: " + averageLoad + "%");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public static void getLoad(ArrayList<Double> idleR, ArrayList<Double> busyR, String coreInfo) {
		long[] busyidle = getCpuTimes(coreInfo);
		busyR.add((double)busyidle[0]);
		idleR.add((double)busyidle[1]);
	}
}
