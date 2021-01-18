/**
 * 
 */
package c0;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Writer;

/**
 * @author ftrww
 *
 */
public class Main {
	public static void main(String [] args) {
		try {
			Syntacticor syntacticor = Syntacticor.getInstance("test.txt");
			syntacticor.analyzeProgram();
//			syntacticor.getGlobalTable().print();
//			syntacticor.getLocalTable().print();
			syntacticor.allSave.print();
			File file = new File("result.txt");
	        FileOutputStream fileOutputStream = new FileOutputStream(file);
	        String code = syntacticor.allSave.toString();
	        System.out.println("Code length: " + code.length());
	        for(int i=0; i<code.length(); i+=2){
	            fileOutputStream.write(Integer.valueOf(code.substring(i, i+2), 16));
	        }
	        fileOutputStream.close();
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
