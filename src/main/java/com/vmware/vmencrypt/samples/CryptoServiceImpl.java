package com.vmware.vmencrypt.samples;

public class CryptoServiceImpl implements CryptoService {
	@Override
	public void encryptVM(String vcurl, String ssourl, String username, String password, String vmpath)
			throws CryptoException {
		VMEncrypt vme = new VMEncrypt();
		try {
			vme.connect(vcurl, ssourl, username, password);
			vme.encryptVMandDisks(vmpath);
		} catch (Exception e) {
			throw new CryptoException(e);
		} finally {
			vme.disconnect();
		}
	}
}
