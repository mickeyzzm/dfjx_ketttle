import org.pentaho.di.core.encryption.Encr;
import org.seaboxdata.systemmng.util.TaskUtil.KettleEncr;

public class Test {
    public static void main(String[] args) {
//        Encr.decryptPassword
        System.out.println(KettleEncr.encryptPassword("root123456ABCD!@#"));


        System.out.println(KettleEncr.decryptPasswd("70d1f7dbf95894c6d1fd388c53fad38f99"));
//System.out.println(8&16);
    }
}
