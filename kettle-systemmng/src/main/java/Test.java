import org.pentaho.di.core.encryption.Encr;
import org.seaboxdata.systemmng.util.TaskUtil.KettleEncr;

public class Test {
    public static void main(String[] args) {
//        Encr.decryptPassword
//        System.out.println(KettleEncr.encryptPassword("root123456ABCD!@#"));


        System.out.println(KettleEncr.decryptPasswd("2beeac0a71e96c0d7ff4cf851fcb18bfa"));

    }
}
