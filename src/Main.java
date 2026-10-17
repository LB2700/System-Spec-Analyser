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
		
	//try {
	//	for (String i : Files.readAllLines(Path.of("/proc/cpuinfo"))) {
	//		if (i.startsWith("cpu cores")){
	//			System.out.println("There are: " + i + " processers (cores)");
	//			break;
	//		}
	//	}
	//}catch (java.io.IOException error) {
	//	System.out.println(error.getMessage());
	//}
		
	//Gets archeitecture, probably not what Mark wants 
	System.out.println("You have: " + System.getProperty("os.arch"));
		
	//Needs to be tested 
	//try {
	//	for (String i : Files.readAllLines(Path.of("/proc/cpuinfo"))) {
	//		if (i.startsWith("model name")){
	//			System.out.println(i);
	//			break;
	//		}
	//	}
	//}catch (java.io.IOException error) {
	//	System.out.println(error.getMessage());
	//}


	//https://stackoverflow.com/questions/55050864/detect-cpu-model-information - I don't know how it works, I'll look further into it
	//try {
	//String model = Files.lines(Paths.get("/proc/cpuinfo"))
   	//	.filter(line -> line.startsWith("model name"))
   	//	.map(line -> line.replaceAll(".*: ", ""))
   	//	.findFirst().orElse("");
	//} catch (java.io.IOException error) {
	//	System.out.println(error.getMessage());
	//}
	
	//https://support.checkpoint.com/results/sk/sk65143
	int numOfCores = Runtime.getRuntime().availableProcessors();
	ArrayList<Double> coreLoads = new ArrayList<Double>();
	try {
		List<String> coreInfoLines = Files.readAllLines(Path.of("/proc/stat"));
		for (int i = 0; i < (numOfCores + 1); i++){
			
			String coreInfo = coreInfoLines.get(i);
			getCpuTimes(coreInfo);

			long[] firstRead = getCpuTimes(coreInfo);
			Thread.sleep(1000);
			long[] secondRead = getCpuTimes(coreInfo);

			long busyDifference = secondRead[0] - firstRead[0];
			long idleDifference = secondRead[1] - firstRead[1];

			double loadRatio = 100.0 * busyDifference / (busyDifference + idleDifference);

			if (i == 0){
				double averageLoad = loadRatio / Runtime.getRuntime().availableProcessors();
				System.out.printf("Average load: " + averageLoad);
			} else {
				coreLoads.add(loadRatio);
			}
		}
	} catch (Exception e) {
		System.out.println(e.getMessage());
		//e.printStackTrace();
	}
	for (Double cpuLoad: coreLoads){
			System.out.println("Cpu has a load of " + cpuLoad);
	}
	
	}

	public static long[] getCpuTimes(String coreInfo){

		//reads first core in "/proc/stat"
		//String readCore = Files.readAllLines(Path.of("/proc/stat")).get(cpuNumber);
		
		//Gets rid of empty space and splits into array
		String[] arrayOfStrings = coreInfo.trim().split("\\s+");
	
		
		long idle = 0;
		long busy = 0;
		final int IDLE_TIME_INDEX = 4;
		
		//e.g cpu 79242 0 74306 842486413 756859 6140 67701 0 ->  index 4 in array is idle (842486413)
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