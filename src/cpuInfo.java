import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Information about the processor, read from /proc/cpuinfo and /proc/stat.
 *
 * The busy and idle values are counts of "ticks" since the machine booted
 * (usually 100 ticks per second). To find the load, read them twice, wait about
 * a second, read them again, and work out busy / (busy + idle) from the DIFFERENCES.
 */
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
		
		//turn each value into long and add the values together, exclude "cpu" -which is at index 0 hence i = 1 
		//And exclude "idle" and "IO" to find busy time
		for (int i = 1; i < arrayOfStrings.length; i++) {
			//System.out.printf("%d: %s \n",i, arrayOfStrings[i]);
			if (i != IDLE_TIME_INDEX & i != IO_TIME_INDEX){
    	    	busy += Long.parseLong(arrayOfStrings[i]);
			} else { 
				idle += Long.parseLong(arrayOfStrings[i]);
			}
		}
		//System.out.println("");
		//System.out.printf("busy: %d | idle: %d \n", busy, idle);
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
}
