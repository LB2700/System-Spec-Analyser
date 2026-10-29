import java.nio.file.Path;
import java.io.PrintWriter; // this is used to write to the CSV file

// java INSISTS on adding this, it is used to throw an exception if an error occurs while reading or writing to the CSV file
import java.io.FileNotFoundException;

// used to access the cache files
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.Scanner;

/**
 * Information about memory, read from /proc/meminfo. All sizes are in KiB
 * (1 KiB = 1024 bytes), which is the unit Linux uses in that file.
 */
public class memInfo
{
    private static final Path MEMINFO = sysFile.path("/proc/meminfo");

    /** Total installed memory in KiB, or -1 if it cannot be read. */
    public static long getTotal()
    {
        return field("MemTotal");
    }

    /** Memory that programs could use right now (free plus easily reclaimed), in KiB. */
    public static long getAvailable()
    {
        return field("MemAvailable");
    }

    /** Memory in use in KiB: total minus available. */
    public static long getUsed()
    {
        long total = getTotal();
        long available = getAvailable();
        return (total < 0 || available < 0) ? -1 : total - available;
    }

    /** The value in KiB of a named line of /proc/meminfo such as "SwapTotal", or -1. */
    public static long field(String name)
    {
        for (String line : sysFile.lines(MEMINFO)) {
            if (line.startsWith(name + ":")) {
                String[] part = line.substring(name.length() + 1).strip().split("\\s+");
                try {
                    return Long.parseLong(part[0]);
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        }
        return -1;
    }

    public static void PrintTotalMemory()
    {
        if(getTotal() == -1) {
            System.out.println("Error reading total memory");
            return;
        }
        System.out.println("Total memory: " + getTotal() / 1024 + " MB");
    }

    public static void PrintTotalUsedMemory()
    {
        if(getUsed() == -1) {
            System.out.println("Error reading total used memory");
            return;
        }
        System.out.println("Total used memory: " + getUsed() / 1024 + " MB");
    }

    public static void PrintPercentageOfMemoryUsed()
    {
        if(getUsed() == -1 || getTotal() == -1) {
            System.out.println("Error reading memory information");
            return;
        }
        System.out.printf("Percentage of memory used: %.2f%%",((double) getUsed() / getTotal() * 100));
    }

	public static void cachePerformanceAnalysis(Scanner scanner) throws FileNotFoundException
    {
        int[] numbers = new int[32 * 1024 * 1024]; // 128 MB worth of integers (4 bytes each)

        // this for loop writes to every element so that all the pages are really mapped
        // they NEED to be mapped so when they are accessed it isnt slower, this is to give more accurate results
        for (int i = 0; i < numbers.length; i++)    
        {
            numbers[i] = i;
        }
        // sum is used for the calculation, it is needed since the JIT compiler will skip the loop if there is no calculations happening in it
        long sum = 0;

        // How many array accesses each measurement should perform (20 million).
        // Big enough that the timing is long enough for nanoTime() to measure accurately.
        final long TARGET = 20_000_000L;

        System.out.print("Enter the Name of the new csv file: ");
        String fileName = scanner.next();
        PrintWriter csv = new PrintWriter("output/csvFiles/" + fileName + ".csv"); // creates the file
        csv.println("stride_bytes,set_size_kb,ns_per_access"); // header row, used for readability. it is ignored in the graph plotter file

        // this for loop prints the time taken for different strides
        for (int stride = 1; stride <= 512; stride *= 2)
        {
            // multiple stride by 4 to show the value in BYTES since it is counted in ints
            System.out.println("\n--------------------Stride: " + (stride * 4) + " bytes--------------------\n");

            // this for loop prints the time taken for different set sizes
            for (int bytes = 16 * 1024; bytes <= 128 * 1024 * 1024; bytes *= 2) // bytes starts at 16 KB and ends at 128 MB
            {
                int setSize = bytes / 4; // divide by 4 because every integer is 4 bytes

                // Number of array reads in ONE pass through the working set.
                // The inner loop jumps 'stride' elements each step, so the reads per pass = setSize / stride.
                // The "+ stride - 1" rounds up so a partial last step still counts
                // (with our power-of-2 values it divides exactly, so it changes nothing)
                int perPass = (setSize + stride - 1) / stride;

                // Number of passes needed so the total reads come to about TARGET (20 mil)
                // the 1 is there so if a pass is bigger than TARGET it guarantees at least one pass
                long passes = Math.max(1, TARGET / perPass); // Math.max gets the highest value of both inputs

                // warmup pass, just to make sure the JIT compiler has optimized the code before the measuring starts
                for (int i = 0; i < setSize; i += stride)
                {
                    sum += numbers[i]; // a calculation is needed
                }

                // measure the time before calculating
                long start = System.nanoTime();

                for (long p = 0; p < passes; p++)
                {
                    for (int i = 0; i < setSize; i += stride)
                    {
                        sum += numbers[i]; // calculation so the memory read can be timed
                    }
                }

                // measure the time after calculating
                long end = System.nanoTime();
                // find the elapsed time for the calculation
                long elapsed = end - start;

                // calculate the time taken for a single access (In nano seconds)
                double nsPerAccess = (double) elapsed / (passes * perPass);

                System.out.println("Set size = " + (bytes / 1024) + " KB" + ", Time per access = " + nsPerAccess + " ns");
                csv.println((stride * 4) + "," + (bytes / 1024) + "," + nsPerAccess); // add the values into the csv file as a new row
            }
        }

        csv.close(); // saves and closes the file

        System.out.println("\n(checksum " + sum + ")"); // stops the JIT removing the loops

        try // try is used because cache files arent acessible in some Virtual machines
        {

            // no need to explain this code...

            String L1Size = Files.readString(Paths.get("/sys/devices/system/cpu/cpu0/cache/index0/size")).trim();
            System.out.println("L1 cache size: " + L1Size);

            String L2Size = Files.readString(Paths.get("/sys/devices/system/cpu/cpu0/cache/index2/size")).trim();
            System.out.println("L2 cache size: " + L2Size);

            String L3Size = Files.readString(Paths.get("/sys/devices/system/cpu/cpu0/cache/index3/size")).trim();
            System.out.println("L3 cache size: " + L3Size);
        } 
        catch (Exception e) 
        {
            // in some virtual machines they just dont have the cache file. im not sure why they do it. maybe for security..?
            System.out.println("Error! Cache files cannot be accessed");
        }
    }
}
