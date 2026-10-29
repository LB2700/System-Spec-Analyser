import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The USB devices that Linux has found, read from /sys/bus/usb/devices.
 *
 * Root hubs (the controllers inside the computer) are included in the list, so a
 * machine with nothing plugged in can still show one or more entries. A virtual
 * machine or the cloud server may have no USB at all; getCount() then returns 0.
 * Devices are listed in the same order as the lsusb command (by bus number, then
 * device number). The list is read once; call rescan() to read it again.
 */
public class usbInfo
{
    private static Path OGROOT = sysFile.path("/sys/bus/usb/devices");
    private static Path ROOT = sysFile.path("/sys/bus/usb/devices");
    private static List<Path> devices;
    protected static idLookup usb;
    
    protected static List<Path> list(Path inRoot)
    {
        //Debug - Jonathan
        //System.out.println("File is checking devices");
        if (devices == null) {
            List<Path> found = new ArrayList<>();
            //Debug - Jonathan
            //System.out.println("File is checking the ROOT folder");
            if (Files.isDirectory(inRoot)) {
// 				(Debug - luke)
//				System.out.printf("Looking through directory: %s \n", ROOT); 
                try (DirectoryStream<Path> dir = Files.newDirectoryStream(inRoot)) {
                    for (Path p : dir) {
// 					(Debug - luke)
// 					System.out.printf("\t looking at: %s \n",p); 
		       if (Files.exists(p.resolve("idVendor")))  // skip interfaces
                            found.add(p);
		}
                } catch (IOException e) {
                    // leave the list as it is
                }
            }
            else {
                //Debug - Jonathan
                System.out.println("File didnt recognise root");
            }
            // lsusb order: by bus number, then device number
            found.sort(Comparator.comparingLong((Path p) -> sysFile.number(p.resolve("busnum")))
                                 .thenComparingLong(p -> sysFile.number(p.resolve("devnum"))));
            devices = found;
        }
        return devices;
    }

    /** Read the list of devices again. */
    public static void rescan()
    {
        devices = null;
    }

    /** How many USB devices there are (0 if the machine has no USB support). */
    public static int getCount()
    {
        return list(ROOT).size();
    }

    /** The name Linux gives the device, for example "1-2" or "usb1". */
    public static String getName(int index)
    {
        return list(ROOT).get(index).getFileName().toString();
    }

    /** The vendor ID, for example 0x046d, or -1. The files hold it as plain hex digits. */
    public static int getVendorId(int index)
    {
        return hex(index, "idVendor");
    }

    /** The product ID (the USB name for the device ID), or -1. */
    public static int getProductId(int index)
    {
        return hex(index, "idProduct");
    }

    /** The manufacturer text reported by the device itself, or "" if it has none. */
    public static String getManufacturer(int index)
    {
        return sysFile.text(list(ROOT).get(index).resolve("manufacturer"));
    }

    /** The product text reported by the device itself, or "" if it has none. */
    public static String getProduct(int index)
    {
        return sysFile.text(list(ROOT).get(index).resolve("product"));
    }

    /** The bus number the device is on, or -1. */
    public static int getBusNumber(int index)
    {
        return (int) sysFile.number(list(ROOT).get(index).resolve("busnum"));
    }

    /** The device number on its bus, or -1. */
    public static int getDeviceNumber(int index)
    {
        return (int) sysFile.number(list(ROOT).get(index).resolve("devnum"));
    }

    // The USB id files hold bare hex such as "1d6b" with no 0x prefix.
    private static int hex(int index, String file)
    {
        String s = sysFile.text(list(ROOT).get(index).resolve(file));
        try {
            return Integer.parseInt(s, 16);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static void printUSBDevices(ArrayList<Integer> index) {
    	System.out.println("USB Devices: " + usbInfo.getCount());
        System.out.println("Your USB Devices: ");
    	for (int i : index) {
    		printUSBDevices(i);
    	}
    }

    public static void printUSBDevices(int i) {
    	int vendor = usbInfo.getVendorId(i);
    	int product = usbInfo.getProductId(i);
    	System.out.printf("  Bus %03d Device %03d: ID %04x:%04x  %s %s %s %s%n %s", usbInfo.getBusNumber(i),
    		                      usbInfo.getDeviceNumber(i), vendor, product,
    		                      "\n\tVendor name:\t", Main.usb.vendorName(vendor), "\n\tDevice type:\t", Main.usb.deviceName(vendor, product), "\n");
    }

    public static void usbDevices(int sortCase) {
    	// try {			
    	// 	System.out.println("USB devices: " + usbInfo.getCount());
    	// 	if (usbInfo.getCount() == 0) {
    	// 	    System.out.println("  No USB devices have been found");
    	// 	}
    	// 	else {
    	// 	    for (int i = 0; i < usbInfo.getCount(); i++) {
    	// 	    int vendor = usbInfo.getVendorId(i);
    	// 	    int product = usbInfo.getProductId(i);
    	// 	    System.out.printf("  Bus %03d Device %03d: ID %04x:%04x  %s %s%n", usbInfo.getBusNumber(i),
    	// 	                      usbInfo.getDeviceNumber(i), vendor, product,
    	// 	                      usb.vendorName(vendor), usb.deviceName(vendor, product));           
    	// 	    }
    	// 	}
    	// } catch (Exception error) {
    	// 	System.out.println(error.getMessage());
    	// 	error.printStackTrace();
    	// }
    	ArrayList<Integer> usbByName = new ArrayList<>();
    	ArrayList<Integer> usbByBus = new ArrayList<>();
    	
    	try {
    		if (usbInfo.getCount() == 0) {
    			System.out.println("USB Devices: " + usbInfo.getCount());
    			System.out.println("  No USB devices have been found");
    		}
    		else {
    			for (int i = 0; i < usbInfo.getCount(); i++) {
    				usbByName.add(i);
    				usbByBus.add(i);
    			}				

    			usbByName.sort((a, b) -> 
    			{
    				return usbInfo.getVendorId(a) - usbInfo.getVendorId(b);
    			});
    			usbByBus.sort((a, b) -> 
    			{
    				return usbInfo.getBusNumber(a) - usbInfo.getBusNumber(b);
    			});

                switch (sortCase) {
                    case 1:
                        printUSBDevices(usbByBus);
                        break;
                    case 2:
                        printUSBDevices(usbByName);
                        break;
                    default:
                        throw new AssertionError();
                }

    			//printUSBDevices(usbByBus);
    			//printUSBDevices(usbByName);
    			
    		}
    	// } catch (IOException error) {
    	// 	System.out.println(error.getMessage());
    	// 	error.printStackTrace();
     	} catch (Exception error) {
    		System.out.println(error.getMessage());
    		error.printStackTrace();
    	}
    }

    public static void printSysRoot(int sortCase)
    {
        usbInfo.setRoot(OGROOT);
        usbInfo.usbDevices(sortCase);      // uses the existing getVendorId, getBusNumber, etc.
    }

    public static void setRoot(Path newRoot)
    {
        ROOT = newRoot;
        rescan();   // forget the devices cached from the old folder
    }

    public static void listUSBs() {
        System.out.println("There are " + getSysCount() + " USB devices in your machine"); 
    }

    public static int getSysCount() {
        usbInfo.setRoot(OGROOT);
        return getCount();
    }

}
