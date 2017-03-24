package com.vmware.vmcreation.samples;

import java.io.IOException;
import java.net.URL;

import org.junit.Test;

import com.svm.vim25.ManagedObjectReference;
import com.svm.vim25.VirtualMachineCloneSpec;
import com.svm.vim25.VirtualMachinePowerState;
import com.svm.vim25.VirtualMachineRelocateSpec;
import com.svm.vim25.mo.Datastore;
import com.svm.vim25.mo.Folder;
import com.svm.vim25.mo.HostSystem;
import com.svm.vim25.mo.ResourcePool;
import com.svm.vim25.mo.ServiceInstance;
import com.svm.vim25.mo.Task;
import com.svm.vim25.mo.VirtualMachine;
import com.vmware.vmencrypt.samples.CryptoManager;
import com.vmware.vmencrypt.samples.CryptoService;

public class VmManager {
    
    @Test
    public void testCreateEncryptedVm() throws Exception {
        
        String vcHost = "";
        String username = "";
        String password = "";
        
        String vcUrl = "https://" + vcHost + "/sdk";
        String ssoUrl = "https://" + vcHost + "/sts/STSService";        
        
        String vmName = "";
        String clusterName = "";
        String templateName = "";
        String folderName = "";
        String hostName = "";
        String dsName = "";
        String dcName = "";
        
        ServiceInstance si = new ServiceInstance(new URL(vcUrl), username, password, true);        
        VirtualMachine templateVm = SearchTool.findVm(si, templateName);
        Folder parentFolder = SearchTool.findFolder(si, folderName);
        //Folder parentFolder = SearchTool.findDataCenter(si, dcName).getVmFolder();
        ResourcePool resPool = SearchTool.findCluster(si, clusterName).getResourcePool();
        HostSystem host = SearchTool.findHost(si, hostName);
        Datastore ds = SearchTool.findDataStore(si, dsName);
        
        System.out.println("Start create and encrypt VM ...");
        
        VirtualMachine vm = cloneVm(vmName, templateVm, parentFolder, resPool, host, ds);
        
        CryptoService cryptService = CryptoManager.getCryptoService();        
        cryptService.encryptVM(vcUrl, ssoUrl, username, password, VmUtil.getInventoryPath(vm));
        
        startVm(vm, host);
        waitForReady(vm);

        System.out.println("Done create and encrypt VM ...");
        
    }

    public VirtualMachine createVm(String vmName, VirtualMachine templateVm, Folder parentFolder, ResourcePool resPool,
            HostSystem host, Datastore ds) throws Exception {
        VirtualMachine vm = cloneVm(vmName, templateVm, parentFolder, resPool, host, ds);
        startVm(vm, host);
        waitForReady(vm);
        return vm;
    }

    public void destoryVm(VirtualMachine vm) throws Exception {
        if (vm == null) {
            return;
        }
        try {
            if (vm.getRuntime().getPowerState() != VirtualMachinePowerState.poweredOff) {
                TaskExecutor.exec(vm.powerOffVM_Task(), "Power off VM: " + vm.getName());
            }
            TaskExecutor.exec(vm.destroy_Task(), "Destory VM: " + vm.getName());
        } catch (Exception e) {
            throw e;
        }
    }

    private VirtualMachine cloneVm(String vmName, VirtualMachine templateVm, Folder folder, ResourcePool resPool,
            HostSystem host, Datastore ds) throws Exception {
        VirtualMachineRelocateSpec relocSpec = new VirtualMachineRelocateSpec();
        relocSpec.setPool(resPool.getMOR());
        relocSpec.setHost(host.getMOR());
        relocSpec.setDatastore(ds.getMOR());

        VirtualMachineCloneSpec cloneSpec = new VirtualMachineCloneSpec();
        cloneSpec.setLocation(relocSpec);
        cloneSpec.setPowerOn(false);
        cloneSpec.setTemplate(false);

        Task task = templateVm.cloneVM_Task(folder, vmName, cloneSpec);
        System.out.println("Cloning VM: " + vmName);
        TaskExecutor.exec(task, "Clone VM: " + vmName);
        return new VirtualMachine(templateVm.getServerConnection(), (ManagedObjectReference) task.getTaskInfo().getResult());
    }
    
    private void startVm(VirtualMachine vm, HostSystem host) throws Exception {
        Task task = vm.powerOnVM_Task(host);
        TaskExecutor.exec(task, "Start VM: " + vm.getName());
    }

    private void waitForReady(VirtualMachine vm) throws IOException {
        System.out.println("Got IP of VM: " + vm.getName() + ", IP address: " + VmUtil.waitForIp(vm));
        System.out.println("VM: " + vm.getName() + " is ready for use now!");
    }

}
