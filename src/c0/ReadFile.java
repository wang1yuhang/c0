/**
 * 
 */
package c0;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * @author ftrww
 *
 */
public class ReadFile {
	private Reader reader;
	private File file;
	
	public ReadFile(String FileName) throws IOException{
		this.setFile(FileName);
	}
	
	public Reader getReader() {
		return reader;
	}

	public void setFile(String FileName) throws IOException{
		this.file = new File(FileName);
		this.reader = new InputStreamReader(new FileInputStream(file));
	}

	public int readInt() throws IOException{
//		ReadFile readFile = new ReadFile("text.txt");
//		int charTemp = readFile.readInt();
//		char c;
//		while(charTemp!=-1) {
//			c = (char)charTemp;
//			System.out.println(c);
//			charTemp = readFile.readInt();
//		}
		int charTemp = this.reader.read();
		if(charTemp == -1) {
			this.reader.close();
		}
		return charTemp;
	}
	
	public char getCharFromInt(int tempInt) {
		return (char)tempInt;
	}
	
	
}
