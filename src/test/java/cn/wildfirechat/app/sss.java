package cn.wildfirechat.app;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.Date;

public class sss {
    public static void main(String[] args) {


        if (true){
            String a = "aAa";
            switch (a){
                case "aaa":
                    System.out.println("1");
                    break;
            }

            return;
        }

        Date date1 = new Date();
        Date date = new Date();
        System.out.println(new Date(date1.getTime()*1000).getYear());
        System.out.println(date.getTime());
        System.out.println(date.getYear());
        Date date2 = new Date(date.getTime()/1000);
        System.out.println(date2.getYear());
//        int nonce = (int)(Math.random() * 100000.0D + 3.0D);
//        long timestamp = System.currentTimeMillis();
//        String adminSecret = "cdblue123123";
//        String str = nonce + "|" + adminSecret + "|" + timestamp;
//        String sign = DigestUtils.sha1Hex(str);
//        System.out.println(nonce+" "+timestamp+" "+sign);
    }
}
