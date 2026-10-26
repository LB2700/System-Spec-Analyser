import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;


public class cpuInfo {
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
		System.out.println("You have: " + System.getProperty("os.arch") + " Cpu archeitecture");
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

    public static long[] getCpuTimes(String coreInfo){
		//Gets rid of empty space and splits into array
		String[] arrayOfStrings = coreInfo.trim().split("\\s+");
	
		long idle = 0;
		long busy = 0;

		//e.g cpu 79242 0 74306 842486413 756859 6140 67701 0 ->  index 4 in array is idle (842486413) index 5 is waiting for I/O to complete (756859)
		final int IDLE_TIME_INDEX = 4;
		final int IO_TIME_INDEX = 5;
		
		//Add values together, exclude "cpu" which is at index 0 hence i = 1 
		//And exclude "idle" and "IO" to find busy time
		for (int i = 1; i < arrayOfStrings.length; i++) {
			//System.out.printf("%d: %s \n",i, arrayOfStrings[i]);
			if (i != IDLE_TIME_INDEX & i != IO_TIME_INDEX){
    	    	busy += Long.parseLong(arrayOfStrings[i]);
			} else { 
				idle += Long.parseLong(arrayOfStrings[i]);
			}
		}

		long[] busyidle = {busy, idle};
		return busyidle;
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
				getLoad(idleR1, busyR1, coreInfo);
			}
			
			Thread.sleep(1000);
	
			coreInfoLines = Files.readAllLines(Path.of("/proc/stat"));
			for (int i = 0; i < (numOfCores + 1); i++){

				String coreInfo = coreInfoLines.get(i);
				getLoad(idleR2, busyR2, coreInfo);
				
			}
			
			for (int i = 0; i < idleR1.size(); i++) {
			
				busyDifference.add(busyR2.get(i) - busyR1.get(i));
				idleDifference.add(idleR2.get(i) - idleR1.get(i));
				//get precentage time busy -> busy divided by total time, times 100
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

    public static void showCpuTemperature() {
	    //https://askubuntu.com/questions/617368/temperatures-files-location
	    try{
    	    String temp = Files.readString(Path.of("/sys/class/thermal/thermal_zone0/temp"));
		    //to get degrees divide by 1000
		    System.out.println("Cpu temperature: " + Double.parseDouble(temp.trim()) / 1000 + " degrees celcius");
	    } catch (Exception e) {
		    System.out.println(e.getMessage());
        }
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

    public static void showCoreCount() {
    	//System.out.println("Processors (cores) avaiable: " + Runtime.getRuntime().availableProcessors());
    	
    	try {
    		for (String line : Files.readAllLines(Path.of("/proc/cpuinfo"))) {
    			if (line.startsWith("cpu cores")){
    				int coreCount = Integer.parseInt(line.split(":")[1].trim());
    				System.out.println("Core count: " + coreCount);
    				break;
    			}
    		}
    	} catch (IOException error) {
    		System.out.println(error.getMessage());
    	}
    }

    public static void showCpuArch() {
    	//Gets archeitecture, not what the requirements wants, even then it's something cool to add
    	System.out.println("CPU Architecture: " + System.getProperty("os.arch"));
    }

    public static void showCpuUsage() {
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
    				//System.out.println(coreInfo);
    				
    				long[] firstRead = getCpuTimes(coreInfo);
    				double busy1 = (double)firstRead[0];
    				double idle1 = (double)firstRead[1];
    
    				busyR1.add(busy1);
    				idleR1.add(idle1);
    			}
    		} catch (Exception e) {
    			System.out.println(e.getMessage());
    		}

			System.out.print("waiting 1000ms... \n\n");
    		
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

    		System.out.print("CPU Load\n");
    		for (int i = 1; i < loadRatio.size(); i++) {
    			// "%.3f%%" inserts a floating point with 3 decimal places and adds a % symbol
    			System.out.printf("Core %d: %.3f%%\n", i, loadRatio.get(i));
    			totalLoad += loadRatio.get(i);
    		}
    
    		double averageLoad = totalLoad / numOfCores;
    		System.out.printf("Average load: %.3f%%\n", averageLoad);
    	}

    public static void start() {

    	System.out.print("Processor Information\n\n");
    	
		showCpuArch();
    	showCoreCount();

    	System.out.print("\n");
		showCpuUsage();
    }

}
