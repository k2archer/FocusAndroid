package com.k2archer.demo.common.path;

import org.junit.Test;

import static org.junit.Assert.*;

public class WearDataPackageTest {

    @Test
    public void test_packing(){
        int code = 65535;
//        String data = null;
        String data = "";
        byte[] bytes = DataPackage.packing(code, data);

        DataPackage dataPackage = DataPackage.unpack(bytes);

        assertNotNull(dataPackage);

        assertEquals(code, dataPackage.code);
        assertEquals(data, dataPackage.data);
    }

}