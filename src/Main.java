import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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

	System.out.println("Processors (cores) avaiable: " + Runtime.getRuntime().availableProcessors());
		
	try {
	for (String i : Files.readAllLines(Path.of("/proc/cpuinfo"))) {
	if (i.startsWith("cpu cores")){
	System.out.println("There are: " + i + " processers (cores)");
	break;
	}
	}
	}catch (java.io.IOException error) {
	System.out.println(error.getMessage());
	}
		
	//Gets archeitecture, not what the requirements wants, even then it's something cool to add
	System.out.println("You have: " + System.getProperty("os.arch"));
		
	//Needs to be tested 
	try {
	for (String i : Files.readAllLines(Path.of("/proc/cpuinfo"))) {
	if (i.startsWith("model name")){
	System.out.println(i);
	break;
	}
	}
	}catch (java.io.IOException error) {
	System.out.println(error.getMessage());
	}
	
	//https://support.checkpoint.com/results/sk/sk65143
	int numOfCores = Runtime.getRuntime().availableProcessors();
	
	ArrayList<Double> coreLoads = new ArrayList<Double>();
	ArrayList<Double> idleR1 = new ArrayList<Double>();
	ArrayList<Double> busyR1 = new ArrayList<Double>();
	ArrayList<Double> idleR2 = new ArrayList<Double>();
	ArrayList<Double> busyR2 = new ArrayList<Double>();

	try {
		List<String> coreInfoLines = Files.readAllLines(Path.of("/proc/stat"));
		for (int i = 0; i < (numOfCores + 1); i++){
			
			String coreInfo = coreInfoLines.get(i);
			System.out.println(coreInfo);
			
			long[] firstRead = getCpuTimes(coreInfo);
			double busy1 = (double)firstRead[0];
			double idle1 = (double)firstRead[1];

			busyR1.add(busy1);
			idleR1.add(idle1);
		}
	} catch (Exception e) {
		System.out.println(e.getMessage());
	}
	
	try {
		Thread.sleep(1000);
	} catch (Exception e){
		System.out.println(e.getMessage());
	}

	try {
		List<String> coreInfoLines = Files.readAllLines(Path.of("/proc/stat"));
		for (int i = 0; i < (numOfCores + 1); i++){
			
			String coreInfo = coreInfoLines.get(i);
			long[] secondRead = getCpuTimes(coreInfo);
			double busy2 = (double)secondRead[0];
			double idle2 = (double)secondRead[1];

			busyR2.add(busy2);
			idleR2.add(idle2);
		}
	} catch (Exception e) {
		System.out.println(e.getMessage());
	}

	ArrayList<Double> busyDifference = new ArrayList<Double>();
	ArrayList<Double> idleDifference = new ArrayList<Double>();
	ArrayList<Double> loadRatio = new ArrayList<Double>();

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
	}

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
}
