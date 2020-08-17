package com.mrikso.apkrepacker.utils.manifestparser;
/**
 * Base representation of a file system resource.
 * <p/>
 * This somewhat limited interface is designed to let classes use file-system
 * resources, without having the manually handle either the standard Java file
 * or the Eclipse file API..
 */
public interface IAbstractResource {

	/**
	 * Returns the name of the resource.
	 */
	String getName();

	/**
	 * Returns the OS path of the folder location.
	 */
	String getOsLocation();

	/**
	 * Returns whether the resource actually exists.
	 */
	boolean exists();

	/**
	 * Returns the parent folder or null if there is no parent.
	 */
	IAbstractFolder getParentFolder();
}
