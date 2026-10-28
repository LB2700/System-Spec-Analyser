import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.io.FileNotFoundException;
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
		} catch (Exception error) {
			System.out.println(error.getMessage());
			error.printStackTrace();
		}
		//usbDevices();

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
		// showCpuTemperature();
	

		Scanner userInput = new Scanner(System.in);

		// Multiple menus can be defined. The main menu might call start() on another menu in response to the user selecting an option
		Menu mainMenu = new Menu(
			"Welcome to System Spec Analyser\n\n" + 
			"Options:\n" +
			"\t 1. Processor\n" +
			"\t 2. Memory\n" + 
			"\t 3. USB Devices\n" +
			"\t 4. PCI Devices\n" +
			"\t 5. Quit\n\n", 5
		);

		Menu pciMenu = new Menu(
			"Options:\n" +
			"\t 1. List PCI Vendors\n" + 
			"\t 2. List PCI Devices\n" + 
			"\t 3. PCI device detailed view\n" +
			"\t 4. Back\n\n", 4
		);

		Menu memoryMenu = new Menu(
			"Options:\n" +
			"\t 1. Total Memory\n" + 
			"\t 2. Total Used Memory\n" +
			"\t 3. Percentage of Memory Used\n" +
			"\t 4. Run cache performance investigation \n" +
			"\t 5. Back\n\n", 5
		);

		Menu cpuMenu = new Menu(
			"Options:\n" +
			"\t 1. Show Core info\n" + 
			"\t 2. Get Core load info\n" +
			//"\t 5. Cpu temperature\n" +
			"\t 3. Back\n\n", 3
		);

		Menu usbMenu = new Menu(
			"Options:\n" +
			"\t 1. Number of USBs\n" + 
			"\t 2. List USB devices by name\n" +
			"\t 3. List USB devices by bus\n" +
			"\t 4. Test USBs\n" +
			"\t 5. Back\n\n", 5
		);

		Menu testUSBMenu = new Menu(
			"Options:\n" + 
			"\t 1. No USB scenario\n" + 
			"\t 2. Unrecognised USB scenario" + 
			"\t 3. Back\n\n", 3
		);

		pciMenu.setOption(1, () -> {pciInfo.listVendors(userInput);});
		pciMenu.setOption(2, () -> {pciInfo.listDevices(userInput);});
		pciMenu.setOption(3, () -> {pciInfo.listDevice(userInput);});
		pciMenu.setOption(4, () -> {mainMenu.start(userInput);});
		pciMenu.setOnStart(() -> {
			System.out.printf("Total Unique Vendors: %d \n", pciInfo.getVendorIds().size());
			System.out.printf("Total Unique Devices: %d \n\n", pciInfo.getDeviceIds().size());	
		});

		memoryMenu.setOption(1, () -> {memInfo.PrintTotalMemory();});
		memoryMenu.setOption(2, () -> {memInfo.PrintTotalUsedMemory();});
		memoryMenu.setOption(3, () -> {memInfo.PrintPercentageOfMemoryUsed();});
		memoryMenu.setOption(4, () -> {
			try {
				memInfo.cachePerformanceAnalysis(userInput);
			} catch (FileNotFoundException e) {
				System.out.printf("Error finding output directory. Please ensure ./output/csvFiles already exists \n");
			}
		});
		memoryMenu.setOption(5, () -> {mainMenu.start(userInput);});

		cpuMenu.setOption(1, () -> {cpuInfo.Coreinfo();});
		cpuMenu.setOption(2, () -> {cpuInfo.showCpuUsage();});
		cpuMenu.setOption(3, () -> {mainMenu.start(userInput);});

		testUSBMenu.setOption(1, () -> {usbTests.printTestRoot(0);});
		testUSBMenu.setOption(2, () -> {usbTests.printTestRoot(1);});
		testUSBMenu.setOption(3, () -> {mainMenu.start(userInput);});

		usbMenu.setOption(1, () -> {usbInfo.listUSBs();});
		usbMenu.setOption(2, () -> {usbInfo.printSysRoot(2);});
		usbMenu.setOption(3, () -> {usbInfo.printSysRoot(1);});
		usbMenu.setOption(4, () -> {testUSBMenu.start(userInput);});
		usbMenu.setOption(5, () -> {mainMenu.start(userInput);});

		
		
		mainMenu.setOption(1, () -> {cpuMenu.start(userInput);});
		mainMenu.setOption(2, () -> {memoryMenu.start(userInput);}); 
		mainMenu.setOption(3, () -> {usbMenu.start(userInput);});
		mainMenu.setOption(4, () -> {pciMenu.start(userInput);}); 
		mainMenu.setOption(5, () -> {
			System.out.print("Thank you for using System Spec Analyser\n");
			System.exit(0);
		});

		mainMenu.start(userInput);

// 		while (true) {
// 			mainMenu.start(userInput);
// 
// 			System.out.print("\nPress enter to continue...\n");
// 			userInput.nextLine();
// 			userInput.nextLine();
// 			
// 			System.out.print(Menu.lineSeperator);			
// 		}
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
