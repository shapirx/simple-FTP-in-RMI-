/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.ftprmi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import static java.lang.System.in;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.net.MalformedURLException;
import java.rmi.registry.*;
import java.util.HashMap;

/**
 *
 * @author Shapir
 */
public class ServerFTP extends UnicastRemoteObject implements ServerRequest {

    private static String serverPath = "Ścieżka Bezwzględna";
    private int clientsId = 0;
    private HashMap<Integer, FileOutputStream> clientsMap = new HashMap<>();
    private HashMap<Integer, FileInputStream> clientsMapIn = new HashMap<>();
    private HashMap<String, Long> fileRequest = new HashMap<>();
    private HashMap<Integer, Long> fileDownload = new HashMap<>();
    public ServerFTP() throws RemoteException {
        super();
    }
    // Pliki servera
    public File[] getFiles() throws RemoteException {
        File f = new File(serverPath);
        System.out.println("Wysłanie zawartości servera");
        return f.listFiles();
    }
    // Przywitanie z serverem
    public int getHandShake(int clientId) throws RemoteException {
        if (clientId == 0) {
            clientsId++;
            FileOutputStream fileOutput = null;
            FileInputStream fileInput = null;
            clientsMap.put(clientsId, fileOutput);
            clientsMapIn.put(clientsId,fileInput);
            System.out.println("Przywitanie klienta -> id[" + clientsId + "]");
            return clientsId;
        } else {
            return 0;
        }
    }
    // Klient chce wyslac plik
    public int wannaSendFile(int clientId, String fileName) throws RemoteException {
        if (clientsMap.get(clientId) == null) {
            try {
                long bytes = 0;
                FileOutputStream fileOutput = new FileOutputStream(serverPath + "\\" + fileName);
                clientsMap.put(clientId, fileOutput);
                fileRequest.put(fileName, bytes);
                System.out.println("Klient o id[" + clientId + "] chce wysłać plik: " + fileName + "\t Poprawnie przyjęto zgłoszenie");
                return 1;
            } catch (FileNotFoundException e) {
                return 0;
            }
        } else {
            return 0;
        }
    }
    // Klient chce pobrac plik
    public long wannaDownloadFile(int clientId, String fileName) throws RemoteException{
        File fileToSend = new File(serverPath+"\\"+fileName);
        if(fileToSend.exists()){
            try {
                FileInputStream fileInput = new FileInputStream(fileToSend);
                clientsMapIn.put(clientId,fileInput);
                fileDownload.put(clientId,fileToSend.length());
                System.out.println("Klient ["+clientId+"] rozpoczyna pobieranie pliku: "+fileName);
                return fileToSend.length();
            } catch (FileNotFoundException e) {
                return 0;
            }
        }else{
            return 0;
        }
    }
    // Wysylanie pliku przez klienta
    public void sendFile(int clientId, byte[] data, int read, long dateLength, String fileName, int alert) throws RemoteException {
        try {
            clientsMap.get(clientId).write(data, 0, read);
            fileRequest.put(fileName, fileRequest.get(fileName) + data.length);
            if (alert == 1) {
                System.out.println("Klient [" + clientId + "] Wysłał-> " + fileName + " który został pobrany na serwerze w 25%");
            } else if (alert == 2) {
                System.out.println("Klient [" + clientId + "] Wysłał-> " + fileName + " który został pobrany na serwerze w 50%");
            } else if (alert == 3) {
                System.out.println("Klient [" + clientId + "] Wysłał-> " + fileName + " który został pobrany na serwerze w 75%");
            } else if (alert == 4) {
                System.out.println("Klient [" + clientId + "] Wysłał-> " + fileName + " który został pobrany na serwerze w 100%");
            }
            if (fileRequest.get(fileName) >= dateLength) {
                clientsMap.get(clientId).close();
                clientsMap.put(clientId, null);
                fileRequest.remove(fileName);
                System.out.println("Plik:" + fileName + " Został pobrany prawidłowo");
            }
        } catch (IOException e) {
            System.out.println("Coś poszło nie tak");
        }
    }
    // Wysylanie pliku do klienta
    public byte[] downloadFile(int clientId, long sentData, int alert) throws RemoteException{
        byte[] data = new byte[4096];
        try{
            if (alert == 1) {
                System.out.println("Klient [" + clientId + "] Pobrał plik w 25%");
            } else if (alert == 2) {
                System.out.println("Klient [" + clientId + "] Pobrał plik w 50%");
            } else if (alert == 3) {
                System.out.println("Klient [" + clientId + "] Pobrał plik w 75%");
            }
            if(sentData+data.length < fileDownload.get(clientId)){
                clientsMapIn.get(clientId).read(data);
                return data;
            }else{
                int read = clientsMapIn.get(clientId).read(data);
                byte[] tmp = new byte[read];
                System.arraycopy(data, 0, tmp, 0, read);
                clientsMapIn.get(clientId).close();
                clientsMapIn.put(clientId,null);
                fileDownload.remove(clientId);
                System.out.println("Klient [" + clientId + "] Pobrał plik w 100%");
                return tmp;
            }
        } catch (IOException e) {
            System.out.println("Coś poszło nie tak");
            return data;
        }
    }
    public static void main(String[] args) {
        try {
            ServerFTP f = new ServerFTP();
            LocateRegistry.createRegistry(40000);
            Naming.rebind("//127.0.0.1:40000/ServerRequest", f);
            System.out.println("***\t\tSerwer Wystartował\t\t***");
        } catch (RemoteException re) {
            System.err.println("blad" + re);
        } catch (MalformedURLException e) {
            System.err.println("blad" + e);
        }
    }
}
