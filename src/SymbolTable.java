/**
 * 
 */


import java.util.LinkedList;
import java.util.ArrayList;
/**
 * @author ftrww
 *
 */

class TableItem{
	
	final public static byte INT = 37;
	final public static byte DOUBLE = 38;
	final public static byte VOID = 39;
	final public static byte FUNTION =40;
	final public static byte STRING = 41;
	final public static byte FAULT = 51;
	
	private String name;
	public byte type;
	public int depth;
	public boolean isConst = false;
	public ArrayList<Byte> paramList = new ArrayList<Byte>();
	public boolean isParam = false;
	public int location = 0;
	public byte returnType = FAULT;
	public String StringValue;
	
	
	public String getName() {
		return this.name;
	}
	
	public TableItem(String name ,byte type,int depth) {
		this.name = name;
		this.type = type;
		this.depth = depth;
	}
	
	public TableItem(String name ,byte type,int depth,byte returnType) {
		this.name = name;
		this.type = type;
		this.depth = depth;
		this.returnType = returnType;
	}
	
	public void setConst() {
		isConst = true;
	}
	
	public void setParam() {
		isParam = true;
	}
	
	public void addParam(byte type) throws Exception{
		switch(type){
		case TableItem.DOUBLE:
		case TableItem.INT:
		case TableItem.VOID:
			this.paramList.add(type);
			break;
		default:
			throw new SemanticException("bad type for function param");
		}
	}
	
}
public class SymbolTable {
	private LinkedList<TableItem> tableList = new LinkedList<TableItem>();

	public LinkedList<TableItem> getTableList() {
		return tableList;
	}

	public void addItem(TableItem item) throws Exception{
		if(item == null) {
			throw new SemanticException("item can not be null");
		}
		else if(item.type == TableItem.VOID) {
			throw new SemanticException("item type can't be void");
		}
		else {
			if(!hasSameName(item)) {
				this.tableList.add(item);
			}
		}
		
	}
	
	public boolean hasSameName(TableItem item) {
		for(int i = this.tableList.size()-1;i>=0;i--) {
			TableItem itemTemp = tableList.get(i);
			if(itemTemp.depth == item.depth && itemTemp.getName().equals(item.getName())){
				return true;
			}
		}
		return false;
	}
	
	public void popTable(int depth) {
		for(int i = this.tableList.size()-1 ;i >= 0;i--) {
			if(this.tableList.get(i).depth >= depth) {
				this.tableList.remove(i);
			}
			else {
				return ;
			}
		}
	}
	
	public TableItem find(String name) {
		for(int i=this.tableList.size()-1 ;i>=0;i--) {
			if(tableList.get(i).getName().equals(name)) {
				return tableList.get(i);
			}
		}
		return null;
	}
	
	public void print(){
        System.out.println("==============================");
        for(TableItem sti : this.tableList){
            System.out.println("("+sti.type+")\t"+sti.getName()+"\t:"+sti.depth);
        }
        System.out.println("==============================");
    }
}



