import org.pentaho.di.core.encryption.Encr;
import org.sxdata.jingwei.util.TaskUtil.KettleEncr;

public class Test {
    public static void main(String[] args) {
//        Encr.decryptPassword
//        System.out.println(KettleEncr.encryptPassword("P@ssw0rd"));


        System.out.println(KettleEncr.decryptPasswd("2be98afc86aa7f2e4cb79fb228fc3fd8f"));

    }
}
