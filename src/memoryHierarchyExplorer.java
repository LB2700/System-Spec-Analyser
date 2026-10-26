import java.io.PrintWriter;
import java.io.IOException;

public class memoryHierarchyExplorer 
{
    public static void main(String[] args) throws IOException
    {
        int[] numbers = new int[32 * 1024 * 1024]; // 128 MB worth of integers (4 bytes each)

        // this for loop writes to every element so that all the pages are really mapped
        for (int i = 0; i < numbers.length; i++)
        {
            numbers[i] = i;
        }

        long sum = 0;
        final long TARGET = 20_000_000L; // accesses per measurement

        PrintWriter csv = new PrintWriter("cacheResults.csv"); // creates the file
        csv.println("stride_bytes,set_size_kb,ns_per_access"); // header row

        // this for loop prints the time taken for different strides
        for (int stride = 1; stride <= 512; stride *= 2)
        {
            System.out.println("\n--------------------Stride: " + (stride * 4) + " bytes--------------------\n");

            // this for loop prints the time taken for different set sizes
            for (int bytes = 16 * 1024; bytes <= 128 * 1024 * 1024; bytes *= 2) // bytes starts at 16 KB and ends at 128 MB
            {
                int setSize = bytes / 4; // divide by 4 because every integer is 4 bytes
                int perPass = (setSize + stride - 1) / stride; // accesses in one pass
                long passes = Math.max(1, TARGET / perPass);

                // warmup pass. just to make sure the JIT compiler has optimized the code before the measuring starts
                for (int i = 0; i < setSize; i += stride)
                {
                    sum += numbers[i];
                }

                long start = System.nanoTime();

                for (long p = 0; p < passes; p++)
                {
                    for (int i = 0; i < setSize; i += stride)
                    {
                        sum += numbers[i];
                    }
                }

                long end = System.nanoTime();
                long elapsed = end - start;

                double nsPerAccess = (double) elapsed / (passes * perPass);

                System.out.println("Set size = " + (bytes / 1024) + " KB" + ", Time per access = " + nsPerAccess + " ns");
                csv.println((stride * 4) + "," + (bytes / 1024) + "," + nsPerAccess); // one row in the csv file
            }
        }

        csv.close(); // saves and closes the file

        System.out.println("\n(checksum " + sum + ")"); // stops the JIT removing the loops
    }
}