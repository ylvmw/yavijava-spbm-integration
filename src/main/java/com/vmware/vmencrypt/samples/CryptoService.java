package com.vmware.vmencrypt.samples;

public interface CryptoService {
	/**
	 * This method encrypts a specified vm and all its disks
	 * 
	 *
	 * @param vcurl
	 *            can be found in vcenter by key: VirtualCenter.VimApiUrl
	 *            example: https://<vcenterhostname>/sdk
	 * @param ssourl
	 * 			  can be found in vcenter by key: config.vpxd.sso.admin.uri
	 * 			  example: https://<vcenterhostname>/sts/STSService/vsphere.local
	 * @param username
	 * 			  vcenter login username
	 * @param password
	 *  		  vcenter login password
	 * @param vmpath
	 *            vmpath of VM, can be found in MOB, example: datacenter1/vm/testvm1
	 * @return
	 * @throws CryptoException
	 */
	public void encryptVM(String vcurl, String ssourl, String username, String password, String vmpath) throws CryptoException;
}
