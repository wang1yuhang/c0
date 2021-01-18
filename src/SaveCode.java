/**
 * 
 */


import java.util.LinkedList;

/**
 * @author ftrww
 *
 */

public class SaveCode {

	public LinkedList<String> codeList;
	private StringBuilder code;
	public int variableSlot = 0;
	public int variableNum = 0;
	public SaveCode() {
		this.code = new StringBuilder();
		this.codeList = new LinkedList<String>();
	}
	
	public String toString() {
		StringBuilder code = new StringBuilder();
		for(int i = 0; i < this.codeList.size(); i++) {
			code.append(this.codeList.get(i));
		}
		return code.toString();
	}
	
	public void addCode(String s) {
		this.codeList.add(s);
	}
	
	public void addCode(int num ,int bytes) {
		this.codeList.add(String.format("%0" + (bytes * 2) + "x",num));
	}
	
	
	public void addCode(SaveCode saveCode) {
		for(String s : saveCode.codeList){
            this.codeList.add(s);
        }
	}
	

	public void addCodeFirst(String s) {
		this.codeList.addFirst(s);
	}
	
	public void addCodeFirst(int num ,int bytes) {
		this.codeList.addFirst(String.format("%0" + (bytes * 2) + "x",num));
	}
	
	public void addCodeFirst(SaveCode saveCode) {
		LinkedList<String> temp = this.codeList;
        this.codeList = new LinkedList<String>(saveCode.codeList);
        for(String s : temp){
            this.codeList.add(s);
  
        }
	}
	
	public void replaceLabel(){
        for(int i=0; i<this.codeList.size(); i++){
            if(this.codeList.get(i).equals(CodeConversion.breakFlag)){
                for(int j=i+1; j<this.codeList.size(); j++){
                    if(this.codeList.get(j).equals(CodeConversion.whileEndFlag)){
                        this.codeList.set(i, CodeConversion.br(j-i));
                        break;
                    }
                }
            }
        }
        for(int i=0; i<this.codeList.size(); i++){
            if(this.codeList.get(i).equals(CodeConversion.contninueFlag)){
                for(int j=i-1; j>=0; j--){
                    if(this.codeList.get(j).equals(CodeConversion.whileStartFlag)){
                        this.codeList.set(i, CodeConversion.br(j-i));
                        break;
                    }
                }
            }
        }
        for(int i=0; i<this.codeList.size(); i++){
            if(this.codeList.get(i).equals(CodeConversion.whileStartFlag)){
                this.codeList.set(i, CodeConversion.br(0));
            }
            else if(this.codeList.get(i).equals(CodeConversion.whileEndFlag)){
                this.codeList.set(i, CodeConversion.br(0));
            }
        }
    }
	
	public void print(){
		for(int i = 0;i<this.codeList.size();i++) {
			System.out.println(this.codeList.get(i));
		}
	}
}
