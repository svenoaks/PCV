/*
* Conditions Of Use 
* 
* This software was developed by employees of the National Institute of
* Standards and Technology (NIST), an agency of the Federal Government.
* Pursuant to title 15 Untied States Code Section 105, works of NIST
* employees are not subject to copyright protection in the United States
* and are considered to be in the public domain.  As a result, a formal
* license is not needed to use the software.
* 
* This software is provided by NIST as a service and is expressly
* provided "AS IS."  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
* OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
* AND DATA ACCURACY.  NIST does not warrant or make any representations
* regarding the use of the software or the results thereof, including but
* not limited to the correctness, accuracy, reliability or usefulness of
* the software.
* 
* Permission to use this software is contingent upon your acceptance
* of the terms of this agreement
*  
* .
* 
*/
/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;
import java.util.Iterator;

import smp.javax.sip.header.*;

/**
 * List of Unsupported headers.
 * @version 1.2 $Revision: 1.5 $ $Date: 2007/10/23 17:34:51 $
 * @author M. Ranganathan
 */
public class UnsupportedList extends SIPHeaderList<Unsupported> {

	
	private static final long serialVersionUID = -4052610269407058661L;


	/** Default Constructor
	 */
	public UnsupportedList() {
		super(Unsupported.class, UnsupportedHeader.NAME);
	}
	
	
	public Object clone() {
		UnsupportedList retval = new UnsupportedList();		
		return retval.clonehlist(this.hlist);
	}
}