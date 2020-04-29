import org.pentaho.di.core.encryption.Encr;
import org.sxdata.jingwei.util.TaskUtil.KettleEncr;

public class Test {
    public static void main(String[] args) {
//        Encr.decryptPassword
        System.out.println(KettleEncr.encryptPassword("sjjr@2020A"));
    }
}
