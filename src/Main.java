import java.util.Scanner;

public class Main
{
    public static void main(String[] args) 
    {
        Scanner scanner = new Scanner(System.in);

        while (true) 
        {
            System.out.println("Which memory information do you want to see?");
            System.out.println("Total memory (1)");
            System.out.println("Used memory (2)");
            System.out.println("Memory usage percentage (3)");
            System.out.println("Exit (4)");
            int choice = scanner.nextInt();

            if(memInfo.getTotal() == -1)
            {
                System.out.println("Error reading memory information. Please Use a Linux machine.");
                break;
            }

            switch(choice)
            {
                case 1:
                    System.out.println("Total memory: " + memInfo.getTotal() / 1024 + " MB");
                    break;
                case 2:
                    System.out.println("Total used memory: " + memInfo.getUsed() / 1024 + " MB");
                    break;
                case 3:
                    System.out.println("Percentage of memory used: " + ((double) memInfo.getUsed() / memInfo.getTotal() * 100) + "%");
                    break;
                case 4:
                    System.out.println("Exiting...");
                    break;
            }
        }
    }
}
