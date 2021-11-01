/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.ftprmi;

import java.io.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;

/**
 *
 * @author Shapir
 */
public class KlientFTP {

    // Przypisanie całej zawartości Folderu
    private static File[] allFiles(String path) {
        File f = new File(path);
        return f.listFiles();
    }

    // Wyświetlenie tylko Plików
    //path -> Scieżka bezwzględna | n - > Opcja filtrowania
    // #1 - Tylko Pliki #2 - Tylko katalogi | #inna -> wszystkie
    public static void showFiles(String path, int n) {
        File[] pathnames = allFiles(path);
        try {
            for (File pathname : pathnames) {
                if (n == 1) {
                    if (pathname.isFile()) {
                        System.out.println(pathname.getName());
                    }
                } else if (n == 2) {
                    if (pathname.isDirectory()) {
                        System.out.println(pathname.getName());
                    }
                } else {
                    System.out.println(pathname.getName());
                }
            }
        } catch (NullPointerException e) {
            System.out.println("Nie ma takiego katalogu");
        }
    }

    // Wyświetla File[] wszystkie pliki
    public static void show(File[] file) {
        try {
            for (File pathname : file) {
                System.out.println(pathname.getName());
            }
        } catch (NullPointerException e) {
            System.out.println("Nie ma takiego katalogu");
        }
    }

    //Zmiana Ścieżki bezwzględnej
    public static Boolean changePath(String path) {
        File f = new File(path);
        return f.exists();
    }

    //Wyjście z folderu
    public static String cdOut(String path) {
        String[] partsPath = path.split("\\\\");
        String output = "";
        if (partsPath.length == 1) {
            return "";
        }
        for (int i = 0; i < partsPath.length - 1; i++) {
            output += partsPath[i];
            if (partsPath.length - 2 != i || partsPath.length == 2) {
                output += "\\";
            }
        }
        return output;
    }

    //Zmiana folderu na wyższy
    public static String cd(String path, String file) {
        String fullpath = "";
        if (path.length() == 3) {
            fullpath = path + file;
        } else {
            fullpath = path + "\\" + file;
        }

        File f = new File(fullpath);
        if (f.exists()) {
            return fullpath;
        } else {
            return "";
        }
    }

    private static int clientId = 0;

    public static void main(String[] args) throws IOException {
        //HANDSHAKE//
        try {//-----------------------------------------------------------------------------------------|
            Object o = Naming.lookup("rmi://127.0.0.1:40000/ServerRequest");//--------------------------|
            ServerRequest s = (ServerRequest) o;//------------------------------------------------------|
            int klient = s.getHandShake(clientId);//----------------------------------------------------|
            if (klient != 0) {//--------------------------------------------------------------------------|
                clientId = klient;//--------------------------------------------------------------------|
            } else {//------------------------------------------------------------------------------------|
                System.out.println("Coś poszło nie tak, połączenie zostało zakończone");//--------------|
                System.exit(0);//-----------------------------------------------------------------------|
            }//-----------------------------------------------------------------------------------------|
        } catch (RemoteException e) {//-----------------------------------------------------------------|
            System.out.println("Nie można połączyć się z serwerem spróbuj ponownie");//-----------------|
            System.exit(0);//---------------------------------------------------------------------------|
        } catch (Exception e) {//-----------------------------------------------------------------------|
            System.err.println("Other exception:" + e);//-----------------------------------------------|
        }//---------------------------------------------------------------------------------------------|
        //-----Koniec HANDSHAKA-------------------------------------------------------------------------|
        Scanner scanner = new Scanner(System.in);
        String path = "Ścieżka bezwzględna";
        String choice, choiceOne, choiceTwo, request, response, fileName;
        Long serverFile;
        while (true) {
            System.out.print("\n\n\n\t    Menu: \n\t1. Wyświetlanie\n\t2. Zmiana ścieżki\n\t3. Pobieranie\n\t4. Wysylanie\nWybor: ");
            choice = scanner.nextLine();
            switch (choice) {
                //Wyświetlanie
                case "1":
                    System.out.print("\n\n\n\t    Menu(Wyświetlanie):\n\t1. Zawartość obecnego folderu\n\t2. Obecna ścieżka\n\t3. Zawartość serwera\nWybor: ");
                    choiceOne = scanner.nextLine();
                    switch (choiceOne) {
                        //Zawartość obecnego folderu
                        case "1":
                            System.out.print("\n\n\n\t    FILTR:\n\t1. Cała zawartość\n\t2. Tylko pliki\n\t3. Tylko foldery\nWybor: ");
                            choiceTwo = scanner.nextLine();
                            switch (choiceTwo) {
                                case "1":
                                    showFiles(path, 0);
                                    break;
                                case "2":
                                    showFiles(path, 1);
                                    break;
                                case "3":
                                    showFiles(path, 2);
                                    break;
                            }
                            break;
                        //Obecna ścieżka    
                        case "2":
                            System.out.println("\nAktualna Ścieżka: " + path);
                            break;
                        //Zawartość servera
                        case "3":
                            try {
                            Object o = Naming.lookup("rmi://127.0.0.1:40000/ServerRequest");
                            ServerRequest s = (ServerRequest) o;
                            File[] klient = s.getFiles();
                            show(klient);
                        } catch (MalformedURLException e) {
                            System.err.println("Is not valid rmi url" + e);
                        } catch (RemoteException e) {
                            System.out.println("Nie można połączyć się z serwerem spróbuj ponownie");
                            System.exit(0);
                        } catch (NotBoundException e) {
                            System.err.println("Could not find remote object--->:" + e);
                        }
                        break;
                    }
                    break;
                //Zmiana Ścieżki
                case "2":
                    System.out.print("\n\n\n\t    Menu(Zmiana Ścieżki):\n\t1. Wejście do folderu wyżej\n\t2. Wyjście z folderu\n\t3. Ścieżka Bezwzględna\nWybor: ");
                    choiceOne = scanner.nextLine();
                    switch (choiceOne) {
                        //Wejście do folderu wyżej
                        case "1":
                            System.out.print("\nPodaj nazwę folderu: ");
                            request = scanner.next();
                            response = cd(path, request);
                            if (response.length() != 0) {
                                path = response;
                                System.out.println("\nAktualna Ścieżka: " + path);
                            } else {
                                System.out.println("\nNie ma takiego folderu");
                            }
                            break;
                        //Wyjście z folderu
                        case "2":
                            response = cdOut(path);
                            if (response.length() != 0) {
                                path = response;
                                System.out.println("\nAktualna Ścieżka: " + path);
                            } else {
                                System.out.println("\nNie możesz zejść niżej, możesz zmienić dysk");
                            }
                            break;
                        //Ścieżka bezwzględna
                        case "3":
                            request = scanner.next();
                            if (changePath(request)) {
                                path = request;
                                System.out.println("\nAktualna Ścieżka: " + path);
                            } else {
                                System.out.println("\nTaka ścieżka nie istnieje");
                            }
                            break;
                    }
                    break;
                //Pobieranie
                case "3":
                    System.out.print("Wprowadź nazwę pliku znajdującego się na serverze: ");
                    fileName = scanner.nextLine();
                    try {
                        Object o = Naming.lookup("rmi://127.0.0.1:40000/ServerRequest");//--------------------------|
                        ServerRequest s = (ServerRequest) o;//------------------------------------------------------|
                        long klient = s.wannaDownloadFile(clientId, fileName);//-------------------------------------|
                        if (klient != 0) {
                            serverFile = klient;
                            int alert = 0;
                            double dp = 0;
                            int stage = 0;
                            long dwdBytes = 0;
                            byte[] dataResponse;
                            try {
                                FileOutputStream fileOutput = new FileOutputStream(path + "\\" + fileName);
                                while (true) {
                                    alert = 0;
                                    dp = ((double) dwdBytes / serverFile) * 100;
                                    if (dp > 25 && stage < 1 && dp < 50) {
                                        alert = 1;
                                        stage = 1;
                                    } else if (dp > 50 && stage < 2 && dp < 75) {
                                        alert = 2;
                                        stage = 2;
                                    } else if (dp > 75 && stage < 3 && dp < 100) {
                                        alert = 3;
                                        stage = 3;
                                    }
                                    dataResponse = s.downloadFile(clientId, dwdBytes, alert);
                                    fileOutput.write(dataResponse);
                                    dwdBytes += dataResponse.length;
                                    System.out.println("Pobrano: [" + dwdBytes + "] Z [" + serverFile + "]");
                                    if (dwdBytes >= serverFile) {
                                        break;
                                    }
                                }
                                fileOutput.close();
                                System.out.println("\n\n Plik Został pobrany prawidłowo \n\n");
                            } catch (RemoteException e) {//-----------------------------------------------------------------|
                                System.out.println("Nie można połączyć się z serwerem spróbuj ponownie");//-----------------|
                                System.exit(0);//---------------------------------------------------------------------------|
                            } catch (Exception e) {//-----------------------------------------------------------------------|
                                System.err.println("Other exception:" + e);//-----------------------------------------------|
                            }
                        } else {
                            System.out.println("Nie ma takiego pliku na serverze");
                        }
                    } catch (RemoteException e) {//-----------------------------------------------------------------|
                        System.out.println("Nie można połączyć się z serwerem spróbuj ponownie");//-----------------|
                        System.exit(0);//---------------------------------------------------------------------------|
                    } catch (Exception e) {//-----------------------------------------------------------------------|
                        System.err.println("Other exception:" + e);//-----------------------------------------------|
                    }
                    break;
                //Wysyłanie
                case "4":
                    System.out.print("Wprowadź nazwę pliku: ");
                    fileName = scanner.nextLine();
                    File fileToSend = new File(path + "\\" + fileName);
                    if (fileToSend.exists()) {
                        //Wanna Send File//
                        try {//-----------------------------------------------------------------------------------------|
                            Object o = Naming.lookup("rmi://127.0.0.1:40000/ServerRequest");//--------------------------|
                            ServerRequest s = (ServerRequest) o;//------------------------------------------------------|
                            int klient = s.wannaSendFile(clientId, fileName);//------------------------------------------|
                            if (klient == 1) {//--------------------------------------------------------------------------|
                                //Sending File###########################################################################
                                try {//-----------------------------------------------------------------------------------------|
                                    //ServerRequest s = (ServerRequest) o;//----------------------------------------------------|
                                    FileInputStream fileIn = new FileInputStream(fileToSend);
                                    long sendBytes = 0;
                                    double dp = 0;
                                    int stage = 0;
                                    int alert = 0;
                                    byte[] data = new byte[4096];
                                    int read = 0;
                                    while ((read = fileIn.read(data)) > 0) {
                                        sendBytes += read;
                                        dp = ((double) sendBytes / fileToSend.length()) * 100;
                                        alert = 0;
                                        System.out.println("Przesłane -> [" + sendBytes + "] Z [" + fileToSend.length() + "]");
                                        if (dp > 25 && stage < 1 && dp < 50) {
                                            alert = 1;
                                            stage = 1;
                                            System.out.println("Wysłałem plik w 25%");
                                        } else if (dp > 50 && stage < 2 && dp < 75) {
                                            alert = 2;
                                            stage = 2;
                                            System.out.println("Wysłałem plik w 50%");
                                        } else if (dp > 75 && stage < 3 && dp < 100) {
                                            alert = 3;
                                            stage = 3;
                                            System.out.println("Wysłałem plik w 75%");
                                        } else if (dp >= 100 && stage < 4) {
                                            alert = 4;
                                            stage = 4;
                                            System.out.println("Wysłałem plik w 100%");
                                        }
                                        s.sendFile(clientId, data, read, fileToSend.length(), fileName, alert);//---------------|
                                    }
                                    fileIn.close();
                                } catch (RemoteException e) {//-----------------------------------------------------------------|
                                    System.out.println("Nie można połączyć się z serwerem spróbuj ponownie");//-----------------|
                                    System.exit(0);//---------------------------------------------------------------------------|
                                } catch (Exception e) {//-----------------------------------------------------------------------|
                                    System.err.println("Other exception:" + e);//-----------------------------------------------|
                                }//---------------------------------------------------------------------------------------------|
                                //#######################################################################################
                            } else {//------------------------------------------------------------------------------------|
                                System.out.println("Aktualnie jest przesyłany plik");//---------------------------------|
                            }//-----------------------------------------------------------------------------------------|
                        } catch (RemoteException e) {//-----------------------------------------------------------------|
                            System.out.println("Nie można połączyć się z serwerem spróbuj ponownie");//-----------------|
                            System.exit(0);//---------------------------------------------------------------------------|
                        } catch (Exception e) {//-----------------------------------------------------------------------|
                            System.err.println("Other exception:" + e);//-----------------------------------------------|
                        }//---------------------------------------------------------------------------------------------|
                        //-----Koniec HANDSHAKA-------------------------------------------------------------------------|
                    } else {
                        System.out.println("\nTaki plik nie istnieje\n");
                        break;
                    }
                    break;
            }
        }

    }
}
