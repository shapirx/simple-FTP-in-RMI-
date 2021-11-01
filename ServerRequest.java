/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.ftprmi;

/**
 *
 * @author Shapir
 */
import java.io.File;
import java.rmi.*;
public interface ServerRequest extends Remote {
    public File[] getFiles() throws RemoteException;
    public int getHandShake(int clientId) throws RemoteException;
    public int wannaSendFile(int clientId,String fileName) throws RemoteException;
    public void sendFile(int clientId, byte[] data, int read, long dateLength, String fileName, int alert) throws RemoteException;
    public long wannaDownloadFile(int clientId, String fileName) throws RemoteException;
    public byte[] downloadFile(int clientId, long sentData, int alert) throws RemoteException;
}
