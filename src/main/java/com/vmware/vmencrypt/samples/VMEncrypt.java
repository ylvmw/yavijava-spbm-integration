/*
 * ******************************************************
 * Copyright VMware, Inc. 2010-2013.  All Rights Reserved.
 * ******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS # OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY # DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY # QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.vmware.vmencrypt.samples;

import java.util.ArrayList;
import java.util.List;

import com.vmware.pbm.InvalidArgumentFaultMsg;
import com.vmware.pbm.PbmCapabilityProfile;
import com.vmware.spbm.connection.BasicConnection;
import com.vmware.spbm.connection.ConnectedServiceBase;
import com.vmware.spbm.connection.Connection;
import com.vmware.spbm.connection.helpers.VCUtil;
import com.vmware.sso.client.utils.Utils;
import com.vmware.spbm.connection.helpers.PbmUtil;
import com.vmware.vim25.ArrayOfVirtualDevice;
import com.vmware.vim25.ConcurrentAccessFaultMsg;
import com.vmware.vim25.CryptoKeyId;
import com.vmware.vim25.CryptoKeyResult;
import com.vmware.vim25.CryptoSpecEncrypt;
import com.vmware.vim25.DuplicateNameFaultMsg;
import com.vmware.vim25.FileFaultFaultMsg;
import com.vmware.vim25.InsufficientResourcesFaultFaultMsg;
import com.vmware.vim25.InvalidCollectorVersionFaultMsg;
import com.vmware.vim25.InvalidDatastoreFaultMsg;
import com.vmware.vim25.InvalidNameFaultMsg;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.InvalidStateFaultMsg;
import com.vmware.vim25.KmipClusterInfo;
import com.vmware.vim25.LocalizedMethodFault;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.TaskInProgressFaultMsg;
import com.vmware.vim25.TaskInfoState;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDeviceBackingInfo;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualDeviceConfigSpecBackingSpec;
import com.vmware.vim25.VirtualDeviceConfigSpecOperation;
import com.vmware.vim25.VirtualDisk;
import com.vmware.vim25.VirtualDiskFlatVer2BackingInfo;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualMachineDefinedProfileSpec;
import com.vmware.vim25.VirtualMachineProfileSpec;
import com.vmware.vim25.VmConfigFaultFaultMsg;

public class VMEncrypt extends ConnectedServiceBase {
	private static final String ENCRYPTION_POLICY_NAME = "VM Encryption Policy";

	/**
	 * This method returns the VirtualMachineDefinedProfileSpec for a given
	 * storage profile name
	 *
	 * @param profileName
	 *            name of the policy based management profile
	 * @return
	 * @throws InvalidArgumentFaultMsg
	 * @throws com.vmware.pbm.RuntimeFaultFaultMsg
	 * @throws RuntimeFaultFaultMsg
	 */
	private VirtualMachineDefinedProfileSpec getVMDefinedProfileSpec(String profileName)
			throws InvalidArgumentFaultMsg, com.vmware.pbm.RuntimeFaultFaultMsg, RuntimeFaultFaultMsg {

		PbmCapabilityProfile profile = PbmUtil.getPbmProfile(connection, profileName);
		VirtualMachineDefinedProfileSpec pbmProfile = new VirtualMachineDefinedProfileSpec();
		pbmProfile.setProfileId(profile.getProfileId().getUniqueId());
		return pbmProfile;

	}

	private VirtualMachineProfileSpec getEncryptionProfileSpec()
			throws InvalidArgumentFaultMsg, com.vmware.pbm.RuntimeFaultFaultMsg, RuntimeFaultFaultMsg {
		return getVMDefinedProfileSpec(ENCRYPTION_POLICY_NAME);
	}

	private List<VirtualDevice> getAllDisks(ManagedObjectReference vmRef)
			throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
		List<VirtualDevice> disks = new ArrayList<>();
		List<VirtualDevice> deviceList = ((ArrayOfVirtualDevice) VCUtil
				.getEntityProps(connection, vmRef, new String[] { "config.hardware.device" })
				.get("config.hardware.device")).getVirtualDevice();
		for (VirtualDevice device : deviceList) {
			if (device instanceof VirtualDisk) {
				disks.add(device);
			}
		}
		return disks;
	}

	private boolean isDiskEncrypted(VirtualDevice disk) {
		VirtualDeviceBackingInfo backing = disk.getBacking();
		if (!(backing instanceof VirtualDiskFlatVer2BackingInfo)) {
			return false;
		}
		return ((VirtualDiskFlatVer2BackingInfo) backing).getKeyId() != null;
	}

	private boolean isVMEncrypted(ManagedObjectReference vmRef) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
		CryptoKeyId keyId = (CryptoKeyId) VCUtil.getEntityProps(connection, vmRef, new String[] { "config.keyId" })
				.get("config.keyId");
		return keyId != null;
	}

	public void encryptVMandDisks(String vmPathname)
			throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, DuplicateNameFaultMsg, TaskInProgressFaultMsg,
			VmConfigFaultFaultMsg, InsufficientResourcesFaultFaultMsg, InvalidDatastoreFaultMsg, FileFaultFaultMsg,
			ConcurrentAccessFaultMsg, InvalidStateFaultMsg, InvalidNameFaultMsg, InvalidCollectorVersionFaultMsg,
			InvalidArgumentFaultMsg, com.vmware.pbm.RuntimeFaultFaultMsg, CryptoException {
		ManagedObjectReference vmRef = connection.getVimPort()
				.findByInventoryPath(connection.getVimServiceContent().getSearchIndex(), vmPathname);
		if (vmRef == null) {
			String msg = String.format("The VMPath specified [%s] is not found %n", vmPathname);
			System.out.println(msg);
			throw new CryptoException(msg);
		}
		encryptVMandDisks(vmRef);
	}

	public void encryptVMandDisks(ManagedObjectReference vmRef)
			throws InvalidPropertyFaultMsg, DuplicateNameFaultMsg, TaskInProgressFaultMsg, VmConfigFaultFaultMsg,
			InsufficientResourcesFaultFaultMsg, InvalidDatastoreFaultMsg, FileFaultFaultMsg, ConcurrentAccessFaultMsg,
			InvalidStateFaultMsg, InvalidNameFaultMsg, InvalidCollectorVersionFaultMsg, RuntimeFaultFaultMsg,
			InvalidArgumentFaultMsg, com.vmware.pbm.RuntimeFaultFaultMsg, CryptoException {

		VirtualMachineConfigSpec vmConfigSpec = new VirtualMachineConfigSpec();

		VirtualMachineProfileSpec encryptionProfile = getEncryptionProfileSpec();
		System.out.println("encrypption profile:" + encryptionProfile);

		System.out.println("generating keyid...");
		CryptoSpecEncrypt cryptoSpec = new CryptoSpecEncrypt();
		cryptoSpec.setCryptoKeyId(getKeyId());
		System.out.println("keyid generated");

		boolean needReconfig = false;

		if (isVMEncrypted(vmRef)) {
			System.out.println("vm configuration files already encrypted");
		} else {
			vmConfigSpec.setCrypto(cryptoSpec);
			vmConfigSpec.getVmProfile().add(encryptionProfile);
			needReconfig = true;
		}

		// disk
		for (VirtualDevice disk : getAllDisks(vmRef)) {
			if (!isDiskEncrypted(disk)) {
				VirtualDeviceConfigSpec diskSpec = new VirtualDeviceConfigSpec();
				diskSpec.setDevice(disk);
				diskSpec.setOperation(VirtualDeviceConfigSpecOperation.EDIT);
				// Add encryption profile to VirtualDeviceConfigSpec
				diskSpec.getProfile().add(encryptionProfile);
				VirtualDeviceConfigSpecBackingSpec backingSpec = new VirtualDeviceConfigSpecBackingSpec();
				backingSpec.setCrypto(cryptoSpec);
				diskSpec.setBacking(backingSpec);
				vmConfigSpec.getDeviceChange().add(diskSpec);
				needReconfig = true;
			}
		}

		if (needReconfig) {
			System.out.println("reconfiguring vm...");
			ManagedObjectReference taskMOR = connection.getVimPort().reconfigVMTask(vmRef, vmConfigSpec);

			if (getTaskResultAfterDone(taskMOR)) {
				System.out.println("VM has been successfully reconfigured (encrypted) ");
			} else {
				throw new CryptoException("Failure -: vm encryption");
			}
		} else {
			System.out.println("vm configuration files and all disks are already encrypted, nothing to do.");
		}
	}

	private CryptoKeyId getKeyId() throws RuntimeFaultFaultMsg, CryptoException {
		ServiceContent serviceContent = connection.getVimServiceContent();
		ManagedObjectReference cryptoManager = serviceContent.getCryptoManager();
		List<KmipClusterInfo> clusters = connection.getVimPort().listKmipServers(cryptoManager, 1);
		if (clusters == null || clusters.isEmpty()) {
			throw new CryptoException("no kmip server found, please ensure kmip is configured in vcenter.");
		}
		CryptoKeyResult keyResult = connection.getVimPort().generateKey(cryptoManager, clusters.get(0).getClusterId());
		CryptoKeyId keyId = keyResult.getKeyId(); // Generate new key for
													// encryption
        if (keyId == null || keyId.getKeyId().isEmpty()) {
            throw new CryptoException(
                    "Failed to generate keyId, please make sure kmip server is healthy and properly configured.");
        }
        System.out.println("keyId:" + keyId.getKeyId());
		return keyId;
	}

	public void connect(String vcurl, String ssourl, String username, String password) {
		Utils.trustAllHttpsCertificates();
		Connection connect = new BasicConnection();
		connect.setSsoUrl(ssourl);
		connect.setVcUrl(vcurl);
		connect.setUsername(username);
		connect.setPassword(password);
		this.setConnection(connect);
		this.start();
	}

	public void disconnect() {
		this.stop();
	}

	private boolean getTaskResultAfterDone(ManagedObjectReference task)
			throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InvalidCollectorVersionFaultMsg {

		boolean retVal = false;

		// info has a property - state for state of the task
		Object[] result = VCUtil.waitForTask(connection, task, new String[] { "info.state", "info.error" },
				new String[] { "state" },
				new Object[][] { new Object[] { TaskInfoState.SUCCESS, TaskInfoState.ERROR } });

		if (result[0].equals(TaskInfoState.SUCCESS)) {
			retVal = true;
		}
		if (result[1] instanceof LocalizedMethodFault) {
			throw new RuntimeException(((LocalizedMethodFault) result[1]).getLocalizedMessage());
		}
		return retVal;
	}

}
