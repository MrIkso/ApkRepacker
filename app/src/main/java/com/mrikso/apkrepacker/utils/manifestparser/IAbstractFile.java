package com.mrikso.apkrepacker.utils.manifestparser;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A file.
 */
public interface IAbstractFile extends IAbstractResource {
	public static enum PreferredWriteMode {
		INPUTSTREAM, OUTPUTSTREAM;
	}

	/**
	 * Returns an {@link InputStream} object on the file content.
	 * 
	 * @throws StreamException
	 */
	InputStream getContents() throws StreamException;

	/**
	 * Sets the content of the file.
	 * 
	 * @param source
	 *            the content
	 * @throws StreamException
	 */
	void setContents(InputStream source) throws StreamException;

	/**
	 * Returns an {@link OutputStream} to write into the file.
	 * 
	 * @throws StreamException
	 */
	OutputStream getOutputStream() throws StreamException;

	/**
	 * Returns the preferred mode to write into the file.
	 */
	PreferredWriteMode getPreferredWriteMode();
}