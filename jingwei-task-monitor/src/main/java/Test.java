import org.pentaho.di.core.encryption.Encr;
import org.sxdata.jingwei.util.TaskUtil.KettleEncr;

public class Test {
    public static void main(String[] args) {
//        Encr.decryptPassword
        System.out.println(KettleEncr.encryptPassword("root123456ABCD!@#"));


//        System.out.println(KettleEncr.decryptPasswd("2be98afc86aa7818ea10b8e228ec0fffb"));

    }
}
