package com.vmware.vmcreation.samples;

import java.io.IOException;

import com.svm.vim25.OptionValue;
import com.svm.vim25.VirtualDevice;
import com.svm.vim25.VirtualDisk;
import com.svm.vim25.mo.ManagedEntity;
import com.svm.vim25.mo.VirtualMachine;

public class VmUtil {

    public static long calcVmDiskSpace(VirtualMachine vm) {
        long diskSpace = 0; // bytes
        VirtualDevice[] devices = vm.getConfig().getHardware().getDevice();
        if (devices != null) {
            for (VirtualDevice device : devices) {
                if (device instanceof VirtualDisk) {
                    VirtualDisk disk = (VirtualDisk) device;
                    diskSpace += disk.getCapacityInBytes();
                }
            }
        }
        return diskSpace;
    }

    public static String waitForIp(VirtualMachine vm) throws IOException {
        String vmName = vm.getName();
        long timeout = 5 * 60 * 1000; // 5 minutes
        long interval = 10 * 1000; // 10 seconds
        long sum = 0;
        while (sum < timeout) {
            String ip = vm.getGuest().getIpAddress();
            if (ip != null) {
                return ip;
            } else {
                try {
                    System.out.println("Waiting for IP of vm: " + vmName);
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
                sum += interval;
            }
        }
        throw new IOException("Could not get IP address of vm: " + vmName);
    }

    public static <T> T findExtraConfigValue(VirtualMachine vm, String key, Class<T> type) {
        OptionValue[] options = vm.getConfig().getExtraConfig();
        for (OptionValue option : options) {
            if (option.getKey().equals(key)) {
                return type.cast(option.getValue());
            }
        }
        return null;
    }
    
    public static String getInventoryPath(VirtualMachine vm) {
        if (vm == null) {
            return null;
        }
        StringBuilder pathStr = new StringBuilder("/" + vm.getName());
        ManagedEntity parent = vm.getParent();
        while (parent != null) {
            pathStr.insert(0, "/" + parent.getName());
            parent = parent.getParent();
        }
        pathStr.delete(0, pathStr.indexOf("/", 1));
        return pathStr.toString();
    }

}
