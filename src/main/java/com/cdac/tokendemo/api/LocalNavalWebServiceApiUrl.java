package com.cdac.tokendemo.api;


/**
 * @author athisii, CDAC
 * Created on 29/03/23
 */
public class LocalNavalWebServiceApiUrl {
    //Suppress default constructor for noninstantiability
    private LocalNavalWebServiceApiUrl() {
        throw new AssertionError("The LocalNavalWebServiceApiUrl methods must be accessed statically.");
    }

    public static String getInitialize() {
        return "http://localhost:8088/N_Initialize";
    }

    public static String getWaitForConnect() {
        return "http://localhost:8088/N_Wait_for_Connect";
    }

    public static String getSelectApp() {
        return "http://localhost:8088/N_SelectApp";
    }

    public static String getReadDataFromNaval() {
        return "http://localhost:8088/N_readDatafromNaval";
    }

    public static String getStoreDataOnNaval() {
        return "http://localhost:8088/N_storeDataonNaval";
    }

    public static String getVerifyCertificate() {
        return "http://localhost:8088/N_verifyCertificate";
    }

    public static String getPkiAuth() {
        return "http://localhost:8088/N_PKIAuth";
    }

    public static String getWaitForRemoval() {
        return "http://localhost:8088/N_Wait_for_Removal";
    }

    public static String getDeInitialize() {
        return "http://localhost:8088/N_DeInitialize";
    }

    public static String getListOfReaders() {
        return "http://localhost:8088/listOfReaders";
    }
}
