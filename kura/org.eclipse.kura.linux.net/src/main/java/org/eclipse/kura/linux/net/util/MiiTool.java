/**
 * Copyright (c) 2011, 2014 Eurotech and/or its affiliates
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Eurotech
 */
/*
* Copyright (c) 2011 Eurotech Inc. All rights reserved.
*/

package org.eclipse.kura.linux.net.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.kura.KuraErrorCode;
import org.eclipse.kura.KuraException;
import org.eclipse.kura.core.util.ProcessUtil;

/**
 * Defines mii-tool utility
 * 
 * @author ilya.binshtok
 *
 */
public class MiiTool implements LinkTool {
	
	private String ifaceName = null; 
	private boolean linkDetected = false;
	private int speed = 0; // in b/s
	private String duplex = null;
	
	/**
	 * MiiTool constructor
	 * 
	 * @param ifaceName - interface name as {@link String}
	 */
	public MiiTool (String ifaceName) {
		this.ifaceName = ifaceName;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.kura.util.net.service.ILinkTool#get()
	 */
	public boolean get () throws KuraException {
		Process proc = null;
		try {
			//start the process
			proc = ProcessUtil.exec("mii-tool " + this.ifaceName);
	
			//get the output
	        BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
	        String line = null;
	        boolean linkDetected = false;
	        int speed = 0;
	        while ((line = br.readLine()) != null) {			
	        	if(line.startsWith(this.ifaceName)) {
	        		if(line.indexOf("link ok") > -1) {
	        			linkDetected = true;
	        			try {
							String tmp = "";
							if(line.indexOf("negotiated")>-1) {
								tmp = line.substring(line.indexOf("negotiated") + 11);
							} else {
								tmp = line.substring(line.indexOf(",") + 2);
							}
							String speedTxt = tmp.substring(0, tmp.indexOf(","));
							if(speedTxt.compareTo("10baseT-HD")==0) {
								speed = 10000000;
							} else if(speedTxt.compareTo("10baseT-FD")==0) {
								speed = 10000000;
							} else if(speedTxt.compareTo("100baseTx-HD")==0) {
								speed = 100000000;
							} else if(speedTxt.compareTo("100baseTx-FD")==0) {
								speed = 100000000;
							} else {
								speed = -2;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
	        		} 
	        	} 
	        }
	        
	        this.speed = speed;
	        this.linkDetected = linkDetected;
	        
	        return (proc.waitFor() == 0)? true : false;
		} catch(IOException e) {
			throw new KuraException(KuraErrorCode.INTERNAL_ERROR, e);
		} catch (InterruptedException e) {
			throw new KuraException(KuraErrorCode.INTERNAL_ERROR, e);
		}
		finally {
			ProcessUtil.destroy(proc);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.kura.util.net.service.ILinkTool#getIfaceName()
	 */
	public String getIfaceName() {
		return this.ifaceName;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.kura.util.net.service.ILinkTool#isLinkDetected()
	 */
	public boolean isLinkDetected() {
		return this.linkDetected;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.kura.util.net.service.ILinkTool#getSpeed()
	 */
	public int getSpeed() {
		return this.speed;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.kura.util.net.service.ILinkTool#getDuplex()
	 */
	public String getDuplex() {
		return this.duplex;
	}
}
