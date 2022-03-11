package com.k2archer.demo.common.path;

public class DataPackage {

    public int code;
    public String data;


//        public static byte[] packing(Object object) {
//            if (object instanceof WearTickingInfo) {
//                packing()
//            }
//
//        }

    public static byte[] packing(int code, String data) {

        int dataLength = data == null ? 0 : data.getBytes().length;
        byte[] bytes = new byte[4 + dataLength];
        System.arraycopy(int2Bytes(code), 0, bytes, 0, 4);
        if (data != null) {
            System.arraycopy(data.getBytes(), 0, bytes, 4, data.getBytes().length);
        }
        return bytes;
    }

    public static DataPackage unpack(byte[] bytes) {
        if (bytes.length < 4) {
            return null;
        }

        byte[] codeBytes = new byte[4];
        System.arraycopy(bytes, 0, codeBytes, 0, 4);
        int code = bytes2Int(codeBytes);

        byte[] dataBytes = new byte[bytes.length - codeBytes.length];
        System.arraycopy(bytes, 4, dataBytes, 0, dataBytes.length);
        String data = new String(dataBytes);


        DataPackage result = new DataPackage();
        result.code = code;
        result.data = data;
        return result;
    }

    public static byte[] int2Bytes(int integer) {
        byte[] bytes = new byte[4];
        bytes[3] = (byte) (integer >> 24);
        bytes[2] = (byte) (integer >> 16);
        bytes[1] = (byte) (integer >> 8);
        bytes[0] = (byte) integer;

        return bytes;
    }

    public static int bytes2Int(byte[] bytes) {
        //如果不与0xff进行按位与操作，转换结果将出错，有兴趣的同学可以试一下。
        int int1 = bytes[0] & 0xff;
        int int2 = (bytes[1] & 0xff) << 8;
        int int3 = (bytes[2] & 0xff) << 16;
        int int4 = (bytes[3] & 0xff) << 24;

        return int1 | int2 | int3 | int4;
    }
}
