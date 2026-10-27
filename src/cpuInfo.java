import java.nio.file.Path;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;


/**
 * Information about the processor, read from /proc/cpuinfo and /proc/stat.
 *
 * The busy and idle values are counts of "ticks" since the machine booted
 * (usually 100 ticks per second). To find the load, read them twice, wait about
 * a second, read them again, and work out busy / (busy + idle) from the DIFFERENCES.
 */
public class cpuInfo
{
    private static final Path CPUINFO = sysFile.path("/proc/cpuinfo");
    private static final Path STAT = sysFile.path("/proc/stat");

    /** Number of cores (logical processors) the operating system sees. */
    public static int getCount()
    {
        int count = 0;
        for (String line : sysFile.lines(CPUINFO))
            if (line.startsWith("processor"))
                count++;
        return count;
    }

    /**
     * The processor model, for example "Intel(R) Xeon(R) Processor @ 2.80GHz".
     * On arm64 there is no model name line, so this returns "unknown"; use
     * getField("CPU part") or getField("CPU implementer") instead.
     */
    public static String getModel()
    {
        String model = getField("model name");
        return model.isEmpty() ? "unknown" : model;
    }

    /** The value of the first line in /proc/cpuinfo whose name is exactly 'name', or "". */
    public static String getField(String name)
    {
        for (String line : sysFile.lines(CPUINFO)) {
            int colon = line.indexOf(':');
            if (colon > 0 && line.substring(0, colon).strip().equals(name))
                return line.substring(colon + 1).strip();
        }
        return "";
    }

    /** Ticks since boot that core number 'core' (0, 1, 2 ...) has spent doing work. */
    public static long getBusy(int core)
    {
        long[] f = fields(core);
        // user + nice + system + irq + softirq + steal
        return f == null ? -1 : f[0] + f[1] + f[2] + f[5] + f[6] + f[7];
    }

    /** Ticks since boot that core number 'core' has spent idle (including waiting for I/O). */
    public static long getIdle(int core)
    {
        long[] f = fields(core);
        return f == null ? -1 : f[3] + f[4];
    }

    /** The numbers from the "cpuN" line of /proc/stat, or null if there is no such core. */
    private static long[] fields(int core)
    {
        String prefix = "cpu" + core + " ";
        for (String line : sysFile.lines(STAT)) {
            if (line.startsWith(prefix)) {
                String[] part = line.strip().split("\\s+");
                long[] f = new long[8];
                for (int i = 0; i < 8; i++)
                    f[i] = (i + 1 < part.length) ? Long.parseLong(part[i + 1]) : 0;
                return f;
            }
        }
        return null;
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
