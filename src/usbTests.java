import java.nio.file.Path;
/**
 * The USB devices that Linux has found, read from /sys/bus/usb/devices.
 *
 * Root hubs (the controllers inside the computer) are included in the list, so a
 * machine with nothing plugged in can still show one or more entries. A virtual
 * machine or the cloud server may have no USB at all; getCount() then returns 0.
 * Devices are listed in the same order as the lsusb command (by bus number, then
 * device number). The list is read once; call rescan() to read it again.
 */
public class usbTests
{
    private static final Path[] ROOTS = {
        sysFile.path("assets/tests/NoUSBs"),
        sysFile.path("assets/tests/FakeUSBs")
    };

    public static void printTestRoot(int root)
    {
        usbInfo.setRoot(ROOTS[root]);
        usbInfo.usbDevices(1);      // uses the existing getVendorId, getBusNumber, etc.
    }

    public static void compareTestUSB () {
        int sysCount = usbInfo.getSysCount();
        usbInfo.setRoot(ROOTS[1]);
        int testCount = usbInfo.getCount();
        if (sysCount > testCount) {
            System.out.println("Your device has more USBs then the test case for USBs");
            System.out.println("Your device has: " + sysCount);
            System.out.println("The test case has: " + testCount);
        }
        else if (testCount > sysCount) {
            System.out.println("Your device has less USBs then the test case for USBs");
            System.out.println("Your device has: " + sysCount);
            System.out.println("The test case has: " + testCount);
        }
        else {
            System.out.println("Your device has the same number of USBs as the test case for USBs has");
            System.out.println("Your device has: " + sysCount);
            System.out.println("The test case has: " + testCount);
        }
        
    }


}
