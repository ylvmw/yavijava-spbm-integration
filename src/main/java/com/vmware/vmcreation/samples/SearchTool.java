package com.vmware.vmcreation.samples;

import java.io.IOException;

import com.svm.vim25.mo.ClusterComputeResource;
import com.svm.vim25.mo.ComputeResource;
import com.svm.vim25.mo.Datacenter;
import com.svm.vim25.mo.Datastore;
import com.svm.vim25.mo.Folder;
import com.svm.vim25.mo.HostSystem;
import com.svm.vim25.mo.InventoryNavigator;
import com.svm.vim25.mo.ManagedEntity;
import com.svm.vim25.mo.ServiceInstance;
import com.svm.vim25.mo.VirtualMachine;

public class SearchTool {

    public static ManagedEntity findByUuid(ServiceInstance si, String dcName, String uuid, boolean isVm)
            throws IOException {
        Datacenter dc = (Datacenter) new InventoryNavigator(si.getRootFolder()).searchManagedEntity("Datacenter",
                dcName);
        if (dc == null) {
            throw new IOException("Not found datacenter: " + dcName);
        }
        ManagedEntity entity = si.getSearchIndex().findByUuid(dc, uuid, isVm);
        return entity;
    }

    public static VirtualMachine findVm(ServiceInstance si, String vmName) throws IOException {
        if (si == null) {
            throw new IOException("Not valid service instance.");
        }
        VirtualMachine vm = (VirtualMachine) new InventoryNavigator(si.getRootFolder())
                .searchManagedEntity("VirtualMachine", vmName);
        if (vm == null) {
            throw new IOException("Not found vm: " + vmName);
        }
        return vm;
    }

    public static HostSystem findHost(ServiceInstance si, String hostName) throws IOException {
        if (si == null) {
            throw new IOException("Not valid service instance.");
        }
        HostSystem host = (HostSystem) (new InventoryNavigator(si.getRootFolder())).searchManagedEntity("HostSystem",
                hostName);
        if (host == null) {
            throw new IOException("Not found host: " + hostName);
        }
        return host;
    }

    public static Datacenter findDataCenter(ServiceInstance si, String dcName) throws IOException {
        if (si == null) {
            throw new IOException("Not valid service instance.");
        }
        Datacenter dc = (Datacenter) (new InventoryNavigator(si.getRootFolder())).searchManagedEntity("Datacenter",
                dcName);
        if (dc == null) {
            throw new IOException("Not found data center: " + dcName);
        }
        return dc;
    }

    public static ComputeResource findComputeResource(ServiceInstance si, String crName) throws IOException {
        if (si == null) {
            throw new IOException("Not valid service instance.");
        }
        ComputeResource cr = (ComputeResource) (new InventoryNavigator(si.getRootFolder()))
                .searchManagedEntity("ComputeResource", crName);
        if (cr == null) {
            throw new IOException("Not found compute resource: " + crName);
        }
        return cr;
    }

    public static ClusterComputeResource findCluster(ServiceInstance si, String clusterName) throws IOException {
        if (si == null) {
            throw new IOException("Not valid service instance.");
        }
        ClusterComputeResource cluster = (ClusterComputeResource) (new InventoryNavigator(si.getRootFolder()))
                .searchManagedEntity("ClusterComputeResource", clusterName);
        if (cluster == null) {
            throw new IOException("Not found cluster: " + clusterName);
        }
        return cluster;
    }

    public static Datastore findDataStore(ServiceInstance si, String dsName) throws IOException {
        if (si == null) {
            throw new IOException("Not valid service instance.");
        }
        Datastore ds = (Datastore) (new InventoryNavigator(si.getRootFolder())).searchManagedEntity("Datastore",
                dsName);
        if (ds == null) {
            throw new IOException("Not found data store: " + dsName);
        }
        return ds;
    }

    public static Folder findFolder(ServiceInstance si, String folderName) throws IOException {
        if (si == null) {
            throw new IOException("Not valid service instance.");
        }
        Folder folder = (Folder) (new InventoryNavigator(si.getRootFolder())).searchManagedEntity("Folder", folderName);
        if (folder == null) {
            throw new IOException("Not found folder: " + folderName);
        }
        return folder;
    }

}
