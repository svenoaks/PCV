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
/******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).      *
 ******************************************************************************/
package gov.nist.javax.sip;

import gov.nist.core.InternalErrorHandler;
import gov.nist.javax.sip.address.SipUri;
import gov.nist.javax.sip.header.Contact;
import gov.nist.javax.sip.header.Event;
import gov.nist.javax.sip.header.ReferTo;
import gov.nist.javax.sip.header.RetryAfter;
import gov.nist.javax.sip.header.Route;
import gov.nist.javax.sip.header.RouteList;
import gov.nist.javax.sip.header.Server;
import gov.nist.javax.sip.message.MessageFactoryImpl;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;
import gov.nist.javax.sip.stack.MessageChannel;
import gov.nist.javax.sip.stack.SIPClientTransaction;
import gov.nist.javax.sip.stack.SIPDialog;
import gov.nist.javax.sip.stack.SIPServerTransaction;
import gov.nist.javax.sip.stack.SIPTransaction;
import gov.nist.javax.sip.stack.ServerRequestInterface;
import gov.nist.javax.sip.stack.ServerResponseInterface;

import java.io.IOException;

import smp.javax.sip.ClientTransaction;
import smp.javax.sip.DialogState;
import smp.javax.sip.InvalidArgumentException;
import smp.javax.sip.RequestEvent;
import smp.javax.sip.ResponseEvent;
import smp.javax.sip.ServerTransaction;
import smp.javax.sip.SipException;
import smp.javax.sip.SipProvider;
import smp.javax.sip.TransactionState;
import smp.javax.sip.header.EventHeader;
import smp.javax.sip.header.ReferToHeader;
import smp.javax.sip.header.ServerHeader;
import smp.javax.sip.message.Request;
import smp.javax.sip.message.Response;

/*
 * Bug fix Contributions by Lamine Brahimi, Andreas Bystrom, Bill Roome, John Martin, Daniel
 * Machin Vasquez-Illa, Antonis Karydas, Joe Provino, Bruce Evangelder, Jeroen van Bemmel, Robert
 * S. Rosen.
 */
/**
 * An adapter class from the JAIN implementation objects to the NIST-SIP stack. The primary
 * purpose of this class is to do early rejection of bad messages and deliver meaningful messages
 * to the application. This class is essentially a Dialog filter. It checks for and rejects
 * requests and responses which may be filtered out because of sequence number, Dialog not found,
 * etc. Note that this is not part of the JAIN-SIP spec (it does not implement a JAIN-SIP
 * interface). This is part of the glue that ties together the NIST-SIP stack and event model with
 * the JAIN-SIP stack. This is strictly an implementation class.
 * 
 * @version 1.2 $Revision: 1.29 $ $Date: 2009/06/23 11:02:17 $
 * 
 * @author M. Ranganathan
 */
class DialogFilter implements ServerRequestInterface, ServerResponseInterface {

    protected SIPTransaction transactionChannel;

    protected ListeningPointImpl listeningPoint;

    private SipStackImpl sipStack;

    public DialogFilter(SipStackImpl sipStack) {
        this.sipStack = sipStack;

    }

    /**
     * Send back an error Response.
     * 
     * @param sipRequest
     * @param transaction
     */

    private void send500Response(SIPRequest sipRequest, SIPServerTransaction transaction) {
        if (sipStack.isLoggingEnabled())
            sipStack.getLogWriter().logDebug("Sending 500 response for out of sequence message");
        SIPResponse sipResponse = sipRequest.createResponse(Response.SERVER_INTERNAL_ERROR);
        sipResponse.setReasonPhrase("Request out of order");

        try {
            transaction.sendMessage(sipResponse);
            sipStack.removeTransaction(transaction);
            transaction.releaseSem();
        } catch (IOException ex) {

            transaction.raiseIOExceptionEvent();
            sipStack.removeTransaction(transaction);

        }
    }

    /**
     * Process a request. Check for various conditions in the dialog that can result in the
     * message being dropped. Possibly return errors for these conditions.
     * 
     * @exception SIPServerException is thrown when there is an error processing the request.
     */
    public void processRequest(SIPRequest sipRequest, MessageChannel incomingMessageChannel) {
        // Generate the wrapper JAIN-SIP object.
        if (sipStack.isLoggingEnabled())
            sipStack.getLogWriter().logDebug(
                    "PROCESSING INCOMING REQUEST " + sipRequest + " transactionChannel = "
                            + transactionChannel + " listening point = "
                            + listeningPoint.getIPAddress() + ":" + listeningPoint.getPort());
        if (listeningPoint == null) {
            if (sipStack.isLoggingEnabled())
                sipStack.getLogWriter().logDebug(
                        "Dropping message: No listening point registered!");
            return;
        }

        SipStackImpl sipStack = (SipStackImpl) transactionChannel.getSIPStack();

        SipProviderImpl sipProvider = listeningPoint.getProvider();
        if (sipProvider == null) {
            if (sipStack.isLoggingEnabled())
                sipStack.getLogWriter().logDebug("No provider - dropping !!");
            return;
        }

        if (sipStack == null)
            InternalErrorHandler.handleException("Egads! no sip stack!");

        // Look for the registered SIPListener for the message channel.

        SIPServerTransaction transaction = (SIPServerTransaction) this.transactionChannel;
        if (transaction != null) {
            if (sipStack.isLoggingEnabled())
                sipStack.getLogWriter().logDebug("transaction state = " + transaction.getState());
        }
        String dialogId = sipRequest.getDialogId(true);
        SIPDialog dialog = sipStack.getDialog(dialogId);
        /*
         * Check if we got this request on the contact address of the dialog If not the dialog
         * does not belong to this request. We check this condition if a contact address has been
         * assigned to the dialog. Forgive the sins of B2BUA's that like to record route ACK's
         */
        if (dialog != null && sipProvider != dialog.getSipProvider()) {
            Contact contact = dialog.getMyContactHeader();
            if (contact != null) {
                SipUri contactUri = (SipUri) (contact.getAddress().getURI());
                String ipAddress = contactUri.getHost();
                int contactPort = contactUri.getPort();
                String contactTransport = contactUri.getTransportParam();
                if (contactTransport == null)
                    contactTransport = "udp";
                if (contactPort == -1) {
                    if (contactTransport.equals("udp") || contactTransport.equals("tcp"))
                        contactPort = 5060;
                    else
                        contactPort = 5061;
                }
                // Check if the dialog contact is the same as the provider on
                // which we got the request. Otherwise, dont assign this
                // dialog to the request.
                if (ipAddress != null
                        && (!ipAddress.equals(listeningPoint.getIPAddress()) || contactPort != listeningPoint
                                .getPort())) {
                    if (sipStack.isLoggingEnabled()) {
                        sipStack.getLogWriter().logDebug(
                                "nulling dialog -- listening point mismatch!  " + contactPort
                                        + "  lp port = " + listeningPoint.getPort());

                    }
                    dialog = null;
                }

            }
        }

        /*
         * RFC 3261 8.2.2.2 Merged requests: If the request has no tag in the To header field, the
         * UAS core MUST check the request against ongoing transactions. If the From tag, Call-ID,
         * and CSeq exactly match those associated with an ongoing transaction, but the request
         * does not match that transaction (based on the matching rules in Section 17.2.3), the
         * UAS core SHOULD generate a 482 (Loop Detected) response and pass it to the server
         * transaction. This support is only enabled when the stack has been instructed to
         * function with Automatic Dialog Support. Otherwise the application is in charge of
         * sending the response. TODO -- JAVADOC documentation explaining this behavior.
         */
        if (sipProvider.isAutomaticDialogSupportEnabled() && sipRequest.getToTag() == null) {
            SIPServerTransaction sipServerTransaction = sipStack
                    .findMergedTransaction(sipRequest);
            if (sipServerTransaction != null
                    && !sipServerTransaction.isMessagePartOfTransaction(sipRequest) &&
                    sipServerTransaction.getState() != TransactionState.TERMINATED ) {
                SIPResponse response = sipRequest.createResponse(Response.LOOP_DETECTED);

                if (sipStack.getLogWriter().isLoggingEnabled())
                    sipStack.getLogWriter().logError("Loop detected while processing request");
                try {
                    sipProvider.sendResponse(response);
                } catch (SipException e) {
                    if (sipStack.getLogWriter().isLoggingEnabled())
                        sipStack.getLogWriter().logError("Error sending response");
                }
                return;
            }
        }

        if (sipStack.isLoggingEnabled()) {
            sipStack.getLogWriter().logDebug("dialogId = " + dialogId);
            sipStack.getLogWriter().logDebug("dialog = " + dialog);
        }

        /*
         * RFC 3261 Section 16.4 If the first value in the Route header field indicates this
         * proxy,the proxy MUST remove that value from the request .
         */

        // If the message is being processed
        // by a Proxy, then the proxy will take care of stripping the
        // Route header. If the request is being processed by an
        // endpoint, then the stack strips off the route header.
        if (sipRequest.getHeader(Route.NAME) != null && transaction.getDialog() != null) {
            RouteList routes = sipRequest.getRouteHeaders();
            Route route = (Route) routes.getFirst();
            SipUri uri = (SipUri) route.getAddress().getURI();
            int port;
            if (uri.getHostPort().hasPort()) {
                port = uri.getHostPort().getPort();
            } else {
                if (listeningPoint.getTransport().equalsIgnoreCase("TLS"))
                    port = 5061;
                else
                    port = 5060;
            }
            String host = uri.getHost();
            if ((host.equals(listeningPoint.getIPAddress()) || host
                    .equalsIgnoreCase(listeningPoint.getSentBy()))
                    && port == listeningPoint.getPort()) {
                if (routes.size() == 1)
                    sipRequest.removeHeader(Route.NAME);
                else
                    routes.removeFirst();
            }
        }

        if (sipRequest.getMethod().equals(Request.REFER)) {
            /*
             * An agent responding to a REFER method MUST return a 400 (Bad Request) if the
             * request contained zero or more than one Refer-To header field values.
             */
            ReferToHeader sipHeader = (ReferToHeader) sipRequest.getHeader(ReferTo.NAME);
            if (sipHeader == null && dialog != null) {
                SIPResponse badRequest = sipRequest.createResponse(Response.BAD_REQUEST);
                badRequest.setReasonPhrase("Refer-To header missing");

                try {
                    sipProvider.sendResponse(badRequest);
                } catch (SipException e) {
                    sipStack.getLogWriter().logError("error sending response", e);
                }
                if (transaction != null) {
                    sipStack.removeTransaction(transaction);
                    transaction.releaseSem();
                }
                return;
            }

        } else if (sipRequest.getMethod().equals(Request.UPDATE)) {
            /*
             * Got an UPDATE method and the user dialog does not exist and the user wants to be a
             * User agent.
             * 
             */
            if (sipProvider.isAutomaticDialogSupportEnabled() && dialog == null) {
                Response notExist = sipRequest
                        .createResponse(Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST);
                try {
                    sipProvider.sendResponse(notExist);
                } catch (SipException e) {
                    sipStack.getLogWriter().logError("error sending response", e);
                }
                if (transaction != null) {
                    sipStack.removeTransaction(transaction);
                    transaction.releaseSem();
                }
                return;
            }
        } else if (sipRequest.getMethod().equals(Request.ACK)) {

            if (transaction != null && transaction.isInviteTransaction()) {
                // This is an ack for a 3xx-6xx response. Just let the tx laer
                // take care of it.
                if (sipStack.isLoggingEnabled())
                    sipStack.getLogWriter().logDebug("Processing ACK for INVITE Tx ");

            } else {
                if (sipStack.isLoggingEnabled())
                    sipStack.getLogWriter().logDebug("Processing ACK for dialog " + dialog);

                if (dialog == null) {
                    if (sipStack.isLoggingEnabled()) {
                        sipStack.getLogWriter().logDebug(
                                "Dialog does not exist " + sipRequest.getFirstLine()
                                        + " isServerTransaction = " + true);

                    }
                    SIPServerTransaction st = sipStack
                            .getRetransmissionAlertTransaction(dialogId);
                    if (st != null && st.isRetransmissionAlertEnabled()) {
                        st.disableRetransmissionAlerts();

                    }
                    /*
                     * JvB: must never drop ACKs that dont match a transaction! One cannot be sure
                     * if it isn't an ACK for a 2xx response
                     * 
                     */

                } else {
                    if (!dialog.handleAck(transaction)) {
                    	if(sipStack.isLooseDialogValidation()) {
                            if (sipStack.isLoggingEnabled()) {
                                sipStack.getLogWriter().logDebug(
                                        "Dialog exists with loose dialog validation " + sipRequest.getFirstLine()
                                                + " isServerTransaction = " + true + " dialog = " + dialog.getDialogId());

                            }
                            SIPServerTransaction st = sipStack
                                    .getRetransmissionAlertTransaction(dialogId);
                            if (st != null && st.isRetransmissionAlertEnabled()) {
                                st.disableRetransmissionAlerts();

                            }
                    	} else {
                    		return;
                    	}
                    } else {
                        transaction.passToListener();
                        dialog.addTransaction(transaction);
                        dialog.addRoute(sipRequest);
                        transaction.setDialog(dialog, dialogId);
                        if (sipStack.isDialogCreated(sipRequest.getMethod())) {
                            sipStack.putInMergeTable(transaction, sipRequest);
                        }
                        /*
                         * Note that ACK is a pseudo transaction. It is never added to the stack
                         * and you do not get transaction terminated events on ACK.
                         */

                        if (sipStack.deliverTerminatedEventForAck) {
                            try {
                                sipStack.addTransaction(transaction);
                                transaction.scheduleAckRemoval();
                            } catch (IOException ex) {

                            }
                        } else {
                            transaction.setMapped(true);
                        }

                        /*
                         * try { sipStack.addTransaction(transaction); } catch (IOException ex) { //
                         * should never happen. }
                         */

                    }
                }
            }
        } else if (sipRequest.getMethod().equals(Request.PRACK)) {

            /*
             * RFC 3262: A matching PRACK is defined as one within the same dialog as the
             * response, and whose method, CSeq-num, and response-num in the RAck header field
             * match, respectively, the method from the CSeq, the sequence number from the CSeq,
             * and the sequence number from the RSeq of the reliable provisional response.
             */

            if (sipStack.isLoggingEnabled())
                sipStack.getLogWriter().logDebug("Processing PRACK for dialog " + dialog);

            if (dialog == null && sipProvider.isAutomaticDialogSupportEnabled()) {
                if (sipStack.isLoggingEnabled()) {
                    sipStack.getLogWriter().logDebug(
                            "Dialog does not exist " + sipRequest.getFirstLine()
                                    + " isServerTransaction = " + true);

                }
                if (sipStack.isLoggingEnabled()) {
                    sipStack
                            .getLogWriter()
                            .logDebug(
                                    "Sending 481 for PRACK - automatic dialog support is enabled -- cant find dialog!");
                }
                SIPResponse notExist = sipRequest
                        .createResponse(Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST);

                try {
                    sipProvider.sendResponse(notExist);
                } catch (SipException e) {
                    sipStack.getLogWriter().logError("error sending response", e);
                }
                if (transaction != null) {
                    sipStack.removeTransaction(transaction);
                    transaction.releaseSem();
                }
                return;

            } else if (dialog != null) {
                if (!dialog.handlePrack(sipRequest)) {
                    sipStack.getLogWriter().logDebug("Dropping out of sequence PRACK ");
                    if (transaction != null) {
                        sipStack.removeTransaction(transaction);
                        transaction.releaseSem();
                    }
                    return;
                } else {
                    try {
                        sipStack.addTransaction(transaction);
                        dialog.addTransaction(transaction);
                        dialog.addRoute(sipRequest);
                        transaction.setDialog(dialog, dialogId);
                    } catch (Exception ex) {
                        InternalErrorHandler.handleException(ex);
                    }
                }
            } else {
                sipStack.getLogWriter().logDebug(
                        "Processing PRACK without a DIALOG -- this must be a proxy element");
            }

        } else if (sipRequest.getMethod().equals(Request.BYE)) {
            // Check for correct sequence numbering of the BYE
            if (dialog != null && !dialog.isRequestConsumable(sipRequest)) {
                if (sipStack.isLoggingEnabled())
                    sipStack.getLogWriter().logDebug(
                            "Dropping out of sequence BYE " + dialog.getRemoteSeqNumber() + " "
                                    + sipRequest.getCSeq().getSeqNumber());

                if (dialog.getRemoteSeqNumber() >= sipRequest.getCSeq().getSeqNumber()
                        && transaction.getState() == TransactionState.TRYING) {

                    this.send500Response(sipRequest, transaction);

                }
                // If the stack knows about the tx, then remove it.
                if (transaction != null)
                    sipStack.removeTransaction(transaction);
                return;

            } else if (dialog == null && sipProvider.isAutomaticDialogSupportEnabled()) {
                // Drop bye's with 481 if dialog does not exist.
                // If dialog support is enabled then
                // there must be a dialog associated with the bye
                // No dialog could be found and requests on this
                // provider. Must act like a user agent -- so drop the request.
                // NOTE: if Automatic dialog support is not enabled,
                // then it is the application's responsibility to
                // take care of this error condition possibly.

                SIPResponse response = sipRequest
                        .createResponse(Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST);
                response.setReasonPhrase("Dialog Not Found");

                sipStack.getLogWriter().logDebug(
                        "dropping request -- automatic dialog "
                                + "support enabled and dialog does not exist!");
                try {
                    transaction.sendResponse(response);
                } catch (SipException ex) {
                    sipStack.getLogWriter().logError("Error in sending response", ex);
                }
                // If the stack knows about the tx, then remove it.
                if (transaction != null) {
                    sipStack.removeTransaction(transaction);
                    transaction.releaseSem();
                    transaction = null;
                }
                return;

            }

            // note that the transaction may be null (which
            // happens when no dialog for the bye was found.
            // and automatic dialog support is disabled (i.e. the app wants
            // to manage its own dialog layer.
            if (transaction != null && dialog != null) {
                try {
                    if (sipProvider == dialog.getSipProvider()) {
                        sipStack.addTransaction(transaction);
                        dialog.addTransaction(transaction);
                        transaction.setDialog(dialog, dialogId);
                    }

                } catch (IOException ex) {
                    InternalErrorHandler.handleException(ex);
                }
            }
            if (sipStack.getLogWriter().isLoggingEnabled()) {
                sipStack.getLogWriter().logDebug(
                        "BYE Tx = " + transaction + " isMapped ="
                                + transaction.isTransactionMapped());
            }

        } else if (sipRequest.getMethod().equals(Request.CANCEL)) {

            SIPServerTransaction st = (SIPServerTransaction) sipStack.findCancelTransaction(
                    sipRequest, true);
            if (sipStack.getLogWriter().isLoggingEnabled()) {
                sipStack.getLogWriter().logDebug(
                        "Got a CANCEL, InviteServerTx = " + st + " cancel Server Tx ID = "
                                + transaction + " isMapped = "
                                + transaction.isTransactionMapped());

            }
            // Processing incoming CANCEL.
            // Check if we can process the CANCEL request.
            if (sipRequest.getMethod().equals(Request.CANCEL)) {
                // If the CANCEL comes in too late, there's not
                // much that the Listener can do so just do the
                // default action and avoid bothering the listener.
                if (st != null && st.getState() == SIPTransaction.TERMINATED_STATE) {
                    // If transaction already exists but it is
                    // too late to cancel the transaction then
                    // just respond OK to the CANCEL and bail.
                    if (sipStack.isLoggingEnabled())
                        sipStack.getLogWriter().logDebug("Too late to cancel Transaction");
                    // send OK and just ignore the CANCEL.
                    try {

                        transaction.sendResponse(sipRequest.createResponse(Response.OK));
                    } catch (Exception ex) {
                        if (ex.getCause() != null && ex.getCause() instanceof IOException) {
                            st.raiseIOExceptionEvent();
                        }
                    }
                    return;
                }
                if (sipStack.isLoggingEnabled())
                    sipStack.getLogWriter().logDebug("Cancel transaction = " + st);

            }
            if (transaction != null && st != null && st.getDialog() != null) {
                // Found an invite tx corresponding to the CANCEL.
                // Set up the client tx and pass up to listener.
                transaction.setDialog((SIPDialog) st.getDialog(), dialogId);
                dialog = (SIPDialog) st.getDialog();
            } else if (st == null && sipProvider.isAutomaticDialogSupportEnabled()
                    && transaction != null) {
                // Could not find a invite tx corresponding to the CANCEL.
                // Automatic dialog support is enabled so I must behave like
                // an endpoint on this provider.
                // Send the error response for the cancel.

                SIPResponse response = sipRequest
                        .createResponse(Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST);
                if (sipStack.isLoggingEnabled()) {
                    sipStack.getLogWriter().logDebug(
                            "dropping request -- automatic dialog support "
                                    + "enabled and INVITE ST does not exist!");
                }
                try {
                    sipProvider.sendResponse(response);
                } catch (SipException ex) {
                    InternalErrorHandler.handleException(ex);
                }
                if (transaction != null) {
                    sipStack.removeTransaction(transaction);
                    transaction.releaseSem();
                }
                return;

            }

            // INVITE was handled statefully so the CANCEL must also be
            // statefully handled.
            if (st != null) {
                try {
                    if (transaction != null) {
                        sipStack.addTransaction(transaction);
                        transaction.setPassToListener();
                        transaction.setInviteTransaction(st);
                        // Dont let the INVITE and CANCEL be concurrently
                        // processed.
                        st.acquireSem();

                    }

                } catch (Exception ex) {
                    InternalErrorHandler.handleException(ex);
                }
            }
        } else if (sipRequest.getMethod().equals(Request.INVITE)) {
            SIPTransaction lastTransaction = dialog == null ? null : dialog
                    .getInviteTransaction();

            /*
             * RFC 3261 Chapter 14. A UAS that receives a second INVITE before it sends the final
             * response to a first INVITE with a lower CSeq sequence number on the same dialog
             * MUST return a 500 (Server Internal Error) response to the second INVITE and MUST
             * include a Retry-After header field with a randomly chosen value of between 0 and 10
             * seconds.
             */

            if (dialog != null && transaction != null && lastTransaction != null
                    && sipRequest.getCSeq().getSeqNumber() > dialog.getRemoteSeqNumber()
                    && lastTransaction instanceof SIPServerTransaction
                    && lastTransaction.isInviteTransaction()
                    && lastTransaction.getState() != TransactionState.COMPLETED
                    && lastTransaction.getState() != TransactionState.TERMINATED
                    && lastTransaction.getState() != TransactionState.CONFIRMED) {

                if (sipStack.isLoggingEnabled())
                    sipStack.getLogWriter().logDebug(
                            "Sending 500 response for out of sequence message");
                SIPResponse sipResponse = sipRequest
                        .createResponse(Response.SERVER_INTERNAL_ERROR);

                RetryAfter retryAfter = new RetryAfter();
                try {
                    retryAfter.setRetryAfter((int) (10 * Math.random()));
                } catch (InvalidArgumentException ex) {
                    ex.printStackTrace();
                }
                sipResponse.addHeader(retryAfter);
                try {
                    transaction.sendMessage(sipResponse);
                } catch (IOException ex) {
                    transaction.raiseIOExceptionEvent();
                }
                return;
            }

            /*
             * RFC 3261 Chapter 14. A UAS that receives an INVITE on a dialog while an INVITE it
             * had sent on that dialog is in progress MUST return a 491 (Request Pending) response
             * to the received INVITE.
             */

            lastTransaction = (dialog == null ? null : dialog.getLastTransaction());

            if (dialog != null && lastTransaction != null
                    && lastTransaction.isInviteTransaction()
                    && lastTransaction instanceof SIPClientTransaction
                    && lastTransaction.getState() != TransactionState.COMPLETED
                    && lastTransaction.getState() != TransactionState.TERMINATED) {

                if (dialog.getRemoteSeqNumber() + 1 == sipRequest.getCSeq().getSeqNumber()) {
                    dialog.setRemoteSequenceNumber(sipRequest.getCSeq().getSeqNumber());
                    if (sipStack.isLoggingEnabled())
                        sipStack.getLogWriter().logDebug(
                                "Sending 491 response for out of sequence message");
                    SIPResponse sipResponse = sipRequest.createResponse(Response.REQUEST_PENDING);
                    try {
                        transaction.sendMessage(sipResponse);
                    } catch (IOException ex) {
                        transaction.raiseIOExceptionEvent();
                    }
                    dialog.requestConsumed();
                } else {
                    if (sipStack.isLoggingEnabled())
                        sipStack.getLogWriter().logDebug(
                                "Dropping message -- sequence number is too high!");
                }
                return;
            }
        }

        // Sequence numbers are supposed to be incremented
        // sequentially within a dialog for RFC 3261
        // Note BYE, CANCEL and ACK is handled above - so no check here.

        sipStack.getLogWriter().logDebug(
                "CHECK FOR OUT OF SEQ MESSAGE " + dialog + " transaction " + transaction);

        if (dialog != null && transaction != null && !sipRequest.getMethod().equals(Request.BYE)
                && !sipRequest.getMethod().equals(Request.CANCEL)
                && !sipRequest.getMethod().equals(Request.ACK)
                && !sipRequest.getMethod().equals(Request.PRACK)) {

            if (!dialog.isRequestConsumable(sipRequest)) {

                /*
                 * RFC 3261: "UAS Behavior" section (12.2.2): If the remote sequence number was
                 * not empty, but the sequence number of the request is lower than the remote
                 * sequence number, the request is out of order and MUST be rejected with a 500
                 * (Server Internal Error) response.
                 */

                // Drop the request
                if (sipStack.isLoggingEnabled()) {
                    sipStack.getLogWriter().logDebug(
                            "Dropping out of sequence message " + dialog.getRemoteSeqNumber()
                                    + " " + sipRequest.getCSeq());
                }

                // send error when stricly higher, ignore when ==
                // (likely still processing, error would interrupt that)
                if (dialog.getRemoteSeqNumber() >= sipRequest.getCSeq().getSeqNumber()
                        && (transaction.getState() == TransactionState.TRYING || transaction
                                .getState() == TransactionState.PROCEEDING)) {

                    this.send500Response(sipRequest, transaction);

                }
                return;
            }

            try {
                if (sipProvider == dialog.getSipProvider()) {
                    sipStack.addTransaction(transaction);
                    // This will set the remote sequence number.
                    dialog.addTransaction(transaction);
                    dialog.addRoute(sipRequest);
                    transaction.setDialog(dialog, dialogId);

                }
            } catch (IOException ex) {
                transaction.raiseIOExceptionEvent();
                sipStack.removeTransaction(transaction);
                return;
            }

        }

        RequestEvent sipEvent;

        if (sipStack.getLogWriter().isLoggingEnabled()) {

            sipStack.getLogWriter().logDebug(
                    sipRequest.getMethod() + " transaction.isMapped = "
                            + transaction.isTransactionMapped());
        }

        /*
         * RFC 3265: Each event package MUST specify whether forked SUBSCRIBE requests are allowed
         * to install multiple subscriptions. If such behavior is not allowed, the first potential
         * dialog- establishing message will create a dialog. All subsequent NOTIFY messages which
         * correspond to the SUBSCRIBE message (i.e., match "To", "From", "From" header "tag"
         * parameter, "Call-ID", "CSeq", "Event", and "Event" header "id" parameter) but which do
         * not match the dialog would be rejected with a 481 response. Note that the 200-class
         * response to the SUBSCRIBE can arrive after a matching NOTIFY has been received; such
         * responses might not correlate to the same dialog established by the NOTIFY. Except as
         * required to complete the SUBSCRIBE transaction, such non-matching 200-class responses
         * are ignored.
         */

        if (dialog == null && sipRequest.getMethod().equals(Request.NOTIFY)) {

            SIPClientTransaction pendingSubscribeClientTx = sipStack.findSubscribeTransaction(
                    sipRequest, listeningPoint);

            if (sipStack.getLogWriter().isLoggingEnabled()) {
                sipStack.getLogWriter().logDebug(
                        "PROCESSING NOTIFY  DIALOG == null " + pendingSubscribeClientTx);
            }

            /*
             * RFC 3265: Upon receiving a NOTIFY request, the subscriber should check that it
             * matches at least one of its outstanding subscriptions; if not, it MUST return a
             * "481 Subscription does not exist" response unless another 400- or 500-class
             * response is more appropriate.
             */
            if (sipProvider.isAutomaticDialogSupportEnabled() && pendingSubscribeClientTx == null
                    && !sipStack.deliverUnsolicitedNotify) {
                /*
                 * This is the case of the UAC receiving a Stray NOTIFY for which it has not
                 * previously sent out a SUBSCRIBE and for which it does not have an established
                 * dialog.
                 */
                try {
                    if (sipStack.isLoggingEnabled()) {
                        sipStack.getLogWriter().logDebug(
                                "Could not find Subscription for Notify Tx.");
                    }
                    Response errorResponse = sipRequest
                            .createResponse(Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST);
                    errorResponse.setReasonPhrase("Subscription does not exist");
                    sipProvider.sendResponse(errorResponse);
                    return;

                } catch (Exception ex) {
                    sipStack.getLogWriter().logError(
                            "Exception while sending error response statelessly", ex);
                    return;
                }

            }

            // If the server transaction cannot be found or if it
            // aleady has a dialog attached to it then just assign the
            // notify to this dialog and pass it up.
            if (pendingSubscribeClientTx != null) {
                // The response to the pending subscribe tx can try to create
                // a dialog at the same time that the notify is trying to
                // create a dialog. Thus we cannot process both at the
                // same time.

                transaction.setPendingSubscribe(pendingSubscribeClientTx);
                // The transaction gets assigned to the dialog from the
                // outgoing subscribe. First see if anybody claimed the
                // default Dialog for the outgoing Subscribe request.
                SIPDialog subscriptionDialog = (SIPDialog) pendingSubscribeClientTx
                        .getDefaultDialog();

                // TODO -- refactor this. Can probably be written far cleaner.
                if (subscriptionDialog == null || subscriptionDialog.getDialogId() == null
                        || !subscriptionDialog.getDialogId().equals(dialogId)) {
                    // Notify came in before you could assign a response to
                    // the subscribe.
                    // grab the default dialog and assign it to the tags in
                    // the notify.
                    if (subscriptionDialog != null && subscriptionDialog.getDialogId() == null) {
                        subscriptionDialog.setDialogId(dialogId);

                    } else {
                        subscriptionDialog = pendingSubscribeClientTx.getDialog(dialogId);
                    }
                    if (sipStack.getLogWriter().isLoggingEnabled()) {
                        sipStack.getLogWriter().logDebug(
                                "PROCESSING NOTIFY Subscribe DIALOG " + subscriptionDialog);
                    }

                    // The user could have createed a dialog before sending out
                    // the SUBSCRIBE on the subscribe tx.
                    if (subscriptionDialog == null
                            && (sipProvider.isAutomaticDialogSupportEnabled() || pendingSubscribeClientTx
                                    .getDefaultDialog() != null)) {
                        Event event = (Event) sipRequest.getHeader(EventHeader.NAME);
                        if (sipStack.isEventForked(event.getEventType())) {

                            subscriptionDialog = SIPDialog.createFromNOTIFY(
                                    pendingSubscribeClientTx, transaction);

                        }

                    }
                    if (subscriptionDialog != null) {
                        transaction.setDialog(subscriptionDialog, dialogId);
                        subscriptionDialog.setState(DialogState.CONFIRMED.getValue());
                        sipStack.putDialog(subscriptionDialog);
                        pendingSubscribeClientTx.setDialog(subscriptionDialog, dialogId);
                        if (!transaction.isTransactionMapped()) {
                            this.sipStack.mapTransaction(transaction);
                            // Let the listener see it if it just got
                            // created.
                            // otherwise, we have already processed the tx
                            // so
                            // we dont want the listener to see it.
                            transaction.setPassToListener();
                            try {
                                this.sipStack.addTransaction(transaction);
                            } catch (Exception ex) {
                            }
                        }
                    }
                } else {
                    // The subscription default dialog is our dialog.
                    // Found a subscrbe dialog for the NOTIFY
                    // So map the tx.
                    transaction.setDialog(subscriptionDialog, dialogId);
                    dialog = subscriptionDialog;
                    if (!transaction.isTransactionMapped()) {
                        this.sipStack.mapTransaction(transaction);
                        // Let the listener see it if it just got created.
                        // otherwise, we have already processed the tx so
                        // we dont want the listener to see it.
                        transaction.setPassToListener();
                        try {
                            this.sipStack.addTransaction(transaction);
                        } catch (Exception ex) {
                        }
                    }
                    sipStack.putDialog(subscriptionDialog);
                    if (pendingSubscribeClientTx != null) {
                        subscriptionDialog.addTransaction(pendingSubscribeClientTx);
                        pendingSubscribeClientTx.setDialog(subscriptionDialog, dialogId);

                    }
                }
                if (transaction != null
                        && ((SIPServerTransaction) transaction).isTransactionMapped()) {
                    // Shadow transaction has been created and the stack
                    // knows
                    // about it.
                    sipEvent = new RequestEvent((SipProvider) sipProvider,
                            (ServerTransaction) transaction, subscriptionDialog,
                            (Request) sipRequest);
                } else {
                    // Shadow transaction has been created but the stack
                    // does
                    // not know
                    // about it.
                    sipEvent = new RequestEvent((SipProvider) sipProvider, null,
                            subscriptionDialog, (Request) sipRequest);
                }

            } else {
                if (sipStack.getLogWriter().isLoggingEnabled()) {
                    sipStack.getLogWriter().logDebug("could not find subscribe tx");
                }

                // Got a notify out of the blue - just pass it up
                // for stateless handling by the application.
                sipEvent = new RequestEvent(sipProvider, null, null, (Request) sipRequest);
            }

        } else {

            // For a dialog creating event - set the transaction to null.
            // The listener can create the dialog if needed.
            if (transaction != null
                    && (((SIPServerTransaction) transaction).isTransactionMapped())) {
                sipEvent = new RequestEvent(sipProvider, (ServerTransaction) transaction, dialog,
                        (Request) sipRequest);
            } else {
                sipEvent = new RequestEvent(sipProvider, null, dialog, (Request) sipRequest);
            }
        }
        sipProvider.handleEvent(sipEvent, transaction);

    }

    /**
     * Process the response.
     * 
     * @exception SIPServerException is thrown when there is an error processing the response
     * @param incomingMessageChannel -- message channel on which the response is received.
     */
    public void processResponse(SIPResponse response, MessageChannel incomingMessageChannel,
            SIPDialog dialog) {
        if (sipStack.isLoggingEnabled()) {
            sipStack.getLogWriter().logDebug(
                    "PROCESSING INCOMING RESPONSE" + response.encodeMessage());
        }
        if (listeningPoint == null) {
            if (sipStack.isLoggingEnabled())
                sipStack.getLogWriter().logError(
                        "Dropping message: No listening point" + " registered!");
            return;
        }

        SipProviderImpl sipProvider = listeningPoint.getProvider();
        if (sipProvider == null) {
            if (sipStack.isLoggingEnabled()) {
                sipStack.getLogWriter().logError("Dropping message:  no provider");
            }
            return;
        }
        if (sipProvider.sipListener == null) {
            if (sipStack.isLoggingEnabled()) {
                sipStack.getLogWriter().logError("No listener -- dropping response!");
            }
            return;
        }

        SIPClientTransaction transaction = (SIPClientTransaction) this.transactionChannel;
        SipStackImpl sipStackImpl = sipProvider.sipStack;

        if (sipStack.isLoggingEnabled())
            sipStackImpl.getLogWriter().logDebug("Transaction = " + transaction);

        if (transaction == null) {
            // Transaction is null but the dialog is not null. This means that
            // the transaction has been removed by the stack.
            // If the dialog exists, then it may need to retransmit ACK so
            // we cannot drop the response.
            if (dialog != null) {
                if (response.getStatusCode() / 100 != 2) {
                    if (sipStack.isLoggingEnabled()) {
                        sipStack
                                .getLogWriter()
                                .logDebug(
                                        "Response is not a final response and dialog is found for response -- dropping response!");
                    }
                    return;
                } else if (dialog.getState() == DialogState.TERMINATED) {
                    if (sipStack.isLoggingEnabled()) {
                        sipStack.getLogWriter().logDebug(
                                "Dialog is terminated -- dropping response!");
                    }
                    return;
                } else {
                    boolean ackAlreadySent = false;
                    if (dialog.isAckSeen() && dialog.getLastAck() != null) {
                        if (dialog.getLastAck().getCSeq().getSeqNumber() == response.getCSeq()
                                .getSeqNumber()) {
                            // the last ack sent corresponded to this 200
                            ackAlreadySent = true;
                        }
                    }
                    // 200 retransmission for the final response.
                    if (ackAlreadySent
                            && response.getCSeq().getMethod().equals(dialog.getMethod())) {
                        try {
                            // Found the dialog - resend the ACK and
                            // dont pass up the null transaction
                            if (sipStack.isLoggingEnabled()) {
                                sipStack.getLogWriter().logDebug(
                                        "Retransmission of OK detected: Resending last ACK");
                            }
                            dialog.resendAck();
                            return;
                        } catch (SipException ex) {
                            // What to do here ?? kill the dialog?
                            sipStack.getLogWriter().logError("could not resend ack", ex);
                        }
                    }
                }
            }

            if (sipStack.isLoggingEnabled()) {
                sipStack.getLogWriter().logDebug(
                        "could not find tx, handling statelessly Dialog = 	" + dialog);
            }
            // Pass the response up to the application layer to handle
            // statelessly.

            ResponseEvent sipEvent = new ResponseEvent(sipProvider, transaction, dialog,
                    (Response) response);

            sipProvider.handleEvent(sipEvent, transaction);
            return;
        }

        ResponseEvent responseEvent = null;

        // Here if there is an assigned dialog
        responseEvent = new smp.javax.sip.ResponseEvent(sipProvider, (ClientTransaction) transaction,
                dialog, (Response) response);
        // Set the Dialog for the response.
        if (dialog != null && response.getStatusCode() != 100) {
            // set the last response for the dialog.
            dialog.setLastResponse(transaction, response);
            transaction.setDialog(dialog, dialog.getDialogId());
        }

        sipProvider.handleEvent(responseEvent, transaction);

    }

    /**
     * Just a placeholder. This is called from the stack for message logging. Auxiliary processing
     * information can be passed back to be written into the log file.
     * 
     * @return auxiliary information that we may have generated during the message processing
     *         which is retrieved by the message logger.
     */
    public String getProcessingInfo() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nist.javax.sip.stack.ServerResponseInterface#processResponse(gov.nist.javax.sip.message.SIPResponse,
     *      gov.nist.javax.sip.stack.MessageChannel)
     */
    public void processResponse(SIPResponse sipResponse, MessageChannel incomingChannel) {
        String dialogID = sipResponse.getDialogId(false);
        SIPDialog sipDialog = this.sipStack.getDialog(dialogID);

        String method = sipResponse.getCSeq().getMethod();
        if (sipStack.isLoggingEnabled()) {
            sipStack.getLogWriter().logDebug(
                    "PROCESSING INCOMING RESPONSE: " + sipResponse.encodeMessage());
        }

        if (listeningPoint == null) {
            if (sipStack.isLoggingEnabled())
                sipStack.getLogWriter().logDebug(
                        "Dropping message: No listening point" + " registered!");
            return;
        }

        SipProviderImpl sipProvider = listeningPoint.getProvider();
        if (sipProvider == null) {
            if (sipStack.isLoggingEnabled()) {
                sipStack.getLogWriter().logDebug("Dropping message:  no provider");
            }
            return;
        }

        if (sipProvider.sipListener == null) {
            if (sipStack.isLoggingEnabled()) {
                sipStack.getLogWriter().logDebug("Dropping message:  no sipListener registered!");
            }
            return;
        }

        SIPClientTransaction transaction = (SIPClientTransaction) this.transactionChannel;
        // This may be a dialog creating method for which the ACK has not yet
        // been sent
        // but the dialog has already been assigned ( happens this way for
        // 3PCC).
        if (sipDialog == null && transaction != null) {
            sipDialog = transaction.getDialog(dialogID);
            if (sipDialog != null && sipDialog.getState() == DialogState.TERMINATED)
                sipDialog = null;
        }

        if (sipStack.isLoggingEnabled())
            sipStack.getLogWriter().logDebug(
                    "Transaction = " + transaction + " sipDialog = " + sipDialog);

        if (this.transactionChannel != null) {
            String originalFrom = ((SIPRequest) this.transactionChannel.getRequest())
                    .getFromTag();
            if (originalFrom == null ^ sipResponse.getFrom().getTag() == null) {
                sipStack.getLogWriter().logDebug("From tag mismatch -- dropping response");
                return;
            }
            if (originalFrom != null
                    && !originalFrom.equalsIgnoreCase(sipResponse.getFrom().getTag())) {
                sipStack.getLogWriter().logDebug("From tag mismatch -- dropping response");
                return;
            }

        }
        if (sipStack.isDialogCreated(method) && sipResponse.getStatusCode() != 100
                && sipResponse.getFrom().getTag() != null && sipResponse.getTo().getTag() != null
                && sipDialog == null) {
            if (sipProvider.isAutomaticDialogSupportEnabled()) {
                if (this.transactionChannel != null) {
                    if (sipDialog == null) {
                        // There could be an existing dialog for this response.
                        sipDialog = sipStack.createDialog(
                                (SIPClientTransaction) this.transactionChannel, sipResponse);

                        this.transactionChannel.setDialog(sipDialog, sipResponse
                                .getDialogId(false));
                    }
                } else {
                    sipDialog = new SIPDialog(sipProvider, sipResponse);
                }
            }

        } else {
            // Have a dialog but could not find transaction.
            if (sipDialog != null && transaction == null) {
                if (sipResponse.getStatusCode() / 100 != 2) {
                    if (sipStack.isLoggingEnabled())
                        sipStack.getLogWriter().logDebug(
                                "status code != 200 ; statusCode = "
                                        + sipResponse.getStatusCode());
                    return;
                } else if (sipDialog.getState() == DialogState.TERMINATED) {
                    if (sipStack.isLoggingEnabled()) {
                        sipStack.getLogWriter().logDebug(
                                "Dialog is terminated -- dropping response!");
                    }
                    return;
                } else {
                    boolean ackAlreadySent = false;
                    if (sipDialog.isAckSeen() && sipDialog.getLastAck() != null) {
                        if (sipDialog.getLastAck().getCSeq().getSeqNumber() == sipResponse
                                .getCSeq().getSeqNumber()
                                && sipResponse.getDialogId(false).equals(
                                        sipDialog.getLastAck().getDialogId(false))) {
                            // the last ack sent corresponded to this 200
                            ackAlreadySent = true;
                        }
                    }
                    // 200 retransmission for the final response.
                    if (ackAlreadySent
                            && sipResponse.getCSeq().getMethod().equals(sipDialog.getMethod())) {
                        try {
                            // Found the dialog - resend the ACK and
                            // dont pass up the null transaction
                            if (sipStack.isLoggingEnabled())
                                sipStack.getLogWriter().logDebug("resending ACK");

                            sipDialog.resendAck();
                            return;
                        } catch (SipException ex) {
                            // What to do here ?? kill the dialog?
                        }
                    }
                }
            }
            // Pass the response up to the application layer to handle
            // statelessly.

        }
        if (sipStack.isLoggingEnabled())
            sipStack.getLogWriter().logDebug("sending response to TU for processing ");

        if (sipDialog != null && sipResponse.getStatusCode() != 100
                && sipResponse.getTo().getTag() != null) {
            sipDialog.setLastResponse(transaction, sipResponse);
        }

        ResponseEvent responseEvent = new smp.javax.sip.ResponseEvent(sipProvider,
                (ClientTransaction) transaction, sipDialog, (Response) sipResponse);

        sipProvider.handleEvent(responseEvent, transaction);

    }
}
