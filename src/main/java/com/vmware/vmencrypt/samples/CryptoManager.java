package com.vmware.vmencrypt.samples;

public class CryptoManager {
	
	public static CryptoService getCryptoService() {
		return new CryptoServiceImpl();
	}
	
}
