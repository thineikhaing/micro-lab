package org.tek.microlab.dms;

public interface DmsPort {
	public void sendFile(String filename, byte[] file, String type);
}
